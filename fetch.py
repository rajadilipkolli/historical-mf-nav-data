import os
import sys
import datetime
import requests
from concurrent.futures import ThreadPoolExecutor, as_completed

def daterange(start_date, end_date):
    for n in range(int((end_date - start_date).days)):
        yield start_date + datetime.timedelta(n)

def should_fetch_file(path):
    return not os.path.isfile(path)

def fetch_file(date, url, out_path):
    try:
        resp = requests.get(url, timeout=30)
        resp.raise_for_status()
        os.makedirs(os.path.dirname(out_path), exist_ok=True)
        with open(out_path, "wb") as f:
            f.write(resp.content)
        # Remove file if HTML (error page)
        if b"<html>" in resp.content[:4096]:
            os.remove(out_path)
            return f"Skipped (HTML): {out_path}"
        return f"Fetched: {out_path}"
    except Exception as e:
        return f"Error {out_path}: {e}"

def main():
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("from_date", nargs="?", help="Start date (YYYY-MM-DD)")
    parser.add_argument("to_date", nargs="?", help="End date (YYYY-MM-DD)")
    args = parser.parse_args()

    today = datetime.date.today()
    if args.from_date and args.to_date:
        d = datetime.datetime.strptime(args.from_date, "%Y-%m-%d").date()
        end_date = datetime.datetime.strptime(args.to_date, "%Y-%m-%d").date() + datetime.timedelta(days=1)
        print(f"Using provided date range: {args.from_date} to {args.to_date}")
    else:
        # Match fetch.sh logic: check Sunday first, then 1st of month, else 45 days
        if today.weekday() == 6:  # Sunday (0=Mon, 6=Sun)
            d = today - datetime.timedelta(days=365)
        else:
            d = today - datetime.timedelta(days=90)
        if today.day == 1:
            d = datetime.date(2005, 12, 31)
        end_date = today
        print(f"Starting date is {d}")

    tasks = []
    for single_date in daterange(d, end_date):
        year = single_date.strftime("%Y")
        month = single_date.strftime("%m")
        day = single_date.strftime("%d")
        # Check for file existence : data/YYYY-MM-DD (no extension, no subfolders)
        legacy_path = f"data/{single_date.strftime('%Y-%m-%d')}"
        out_path = f"data/{year}/{month}/{day}.csv"
        if not os.path.isfile(legacy_path):
            df = single_date.strftime("%d-%b-%Y")
            url = f"https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt={df}"
            tasks.append((single_date, url, out_path))

    print(f"Total files to fetch: {len(tasks)}")
    with ThreadPoolExecutor(max_workers=8) as executor:
        future_to_task = {executor.submit(fetch_file, *task): task for task in tasks}
        for future in as_completed(future_to_task):
            print(future.result())

if __name__ == "__main__":
    main()