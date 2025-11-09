#!/usr/bin/env python3
import sys
import json

def print_description():
    """Print tool description in the required format."""
    print("Configure weather alerts for a location based on temperature and precipitation thresholds")
    print("enable_temperature_alert\tboolean\trequired\tWhether to enable temperature-based alerts")
    print("enable_precipitation_alert\tboolean\trequired\tWhether to enable precipitation-based alerts")
    print("temperature_threshold_celsius\tnumber\trequired\tTemperature threshold in Celsius (e.g., 35.0 for heat warning)")
    print("precipitation_threshold_mm\tnumber\trequired\tPrecipitation threshold in millimeters (e.g., 50.0 for flood risk)")

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

def main():
    # Check for --description flag
    if len(sys.argv) == 2 and sys.argv[1] == "--description":
        print_description()
        sys.exit(0)
    
    # Parse command line arguments
    try:
        if len(sys.argv) != 5:
            raise ValueError("Expected 4 arguments: enable_temperature_alert enable_precipitation_alert temperature_threshold_celsius precipitation_threshold_mm")
        
        # Parse parameters from command line
        enable_temperature_alert = parse_boolean(sys.argv[1])
        enable_precipitation_alert = parse_boolean(sys.argv[2])
        temperature_threshold_celsius = float(sys.argv[3])
        precipitation_threshold_mm = float(sys.argv[4])
        
        # Execute tool
        result = set_weather_alert(
            enable_temperature_alert,
            enable_precipitation_alert,
            temperature_threshold_celsius,
            precipitation_threshold_mm
        )
        
        print(result)
        sys.exit(0)
        
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
