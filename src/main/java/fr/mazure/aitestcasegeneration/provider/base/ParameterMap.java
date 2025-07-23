package fr.mazure.aitestcasegeneration.provider.base;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public class ParameterMap {

    private final Map<String, Object> data;

    public ParameterMap(Map<String, Object> data) {
        this.data = data;
    }

    public String getString(final String parameterName) throws MissingModelParameter, InvalidModelParameter {
        final Optional<String> string = getOptionalString(parameterName);
        if (string.isEmpty()) {
            throw new MissingModelParameter(parameterName);
        }
        return string.get();
    }

    public URL getUrl(final String parameterName) throws MissingModelParameter, InvalidModelParameter {
        final Optional<URL> url = getOptionalUrl(parameterName);
        if (url.isEmpty()) {
            throw new MissingModelParameter(parameterName);
        }
        return url.get();
    }

    public Map<String, String> getMap(final String parameterName) throws MissingModelParameter, InvalidModelParameter {
        final Optional<Map<String, String>> map = getOptionalMap(parameterName);
        if (map.isEmpty()) {
            throw new MissingModelParameter(parameterName);
        }
        return map.get();
    }

    public Optional<URL> getOptionalUrl(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (!(value instanceof String stringValue)) {
            throw new InvalidModelParameter(parameterName, "URL", value.toString());
        }
        URL url;
        try {
            url = (new URI(stringValue)).toURL();
        } catch (final IllegalArgumentException | MalformedURLException | URISyntaxException e) {
            throw new InvalidModelParameter(parameterName, "URL", stringValue);
        }
        return Optional.of(url);
    }

    public Optional<Map<String, String>> getOptionalMap(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (value instanceof Map<?, ?> mapValue) {
            try {
                @SuppressWarnings("unchecked")
                final Map<String, String> typedMap = (Map<String, String>) mapValue;
                return Optional.of(typedMap);
            } catch (final ClassCastException e) {
                throw new InvalidModelParameter(parameterName, "Map<String, String>", value.toString());
            }
        } else {
            throw new InvalidModelParameter(parameterName, "Map<String, String>", value.toString());
        }
    }

    public Optional<String> getOptionalString(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (value instanceof String stringValue) {
            return Optional.of(stringValue);
        }
        throw new InvalidModelParameter(parameterName, "String", value.toString());
    }

    public Optional<Double> getOptionalDouble(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (value instanceof Double doubleValue) {
            return Optional.of(doubleValue);
        }
        throw new InvalidModelParameter(parameterName, "Double", value.toString());
    }

    public Optional<Integer> getOptionalInteger(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (value instanceof Integer integerValue) {
            return Optional.of(integerValue);
        }
        throw new InvalidModelParameter(parameterName, "Integer", value.toString());
    }

    public Optional<Boolean> getOptionalBoolean(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (value instanceof Boolean booleanValue) {
            return Optional.of(booleanValue);
        }
        throw new InvalidModelParameter(parameterName, "Boolean", value.toString());
    }
}
