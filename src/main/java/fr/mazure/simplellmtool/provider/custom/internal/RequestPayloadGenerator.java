package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import fr.mazure.simplellmtool.ToolParameterType;
import fr.mazure.simplellmtool.ToolParameterValue;

/*
 * Generates a payload by evaluating Handlebars templates
 */
class RequestPayloadGenerator {

    /**
     * Generates a payload by evaluating a Handlebars template with the provided messages.
     *
     * @param handlebarsTemplate the Handlebars template to evaluate
     * @param messages the list of message rounds
     * @param modelName the name of the model
     * @param tools the list of tools
     * @param apiKey the API key
     * @return the evaluated template as a string
     */
    static String generate(final String handlebarsTemplate,
                           final List<MessageRound> messages,
                           final String modelName,
                           final List<ToolSpecification> tools,
                           final String apiKey) {
        try {
            final Handlebars handlebars = new Handlebars();

            handlebars.with(EscapingStrategy.NOOP);

            registerHelpers(handlebars);

            final Template template = handlebars.compileInline(handlebarsTemplate);

            final Map<String, Object> context = new HashMap<>();
            context.put("messages", convertMessageRounds(messages));
            context.put("modelName", modelName);
            context.put("tools", convertToolSpecifications(tools));
            context.put("apiKey", apiKey);

            return template.apply(context);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to process Handlebars template\n" + StringUtils.addLineNumbers(handlebarsTemplate), e);
        }
    }

    /*
     * Registers custom Handlebars helpers for template evaluation
     */
    private static void registerHelpers(final Handlebars handlebars) {
        handlebars.registerHelper("isSystem", (final MessageRound.Role role, final Options _) -> Boolean.valueOf(MessageRound.Role.SYSTEM.equals(role)));
        handlebars.registerHelper("isUser",   (final MessageRound.Role role, final Options _) -> Boolean.valueOf(MessageRound.Role.USER.equals(role)));
        handlebars.registerHelper("isModel",  (final MessageRound.Role role, final Options _) -> Boolean.valueOf(MessageRound.Role.MODEL.equals(role)));
        handlebars.registerHelper("isTool",   (final MessageRound.Role role, final Options _) -> Boolean.valueOf(MessageRound.Role.TOOL.equals(role)));

        handlebars.registerHelper("convertStringToJsonString", (final String text, final Options _) -> jsonConverter(text));
        handlebars.registerHelper("convertToolParametersToJsonString", (final List<Map<String, Object>> list, final Options _) -> jsonToolParametersConverter(list));
        handlebars.registerHelper("convertToolParameterValueToJsonString", (final ToolParameterValue value, final Options _) -> jsonToolParameterConverter(value));

        handlebars.registerHelper("isStringType",  (final String type, final Options _) -> Boolean.valueOf("string".equals(type)));
        handlebars.registerHelper("isIntegerType", (final String type, final Options _) -> Boolean.valueOf("integer".equals(type)));
        handlebars.registerHelper("isNumberType",  (final String type, final Options _) -> Boolean.valueOf("number".equals(type)));
        handlebars.registerHelper("isBooleanType", (final String type, final Options _) -> Boolean.valueOf("boolean".equals(type)));
    }

    private static String jsonToolParameterConverter(final ToolParameterValue value) {
        return switch (value.type()) {
            case ToolParameterType.STRING -> "\"" + StringUtils.escapeStringForJson(value.getString()) + "\"";
            case ToolParameterType.INTEGER -> value.getInteger().toString();
            case ToolParameterType.NUMBER -> value.getDouble().toString();
            case ToolParameterType.BOOLEAN -> value.getBoolean().toString();
        };
    }
    
