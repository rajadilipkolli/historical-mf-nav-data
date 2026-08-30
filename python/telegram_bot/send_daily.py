import asyncio
from telegram import Bot
from bot import fetch_and_calculate_dma, BOT_TOKEN, CHAT_ID, get_db_connection
import pandas as pd
from github_sync import get_watchlist

async def main():
    if not BOT_TOKEN or not CHAT_ID:
        print("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID")
        return
        
    print("Running daily alert script...")
    
    # Check if watchlist has items
    watchlist = get_watchlist()
    if watchlist:
        print(f"Using watchlist with {len(watchlist)} funds.")
        import sqlite3
        conn = get_db_connection()
        scheme_codes = tuple(int(item['scheme_code']) for item in watchlist)
        placeholders = ",".join("?" * len(scheme_codes))
        
        query_nav = f"""
        SELECT scheme_code, date, nav 
        FROM nav 
        WHERE scheme_code IN ({placeholders})
        ORDER BY date ASC
        """
        nav_df = pd.read_sql_query(query_nav, conn, params=scheme_codes)
        conn.close()
        
        if nav_df.empty:
            print("No data for watchlist.")
            return
            
        nav_df['nav'] = nav_df['nav'].astype(float) / 10000.0
        nav_df['date'] = pd.to_datetime(nav_df['date'])
        
        results = []
        for code, group in nav_df.groupby('scheme_code'):
            group = group.sort_values('date').tail(200)
            if len(group) < 150: continue
            
            group['50_dma'] = group['nav'].rolling(window=50).mean()
            group['150_dma'] = group['nav'].rolling(window=150).mean()
            
            last_2 = group.tail(2)
            if len(last_2) < 2: continue
            
            prev, curr = last_2.iloc[0], last_2.iloc[1]
            scheme_name = next((i['scheme_name'] for i in watchlist if int(i['scheme_code']) == code), str(code))
            
            crossover_50 = None
            if prev['nav'] < prev['50_dma'] and curr['nav'] > curr['50_dma']:
                crossover_50 = "BULLISH (Crossed Above 50-DMA) 📈"
            elif prev['nav'] > prev['50_dma'] and curr['nav'] < curr['50_dma']:
                crossover_50 = "BEARISH (Crossed Below 50-DMA) 📉"
                
            crossover_150 = None
            if prev['nav'] < prev['150_dma'] and curr['nav'] > curr['150_dma']:
                crossover_150 = "BULLISH (Crossed Above 150-DMA) 📈"
            elif prev['nav'] > prev['150_dma'] and curr['nav'] < curr['150_dma']:
                crossover_150 = "BEARISH (Crossed Below 150-DMA) 📉"
                
            crossover_golden = None
            if prev['50_dma'] < prev['150_dma'] and curr['50_dma'] > curr['150_dma']:
                crossover_golden = "🌟 GOLDEN CROSS 🌟 (50-DMA Crossed Above 150-DMA)"
            elif prev['50_dma'] > prev['150_dma'] and curr['50_dma'] < curr['150_dma']:
                crossover_golden = "☠️ DEATH CROSS ☠️ (50-DMA Crossed Below 150-DMA)"
                
            results.append({
                'scheme_name': scheme_name,
                'date': curr['date'].strftime('%Y-%m-%d'),
                'nav': curr['nav'],
                '50_dma': curr['50_dma'], '150_dma': curr['150_dma'],
                'crossover_50': crossover_50, 'crossover_150': crossover_150,
                'crossover_golden': crossover_golden
            })
    else:
        print("Watchlist empty, checking all equity growth direct...")
        results = fetch_and_calculate_dma()
        
    if not results:
        print("No results or db empty.")
        return
        
    import html
    alerts = []
    for r in results:
        if r['crossover_50'] or r['crossover_150'] or r['crossover_golden']:
            safe_name = html.escape(r['scheme_name'])
            msg = (f"<b>{safe_name}</b>\n"
                   f"NAV: ₹{r['nav']:.2f}\n")
            if r['crossover_50']:
                msg += f"50-DMA Alert: {r['crossover_50']}\n"
            if r['crossover_150']:
                msg += f"150-DMA Alert: {r['crossover_150']}\n"
            if r['crossover_golden']:
                msg += f"MA Alert: {r['crossover_golden']}\n"
            alerts.append(msg)
            
    if alerts:
        bot = Bot(token=BOT_TOKEN)
        header = "🚨 <b>Daily MF DMA Alerts</b> 🚨\n\n"
        chunks = []
        current_chunk = header
        
        for alert in alerts:
            if len(current_chunk) + len(alert) + 2 > 4000:
                chunks.append(current_chunk)
                current_chunk = alert
            else:
                if current_chunk != header:
                    current_chunk += "\n\n"
                current_chunk += alert
        if current_chunk:
            chunks.append(current_chunk)
            
        for chunk in chunks:
            await bot.send_message(chat_id=CHAT_ID, text=chunk, parse_mode='HTML')
        print("Alerts sent successfully!")
    else:
        print("No crossovers today. No message sent.")

if __name__ == '__main__':
    asyncio.run(main())
