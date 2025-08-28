# 200-Day Moving Average Analysis

This repository now includes automated calculation of 200-day moving averages (DMA) for all mutual fund schemes, with analysis of funds trading above or below their 200-day moving averages.

## Features

### 🔄 Automated Daily Analysis
- Calculates 200-day moving averages for all schemes with sufficient data
- Excludes weekends from moving average calculations (trading days only)
- Identifies funds above and below their 200-day moving averages
- Generates detailed reports in both markdown and JSON formats

### 📊 Analysis Components
- **Current NAV vs 200-DMA**: Comparison of latest NAV with 200-day moving average
- **Percentage Difference**: Shows how much above/below the DMA each fund is
- **Fund Classification**: Automatically categorizes funds as bullish (above DMA) or bearish (below DMA)
- **Summary Statistics**: Provides overview of market sentiment

## How It Works

### 1. Data Processing
The `calculate_dma.py` script:
- Connects to the SQLite database containing historical NAV data
- Filters out weekends and holidays (using only trading days)
- Calculates rolling 200-day averages for each scheme
- Only includes schemes with at least 200 trading days of data

### 2. Analysis
For each fund, the script:
- Compares current NAV with its 200-day moving average
- Calculates percentage difference
- Classifies as above/below DMA
- Ranks funds by performance relative to their DMA

### 3. Output Generation
The analysis produces:
- **`dma_analysis.md`**: Markdown report with tables for GitHub release notes
- **`dma_summary.json`**: JSON summary for programmatic access

## Technical Details

### Moving Average Calculation
```python
# 200-day rolling mean, excluding weekends
df['dma_200'] = df.groupby('scheme_code')['nav'].transform(
    lambda x: x.rolling(window=200, min_periods=200).mean()
)
```

### Weekend Exclusion
```python
# Remove weekends (Saturday=5, Sunday=6)
df = df[~df['date'].dt.weekday.isin([5, 6])]
```

### Classification Logic
```python
# Above DMA = Bullish, Below DMA = Bearish
funds_above_dma = latest_data[latest_data['nav'] > latest_data['dma_200']]
funds_below_dma = latest_data[latest_data['nav'] < latest_data['dma_200']]
```

## Output Format

The analysis appears in GitHub releases with tables like:

### 🟢 Top Funds Trading Above 200-Day Moving Average
| Scheme Name | Current NAV | 200-DMA | Difference (%) |
|-------------|-------------|---------|----------------|
| ABC Growth Fund | 125.45 | 118.30 | +6.04% |
| XYZ Equity Fund | 98.75 | 92.10 | +7.22% |

### 🔴 Top Funds Trading Below 200-Day Moving Average  
| Scheme Name | Current NAV | 200-DMA | Difference (%) |
|-------------|-------------|---------|----------------|
| DEF Value Fund | 87.20 | 95.50 | -8.69% |
| GHI Balanced Fund | 156.30 | 162.80 | -3.99% |

## Integration with CI/CD

The DMA analysis is automatically integrated into the GitHub Actions workflow:

1. **Data Fetch**: Historical NAV data is updated daily
2. **Database Generation**: SQLite database is created/updated
3. **DMA Calculation**: 200-day moving averages are calculated
4. **Report Generation**: Analysis is added to release notes
5. **Artifact Upload**: Reports are included in build artifacts

## Dependencies

- `pandas`: For data manipulation and moving average calculations
- `sqlite3`: For database operations (built into Python)
- `datetime`: For date handling (built into Python)

## Usage

### Manual Execution
```bash
# After database is generated
python calculate_dma.py
```

### Programmatic Access
```python
import json

# Load summary statistics
with open('dma_summary.json', 'r') as f:
    summary = json.load(f)
    
print(f"Funds above 200-DMA: {summary['funds_above_dma']}")
print(f"Funds below 200-DMA: {summary['funds_below_dma']}")
print(f"Bullish percentage: {summary['percentage_above']:.1f}%")
```

## Interpretation

### 200-Day Moving Average as a Trend Filter
- **Above 200-DMA**: Generally indicates bullish trend
- **Below 200-DMA**: Generally indicates bearish trend
- **Percentage Difference**: Shows strength of trend

### Market Sentiment Analysis
- High percentage of funds above 200-DMA = Bullish market
- High percentage of funds below 200-DMA = Bearish market
- Can be used as a broad market indicator for Indian mutual funds

## Limitations

1. **Data Requirements**: Only schemes with 200+ trading days are included
2. **Market Holidays**: Currently excludes weekends; major holidays may still be included
3. **Technical Analysis**: 200-DMA is one indicator among many; should not be used in isolation
4. **Lag Effect**: Moving averages are lagging indicators

## Future Enhancements

- Support for multiple moving average periods (50-day, 100-day)
- Holiday calendar integration for more accurate trading day calculation
- Sector-wise analysis
- Trend strength indicators
- Historical DMA crossover alerts
