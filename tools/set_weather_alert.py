#!/usr/bin/env python3
import sys
import json

def print_description():
    """Print tool description with JSON schema."""
    print(
        "Configure weather alerts for a location based on temperature and precipitation thresholds"
    )
    schema = {
        "type": "object",
        "properties": {
            "enable_temperature_alert": {
                "type": "boolean",
                "description": "Whether to enable temperature-based alerts",
            },
            "enable_precipitation_alert": {
                "type": "boolean",
                "description": "Whether to enable precipitation-based alerts",
            },
            "temperature_threshold_celsius": {
                "type": "number",
                "description": "Temperature threshold in Celsius (e.g., 35.0 for heat warning)",
            },
            "precipitation_threshold_mm": {
                "type": "number",
                "description": "Precipitation threshold in millimeters (e.g., 50.0 for flood risk)",
            },
        },
        "required": [
            "enable_temperature_alert",
            "enable_precipitation_alert",
            "temperature_threshold_celsius",
            "precipitation_threshold_mm",
        ],
    }
    print(json.dumps(schema, separators=(",", ":")))

def parse_boolean(value: str) -> bool:
    """Parse a boolean value from string."""
    if value.lower() in ('true', '1', 'yes', 'on'):
        return True
    elif value.lower() in ('false', '0', 'no', 'off'):
        return False
    else:
        raise ValueError(f"Invalid boolean value: {value}")

def set_weather_alert(
    enable_temperature_alert: bool,
    enable_precipitation_alert: bool,
    temperature_threshold_celsius: float,
    precipitation_threshold_mm: float
) -> str:
    """
    Configure weather alerts for a location.
    Returns a JSON string with the configuration result.
    """
    
    # Validate inputs
    if temperature_threshold_celsius < -100 or temperature_threshold_celsius > 100:
        raise ValueError("Temperature threshold must be between -100°C and 100°C")
    
    if precipitation_threshold_mm < 0:
        raise ValueError("Precipitation threshold must be non-negative")
    
    # Build alert configuration
    config = {
        "success": True,
        "alerts": {
            "temperature": {
                "enabled": enable_temperature_alert,
                "threshold_celsius": temperature_threshold_celsius if enable_temperature_alert else None
            },
            "precipitation": {
                "enabled": enable_precipitation_alert,
                "threshold_mm": precipitation_threshold_mm if enable_precipitation_alert else None
            }
        }
    }
    
    # Add summary message
    active_alerts = []
    if enable_temperature_alert:
        active_alerts.append(f"temperature {temperature_threshold_celsius}°C")
    if enable_precipitation_alert:
        active_alerts.append(f"precipitation {precipitation_threshold_mm}mm")
    
    if active_alerts:
        config["message"] = f"Weather alerts configured for: {' and '.join(active_alerts)}"
    else:
        config["message"] = "No weather alerts enabled"
    
    return json.dumps(config)

def parse_input(argument):
    try:
        payload = json.loads(argument)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON input: {exc.msg}") from exc

    if not isinstance(payload, dict):
        raise ValueError("Input JSON must describe an object")

    required_fields = (
        "enable_temperature_alert",
        "enable_precipitation_alert",
        "temperature_threshold_celsius",
        "precipitation_threshold_mm",
    )

    missing = [field for field in required_fields if field not in payload]
    if missing:
        raise ValueError(f"Missing required field(s): {', '.join(missing)}")

    def coerce_bool(value, field_name):
        if isinstance(value, bool):
            return value
        if isinstance(value, str):
            return parse_boolean(value)
        raise ValueError(f"Field '{field_name}' must be a boolean")

    try:
        enable_temperature_alert = coerce_bool(payload["enable_temperature_alert"], "enable_temperature_alert")
        enable_precipitation_alert = coerce_bool(payload["enable_precipitation_alert"], "enable_precipitation_alert")
    except ValueError as exc:
        raise ValueError(str(exc)) from exc

    try:
        temperature_threshold_celsius = float(payload["temperature_threshold_celsius"])
        precipitation_threshold_mm = float(payload["precipitation_threshold_mm"])
    except (TypeError, ValueError) as exc:
        raise ValueError("Threshold fields must be numbers") from exc

    return (
        enable_temperature_alert,
        enable_precipitation_alert,
        temperature_threshold_celsius,
        precipitation_threshold_mm,
    )


def main():
    # Check for --description flag
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print_description()
        sys.exit(0)

    try:
        if len(sys.argv) != 2:
            raise ValueError("Expected a single JSON argument")

        (
            enable_temperature_alert,
            enable_precipitation_alert,
            temperature_threshold_celsius,
            precipitation_threshold_mm,
        ) = parse_input(sys.argv[1])

        result = set_weather_alert(
            enable_temperature_alert,
            enable_precipitation_alert,
            temperature_threshold_celsius,
            precipitation_threshold_mm,
        )

        print(result)
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
