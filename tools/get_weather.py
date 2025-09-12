#!/usr/bin/env python3
"""
Tool to return current weather for a given city
"""

import sys
import requests
import json

def get_coordinates(city_name):
    """
    Gets coordinates for a city using OpenMeteo's geocoding API.

    Args:
        city_name (str): Name of the city

    Returns:
        tuple: (latitude, longitude, display_name) or None if not found
    """
    try:
        # Use OpenMeteo geocoding API
        geocode_url = "https://geocoding-api.open-meteo.com/v1/search"
        params = {'name': city_name, 'count': 1, 'language': 'en', 'format': 'json'}

        response = requests.get(geocode_url, params=params)
        response.raise_for_status()

        data = response.json()

        if 'results' not in data or len(data['results']) == 0:
            return None

        result = data['results'][0]
        lat = result['latitude']
        lon = result['longitude']

        # Create display name
        name_parts = [result['name']]
        if 'admin1' in result and result['admin1']:
            name_parts.append(result['admin1'])
        if 'country' in result and result['country']:
            name_parts.append(result['country'])

        display_name = ', '.join(name_parts)

        return (lat, lon, display_name)

    except Exception:
        return None

def get_weather(city_name):
    """
    Gets the current weather for a given city using OpenMeteo API.

    Args:
        city_name (str): Name of the city

    Returns:
        str: Weather information formatted as a string
    """
    # First, get coordinates for the city
    coords = get_coordinates(city_name)
    if coords is None:
        return f"Error: City '{city_name}' not found"

    lat, lon, display_name = coords

    try:
        # Make API request to OpenMeteo
        BASE_URL = "https://api.open-meteo.com/v1/forecast"
        params = {
            'latitude': lat,
            'longitude': lon,
            'current': 'temperature_2m,relative_humidity_2m,apparent_temperature,weather_code',
            'timezone': 'auto'
        }

        response = requests.get(BASE_URL, params=params)
        response.raise_for_status()

        data = response.json()

        # Extract weather information
        current = data['current']
        temp = current['temperature_2m']
        feels_like = current['apparent_temperature']
        humidity = current['relative_humidity_2m']
        weather_code = current['weather_code']

        # Convert weather code to description
        weather_descriptions = {
            0: "Clear Sky", 1: "Mainly Clear",
            2: "Partly Cloudy", 3: "Overcast",
            45: "Fog",
            48: "Depositing Rime Fog",
            51: "Light Drizzle",
            53: "Moderate Drizzle",
            55: "Dense Drizzle",
            56: "Light Freezing Drizzle",
            57: "Dense Freezing Drizzle",
            61: "Slight Rain",
            63: "Moderate Rain",
            65: "Heavy Rain",
            66: "Light Freezing Rain",
            67: "Heavy Freezing Rain",
            71: "Slight Snow",
            73: "Moderate Snow",
            75: "Heavy Snow",
            77: "Snow Grains",
            80: "Slight Rain Showers",
            81: "Moderate Rain Showers",
            82: "Violent Rain Showers",
            85: "Slight Snow Showers",
            86: "Heavy Snow Showers",
            95: "Thunderstorm",
            96: "Thunderstorm With Slight Hail",
            99: "Thunderstorm With Heavy Hail"
        }

        description = weather_descriptions.get(weather_code, "Unknown")

        # Format weather information
        weather_info = f"{display_name}: {temp}°C, {description}, Feels like {feels_like}°C, Humidity {humidity}%"
        return weather_info

    except requests.exceptions.RequestException as e:
        return f"Error: Unable to fetch weather data - {str(e)}"
    except KeyError as e:
        return f"Error: Invalid response format - {str(e)}"
    except json.JSONDecodeError:
        return "Error: Invalid JSON response"

if __name__ == "__main__":
    # Check command line arguments
    if len(sys.argv) == 2:
        if sys.argv[1] == "--description":
            print("Returns the current weather for a given city\ncity\tThe city for which the weather forecast should be returned, only the city name should be present")
        else:
            # City name provided
            city_name = sys.argv[1]
            weather = get_weather(city_name)
            print(weather)
    elif len(sys.argv) == 1:
        print("Error: City name is required.", file=sys.stderr)
        sys.exit(1)
    else:
        print("Error: Invalid arguments. Usage: get_weather.py <city_name> or get_weather.py --description", file=sys.stderr)
        sys.exit(1)