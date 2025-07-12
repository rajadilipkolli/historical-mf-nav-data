#!/bin/bash
# Accept optional from and to date arguments
FROM_DATE="$1"
TO_DATE="$2"

# If both from and to dates are provided, use them
if [ -n "$FROM_DATE" ] && [ -n "$TO_DATE" ]; then
  d="$FROM_DATE"
  end_date=$(date -I -d "$TO_DATE + 1 day")
  echo "Using provided date range: $FROM_DATE to $TO_DATE"
else
  # Data is often backfilled, but not very often.
  # We default to checking last 30 days
  # But on every Sunday, go back a year.
  # Returns a 1-7, 1=Mon
  if [ "$(date +%u)" -eq 7 ]; then
	d=$(date -I -d '-365 days')
  else
	d=$(date -I -d '-30 days')
  fi

  # On the first of every month, go back to starting of the dataset
  if [ "$(date +%d)" -eq 1 ]; then
	d='2005-12-31'
  fi
  end_date=$(date -I -d '+1 day')
  echo "Starting date is $d"
fi

# Loop till we reach end_date (exclusive)
while [ "$d" != "$end_date" ]; do 
  if [ ! -f "data/$d" ]; then
	echo "Fetching $d"
	df=$(date '+%d-%b-%Y' -d "$d")
	year=$(date '+%Y' -d "$d")
	month=$(date '+%m' -d "$d")
	date=$(date '+%d' -d "$d")
	URL="https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt=$df"
	mkdir -p "data/$year/$month"
	wget --output-document="data/$year/$month/$date.csv" --quiet "$URL"
	# Delete the file if we got HTML in return
	grep "<html>" "data/$year/$month/$date.csv" && rm "data/$year/$month/$date.csv"
  fi
  d=$(date -I -d "$d + 1 day")
done
