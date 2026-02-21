# Daily NAV: Historical Mutual Fund Data for India

An automated, self-contained Java library and dataset providing instant access to historical mutual fund NAV (Net Asset Value) data for India. Includes raw data, optimized SQLite database, and a Spring Boot auto-configuring library for seamless integration and health monitoring.

**🆕 NEW**: Now includes automated 200-Day Moving Average (DMA) analysis in daily releases! See funds trading above/below their 200-DMA with percentage differences in release notes.

---

## 🚀 Quick Start

### Option 1: Java Library (Recommended)

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.rajadilipkolli</groupId>
    <artifactId>historical-mf-nav-data</artifactId>
    <version>1.0.20260221</version> <!-- Use latest version from releases -->
</dependency>
```

Or for Gradle (`build.gradle`):

```gradle
dependencies {
    implementation 'io.github.rajadilipkolli:historical-mf-nav-data:1.0.20260221'
}
```

> 💡 **Finding the Latest Version**: Visit the [GitHub Releases page](https://github.com/rajadilipkolli/historical-mf-nav-data/releases) to get the latest version number.

**Zero configuration required!** The library auto-configures with Spring Boot. See below for usage and configuration.

### Option 2: Direct Database Download

Get the latest dataset at <https://github.com/rajadilipkolli/historical-mf-nav-data/releases/latest/funds.db.zst>

Unpack and create indexes (optional for advanced/manual use):

```bash
wget https://github.com/rajadilipkolli/historical-mf-nav-data/releases/latest/funds.db.zst
unzstd funds.db.zst
# Create search indexes
echo 'CREATE INDEX "nav-main" ON "nav" ("date","scheme_code")' | sqlite3 funds.db
echo 'CREATE INDEX "nav-scheme" ON "nav" ("scheme_code")' | sqlite3 funds.db
echo 'CREATE INDEX "securities-scheme" ON "securities" ("scheme_code")' | sqlite3 funds.db
echo 'CREATE INDEX "securities-isin" ON "securities" ("isin")' | sqlite3 funds.db
```

---

## ✨ Features & Architecture

- **Complete historical NAV data** for Indian mutual funds
- **Spring Boot auto-configuration**: just add the JAR, all services and repositories are ready
- **Embedded SQLite database**: in-memory by default, file-based optional
- **Optimized indexes** for fast querying
- **Comprehensive health monitoring**: programmatic, actuator, and web endpoints
- **Automated daily updates** via GitHub Actions
- **200-Day Moving Average Analysis**: Daily DMA calculations with trend analysis
- **Zero manual intervention**: always up-to-date

## 📈 200-Day Moving Average Analysis

Each daily release includes automated analysis of funds relative to their 200-day moving averages:

- **Trend Classification**: Funds above/below 200-DMA (bullish/bearish indicators)
- **Percentage Differences**: Shows strength of current trend
- **Market Sentiment**: Overall percentage of funds above/below DMA
- **Trading Days Only**: Excludes weekends from calculations
- **Minimum Data Requirement**: Only funds with 200+ trading days included

See [DMA_README.md](python/DMA_README.md) for detailed documentation and technical details.

### Database Schema

#### schemes
```sql
scheme_code INTEGER PRIMARY KEY
scheme_name TEXT
```

#### nav (NAV records)
```sql
date TEXT
scheme_code INTEGER
nav FLOAT
FOREIGN KEY (scheme_code) REFERENCES schemes(scheme_code)
```

#### securities (ISIN mappings)
```sql
isin TEXT UNIQUE
type INTEGER  -- 0=Growth/Dividend Payout, 1=Dividend Reinvestment
scheme_code INTEGER
FOREIGN KEY (scheme_code) REFERENCES schemes(scheme_code)
```

#### nav_by_isin (View)
```sql
isin TEXT
date TEXT
nav FLOAT
```

---

## 📊 Usage Example

```java
@RestController
public class MutualFundController {
    private final MutualFundService mutualFundService;
    public MutualFundController(MutualFundService mutualFundService) {
        this.mutualFundService = mutualFundService;
    }
    @GetMapping("/nav/latest/{isin}")
    public NavByIsin getLatestNav(@PathVariable String isin) {
        return mutualFundService.getLatestNavByIsin(isin);
    }
    @GetMapping("/nav/{isin}/{date}")
    public NavByIsin getNavByDate(@PathVariable String isin, @PathVariable LocalDate date) {
        return mutualFundService.getNavByIsinAndDate(isin, date);
    }
    @GetMapping("/fund/info/{isin}")
    public MutualFundService.FundInfo getFundInfo(@PathVariable String isin) {
        return mutualFundService.getFundInfo(isin);
    }
}
```

### Available Services

- `getLatestNavByIsin(String isin)` - Get the most recent NAV for an ISIN
- `getNavByIsinAndDate(String isin, LocalDate date)` - Get NAV on or before a specific date
- `getLastNDaysNav(String isin, int days)` - Get last N NAV records
- `getNavHistory(String isin, LocalDate start, LocalDate end)` - Get NAV within date range
- `getFundInfo(String isin)` - Get complete fund information
- `searchSchemes(String pattern)` - Search funds by name
- `getAllSchemes()` - Get all available schemes

---

## ⚙️ Configuration Options

Customize via `application.properties`:

```properties
# Enable/disable auto-initialization (Default: true)
daily-nav.auto-init=true
# Use a custom JDBC path (Default: jdbc:sqlite::memory:)
daily-nav.database-path=jdbc:sqlite::memory:
# Use persistent database file (Overrides database-path if set)
daily-nav.database-file=/path/to/persistent/database.db
# Enable/disable automatic index creation after loading (Default: true)
daily-nav.create-indexes=true
# Enable debug logging for DB operations (Default: false)
daily-nav.debug=false
# Enable data validation (count records) after loading (Default: true)
daily-nav.validate-data=true
```

---

## 🩺 Health Monitoring & Observability

The library provides multiple ways to monitor health and data status:

### 1. Programmatic Health Checks

```java
@Autowired
private DailyNavHealthService healthService;

