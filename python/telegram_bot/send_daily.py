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
            
    if alerts:
        bot = Bot(token=BOT_TOKEN)
        full_msg = "🚨 *Daily MF DMA Alerts* 🚨\n\n" + "\n\n".join(alerts)
        if len(full_msg) > 4000:
            full_msg = full_msg[:4000] + "\n...[truncated]"
        await bot.send_message(chat_id=CHAT_ID, text=full_msg, parse_mode='Markdown')
        print("Alerts sent successfully!")
    else:
        print("No crossovers today. No message sent.")

if __name__ == '__main__':
    asyncio.run(main())
