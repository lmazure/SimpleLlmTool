#!/usr/bin/env python3
"""
Tool to add two numbers
"""

import sys


def add_two_numbers(num1, num2):
    return num1 + num2

if __name__ == "__main__":
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print("Returns the sum of two numbers")
        print("num1\tinteger\trequired\tThe first number")
        print("num2\tinteger\trequired\tThe second number")
        sys.exit(0)

    if len(sys.argv) != 3:
        print("Usage: python add_two_numbers <number_1> <number_2")
        sys.exit(1)

    num1 = int(sys.argv[1])
    num2 = int(sys.argv[2]) 
    result = add_two_numbers(num1, num2)
    print(result)