// Simple health check
boolean isHealthy = healthService.isHealthy();

// Detailed health status
DailyNavHealthStatus status = healthService.checkHealth();
if (!status.isHealthy()) {
    for (String issue : status.getIssues()) {
        logger.warn("Daily NAV issue: {}", issue);
    }
}

// Get statistics
Map<String, Object> stats = healthService.getStatistics();
```

### 2. Spring Boot Actuator Integration

Add dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
Configure in `application.properties`:
```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
daily-nav.validate-data=true
```
Access health info:
```bash
curl http://localhost:8080/actuator/health
```
Sample response includes a `dailyNavHealthIndicator` section with details.

### 3. Standalone Web Endpoints

If you have `spring-boot-starter-web` but not actuator:
```bash
curl http://localhost:8080/historical-mf-nav-data/health   # 200 if healthy, 503 if not
curl http://localhost:8080/historical-mf-nav-data/info     # Detailed info
```

### 4. Custom Monitoring & Metrics

You can build custom health checks and Prometheus metrics using the service layer. See the source for advanced examples.

---

## 🗄️ Common Queries & Examples

### Get Latest NAV
```java
NavByIsin latestNav = mutualFundService.getLatestNavByIsin("INF277K01741");
System.out.println("Latest NAV: " + latestNav.getNav());
```

### Get Historical Performance
```java
List<NavByIsin> last90Days = mutualFundService.getLastNDaysNav("INF277K01741", 90);
// Calculate returns, plot charts, etc.
```

### Find Funds
```java
List<Scheme> sbiSchemes = mutualFundService.searchSchemes("SBI");
// Browse available SBI mutual funds
```

### SQL Examples

#### NAV as per Date from ISIN
```sql
SELECT date,nav from nav_by_isin
WHERE isin='INF277K01741'
AND date<='2023-03-23'
ORDER BY date DESC
LIMIT 0,1
```

#### Latest NAV
```sql
SELECT nav from nav_by_isin
WHERE isin='INF277K01741'
ORDER BY date DESC
LIMIT 0,1
```

#### Last 90 Financial Days NAV
```sql
SELECT date,nav from nav_by_isin
WHERE isin='INF277K01741'
ORDER BY date DESC
LIMIT 0,90
```

#### Get Metadata of all Mutual Funds from ISIN
```sql
SELECT isin,type,S1.scheme_code,scheme_name FROM securities S1
LEFT JOIN schemes S2 ON S1.scheme_code = S2.scheme_code
```

#### Get Information of Specific Funds from ISIN
```sql
SELECT isin,type,S1.scheme_code,scheme_name FROM securities S1
LEFT JOIN schemes S2 ON S1.scheme_code = S2.scheme_code
WHERE isin='INF277K01741'
```

---

## 📦 What's Included

- Complete historical NAV data for Indian mutual funds
- Auto-configured Spring Boot services
- Optimized database indexes
- Type-safe Java models
- Repository pattern implementation
- Comprehensive documentation
- Usage examples
- Health monitoring and observability

---

## 🏗️ Automation & Updates

- **Daily Data Fetching**: GitHub Actions fetches latest NAV data every day
- **Database Generation**: Python scripts process CSVs into SQLite
- **JAR Creation**: Maven builds a self-contained JAR with embedded DB
- **Auto-Release**: Every update triggers a new release with JAR and DB
- **Zero Manual Intervention**: Always up-to-date

---

## Data Storage Change: Zipped CSVs

To reduce repository size, daily fetched CSVs are now stored as compressed `.zip` files under the `data/YYYY/MM/` folders. Each archive contains the original `DD.csv` file (for example `01.zip` -> `01.csv`).

- To fetch and store data, run the `python/fetch.py` script as before; it now saves `.zip` files instead of raw `.csv`.
- Tools that process CSVs (for example `python/generate.py`) detect `.zip` files and read the contained CSV automatically.

If you need to extract a CSV manually:

```bash
python -c "import zipfile; z=zipfile.ZipFile('data/2025/12/01.zip'); z.extractall('outdir')"
```


---

## 📝 Versioning

Follows a timestamp-based versioning scheme: `MAJOR.MINOR.YYYYMMDD`.

1. Major: 1 for stable releases  
2. Minor: 0 for normal releases
3. Patch: Date-based (YYYYMMDD format)
4. Example: `1.0.20250713` for July 13, 2025

**Note**: No guarantee all pricing info is present for the release date. Daily releases are automatically generated with the latest available data.

---

## 🤝 Contributing

Contributions welcome! See [LICENSE](LICENSE) for details.

---

## 📄 License

MIT License. See the [LICENSE](LICENSE) file for details.
