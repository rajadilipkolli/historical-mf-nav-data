#!/usr/bin/env python3
"""
Calculate 200-day moving averages for mutual fund NAV data.
This script analyzes the funds database and generates reports for funds
that are above or below their 200-day moving averages.
"""

import sqlite3
import pandas as pd
import datetime
import sys
import os
from typing import Dict, List, Tuple

def connect_to_db(db_file: str = "funds.db") -> sqlite3.Connection:
    """Connect to the funds database."""
    if not os.path.exists(db_file):
        raise FileNotFoundError(f"Database file {db_file} not found. Run generate.py first.")
    return sqlite3.connect(db_file)

def get_trading_days_data(conn: sqlite3.Connection) -> pd.DataFrame:
    """
    Get all NAV data excluding weekends and holidays.
    Returns DataFrame with columns: scheme_code, date, nav, scheme_name
    """
    query = """
    SELECT 
        n.scheme_code,
        n.date,
        n.nav,
        s.scheme_name
    FROM nav n
    JOIN schemes s ON n.scheme_code = s.scheme_code
    WHERE n.nav IS NOT NULL
    ORDER BY n.scheme_code, n.date
    """
    
    df = pd.read_sql_query(query, conn)
    df['date'] = pd.to_datetime(df['date'])
    
    # Remove weekends (Saturday=5, Sunday=6)
    df = df[~df['date'].dt.weekday.isin([5, 6])]
    
    # Sort by scheme and date to ensure proper order for rolling calculations
    df = df.sort_values(['scheme_code', 'date']).reset_index(drop=True)
    
    return df

def calculate_200_dma(df: pd.DataFrame) -> pd.DataFrame:
    """
    Calculate 200-day moving average for each scheme.
    Returns DataFrame with DMA calculations.
    """
    # Group by scheme_code and calculate 200-day rolling mean
    df['dma_200'] = df.groupby('scheme_code')['nav'].transform(
        lambda x: x.rolling(window=200, min_periods=200).mean()
    )
    
    # Only keep rows where we have enough data for 200-day average
    df = df.dropna(subset=['dma_200'])
    
    return df

def get_latest_nav_per_scheme(df: pd.DataFrame) -> pd.DataFrame:
    """
    Get the latest NAV data for each scheme (current day).
    """
    # Get the most recent date for each scheme
    latest_data = df.groupby('scheme_code').apply(
        lambda x: x.loc[x['date'].idxmax()]
    ).reset_index(drop=True)
    
    return latest_data

def classify_funds(latest_data: pd.DataFrame) -> Tuple[pd.DataFrame, pd.DataFrame]:
    """
    Classify funds as above or below their 200-day moving average.
    Returns tuple of (funds_above_dma, funds_below_dma)
    """
    # Calculate percentage difference from 200-DMA
    latest_data['dma_diff_pct'] = ((latest_data['nav'] - latest_data['dma_200']) / latest_data['dma_200'] * 100)
    
    # Classify funds
    funds_above_dma = latest_data[latest_data['nav'] > latest_data['dma_200']].copy()
    funds_below_dma = latest_data[latest_data['nav'] < latest_data['dma_200']].copy()
    
    # Sort by percentage difference (descending for above, ascending for below)
    funds_above_dma = funds_above_dma.sort_values('dma_diff_pct', ascending=False)
    funds_below_dma = funds_below_dma.sort_values('dma_diff_pct', ascending=True)
    
    return funds_above_dma, funds_below_dma

def format_table_for_markdown(df: pd.DataFrame, title: str, max_rows: int = 20) -> str:
    """
    Format DataFrame as a markdown table for release notes.
    """
    if df.empty:
        return f"\n### {title}\nNo funds in this category.\n"
    
    # Limit the number of rows to avoid very long tables
    df_display = df.head(max_rows)
    
    # Select and rename columns for display
    display_df = df_display[['scheme_name', 'nav', 'dma_200', 'dma_diff_pct']].copy()
    display_df['nav'] = display_df['nav'].round(4)
    display_df['dma_200'] = display_df['dma_200'].round(4)
    display_df['dma_diff_pct'] = display_df['dma_diff_pct'].round(2)
    
    # Create markdown table
    markdown = f"\n### {title}\n\n"
    
    if len(df) > max_rows:
        markdown += f"Showing top {max_rows} out of {len(df)} funds:\n\n"
    else:
        markdown += f"Total funds: {len(df)}\n\n"
    
    markdown += "| Scheme Name | Current NAV | 200-DMA | Difference (%) |\n"
    markdown += "|-------------|-------------|---------|----------------|\n"
    
    for _, row in display_df.iterrows():
        scheme_name = row['scheme_name'][:60] + "..." if len(row['scheme_name']) > 60 else row['scheme_name']
        markdown += f"| {scheme_name} | {row['nav']} | {row['dma_200']} | {row['dma_diff_pct']:+.2f}% |\n"
    
    return markdown

