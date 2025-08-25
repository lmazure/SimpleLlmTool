"""
Tool to return current date and time in ISO 8601 format (YYYY-MM-DDTHH:MM:SS.ffffff)
"""

import sys
from datetime import datetime

if __name__ == "__main__":
    # Check command line arguments
    if len(sys.argv) > 1:
        if len(sys.argv) == 2 and sys.argv[1] == "--description":
            print("Returns the current date and time formated as YYYY-MM-DDTHH:MM:SS.ffffff")
        else:
            print("Error: Invalid argument. Only '--description' is allowed.", file=sys.stderr)
            sys.exit(1)
    else:
        # Local time in ISO 8601 format
        local_iso = datetime.now().isoformat()
        print(local_iso)