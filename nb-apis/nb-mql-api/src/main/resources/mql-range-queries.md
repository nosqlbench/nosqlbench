# Range Queries

Retrieve time-series data over a time window. Range queries return all data points within a specified time range.

**Database:** `examples.db`

---

## Example 5: Time Series - Full History

**Command:**
```
range --metric api_requests_total --window 1h --labels method=GET,endpoint=/users,status=200
```

**Expected Output:**
```
timestamp             | value   | labels
----------------------------------------------------------------------------------
2025-10-23 10:00:00.0 | 0.0     | endpoint=/users, method=GET, status=200
2025-10-23 10:01:00.0 | 1000.0  | endpoint=/users, method=GET, status=200
2025-10-23 10:02:00.0 | 2200.0  | endpoint=/users, method=GET, status=200
2025-10-23 10:05:00.0 | 5500.0  | endpoint=/users, method=GET, status=200
2025-10-23 10:10:00.0 | 11000.0 | endpoint=/users, method=GET, status=200

5 rows (Xms)
```

**What it shows:** Complete time-series progression showing how a counter grew from 0 to 11,000 requests over 10 minutes.

**Use cases:**
- Visualize traffic patterns over time
- Analyze growth trends
- Export to graphing tools
- Historical analysis

---

## Example 6: Track Errors Over Time

**Command:**
```
range --metric api_requests_total --window 1h --labels status=500
```

**Expected Output:**
```
timestamp             | value | labels
--------------------------------------------------------------------------------
2025-10-23 10:00:00.0 | 0.0   | endpoint=/users, method=GET, status=500
2025-10-23 10:01:00.0 | 2.0   | endpoint=/users, method=GET, status=500
2025-10-23 10:02:00.0 | 4.0   | endpoint=/users, method=GET, status=500
2025-10-23 10:05:00.0 | 8.0   | endpoint=/users, method=GET, status=500
2025-10-23 10:10:00.0 | 15.0  | endpoint=/users, method=GET, status=500

5 rows (Xms)
```

**What it shows:** Track how server errors accumulated during the session. Notice the accelerated error rate during the high-load period (10:02 to 10:05).

**Use cases:**
- Incident investigation
- Error rate trending
- Correlate errors with load spikes
- SLA violation detection

---

## Key Concepts

**Time Windows:**
- `--window 5m` - Last 5 minutes from latest snapshot
- `--window 1h` - Last hour
- `--last 10m` - Alternative syntax for --window

**Alternative: Explicit Time Range (Future):**
```
range --metric api_requests_total --start 1729681200000 --end 1729684800000
```

**Output Ordering:**
- Results are ordered by timestamp (oldest first)
- Within same timestamp, ordered by labels alphabetically

**Performance:**
- Range queries scan all data in the time window
- Use label filters to reduce result set
- Typical query time: <20ms for 1 hour of data

**Visualization:**
- Export to CSV for Excel/Sheets: `--format csv`
- Export to JSON for plotting libraries: `--format json`
- Table format for quick console analysis
