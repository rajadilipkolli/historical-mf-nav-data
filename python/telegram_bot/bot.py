import os
import sqlite3
import pandas as pd
from dotenv import load_dotenv
from telegram import Update
from telegram.ext import ApplicationBuilder, CommandHandler, MessageHandler, filters, ContextTypes
import datetime

load_dotenv()

BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN")
CHAT_ID = os.getenv("TELEGRAM_CHAT_ID")
DB_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "funds.db")

def get_db_connection():
    return sqlite3.connect(DB_PATH)

def fetch_and_calculate_dma(scheme_name_query=None):
    """
    Fetches NAV data and calculates 50-DMA and 150-DMA.
    If scheme_name_query is provided, filters for that specific fund.
    Otherwise, filters for Direct Growth Equity schemes.
    """
    conn = get_db_connection()
    
    # Base query for schemes
    if scheme_name_query:
        # Search by exact or partial name
        query_schemes = f"SELECT scheme_code, scheme_name FROM schemes WHERE scheme_name LIKE '%{scheme_name_query}%' COLLATE NOCASE"
    else:
        # Filter for Direct Growth Equity (approximate via name)
        query_schemes = """
        SELECT scheme_code, scheme_name FROM schemes 
        WHERE scheme_name LIKE '%Direct%' 
          AND scheme_name LIKE '%Growth%' 
          AND scheme_name NOT LIKE '%Debt%' 
          AND scheme_name NOT LIKE '%Liquid%' 
          AND scheme_name NOT LIKE '%Bond%'
          AND scheme_name NOT LIKE '%Gilt%'
        """
        
    schemes_df = pd.read_sql_query(query_schemes, conn)
    
    if schemes_df.empty:
        conn.close()
        return None
        
    scheme_codes = tuple(schemes_df['scheme_code'].tolist())
    if len(scheme_codes) == 1:
        scheme_codes_str = f"({scheme_codes[0]})"
    else:
        scheme_codes_str = str(scheme_codes)
        
    # Fetch last 150 days for these schemes. We fetch more to ensure rolling 150 has enough data.
    # A simple way is to fetch last 200 trading days. Since date is YYYY-MM-DD or similar, we can sort.
    query_nav = f"""
    SELECT scheme_code, date, nav 
    FROM nav 
    WHERE scheme_code IN {scheme_codes_str}
    ORDER BY date ASC
    """
    nav_df = pd.read_sql_query(query_nav, conn)
    conn.close()
    
    if nav_df.empty:
        return None
        
    # Ensure nav is float (in case it's integer * 10000 from our previous tasks)
    nav_df['nav'] = nav_df['nav'].astype(float)
    # If the DB has nav as integer (multiplied by 10000), we should divide by 10000.
    # We can detect this if average NAV is abnormally high. Let's assume it's raw or we divide by 10000.0 if values > 10000 often.
    # Actually, we know it's * 10000 from Task 2.1!
    nav_df['nav'] = nav_df['nav'] / 10000.0

    nav_df['date'] = pd.to_datetime(nav_df['date'])
    
    results = []
    # Process each scheme
    for code, group in nav_df.groupby('scheme_code'):
        group = group.sort_values('date').tail(200) # get last 200 days
        if len(group) < 150:
            continue # not enough data for 150-DMA
            
        group['50_dma'] = group['nav'].rolling(window=50).mean()
        group['150_dma'] = group['nav'].rolling(window=150).mean()
        
        # Get last two days to check for crossover
        last_2 = group.tail(2)
        if len(last_2) < 2:
            continue
            
        prev = last_2.iloc[0]
        curr = last_2.iloc[1]
        
        scheme_name = schemes_df[schemes_df['scheme_code'] == code].iloc[0]['scheme_name']
        
        # Check crossovers
        crossover_50 = None
        crossover_150 = None
        
        if prev['nav'] < prev['50_dma'] and curr['nav'] > curr['50_dma']:
            crossover_50 = "BULLISH (Crossed Above 50-DMA) 📈"
        elif prev['nav'] > prev['50_dma'] and curr['nav'] < curr['50_dma']:
            crossover_50 = "BEARISH (Crossed Below 50-DMA) 📉"
            
        if prev['nav'] < prev['150_dma'] and curr['nav'] > curr['150_dma']:
            crossover_150 = "BULLISH (Crossed Above 150-DMA) 📈"
        elif prev['nav'] > prev['150_dma'] and curr['nav'] < curr['150_dma']:
            crossover_150 = "BEARISH (Crossed Below 150-DMA) 📉"
            
        # For on-demand, we want to return the current status even if no crossover today
        results.append({
            'scheme_name': scheme_name,
            'date': curr['date'].strftime('%Y-%m-%d'),
            'nav': curr['nav'],
            '50_dma': curr['50_dma'],
            '150_dma': curr['150_dma'],
            'crossover_50': crossover_50,
            'crossover_150': crossover_150
        })
        
    return results

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await update.message.reply_text("Hello! I am your MF DMA Alert Bot. Send me a fund name to get its current DMA status.")

