import sqlite3
import math
import pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from io import BytesIO
from pyxirr import xirr
from datetime import datetime, timedelta
import os

DB_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "funds.db")

def get_db_connection():
    return sqlite3.connect(DB_PATH)

def calculate_and_plot_sip(scheme_code, scheme_name, amount, years=5):
    try:
        inputs_are_valid = (
            math.isfinite(amount)
            and amount > 0
            and math.isfinite(years)
            and years > 0
        )
    except TypeError:
        inputs_are_valid = False

    if not inputs_are_valid:
        return None, "Amount and duration must be finite values greater than zero."

    conn = get_db_connection()
    
    # Get the latest date available for this scheme
    max_date_query = f"SELECT MAX(date) as max_date FROM nav WHERE scheme_code = {scheme_code}"
    max_date_df = pd.read_sql_query(max_date_query, conn)
    
    if max_date_df.empty or pd.isna(max_date_df.iloc[0]['max_date']):
        conn.close()
        return None, "No historical data found."
        
    latest_date_str = max_date_df.iloc[0]['max_date']
    latest_date_obj = datetime.strptime(latest_date_str, '%Y-%m-%d')
    start_date = (latest_date_obj - timedelta(days=years*365)).strftime('%Y-%m-%d')
    
    query = f"SELECT date, nav FROM nav WHERE scheme_code = {scheme_code} AND date >= '{start_date}' ORDER BY date ASC"
    df = pd.read_sql_query(query, conn)
    conn.close()
    
    if df.empty:
        return None, "No historical data found for the last 5 years."
        
    df['nav'] = df['nav'].astype(float) / 10000.0
    df['date'] = pd.to_datetime(df['date'])
    
    # We need the first available trading day of each month
    df['year_month'] = df['date'].dt.to_period('M')
    sip_dates = df.groupby('year_month').first().reset_index()
    
    if sip_dates.empty:
        return None, "Not enough data to simulate SIP."
        
    total_units = 0.0
    total_invested = 0.0
    
    cashflows = []
    dates = []
    
    portfolio_dates = []
    invested_values = []
    portfolio_values = []
    
    for index, row in sip_dates.iterrows():
        date = row['date']
        nav = row['nav']
        
        units_bought = amount / nav
        total_units += units_bought
        total_invested += amount
        
        dates.append(date.date())
        cashflows.append(-amount) # Outflow
        
        portfolio_dates.append(date)
        invested_values.append(total_invested)
        portfolio_values.append(total_units * nav)
        
    # Add current value to cashflows for XIRR
    latest_date = df.iloc[-1]['date'].date()
    latest_nav = df.iloc[-1]['nav']
    current_value = total_units * latest_nav
    
    dates.append(latest_date)
    cashflows.append(current_value) # Inflow (as if we sold everything)
    
    # Calculate XIRR
    try:
        sip_xirr = xirr(dates, cashflows) * 100 # percentage
    except:
        sip_xirr = 0.0
        
    # Plotting
    plt.figure(figsize=(10, 6))
    plt.plot(portfolio_dates, portfolio_values, label='Portfolio Value (₹)', color='green', linewidth=2)
    plt.plot(portfolio_dates, invested_values, label='Total Invested (₹)', color='blue', linestyle='--')
    plt.title(f"SIP of ₹{amount}/mo in {scheme_name[:40]}...")
    plt.xlabel('Year')
    plt.ylabel('Value (₹)')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    
    # Save to buffer
    buf = BytesIO()
    plt.savefig(buf, format='png', dpi=100)
    buf.seek(0)
    plt.close()
    
    summary = (
        f"📈 *SIP Simulation: {scheme_name}*\n\n"
        f"Monthly SIP: ₹{amount:,.2f}\n"
        f"Duration: {years} Years ({len(sip_dates)} months)\n"
        f"Total Invested: ₹{total_invested:,.2f}\n"
        f"Current Value: ₹{current_value:,.2f}\n"
        f"Absolute Return: {((current_value/total_invested)-1)*100:.2f}%\n"
        f"**XIRR (Annualized):** {sip_xirr:.2f}%"
    )
    
    return buf, summary
