import os
import sqlite3
import pandas as pd
from dotenv import load_dotenv
import datetime
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import ApplicationBuilder, CommandHandler, MessageHandler, CallbackQueryHandler, filters, ContextTypes

load_dotenv()

BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN")
CHAT_ID = os.getenv("TELEGRAM_CHAT_ID")
DB_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "funds.db")

def get_db_connection():
    return sqlite3.connect(DB_PATH)

def build_search_query(fund_name, limit=20):
    # Replace hyphens with spaces, then split into words
    words = fund_name.replace("-", " ").split()
    conditions = []
    params = []
    
    for word in words:
        escaped = word.lower().replace("%", "\\%").replace("_", "\\_")
        # Strip spaces and hyphens from DB column for robust matching against exact or combined words (e.g. flexicap)
        conditions.append("REPLACE(REPLACE(LOWER(scheme_name), ' ', ''), '-', '') LIKE ? ESCAPE '\\'")
        params.append(f"%{escaped}%")
        
    where_clause = " AND ".join(conditions)
    query = f"SELECT scheme_code, scheme_name FROM schemes WHERE {where_clause} LIMIT {limit}"
    return query, tuple(params)

def fetch_and_calculate_dma(scheme_name_query=None):
    """
    Fetches NAV data and calculates 50-DMA and 150-DMA.
    If scheme_name_query is provided, filters for that specific fund.
    Otherwise, filters for Direct Growth Equity schemes.
    """
    conn = get_db_connection()
    
    # Base query for schemes
    if scheme_name_query:
        query_schemes, params = build_search_query(scheme_name_query, limit=20)
        schemes_df = pd.read_sql_query(query_schemes, conn, params=params)
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
    placeholders = ",".join("?" * len(scheme_codes))
        
    # Fetch last 150 days for these schemes. We fetch more to ensure rolling 150 has enough data.
    # A simple way is to fetch last 200 trading days. Since date is YYYY-MM-DD or similar, we can sort.
    query_nav = f"""
    SELECT scheme_code, date, nav 
    FROM nav 
    WHERE scheme_code IN ({placeholders})
    ORDER BY date ASC
    """
    nav_df = pd.read_sql_query(query_nav, conn, params=scheme_codes)
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
            
        crossover_golden = None
        if prev['50_dma'] < prev['150_dma'] and curr['50_dma'] > curr['150_dma']:
            crossover_golden = "🌟 GOLDEN CROSS 🌟 (50-DMA Crossed Above 150-DMA)"
        elif prev['50_dma'] > prev['150_dma'] and curr['50_dma'] < curr['150_dma']:
            crossover_golden = "☠️ DEATH CROSS ☠️ (50-DMA Crossed Below 150-DMA)"
            
        # For on-demand, we want to return the current status even if no crossover today
        results.append({
            'scheme_name': scheme_name,
            'date': curr['date'].strftime('%Y-%m-%d'),
            'nav': curr['nav'],
            '50_dma': curr['50_dma'],
            '150_dma': curr['150_dma'],
            'crossover_50': crossover_50,
            'crossover_150': crossover_150,
            'crossover_golden': crossover_golden
        })
        
    return results

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await update.message.reply_text("Hello! I am your MF DMA Alert Bot. Send me a fund name to get its current DMA status.")

async def handle_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    fund_name = update.message.text
    
    conn = get_db_connection()
    query, params = build_search_query(fund_name, limit=5)
    df = pd.read_sql_query(query, conn, params=params)
    conn.close()
    
    if df.empty:
        await update.message.reply_text(f"Could not find any funds matching '{fund_name}'.")
        return
        
    if len(df) == 1:
        scheme_code = int(df.iloc[0]['scheme_code'])
        scheme_name = df.iloc[0]['scheme_name']
        msg = await update.message.reply_text(f"Calculating DMA for {scheme_name}...")
        await process_dma(msg, scheme_code, scheme_name)
    else:
        keyboard = []
        for _, row in df.iterrows():
            callback_data = f"dma|{row['scheme_code']}"
            keyboard.append([InlineKeyboardButton(row['scheme_name'][:50], callback_data=callback_data)])
            
        reply_markup = InlineKeyboardMarkup(keyboard)
        await update.message.reply_text("Multiple funds found. Please select one:", reply_markup=reply_markup)