async def handle_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    fund_name = update.message.text
    await update.message.reply_text(f"Searching for '{fund_name}' and calculating DMA...")
    
    results = fetch_and_calculate_dma(fund_name)
    if not results:
        await update.message.reply_text(f"Could not find any funds matching '{fund_name}' or insufficient data.")
        return
        
    response = []
    # Limit to top 5 matches to avoid spam
    for r in results[:5]:
        msg = (f"*{r['scheme_name']}*\n"
               f"Date: {r['date']}\n"
               f"NAV: ₹{r['nav']:.2f}\n"
               f"50-DMA: ₹{r['50_dma']:.2f} " + (f"({r['crossover_50']})" if r['crossover_50'] else "") + "\n"
               f"150-DMA: ₹{r['150_dma']:.2f} " + (f"({r['crossover_150']})" if r['crossover_150'] else ""))
        response.append(msg)
        
    await update.message.reply_text("\n\n".join(response), parse_mode='Markdown')

async def daily_alert_job(context: ContextTypes.DEFAULT_TYPE):
    print(f"[{datetime.datetime.now()}] Running daily alert job...")
    results = fetch_and_calculate_dma()
    if not results:
        print("No results or db empty.")
        return
        
    alerts = []
    for r in results:
        if r['crossover_50'] or r['crossover_150']:
            msg = (f"*{r['scheme_name']}*\n"
                   f"NAV: ₹{r['nav']:.2f}\n")
            if r['crossover_50']:
                msg += f"50-DMA Alert: {r['crossover_50']}\n"
            if r['crossover_150']:
                msg += f"150-DMA Alert: {r['crossover_150']}"
            alerts.append(msg)
            
    if alerts and CHAT_ID:
        full_msg = "🚨 *Daily MF DMA Alerts* 🚨\n\n" + "\n\n".join(alerts)
        # Telegram max message length is 4096. Truncate if necessary.
        if len(full_msg) > 4000:
            full_msg = full_msg[:4000] + "\n...[truncated]"
        await context.bot.send_message(chat_id=CHAT_ID, text=full_msg, parse_mode='Markdown')
        print(f"[{datetime.datetime.now()}] Alerts sent!")
    else:
        print(f"[{datetime.datetime.now()}] No crossovers today.")

if __name__ == '__main__':
    if not BOT_TOKEN:
        print("ERROR: TELEGRAM_BOT_TOKEN is not set in .env")
        exit(1)
        
    app = ApplicationBuilder().token(BOT_TOKEN).build()
    
    app.add_handler(CommandHandler("start", start))
    app.add_handler(MessageHandler(filters.TEXT & (~filters.COMMAND), handle_message))
    
    # Schedule daily job at 8:00 AM UTC
    job_queue = app.job_queue
    t = datetime.time(hour=8, minute=0, second=0)
    job_queue.run_daily(daily_alert_job, time=t)
    
    print("Bot is running...")
    app.run_polling()
