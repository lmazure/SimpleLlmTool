"""
Tool to return current date and time in ISO 8601 format (YYYY-MM-DDTHH:MM:SS.ffffff)
"""

import json
import sys
from datetime import datetime


def print_description():
    print("Returns the current date and time formated as YYYY-MM-DDTHH:MM:SS.ffffff")
    schema = {
        "type": "object",
        "properties": {},
        "required": [],
    }
    print(json.dumps(schema, separators=(",", ":")))


def parse_input(argument):
    try:
        payload = json.loads(argument)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON input: {exc.msg}") from exc

    if not isinstance(payload, dict):
        raise ValueError("Input JSON must describe an object")

    if payload:
        raise ValueError("No parameters are supported for this tool")


if __name__ == "__main__":
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print_description()
        sys.exit(0)

    if len(sys.argv) != 2:
        print("Expected a single JSON argument", file=sys.stderr)
        sys.exit(1)

    try:
        parse_input(sys.argv[1])
    except ValueError as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)

    local_iso = datetime.now().isoformat()
    print(local_iso)