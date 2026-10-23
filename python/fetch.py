import os
import sys
import datetime
import requests
from concurrent.futures import ThreadPoolExecutor, as_completed
import zipfile

def daterange(start_date, end_date):
    for n in range(int((end_date - start_date).days)):
        yield start_date + datetime.timedelta(n)

def should_fetch_file(path):
    return not os.path.isfile(path)

def fetch_file(date, url, out_path):
    """Download a NAV CSV, skipping empty responses and content detected as HTML.

    ``out_path`` supplies the archive directory and inner CSV name; the archive
    uses the same path with a ``.zip`` suffix. The function returns a status
    message for successful, skipped, and failed downloads rather than raising
    request or file-system errors. ``date`` is retained as task metadata but is
    not used to build the request.
    """
    try:
        resp = requests.get(url, timeout=30)
        resp.raise_for_status()
        if not resp.content.strip():
            return f"Skipped (0 bytes): {out_path}"
            
        # Save as a zipped CSV to reduce repo size: DD.zip containing DD.csv
        zip_path = out_path.replace('.csv', '.zip')
        inner_name = os.path.basename(out_path)
        os.makedirs(os.path.dirname(zip_path), exist_ok=True)
        # Write response content into a zip archive
        with zipfile.ZipFile(zip_path, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
            zf.writestr(inner_name, resp.content)
        # Remove file if HTML (error page)
        if b"<html>" in resp.content[:4096]:
            # remove zip if it's an HTML error page
            try:
                os.remove(zip_path)
            except OSError:
                pass
            return f"Skipped (HTML): {zip_path}"
        return f"Fetched: {zip_path}"
    except Exception as e:
        return f"Error {out_path}: {e}"

def compute_date_range(today, from_date=None, to_date=None):
    """Compute the start date and exclusive end date for fetching NAV data.

    If both explicit dates are supplied, include both dates by adding one day to
    ``to_date`` for the exclusive end. Explicit ranges bypass calendar rules.

    For default ranges:
    - First Sunday of the month (Sunday on days 1–7): starts at 2005-12-31.
    - Other Sundays: starts 365 days back.
    - All other days: starts 90 days back.
    In every default case, ``today`` is used as the exclusive end date.
    """
    if from_date and to_date:
        if isinstance(from_date, str):
            start = datetime.datetime.strptime(from_date, "%Y-%m-%d").date()
        elif isinstance(from_date, datetime.datetime):
            start = from_date.date()
        else:
            start = from_date

        if isinstance(to_date, str):
            end_exclusive = datetime.datetime.strptime(to_date, "%Y-%m-%d").date() + datetime.timedelta(days=1)
        elif isinstance(to_date, datetime.datetime):
            end_exclusive = to_date.date() + datetime.timedelta(days=1)
        else:
            end_exclusive = to_date + datetime.timedelta(days=1)

        return start, end_exclusive

    if isinstance(today, datetime.datetime):
        today = today.date()

    # Default ranges: Sunday on days 1-7 starts at 2005-12-31;
    # other Sundays start 365 days back; all other days start 90 days back.
    if today.weekday() == 6 and 1 <= today.day <= 7:
        start = datetime.date(2005, 12, 31)
    elif today.weekday() == 6:
        start = today - datetime.timedelta(days=365)
    else:
        start = today - datetime.timedelta(days=90)

    end_exclusive = today
    return start, end_exclusive

def main():
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("from_date", nargs="?", help="Start date (YYYY-MM-DD)")
    parser.add_argument("to_date", nargs="?", help="End date (YYYY-MM-DD)")
    args = parser.parse_args()

    today = datetime.date.today()
    d, end_date = compute_date_range(today, args.from_date, args.to_date)
    if args.from_date and args.to_date:
        print(f"Using provided date range: {args.from_date} to {args.to_date}")
    else:
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