def generate_summary_stats(funds_above: pd.DataFrame, funds_below: pd.DataFrame, total_schemes_with_dma: int) -> str:
    """
    Generate summary statistics for the analysis.
    """
    above_count = len(funds_above)
    below_count = len(funds_below)
    
    if total_schemes_with_dma > 0:
        above_pct = (above_count / total_schemes_with_dma) * 100
        below_pct = (below_count / total_schemes_with_dma) * 100
    else:
        above_pct = below_pct = 0
    
    summary = f"""
## 📈 200-Day Moving Average Analysis

**Analysis Date:** {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')} UTC

### Summary Statistics
- **Total schemes analyzed:** {total_schemes_with_dma}
- **Funds above 200-DMA:** {above_count} ({above_pct:.1f}%)
- **Funds below 200-DMA:** {below_count} ({below_pct:.1f}%)

*Note: Only schemes with at least 200 trading days of data are included in this analysis.*
*Weekends are excluded from the moving average calculation.*
"""
    
    return summary

def main():
    """Main function to calculate DMA and generate reports."""
    try:
        # Connect to database
        print("Connecting to database...")
        conn = connect_to_db()
        
        # Get trading days data (excluding weekends)
        print("Loading NAV data (excluding weekends)...")
        df = get_trading_days_data(conn)
        
        if df.empty:
            print("No NAV data found in database.")
            sys.exit(1)
        
        print(f"Loaded {len(df)} NAV records for {df['scheme_code'].nunique()} schemes")
        
        # Calculate 200-day moving averages
        print("Calculating 200-day moving averages...")
        df_with_dma = calculate_200_dma(df)
        
        schemes_with_dma = df_with_dma['scheme_code'].nunique()
        print(f"Calculated 200-DMA for {schemes_with_dma} schemes (schemes with at least 200 trading days)")
        
        # Get latest data for each scheme
        print("Getting latest NAV data...")
        latest_data = get_latest_nav_per_scheme(df_with_dma)
        
        # Classify funds
        print("Classifying funds...")
        funds_above_dma, funds_below_dma = classify_funds(latest_data)
        
        print(f"Funds above 200-DMA: {len(funds_above_dma)}")
        print(f"Funds below 200-DMA: {len(funds_below_dma)}")
        
        # Generate markdown report
        print("Generating markdown report...")
        summary_stats = generate_summary_stats(funds_above_dma, funds_below_dma, len(latest_data))
        
        above_table = format_table_for_markdown(
            funds_above_dma, 
            "🟢 Top Funds Trading Above 200-Day Moving Average",
            max_rows=15
        )
        
        below_table = format_table_for_markdown(
            funds_below_dma,
            "🔴 Top Funds Trading Below 200-Day Moving Average", 
            max_rows=15
        )
        
        # Write to file for GitHub Actions to use
        report = summary_stats + above_table + below_table
        
        with open("dma_analysis.md", "w", encoding="utf-8") as f:
            f.write(report)
        
        print("DMA analysis complete. Report saved to dma_analysis.md")
        
        # Also create a JSON summary for programmatic access
        summary_json = {
            "analysis_date": datetime.datetime.now().isoformat(),
            "total_schemes_analyzed": len(latest_data),
            "funds_above_dma": len(funds_above_dma),
            "funds_below_dma": len(funds_below_dma),
            "percentage_above": (len(funds_above_dma) / len(latest_data)) * 100 if latest_data else 0,
            "percentage_below": (len(funds_below_dma) / len(latest_data)) * 100 if latest_data else 0
        }
        
        import json
        with open("dma_summary.json", "w") as f:
            json.dump(summary_json, f, indent=2)
        
        print("Summary JSON saved to dma_summary.json")
        
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)
    finally:
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    main()
