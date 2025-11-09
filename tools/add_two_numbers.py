#!/usr/bin/env python3
"""
Tool to add two numbers
"""

import json
import sys


def add_two_numbers(num1, num2):
    return num1 + num2


def print_description():
    print("Returns the sum of two numbers")
    schema = {
        "type": "object",
        "properties": {
            "num1": {"type": "integer", "description": "The first number"},
            "num2": {"type": "integer", "description": "The second number"},
        },
        "required": ["num1", "num2"],
    }
    print(json.dumps(schema, separators=(",", ":")))


def parse_input(argument):
    try:
        payload = json.loads(argument)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON input: {exc.msg}") from exc

    if not isinstance(payload, dict):
        raise ValueError("Input JSON must describe an object")

    for key in ("num1", "num2"):
        if key not in payload:
            raise ValueError(f"Missing required field '{key}'")

    try:
        num1 = int(payload["num1"])
        num2 = int(payload["num2"])
    except (TypeError, ValueError) as exc:
        raise ValueError("Fields 'num1' and 'num2' must be integers") from exc

    return num1, num2


if __name__ == "__main__":
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print_description()
        sys.exit(0)

    if len(sys.argv) != 2:
        print("Expected a single JSON argument", file=sys.stderr)
        sys.exit(1)

    try:
        num1, num2 = parse_input(sys.argv[1])
    except ValueError as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)

    result = add_two_numbers(num1, num2)
    print(result)
