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

    public String getString(final String parameterName) throws MissingModelParameter {
        if (!data.containsKey(parameterName) || !(data.get(parameterName) instanceof String)) {
            throw new MissingModelParameter(parameterName);
        }
        return (String) data.get(parameterName);
    }

    public Optional<URL> getOptionalUrl(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        URL url;
        try {
            url = (new URI((String) data.get(parameterName))).toURL();
        } catch (final IllegalArgumentException | MalformedURLException | URISyntaxException e) {
            throw new InvalidModelParameter(parameterName, "URL", data.get(parameterName).toString());
        }
        return Optional.of(url);
    }

    public Optional<String> getOptionalString(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (!(value instanceof String)) {
            throw new InvalidModelParameter(parameterName, "String", data.get(parameterName).toString());
        }
        return Optional.of((String) value);
    }

    public Optional<Double> getOptionalDouble(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (!(value instanceof Double)) {
            throw new InvalidModelParameter(parameterName, "Double", data.get(parameterName).toString());
        }
        return Optional.of((Double) value);
    }

    public Optional<Integer> getOptionalInteger(final String parameterName) throws InvalidModelParameter {
        if (!data.containsKey(parameterName)) {
            return Optional.empty();
        }
        final Object value = data.get(parameterName);
        if (!(value instanceof Integer)) {
            throw new InvalidModelParameter(parameterName, "Integer", data.get(parameterName).toString());
        }
        return Optional.of((Integer) value);
    }
}
