"""
Calculate the number of days between two YYYY-MM-DD formatted dates.

Usage: python date_diff.py 2023-01-01 2023-12-31
"""

import sys
from datetime import datetime

def parse_date(date_string):
    """Parse a YYYY-MM-DD formatted date string."""
    try:
        return datetime.strptime(date_string, "%Y-%m-%d")
    except ValueError:
        print(f"Error: Invalid date format '{date_string}'. Use YYYY-MM-DD format (e.g., 2023-01-17)")
        sys.exit(1)

def main():
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print("Calculate the number of days between start_date and end_date")
        print("start_date\tstart date formatted as YYYY-MM-DD")
        print("end_date\tend date formatted as YYYY-MM-DD")
        sys.exit(0)
    
    if len(sys.argv) != 3:
        print("Usage: python date_diff.py <start_date> <end_date>")
        sys.exit(1)
    
    start_date_str = sys.argv[1]
    end_date_str = sys.argv[2]
    
    start_date = parse_date(start_date_str)
    end_date = parse_date(end_date_str)
    
    days_difference = (end_date - start_date).days
    print(days_difference)


if __name__ == "__main__":
    main()
