package fr.mazure.aitestcasegeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;

public class ToolManager {
    
    public record ToolParameter(String name, String description) {}
    public record Tool(String name, String description, List<ToolParameter> parameters) {}

    final static List<Tool> toolList = initToolList();

    public static List<Tool> getToolList() {
        return toolList;
    }

    public static List<ToolSpecification> getSpecifications() {
        final List<ToolSpecification> specifications = new ArrayList<>();
        for (final Tool tool: toolList) {
            specifications.add(getSpecification(tool));
        }
        return specifications;
    }

    private static ToolSpecification getSpecification(final Tool tool) {
        final ToolSpecification.Builder builder = ToolSpecification.builder()
                                                                   .name(tool.name())
                                                                   .description(tool.description());
        for (final ToolParameter parameter: tool.parameters()) {
            builder.parameters(JsonObjectSchema.builder()
                                               .addStringProperty(parameter.name(), parameter.description)
                                               .required(parameter.name())
                                               .build());
        }
        return builder.build();
    }

    public static List<ToolExecutionResultMessage> handleToolExecutionRequests(final List<ToolExecutionRequest> requests) {
        final List<ToolExecutionResultMessage> resultMessages = new ArrayList<>();
        for (final ToolExecutionRequest request: requests) {
            resultMessages.add(handleToolExecutionRequest(request));
        }
        return resultMessages;
    }

    private static ToolExecutionResultMessage handleToolExecutionRequest(final ToolExecutionRequest request) {

        final String output = executeTool(request.name(), extractValues(request.arguments()));
        return new ToolExecutionResultMessage(request.id(), request.name(), output);
    }

    private static List<Tool> initToolList() {
        final List<Tool> toolList = new ArrayList<>();

        final File toolsDir = new File("tools");
        if (!toolsDir.exists() || !toolsDir.isDirectory()) {
            throw new RuntimeException("No tools directory found.");
        }

        final File[] pythonFiles = toolsDir.listFiles((_, name) -> name.endsWith(".py"));
        if (pythonFiles == null || pythonFiles.length == 0) {
            return toolList;
        }

        for (final File pythonFile : pythonFiles) {
            final Tool tool = getToolDescription(pythonFile.getName().replace(".py", ""));
            toolList.add(tool);
        }

        return toolList;
    }

    private static Tool getToolDescription(final String toolName) {
        final String output = executeTool(toolName, "--description");

        final StringReader stringReader = new StringReader(output);
        final BufferedReader bufferedReader = new BufferedReader(stringReader);

        try {
            final String description = bufferedReader.readLine();
            if (description == null) {
                throw new RuntimeException("failed to get description of " + toolName);
            }
            final List<ToolParameter> parameters = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] parts = line.split("\t");
                if (parts.length == 2) {
                    parameters.add(new ToolParameter(parts[0].trim(), parts[1].trim()));
                } else {
                    throw new RuntimeException("incorrect parameter description for " + toolName);
                }
            }
            return new Tool(toolName, description, parameters);
        } catch (final IOException e) {
            throw new RuntimeException("failed to get description of " + toolName, e);
        }
    }

    private static String executeTool(final String toolName,
                                      final String arguments) {
        final File toolsDir = new File("tools");
        final File pythonFile = new File(toolsDir, toolName + ".py");
        if (!pythonFile.exists() || !pythonFile.isFile()) {
            throw new RuntimeException("Tool " + toolName + " not found.");
        }

        final StringBuilder output = new StringBuilder();
        try {
            final List<String> args = new ArrayList<>();
            args.add("python");
            args.add(pythonFile.getAbsolutePath());
            if (!arguments.isEmpty()) {
                args.add(arguments);
            }
            final ProcessBuilder pb = new ProcessBuilder(args);
            System.out.println("python " + pythonFile.getAbsolutePath() + " " + arguments);
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

    public static String extractValues(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        final StringBuilder result = new StringBuilder();

        for (final String key : jsonObject.keySet()) { //TODO the order of the parameters is not guaranteed
            final String value = jsonObject.getString(key);
            final String escapedValue = value.replace("\"", "\\\"");
            result.append("\"").append(escapedValue).append("\" ");
            if (result.length() > 0) {
                result.append(" ");
            }
        }

        // Remove the trailing space and return
        return result.toString();
    }
}
