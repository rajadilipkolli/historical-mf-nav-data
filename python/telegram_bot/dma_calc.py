import sqlite3
import pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from io import BytesIO
from datetime import datetime, timedelta
import os

DB_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "funds.db")

def get_db_connection():
    return sqlite3.connect(DB_PATH)

def calculate_and_plot_dma(scheme_code, scheme_name):
    conn = get_db_connection()
    
    # We need 200 days of data + 1 year for plotting. So fetch last 2 years (to be safe).
    max_date_query = f"SELECT MAX(date) as max_date FROM nav WHERE scheme_code = {scheme_code}"
    max_date_df = pd.read_sql_query(max_date_query, conn)
    
    if max_date_df.empty or pd.isna(max_date_df.iloc[0]['max_date']):
        conn.close()
        return None, "No historical data found."
        
    latest_date_str = max_date_df.iloc[0]['max_date']
    latest_date_obj = datetime.strptime(latest_date_str, '%Y-%m-%d')
    start_date = (latest_date_obj - timedelta(days=365 + 200)).strftime('%Y-%m-%d') # 200 extra days for DMA warmup
    
    query = f"SELECT date, nav FROM nav WHERE scheme_code = {scheme_code} AND date >= '{start_date}' ORDER BY date ASC"
    df = pd.read_sql_query(query, conn)
    conn.close()
    
    if len(df) < 200:
        return None, "Not enough data."
        
    df['nav'] = df['nav'].astype(float) / 10000.0
    df['date'] = pd.to_datetime(df['date'])
    
    df['50_dma'] = df['nav'].rolling(window=50).mean()
    df['200_dma'] = df['nav'].rolling(window=200).mean()
    
    # Filter for the last 1 year for plotting
    plot_start_date = latest_date_obj - timedelta(days=365)
    plot_df = df[df['date'] >= plot_start_date].copy()
    
    if plot_df.empty or len(plot_df) < 2:
        return None, "Not enough recent data for plotting."
        
    # Get last 2 days to check crossovers
    last_2 = plot_df.tail(2)
    prev = last_2.iloc[0]
    curr = last_2.iloc[1]
    
    crossover_50 = None
    if prev['nav'] < prev['50_dma'] and curr['nav'] > curr['50_dma']:
        crossover_50 = "BULLISH (Crossed Above 50-DMA) 📈"
    elif prev['nav'] > prev['50_dma'] and curr['nav'] < curr['50_dma']:
        crossover_50 = "BEARISH (Crossed Below 50-DMA) 📉"
        
    crossover_200 = None
    if prev['nav'] < prev['200_dma'] and curr['nav'] > curr['200_dma']:
        crossover_200 = "BULLISH (Crossed Above 200-DMA) 📈"
    elif prev['nav'] > prev['200_dma'] and curr['nav'] < curr['200_dma']:
        crossover_200 = "BEARISH (Crossed Below 200-DMA) 📉"
        
    crossover_golden = None
    if prev['50_dma'] < prev['200_dma'] and curr['50_dma'] > curr['200_dma'] and curr['nav'] > curr['200_dma']:
        crossover_golden = "🌟 GOLDEN CROSS 🌟 (50-DMA Crossed Above 200-DMA)"
    elif prev['50_dma'] > prev['200_dma'] and curr['50_dma'] < curr['200_dma'] and curr['nav'] < curr['200_dma']:
        crossover_golden = "☠️ DEATH CROSS ☠️ (50-DMA Crossed Below 200-DMA)"
    
    # Plotting
    fig, ax = plt.subplots(figsize=(10, 6))
    try:
        ax.plot(plot_df['date'], plot_df['nav'], label='NAV (₹)', color='black', linewidth=1.5)
        ax.plot(plot_df['date'], plot_df['50_dma'], label='50-DMA', color='blue', linewidth=1.2)
        ax.plot(plot_df['date'], plot_df['200_dma'], label='200-DMA', color='red', linewidth=1.2)
        
        ax.set_title(f"{scheme_name[:50]}...")
        ax.set_xlabel('Date')
        ax.set_ylabel('Value (₹)')
        ax.legend()
        ax.grid(True, alpha=0.3)
        fig.tight_layout()
        
        buf = BytesIO()
        fig.savefig(buf, format='png', dpi=100)
        buf.seek(0)
    finally:
        plt.close(fig)
    
    summary = (
        f"*{scheme_name}*\n"
        f"Date: {curr['date'].strftime('%Y-%m-%d')}\n"
        f"NAV: ₹{curr['nav']:.2f}\n"
        f"50-DMA: ₹{curr['50_dma']:.2f} " + (f"({crossover_50})" if crossover_50 else "") + "\n"
        f"200-DMA: ₹{curr['200_dma']:.2f} " + (f"({crossover_200})" if crossover_200 else "") + "\n"
    )
    if crossover_golden:
        summary += f"\n🚨 **{crossover_golden}** 🚨\n"
        
    return buf, summary