async def process_dma(message, scheme_code, scheme_name):
    import asyncio
    from dma_calc import calculate_and_plot_dma
    
    loop = asyncio.get_running_loop()
    buf, summary = await loop.run_in_executor(None, calculate_and_plot_dma, scheme_code, scheme_name)
    
    if buf is None:
        await message.edit_text(summary)
        return
        
    await message.reply_photo(photo=buf, caption=summary, parse_mode='Markdown')

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

from github_sync import add_to_watchlist, remove_from_watchlist, get_watchlist

async def add_fund(update: Update, context: ContextTypes.DEFAULT_TYPE):
    if not context.args:
        await update.message.reply_text("Usage: /add <fund_name>")
        return
        
    fund_name = " ".join(context.args)
    import asyncio
    loop = asyncio.get_running_loop()
    results = await loop.run_in_executor(None, fetch_and_calculate_dma, fund_name)
    
    if not results:
        await update.message.reply_text(f"Could not find any funds matching '{fund_name}'.")
        return
        
    # We take the first match for simplicity
    best_match = results[0]
    # We need scheme_code, but fetch_and_calculate_dma currently doesn't return it!
    # Let's modify fetch_and_calculate_dma return to include scheme_code. Wait, I'll fetch it here via SQL.
    conn = get_db_connection()
    query, params = build_search_query(fund_name, limit=1)
    df = pd.read_sql_query(query, conn, params=params)
    conn.close()
    
    if df.empty:
        await update.message.reply_text("Fund not found.")
        return
        
    scheme_code = int(df.iloc[0]['scheme_code'])
    scheme_name = df.iloc[0]['scheme_name']
    
    # Run github sync in executor to avoid blocking
    success, msg = await loop.run_in_executor(None, add_to_watchlist, scheme_code, scheme_name)
    await update.message.reply_text(f"{msg}\n\nAdded: {scheme_name}")

async def remove_fund(update: Update, context: ContextTypes.DEFAULT_TYPE):
    if not context.args:
        await update.message.reply_text("Usage: /remove <scheme_code>")
        return
        
    scheme_code = context.args[0]
    import asyncio
    loop = asyncio.get_running_loop()
    success, msg = await loop.run_in_executor(None, remove_from_watchlist, scheme_code)
    await update.message.reply_text(msg)

async def view_portfolio(update: Update, context: ContextTypes.DEFAULT_TYPE):
    import asyncio
    loop = asyncio.get_running_loop()
    watchlist = await loop.run_in_executor(None, get_watchlist)
    
    if not watchlist:
        await update.message.reply_text("Your portfolio is empty! Use /add <fund_name> to add some.")
        return
        
    lines = ["📋 *Your Watchlist:*"]
    for item in watchlist:
        lines.append(f"• `{item['scheme_code']}`: {item['scheme_name']}")
    lines.append("\n(Use `/remove <scheme_code>` to remove a fund)")
    
    await update.message.reply_text("\n".join(lines), parse_mode='Markdown')

