import asyncio
from telegram import Bot
from bot import fetch_and_calculate_dma, BOT_TOKEN, CHAT_ID

async def main():
    if not BOT_TOKEN or not CHAT_ID:
        print("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID")
        return
        
    print("Running daily alert script...")
    results = fetch_and_calculate_dma()
    if not results:
        print("No results or db empty.")
        return
        
    import html
    alerts = []
    for r in results:
        if r['crossover_50'] or r['crossover_150']:
            safe_name = html.escape(r['scheme_name'])
            msg = (f"<b>{safe_name}</b>\n"
                   f"NAV: ₹{r['nav']:.2f}\n")
            if r['crossover_50']:
                msg += f"50-DMA Alert: {r['crossover_50']}\n"
            if r['crossover_150']:
                msg += f"150-DMA Alert: {r['crossover_150']}"
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
