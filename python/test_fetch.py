import datetime
import os
import sys
import unittest

# Add python/ directory to sys.path because it is not a package
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fetch import compute_date_range


class TestFetchDateRange(unittest.TestCase):
    def test_sunday_day_1(self):
        # 2024-09-01 is Sunday, day 1 (first Sunday of month)
        today = datetime.date(2024, 9, 1)
        self.assertEqual(today.weekday(), 6)
        self.assertEqual(today.day, 1)
        start, end = compute_date_range(today)
        self.assertEqual(start, datetime.date(2005, 12, 31))
        self.assertEqual(end, today)

    def test_sunday_day_7(self):
        # 2024-07-07 is Sunday, day 7 (first Sunday of month)
        today = datetime.date(2024, 7, 7)
        self.assertEqual(today.weekday(), 6)
        self.assertEqual(today.day, 7)
        start, end = compute_date_range(today)
        self.assertEqual(start, datetime.date(2005, 12, 31))
        self.assertEqual(end, today)

    def test_sunday_day_8(self):
        # 2024-09-08 is Sunday, day 8 (second Sunday of month)
        today = datetime.date(2024, 9, 8)
        self.assertEqual(today.weekday(), 6)
        self.assertEqual(today.day, 8)
        start, end = compute_date_range(today)
        self.assertEqual(start, today - datetime.timedelta(days=365))
        self.assertEqual(end, today)

    def test_non_sunday_day_1(self):
        # 2024-10-01 is Tuesday, day 1 (non-Sunday day 1)
        today = datetime.date(2024, 10, 1)
        self.assertNotEqual(today.weekday(), 6)
        self.assertEqual(today.day, 1)
        start, end = compute_date_range(today)
        self.assertEqual(start, today - datetime.timedelta(days=90))
        self.assertEqual(end, today)

    def test_normal_weekday(self):
        # 2024-10-16 is Wednesday, day 16 (normal weekday)
        today = datetime.date(2024, 10, 16)
        self.assertEqual(today.weekday(), 2)  # Wednesday
        self.assertEqual(today.day, 16)
        start, end = compute_date_range(today)
        self.assertEqual(start, today - datetime.timedelta(days=90))
        self.assertEqual(end, today)

    def test_inclusive_explicit_to_date(self):
        # Explicit ranges bypass calendar rules; to_date is included by adding one day to end_exclusive
        today = datetime.date(2024, 9, 1)  # Even if today is first Sunday
        from_str = "2023-01-01"
        to_str = "2023-01-10"
        start, end = compute_date_range(today, from_date=from_str, to_date=to_str)
        self.assertEqual(start, datetime.date(2023, 1, 1))
        # Exclusive end is to_date + 1 day
        self.assertEqual(end, datetime.date(2023, 1, 11))

        # Also verify with datetime.date objects
        from_d = datetime.date(2023, 5, 1)
        to_d = datetime.date(2023, 5, 15)
        start_d, end_d = compute_date_range(today, from_date=from_d, to_date=to_d)
        self.assertEqual(start_d, datetime.date(2023, 5, 1))
        self.assertEqual(end_d, datetime.date(2023, 5, 16))

    def test_exclusion_of_today_in_all_default_windows(self):
        # Test that in all default cases, today is the exclusive end
        sample_dates = [
            datetime.date(2024, 9, 1),   # Sunday day 1
            datetime.date(2024, 7, 7),   # Sunday day 7
            datetime.date(2024, 9, 8),   # Sunday day 8
            datetime.date(2024, 10, 1),  # Tuesday day 1
            datetime.date(2024, 10, 16), # Wednesday day 16
            datetime.date(2024, 12, 15), # Sunday day 15
            datetime.date(2024, 12, 31), # Tuesday day 31
        ]
        for sample_today in sample_dates:
            with self.subTest(today=sample_today):
                _, end = compute_date_range(sample_today)
                self.assertEqual(end, sample_today)


if __name__ == "__main__":
    unittest.main()