async def handle_sip_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    if not context.args or len(context.args) < 2:
        await update.message.reply_text("Usage: /sip <fund name> <amount> [years]\nExample: /sip Parag Parikh Flexi 5000 10")
        return
        
    try:
        last_arg = context.args[-1]
        second_last_arg = context.args[-2]
        
        try:
            years = int(last_arg)
            amount = float(second_last_arg)
            fund_name = " ".join(context.args[:-2])
        except ValueError:
            amount = float(last_arg)
            years = 5
            fund_name = " ".join(context.args[:-1])
            
    except ValueError:
        await update.message.reply_text("Please provide a valid amount.\nExample: /sip Axis Bluechip 5000")
        return
        
    conn = get_db_connection()
    query, params = build_search_query(fund_name, limit=5)
    df = pd.read_sql_query(query, conn, params=params)
    conn.close()
    
    if df.empty:
        await update.message.reply_text(f"Could not find any funds matching '{fund_name}'.")
        return
        
    if len(df) == 1:
        scheme_code = int(df.iloc[0]['scheme_code'])
        scheme_name = df.iloc[0]['scheme_name']
        msg = await update.message.reply_text(f"Calculating SIP for {scheme_name} ({years} Years)...")
        await process_sip(msg, scheme_code, scheme_name, amount, years)
    else:
        keyboard = []
        for _, row in df.iterrows():
            callback_data = f"sip|{row['scheme_code']}|{amount}|{years}"
            keyboard.append([InlineKeyboardButton(row['scheme_name'][:50], callback_data=callback_data)])
            
        reply_markup = InlineKeyboardMarkup(keyboard)
        await update.message.reply_text("Multiple funds found. Please select one:", reply_markup=reply_markup)

async def handle_callback_query(update: Update, context: ContextTypes.DEFAULT_TYPE):
    query = update.callback_query
    await query.answer()
    
    data = query.data
    if data.startswith("sip|"):
        parts = data.split("|")
        scheme_code = int(parts[1])
        amount = float(parts[2])
        years = int(parts[3]) if len(parts) > 3 else 5
        
        conn = get_db_connection()
        name_df = pd.read_sql_query(f"SELECT scheme_name FROM schemes WHERE scheme_code = {scheme_code}", conn)
        conn.close()
        scheme_name = name_df.iloc[0]['scheme_name'] if not name_df.empty else f"Fund {scheme_code}"
        
        await query.edit_message_text(f"Calculating SIP for {scheme_name} ({years} Years)...")
        await process_sip(query.message, scheme_code, scheme_name, amount, years)
    elif data.startswith("dma|"):
        parts = data.split("|")
        scheme_code = int(parts[1])
        
        conn = get_db_connection()
        name_df = pd.read_sql_query(f"SELECT scheme_name FROM schemes WHERE scheme_code = {scheme_code}", conn)
        conn.close()
        scheme_name = name_df.iloc[0]['scheme_name'] if not name_df.empty else f"Fund {scheme_code}"
        
        await query.edit_message_text(f"Calculating DMA for {scheme_name}...")
        await process_dma(query.message, scheme_code, scheme_name)

async def process_sip(message, scheme_code, scheme_name, amount, years):
    import asyncio
    from sip_calc import calculate_and_plot_sip
    
    loop = asyncio.get_running_loop()
    buf, summary = await loop.run_in_executor(None, calculate_and_plot_sip, scheme_code, scheme_name, amount, years)
    
    if buf is None:
        await message.edit_text(summary)
        return
        
    await message.reply_photo(photo=buf, caption=summary, parse_mode='Markdown')

if __name__ == '__main__':
    if not BOT_TOKEN:
        print("ERROR: TELEGRAM_BOT_TOKEN is not set in .env")
        exit(1)
        
    app = ApplicationBuilder().token(BOT_TOKEN).build()
    
    app.add_handler(CommandHandler("start", start))
    app.add_handler(CommandHandler("add", add_fund))
    app.add_handler(CommandHandler("remove", remove_fund))
    app.add_handler(CommandHandler("portfolio", view_portfolio))
    app.add_handler(CommandHandler("sip", handle_sip_command))
    app.add_handler(CallbackQueryHandler(handle_callback_query))
    app.add_handler(MessageHandler(filters.TEXT & (~filters.COMMAND), handle_message))
    
    # Schedule daily job at 8:00 AM UTC
    job_queue = app.job_queue
    t = datetime.time(hour=8, minute=0, second=0)
    job_queue.run_daily(daily_alert_job, time=t)
    
    print("Bot is running...")
    app.run_polling()
