package fr.mazure.simplellmtool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The CommandLine class parses command-line arguments to configure a chat application.
 */
public class CommandLine {

    /**
     * Parameters for the chat application.
     *
     * @param sysPrompt the optional system prompt to send to the model
     * @param userPrompt the optional user prompt to send to the model
     * @param outputFile the optional file to write output to
     * @param errorFile the optional file to write errors to
     * @param logFile the optional file to write logs to
     * @param logLevel the optional log level
     * @param toolsDir the optional directory containing tools
     * @param provider the provider to use
     * @param modelFile the file containing the model
     * @param overridingModelName the name to use instead of the name in the model file
     * @param chatMode whether to run in chat mode
     */
    public record Parameters(Optional<String> sysPrompt,
                             Optional<String> userPrompt,
                             Optional<Path> outputFile,
                             Optional<Path> errorFile,
                             Optional<Path> logFile,
                             Optional<LogLevel> logLevel,
                             Optional<Path> toolsDir,
                             ProviderEnum provider,
                             Path modelFile,
                             Optional<String> overridingModelName,
                             boolean chatMode) {}

    /**
     * Parses command-line arguments to configure a chat application.
     *
     * @param args The command-line arguments to parse.
     * @return A Parameters object containing the parsed arguments.
     */
    public static Parameters parseCommandLine(final String[] args) {
        String sysPrompt = null;
        String userPrompt = null;
        Path outputFile = null;
        Path errorFile = null;
        Path logFile = null;
        LogLevel logLevel = null;
        Path toolsDir = null;
        ProviderEnum provider = null;
        Path modelFile = null;
        String overridingModelName = null;
        boolean chatMode = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--system-prompt-string")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --system-prompt-string");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                if (Objects.nonNull(sysPrompt)) {
                    System.err.println("System prompt already set");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                sysPrompt = args[i + 1];
                i++;
                continue;
            }
            if (args[i].equals("--user-prompt-string")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --user-prompt-string");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                if (Objects.nonNull(userPrompt)) {
                    System.err.println("User prompt already set");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                userPrompt = args[i + 1];
                i++;
                continue;
            }
            if (args[i].equals("--system-prompt-file")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --system-prompt-file");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                if (Objects.nonNull(sysPrompt)) {
                    System.err.println("System prompt already set");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                sysPrompt = slurpFile(Paths.get(args[i + 1]));
                i++;
                continue;
            }
            if (args[i].equals("--user-prompt-file")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --user-prompt-file");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                if (Objects.nonNull(userPrompt)) {
                    System.err.println("User prompt already set");
                    displayHelpAndExit(i);
                }
                userPrompt = slurpFile(Paths.get( args[i + 1]));
                i++;
                continue;
            }
            if (args[i].equals("--output-file")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --output-file");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                outputFile = Paths.get(args[i + 1]);
                i++;
                continue;
            }
            if (args[i].equals("--error-file")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --error-file");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                errorFile = Paths.get(args[i + 1]);
                i++;
                continue;
            }
            if (args[i].equals("--log-file")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --log-file");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                logFile = Paths.get(args[i + 1]);
                i++;
                continue;
            }
            if (args[i].equals("--log-level")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --log-level");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                try {
                    logLevel = LogLevel.fromString(args[i + 1]);
                }
                catch (final IllegalArgumentException e) {
                    System.err.println("Invalid log level: " + args[i + 1]);
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                i++;
                continue;
            }
            if (args[i].equals("--tools-dir")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --tools-dir");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                toolsDir = Paths.get(args[i + 1]);
                i++;
                continue;
            }
            if (args[i].equals("--provider")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --provider");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                try {
                    provider = ProviderEnum.fromString(args[i + 1]);
                } catch (final IllegalArgumentException e) {
                    System.err.println("Unknown provider: " + args[i + 1]);
                    displayHelpAndExit(ExitCode.INVALID_PROVIDER.getCode());
                }
                i++;
                continue;
            }
            if (args[i].equals("--model-file")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --model-file");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                modelFile= Path.of(args[i + 1]);
                i++;
                continue;
            }
            if (args[i].equals("--model-name")) {
                if ((i + 1 ) >= args.length) {
                    System.err.println("Missing argument for --model-name");
                    System.exit(ExitCode.INVALID_COMMAND_LINE.getCode());
                }
                overridingModelName = args[i + 1];
                i++;
                continue;
            }
            if (args[i].equals("--chat-mode")) {
                chatMode = true;
                continue;
            }
            if (args[i].equals("--help")) {
                System.exit(ExitCode.SUCCESS.getCode());
            }
            System.err.println("Unknown argument: " + args[i]);
            displayHelpAndExit(ExitCode.INVALID_COMMAND_LINE.getCode());
        }
        if (Objects.isNull(provider)) {
            System.err.println("Missing provider");
            displayHelpAndExit(ExitCode.INVALID_COMMAND_LINE.getCode());
        }
        if (Objects.isNull(modelFile)) {
            System.err.println("Missing model file");
            displayHelpAndExit(ExitCode.INVALID_COMMAND_LINE.getCode());
        }
        if (chatMode) {
            if (Objects.nonNull(outputFile)) {
                System.err.println("Output file is not allowed in chat mode");
                displayHelpAndExit(ExitCode.INVALID_COMMAND_LINE.getCode());
            }
            if (Objects.nonNull(errorFile)) {
                System.err.println("Error file is not allowed in chat mode");
                displayHelpAndExit(ExitCode.INVALID_COMMAND_LINE.getCode());
            }
        } else {
            if (Objects.isNull(userPrompt)) {
                System.err.println("User prompt is required (except in chat mode where it is optional)");
                displayHelpAndExit(ExitCode.INVALID_COMMAND_LINE.getCode());
            }
        }
        return new Parameters(Optional.ofNullable(sysPrompt),
                              Optional.ofNullable(userPrompt),
                              Optional.ofNullable(outputFile),
                              Optional.ofNullable(errorFile),
                              Optional.ofNullable(logFile),
                              Optional.ofNullable(logLevel),
                              Optional.ofNullable(toolsDir),
                              provider,
                              modelFile,
                              Optional.ofNullable(overridingModelName),
                              chatMode);
    }

    private static void displayHelpAndExit(final int exitCode) {
        final String executableName = "textimprover.jar";
        System.err.println("Usage: java -jar " +
                           executableName +
                           " {--user-prompt-string <user-prompt-string>|--user-prompt-file <user-prompt-file>}\n" +
                           "    [--system-prompt-string <system-prompt-string>]  [--system-prompt-file <system-prompt-file>]\n" +
                           "    [--provider <provider>] [--model-file <model-file>] [--model-name <model-name>]\n" +
                           "    [--output-file <output-file>] [--error-file <error-file>] [--log-file <log-file>]\n" +
                           "    [--log-level <log-level>] [--chat-mode] [--help]");
        System.err.println(
            """
            --system-prompt-string <system-prompt-string> system prompt as a string
            --system-prompt-file <system-prompt-file>     system prompt as the content of a file
            --user-prompt-string <user-prompt-string>     user prompt as a string
            --user-prompt-file <user-prompt-file>         user prompt as the content of a file
            --model-name <model-name>                     model name
            --output-file <output-file>                   output file (stdout by default)
            --error-file <error-file>                     error file (stderr by default)
            --log-file <log-file>                         log file (stderr by default)
            --log-level <log-level>                       log level (info by default, can be trace, debug, info, warn, or error)
            --tools-dir <tools-dir>                       directory containing tools
            --provider <provider>                         provider
            --model-file <model-file>                     file defining the model and its parameters
            --chat-mode                                   trigger chat mode
            --help                                        display help and exit
            """
        );
        System.err.println("Available providers: " + Arrays.stream(ProviderEnum.values()).map(Enum::toString).collect(Collectors.joining(", ")));
        System.exit(exitCode);
    }

    private static String slurpFile(final Path path)  {
        try {
            return Files.readString(path);
        }
        catch (final IOException e) {
            System.err.println("Error: Unable to read file: " + path);
            System.exit(ExitCode.FILE_ERROR.getCode());
            return null;
        }
    }
}
