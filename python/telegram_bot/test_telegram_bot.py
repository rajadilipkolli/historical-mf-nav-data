import math
import os
import sys
import unittest
from types import SimpleNamespace
from unittest.mock import AsyncMock, MagicMock, patch

import pandas as pd

sys.path.insert(0, os.path.dirname(__file__))

import bot
import github_sync
import sip_calc


class DailyAlertTests(unittest.IsolatedAsyncioTestCase):
    async def test_daily_alert_uses_watchlist_codes_and_reports_golden_cross(self):
        context = SimpleNamespace(bot=SimpleNamespace(send_message=AsyncMock()))
        result = {
            "scheme_name": "Example Fund",
            "nav": 123.45,
            "crossover_50": None,
            "crossover_150": None,
            "crossover_golden": "Golden cross",
        }

        with (
            patch.object(
                bot,
                "get_watchlist",
                return_value=[
                    {"scheme_code": 101, "scheme_name": "One"},
                    {"scheme_code": 202, "scheme_name": "Two"},
                ],
            ),
            patch.object(bot, "fetch_and_calculate_dma", return_value=[result]) as fetch,
            patch.object(bot, "CHAT_ID", "123"),
        ):
            await bot.daily_alert_job(context)

        fetch.assert_called_once_with(scheme_codes=(101, 202))
        sent_message = context.bot.send_message.await_args.kwargs["text"]
        self.assertIn("MA Alert: Golden cross", sent_message)

    async def test_daily_alert_stops_when_watchlist_read_fails(self):
        context = SimpleNamespace(bot=SimpleNamespace(send_message=AsyncMock()))

        with (
            patch.object(bot, "get_watchlist", return_value=None),
            patch.object(bot, "fetch_and_calculate_dma") as fetch,
        ):
            await bot.daily_alert_job(context)

        fetch.assert_not_called()
        context.bot.send_message.assert_not_awaited()


class AuthorizationTests(unittest.IsolatedAsyncioTestCase):
    async def test_unauthorized_user_cannot_remove_fund(self):
        update = SimpleNamespace(
            effective_user=SimpleNamespace(id=7),
            message=SimpleNamespace(reply_text=AsyncMock()),
        )
        context = SimpleNamespace(args=["101"])

        with (
            patch.object(bot, "AUTHORIZED_USER_IDS", frozenset({"42"})),
            patch.object(bot, "remove_from_watchlist") as remove,
        ):
            await bot.remove_fund(update, context)

        remove.assert_not_called()
        update.message.reply_text.assert_awaited_once_with(
            "You are not authorized to modify the watchlist."
        )

    async def test_add_message_only_appends_added_after_success(self):
        update = SimpleNamespace(
            effective_user=SimpleNamespace(id=42),
            message=SimpleNamespace(reply_text=AsyncMock()),
        )
        context = SimpleNamespace(args=["Example", "Fund"])
        fund = pd.DataFrame([{"scheme_code": 101, "scheme_name": "Example Fund"}])

        for success, expected in (
            (False, "Could not save watchlist."),
            (True, "Fund added successfully.\n\nAdded: Example Fund"),
        ):
            update.message.reply_text.reset_mock()
            with (
                patch.object(bot, "AUTHORIZED_USER_IDS", frozenset({"42"})),
                patch.object(bot, "fetch_and_calculate_dma", return_value=[{}]),
                patch.object(bot, "get_db_connection", return_value=MagicMock()),
                patch.object(bot.pd, "read_sql_query", return_value=fund),
                patch.object(
                    bot,
                    "add_to_watchlist",
                    return_value=(success, "Fund added successfully." if success else expected),
                ),
            ):
                await bot.add_fund(update, context)

            update.message.reply_text.assert_awaited_once_with(expected)


class SipValidationTests(unittest.IsolatedAsyncioTestCase):
    async def test_invalid_command_values_do_not_query_database(self):
        invalid_args = (
            ["Example", "nan"],
            ["Example", "100", "0"],
            ["5000", "10"],
        )

        for args in invalid_args:
            update = SimpleNamespace(message=SimpleNamespace(reply_text=AsyncMock()))
            context = SimpleNamespace(args=args)
            with patch.object(bot, "get_db_connection") as connect:
                await bot.handle_sip_command(update, context)
            connect.assert_not_called()

    def test_calculation_rejects_invalid_values_before_database_access(self):
        invalid_values = (
            (0, 5),
            (-1, 5),
            (math.nan, 5),
            (math.inf, 5),
            (100, 0),
            (100, math.inf),
        )

        with patch.object(sip_calc, "get_db_connection") as connect:
            for amount, years in invalid_values:
                result = sip_calc.calculate_and_plot_sip(101, "Fund", amount, years)
                self.assertIsNone(result[0])

        connect.assert_not_called()


class WatchlistSyncTests(unittest.TestCase):
    def test_decode_failure_has_distinct_result(self):
        repo = MagicMock()
        repo.get_contents.return_value.decoded_content = b"not json"
        github = MagicMock()
        github.get_repo.return_value = repo

        with (
            patch.object(github_sync, "GITHUB_TOKEN", "token"),
            patch.object(github_sync, "GITHUB_REPO", "owner/repo"),
            patch.object(github_sync, "Github", return_value=github),
        ):
            self.assertIsNone(github_sync.get_watchlist())

    def test_mutations_do_not_save_after_read_failure(self):
        with (
            patch.object(github_sync, "get_watchlist", return_value=None),
            patch.object(github_sync, "save_watchlist") as save,
        ):
            add_result = github_sync.add_to_watchlist(101, "Fund")
            remove_result = github_sync.remove_from_watchlist(101)

        self.assertFalse(add_result[0])
        self.assertFalse(remove_result[0])
        save.assert_not_called()

    def test_mutations_report_save_failure(self):
        with (
            patch.object(
                github_sync,
                "get_watchlist",
                side_effect=[[], [{"scheme_code": 101, "scheme_name": "Fund"}]],
            ),
            patch.object(github_sync, "save_watchlist", return_value=False),
        ):
            add_result = github_sync.add_to_watchlist(101, "Fund")
            remove_result = github_sync.remove_from_watchlist(101)

        self.assertEqual(add_result, (False, "Could not save watchlist."))
        self.assertEqual(remove_result, (False, "Could not save watchlist."))


if __name__ == "__main__":
    unittest.main()
