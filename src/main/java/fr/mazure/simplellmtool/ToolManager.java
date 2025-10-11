package fr.mazure.simplellmtool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;

public class ToolManager {

    public enum ToolParameterType {
        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN
    }
    public record ToolParameter(String name, String description, ToolParameterType type, boolean required) {}
    public record Tool(String name, String description, List<ToolParameter> parameters) {}

    private final Path toolsDir;
    private final List<Tool> toolList;

    public ToolManager(final Path toolsDir) {
        this.toolsDir = toolsDir;
        this.toolList = initToolList(toolsDir);
    }

    public List<Tool> getToolList() {
        return this.toolList;
    }

    public List<ToolSpecification> getSpecifications() {
        final List<ToolSpecification> specifications = new ArrayList<>();
        for (final Tool tool: this.toolList) {
            specifications.add(getSpecification(tool));
        }
        return specifications;
    }

    public static ToolSpecification getSpecification(final Tool tool) {
        final ToolSpecification.Builder builder = ToolSpecification.builder()
                                                                   .name(tool.name())
                                                                   .description(tool.description());

        final JsonObjectSchema.Builder jsonBuilder = JsonObjectSchema.builder();
        for (final ToolParameter parameter: tool.parameters()) {
            switch (parameter.type()) {
                case STRING -> jsonBuilder.addStringProperty(parameter.name(), parameter.description);
                case INTEGER -> jsonBuilder.addIntegerProperty(parameter.name(), parameter.description);
                case NUMBER -> jsonBuilder.addNumberProperty(parameter.name(), parameter.description);
                case BOOLEAN -> jsonBuilder.addBooleanProperty(parameter.name(), parameter.description);
                default -> throw new IllegalArgumentException("type " + parameter.type() + " is not supported");
            }
        }
        jsonBuilder.required(tool.parameters().stream().filter(ToolParameter::required).map(ToolParameter::name).toList());

        builder.parameters(jsonBuilder.build());
        return builder.build();
    }

    public List<ToolExecutionResultMessage> handleToolExecutionRequests(final List<ToolExecutionRequest> requests) {
        final List<ToolExecutionResultMessage> resultMessages = new ArrayList<>();
        for (final ToolExecutionRequest request: requests) {
            resultMessages.add(handleToolExecutionRequest(request));
        }
        return resultMessages;
    }

    private ToolExecutionResultMessage handleToolExecutionRequest(final ToolExecutionRequest request) {

        final String toolName = request.name();
        final Tool tool = this.toolList.stream()
                                       .filter(t -> t.name().equals(toolName))
                                       .findFirst()
                                       .orElse(null);
        if (Objects.isNull(tool)) {
            throw new RuntimeException("The model called a tool " + toolName + " that does not exist.");
        }
        final String output = executeTool(toolName, extractValues(tool.parameters(), request.arguments()));
        return new ToolExecutionResultMessage(request.id(), toolName, output);
    }

    private List<Tool> initToolList(final Path toolsDir) {
        final List<Tool> toolList = new ArrayList<>();

        final File dir = toolsDir.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("Tools directory not found: " + toolsDir);
        }

        final File[] pythonFiles = dir.listFiles((_, name) -> name.endsWith(".py"));
        if (Objects.isNull(pythonFiles) || pythonFiles.length == 0) {
            return toolList;
        }

        for (final File pythonFile: pythonFiles) {
            final Tool tool = getToolDescription(pythonFile.getName().replace(".py", ""));
            toolList.add(tool);
        }

        return toolList;
    }

    private Tool getToolDescription(final String toolName) {
        final String output = executeTool(toolName, List.of("--description"));

        final StringReader stringReader = new StringReader(output);
        final BufferedReader bufferedReader = new BufferedReader(stringReader);

        try {
            final String description = bufferedReader.readLine();
            if (Objects.isNull(description)) {
                throw new RuntimeException("failed to get description of " + toolName);
            }
            final List<ToolParameter> parameters = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] parts = line.split("\t");
                if (parts.length == 2) {
                    parameters.add(new ToolParameter(parts[0].trim(), parts[1].trim(), ToolParameterType.STRING, true));
                } else {
                    throw new RuntimeException("incorrect parameter description for " + toolName);
                }
            }
            return new Tool(toolName, description, parameters);
        } catch (final IOException e) {
            throw new RuntimeException("failed to get description of " + toolName, e);
        }
    }

    private String executeTool(final String toolName,
                               final List<String> arguments) {
        final File toolsDir = this.toolsDir.toFile();
        final File pythonFile = new File(toolsDir, toolName + ".py");
        if (!pythonFile.exists() || !pythonFile.isFile()) {
            throw new RuntimeException("Tool " + toolName + " not found.");
        }

        final StringBuilder output = new StringBuilder();
        try {
            final List<String> args = new ArrayList<>();
            args.add("python");
            args.add(pythonFile.getAbsolutePath());
            args.addAll(arguments);
            final ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            final Process process = pb.start();

            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Tool " + toolName + " failed with exit code " + exitCode);
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("failed to execute " + pythonFile, e);
        }

        return output.toString();
    }

    public static List<String> extractValues(final List<ToolParameter> parameters,
                                             final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        final List<String> result = new ArrayList<>();

        try {
            for (final String key: parameters.stream().map(ToolParameter::name).toList()) {
                result.add(jsonObject.getString(key));
            }
        } catch (final JSONException e) {
            throw new RuntimeException("the model did not return a value for the tool parameters " + parameters + " in " + jsonString, e);
        }

        return result;
    }
}
