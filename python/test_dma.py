#!/usr/bin/env python3
"""
Test script for DMA calculations with sample data.
"""

import sqlite3
import pandas as pd
import os
import sys
from datetime import datetime, timedelta
import tempfile

def create_test_database():
    """Create a test database with sample data."""
    # Create temporary database
    fd, db_path = tempfile.mkstemp(suffix='.db')
    os.close(fd)
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # Create tables
    cursor.execute('''
        CREATE TABLE schemes (
            scheme_code INTEGER PRIMARY KEY,
            scheme_name TEXT
        )
    ''')
    
    cursor.execute('''
        CREATE TABLE nav (
            scheme_code INTEGER,
            date TEXT,
            nav REAL,
            FOREIGN KEY (scheme_code) REFERENCES schemes(scheme_code)
        )
    ''')
    
    cursor.execute('''
        CREATE TABLE securities (
            isin TEXT UNIQUE,
            type INTEGER,
            scheme_code INTEGER,
            FOREIGN KEY (scheme_code) REFERENCES schemes(scheme_code)
        )
    ''')
    
    # Insert test schemes
    schemes = [
        (1, "Test Growth Fund"),
        (2, "Sample Equity Fund"),
        (3, "Demo Balanced Fund")
    ]
    
    cursor.executemany('INSERT INTO schemes VALUES (?, ?)', schemes)
    
    # Generate test NAV data for 300 trading days (excluding weekends)
    base_date = datetime(2023, 1, 1)
    nav_data = []
    
    for scheme_code in [1, 2, 3]:
        current_date = base_date
        nav_value = 100.0 + scheme_code * 10  # Different starting values
        
        trading_days = 0
        while trading_days < 300:
            # Skip weekends
            if current_date.weekday() < 5:  # Monday=0, Friday=4
                # Add some randomness to NAV (simulate market movements)
                import random
                random.seed(scheme_code * 1000 + trading_days)  # Reproducible randomness
                change = random.uniform(-0.02, 0.02)  # ±2% daily change
                nav_value *= (1 + change)
                
                nav_data.append((scheme_code, current_date.strftime('%Y-%m-%d'), round(nav_value, 4)))
                trading_days += 1
            
            current_date += timedelta(days=1)
    
    cursor.executemany('INSERT INTO nav VALUES (?, ?, ?)', nav_data)
    
    # Insert test securities
    securities = [
        ("INF123456789", 0, 1),
        ("INF987654321", 0, 2),
        ("INF111222333", 0, 3)
    ]
    cursor.executemany('INSERT INTO securities VALUES (?, ?, ?)', securities)
    
    conn.commit()
    conn.close()
    
    return db_path

def test_dma_basic_logic():
    """Test the basic DMA calculation logic without importing the main module."""
    # Create test database
    test_db = create_test_database()
    
    try:
        # Test database connection
        print("Testing database connection...")
        conn = sqlite3.connect(test_db)
        
        # Test data loading (similar to get_trading_days_data)
        print("Testing data loading...")
        query = """
        SELECT 
            n.scheme_code,
            n.date,
            n.nav,
            s.scheme_name
        FROM nav n
        JOIN schemes s ON n.scheme_code = s.scheme_code
        WHERE n.nav IS NOT NULL
        ORDER BY n.scheme_code, n.date
        """
        
        df = pd.read_sql_query(query, conn)
        df['date'] = pd.to_datetime(df['date'])
        
        # Remove weekends
        df = df[~df['date'].dt.weekday.isin([5, 6])]
        df = df.sort_values(['scheme_code', 'date']).reset_index(drop=True)
        
        print(f"Loaded {len(df)} NAV records for {df['scheme_code'].nunique()} schemes")
        
        # Test 200-DMA calculation
        print("Testing 200-DMA calculation...")
        df['dma_200'] = df.groupby('scheme_code')['nav'].transform(
            lambda x: x.rolling(window=200, min_periods=200).mean()
        )
        
        df_with_dma = df.dropna(subset=['dma_200'])
        schemes_with_dma = df_with_dma['scheme_code'].nunique()
        print(f"Calculated 200-DMA for {schemes_with_dma} schemes")
        
        # Test getting latest data
        print("Testing latest data extraction...")
        latest_data = df_with_dma.groupby('scheme_code').apply(
            lambda x: x.loc[x['date'].idxmax()]
        ).reset_index(drop=True)
        
        print(f"Latest data for {len(latest_data)} schemes")
        
        # Test classification
        print("Testing classification...")
        latest_data['dma_diff_pct'] = ((latest_data['nav'] - latest_data['dma_200']) / latest_data['dma_200'] * 100)
        
        funds_above_dma = latest_data[latest_data['nav'] > latest_data['dma_200']].copy()
        funds_below_dma = latest_data[latest_data['nav'] < latest_data['dma_200']].copy()
        
        print(f"Funds above 200-DMA: {len(funds_above_dma)}")
        print(f"Funds below 200-DMA: {len(funds_below_dma)}")
        
        # Show sample results
        print("\nSample results:")
        for _, row in latest_data.iterrows():
            status = "ABOVE" if row['nav'] > row['dma_200'] else "BELOW"
            diff_pct = row['dma_diff_pct']
            print(f"{row['scheme_name']}: Current NAV={row['nav']:.4f}, 200-DMA={row['dma_200']:.4f}, Status={status} ({diff_pct:+.2f}%)")
        
        conn.close()
        print("\nBasic DMA logic test passed successfully!")
        
        return True
        
    except Exception as e:
        print(f"Test failed: {e}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        # Clean up test database
        try:
            if os.path.exists(test_db):
                os.unlink(test_db)
        except:
            pass  # Ignore cleanup errors on Windows
    
if __name__ == "__main__":
    success = test_dma_basic_logic()
    sys.exit(0 if success else 1)
