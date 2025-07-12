-- Embedded SQL schema and minimal data for testing DatabaseInitializer

CREATE TABLE schemes (
  scheme_code INTEGER PRIMARY KEY,
  scheme_name TEXT
);

CREATE TABLE nav (
  scheme_code INTEGER,
  date TEXT,
  nav REAL,
  FOREIGN KEY (scheme_code) REFERENCES schemes(scheme_code)
);

CREATE TABLE securities (
  isin TEXT,
  type INTEGER,
  scheme_code INTEGER,
  FOREIGN KEY (scheme_code) REFERENCES schemes(scheme_code)
);

-- Insert minimal data for test validation
INSERT INTO schemes (scheme_code, scheme_name) VALUES (1, 'Test Scheme');
INSERT INTO nav (scheme_code, date, nav) VALUES (1, '2025-07-01', 100.0);
INSERT INTO securities (isin, type, scheme_code) VALUES ('ISIN123', 0, 1);
