"""
Calculate the number of days between two YYYY-MM-DD formatted dates.
"""

import json
import sys
from datetime import datetime


def parse_date(date_string):
    """Parse a YYYY-MM-DD formatted date string."""
    try:
        return datetime.strptime(date_string, "%Y-%m-%d")
    except ValueError as exc:
        raise ValueError(
            f"Invalid date format '{date_string}'. Use YYYY-MM-DD (e.g., 2023-01-17)"
        ) from exc


def print_description():
    print("Calculate the number of days between start_date and end_date")
    schema = {
        "type": "object",
        "properties": {
            "start_date": {
                "type": "string",
                "description": "start date formatted as YYYY-MM-DD",
            },
            "end_date": {
                "type": "string",
                "description": "end date formatted as YYYY-MM-DD",
            },
        },
        "required": ["start_date", "end_date"],
    }
    print(json.dumps(schema, separators=(",", ":")))


def parse_input(argument):
    try:
        payload = json.loads(argument)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON input: {exc.msg}") from exc

    if not isinstance(payload, dict):
        raise ValueError("Input JSON must describe an object")

    missing = [field for field in ("start_date", "end_date") if field not in payload]
    if missing:
        raise ValueError(f"Missing required field(s): {', '.join(missing)}")

    start_date = payload["start_date"]
    end_date = payload["end_date"]

    if not isinstance(start_date, str) or not isinstance(end_date, str):
        raise ValueError("Fields 'start_date' and 'end_date' must be strings")

    return start_date, end_date


def main():
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print_description()
        sys.exit(0)

    if len(sys.argv) != 2:
        print("Expected a single JSON argument", file=sys.stderr)
        sys.exit(1)

    try:
        start_date_str, end_date_str = parse_input(sys.argv[1])
        start_date = parse_date(start_date_str)
        end_date = parse_date(end_date_str)
    except ValueError as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)

    days_difference = (end_date - start_date).days
    print(days_difference)


if __name__ == "__main__":
    main()
