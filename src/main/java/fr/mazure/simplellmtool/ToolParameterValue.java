package fr.mazure.simplellmtool;

import java.util.Objects;

/*
 * ToolParameterValue represents the value of a tool parameter
 */
public record ToolParameterValue(ToolParameterType type, Object value) {
    public ToolParameterValue {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        
        validateType(type, value);
    }
    
    public static ToolParameterValue convert(final Object value) {
        return switch(value) {
            case String str -> new ToolParameterValue(ToolParameterType.STRING, str);
            case Integer integ -> new ToolParameterValue(ToolParameterType.INTEGER, integ);
            case Double dbl -> new ToolParameterValue(ToolParameterType.NUMBER, dbl);
            case Boolean bl -> new ToolParameterValue(ToolParameterType.BOOLEAN , bl);
            default -> throw new IllegalArgumentException("Unsupported value type " + value.getClass().getName());
        };
    }

    private static void validateType(final ToolParameterType type,
                                     final Object value) {
        final boolean valid = switch (type) {
            case STRING -> value instanceof String;
            case INTEGER -> value instanceof Integer;
            case NUMBER -> value instanceof Double;
            case BOOLEAN -> value instanceof Boolean;
        };
        
        if (!valid) {
            throw new IllegalArgumentException("parameter value type mismatch: expected " + type + ", got " + value.getClass().getName());
        }
    }
    
    public String getString() {
        return (String) this.value;
    }
    
    public Integer getInteger() {
        return (Integer) this.value;
    }
    
    public Double getDouble() {
        return (Double) this.value;
    }
    
    public Boolean getBoolean() {
        return (Boolean) this.value;
    }

    public String convertToString() {
        return String.valueOf(this.value);
    }
}