    /**
     * Converts a list of tool parameters to a JSON string
     *
     * @param list the list of tool parameters
     * @return the JSON string
     */
    private static String jsonToolParametersConverter(final List<Map<String, Object>> list) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\"{ ");
        for (int i = 0; i < list.size(); i++) {
            final Map<String, Object> map = list.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\\\"");
            sb.append(StringUtils.escapeStringForJson(map.get("parameterName").toString()));
            sb.append("\\\": ");
            final ToolParameterValue value = (ToolParameterValue)map.get("parameterValue");
            switch (value.type()) {
                case ToolParameterType.STRING -> sb.append("\\\"" + StringUtils.escapeStringForJson(value.getString()) + "\\\"");
                case ToolParameterType.INTEGER -> sb.append(value.getInteger().toString());
                case ToolParameterType.NUMBER -> sb.append(value.getDouble().toString());
                case ToolParameterType.BOOLEAN -> sb.append(value.getBoolean().toString());
            }
        }
        sb.append(" }\"");
        return sb.toString();
    }

    /**
     * Convert a string to a JSON string (including the enclosing quotes)
     *
     * @param text the string to convert
     * @return the JSON string
     */
    private static String jsonConverter(final String input) {
        if (Objects.isNull(input)) { //TODO why to we need this?
            return null;
        }
        return "\"" + StringUtils.escapeStringForJson(input) + "\"";
    }

    /*
     * Converts a list of MessageRound objects into a list of maps suitable for template evaluation.
     */
    private static List<Map<String, Object>> convertMessageRounds(final List<MessageRound> rounds) {
        final List<Map<String, Object>> tools = new ArrayList<>();

        for (final MessageRound round: rounds) {
            final Map<String, Object> tool = new HashMap<>();
            tool.put("role", round.role());
            tool.put("content", round.content());
            tool.put("toolCalls", convertMessageRoundToolCalls(round.toolCalls()));
            tool.put("toolName", round.tool());
            tool.put("toolCallId", round.toolCallId());
            tools.add(tool);
        }
        return tools;
    }

    /*
     * Converts a list of ToolCall objects to a list of maps for template data.
     */
    private static List<Map<String, Object>> convertMessageRoundToolCalls(final List<MessageRound.ToolCall> toolCalls) {
        final List<Map<String, Object>> tools = new ArrayList<>();

        for (final MessageRound.ToolCall toolCall: toolCalls) {
            final Map<String, Object> tool = new HashMap<>();
            tool.put("toolName", toolCall.toolName());
            tool.put("toolCallId", toolCall.toolCallId());
            tool.put("toolParameters", convertMessageRoundToolParameters(toolCall.toolParameters()));
            tools.add(tool);
        }
        return tools;
    }

    /*
     * Converts a list of ToolParameter objects to a list of maps for template data.
     */
    private static List<Map<String, Object>> convertMessageRoundToolParameters(final List<MessageRound.ToolParameter> parameters) {
        final List<Map<String, Object>> tools = new ArrayList<>();

        for (final MessageRound.ToolParameter parameter: parameters) {
            final Map<String, Object> tool = new HashMap<>();
            tool.put("parameterName", parameter.parameterName());
            tool.put("parameterValue", parameter.parameterValue());
            tools.add(tool);
        }
        return tools;
    }

    /**
     * Convert ToolSpecifications to a structure that matches the Handlebars template expectations.
     * The template expects tools with: name, description, parameters, and requiredParameters.
     *
     * @param toolSpecifications the list of tool specifications from langchain4j
     * @return a list of maps representing tools in the expected format
     */
    private static List<Map<String, Object>> convertToolSpecifications(final List<ToolSpecification> ktools) {
        final List<Map<String, Object>> tools = new ArrayList<>();

        for (final ToolSpecification spec: ktools) {
            final Map<String, Object> tool = new HashMap<>();
            tool.put("name", spec.name());
            tool.put("description", spec.description());

            // Extract parameters from the JSON schema
            final List<Map<String, Object>> parameters = new ArrayList<>();
            final List<Map<String, Object>> requiredParameters = new ArrayList<>();

            final JsonObjectSchema schema = spec.parameters();

            final Map<String, JsonSchemaElement> properties = schema.properties();

            for (final Map.Entry<String, JsonSchemaElement> entry: properties.entrySet()) {
                final String paramName = entry.getKey();
                final JsonSchemaElement element = entry.getValue();
                final String description = element.description();
                final String type = getTypeFromElement(element);
                final List<String> requiredParams = schema.required();
                final boolean isRequired = requiredParams != null && requiredParams.contains(paramName);

                final Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("name", paramName);
                parameterMap.put("description", description);
                parameterMap.put("type", type);
                parameters.add(parameterMap);

                if (isRequired) {
                    requiredParameters.add(parameterMap);
                }
            }

            tool.put("parameters", parameters);
            tool.put("requiredParameters", requiredParameters);
            tools.add(tool);
        }

        return tools;
    }

    private static String getTypeFromElement(JsonSchemaElement element) {
        return switch (element) {
            case JsonStringSchema _ -> "string";
            case JsonIntegerSchema _ -> "integer";
            case JsonNumberSchema _ -> "number";
            case JsonBooleanSchema _ -> "boolean";
            case JsonObjectSchema _ -> throw new IllegalArgumentException("type 'object' is not supported");
            case JsonArraySchema _ -> throw new IllegalArgumentException("type 'array' is not supported");
            case JsonEnumSchema _ -> throw new IllegalArgumentException("type 'enum' is not supported");
            case null -> throw new IllegalArgumentException("element cannot be null");
            default -> throw new IllegalArgumentException("type '" + element.getClass().getSimpleName() + "' is not supported");
        };
    }
}
