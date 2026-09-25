CREATE TABLE schemes (
  scheme_code BIGINT PRIMARY KEY,
  scheme_name TEXT,
  amc TEXT,
  category TEXT,
  plan TEXT,
  option TEXT
);

CREATE TABLE nav (
  scheme_code BIGINT,
  date DATE,
  nav BIGINT,
  CONSTRAINT uq_nav_scheme_date UNIQUE (scheme_code, date)
);

CREATE TABLE securities (
  isin TEXT,
  type INTEGER,
  scheme_code BIGINT
);

CREATE VIEW nav_by_isin AS
SELECT date, isin, nav
FROM nav JOIN securities ON nav.scheme_code = securities.scheme_code;
