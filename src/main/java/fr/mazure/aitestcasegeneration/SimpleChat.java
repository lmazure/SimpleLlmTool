package fr.mazure.aitestcasegeneration;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import dev.langchain4j.model.chat.ChatModel;

import fr.mazure.aitestcasegeneration.provider.anthropic.AnthropicChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.anthropic.AnthropicModelParameters;
import fr.mazure.aitestcasegeneration.provider.base.InvalidModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.MissingEnvironmentVariable;
import fr.mazure.aitestcasegeneration.provider.base.MissingModelParameter;
import fr.mazure.aitestcasegeneration.provider.custom.CustomChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.custom.CustomModelParameters;
import fr.mazure.aitestcasegeneration.provider.googlegemini.GoogleGeminiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.googlegemini.GoogleGeminiModelParameters;
import fr.mazure.aitestcasegeneration.provider.mistralai.MistralAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.mistralai.MistralAiModelParameters;
import fr.mazure.aitestcasegeneration.provider.mock.MockChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.mock.MockModelParameters;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiModelParameters;

/**
 * Simple chat application.
 */
public class SimpleChat {

    public static void main(final String[] args) throws MissingEnvironmentVariable, IOException, MissingModelParameter, InvalidModelParameter {

        final CommandLine.Parameters cli = CommandLine.parseCommandLine(args);

        final PrintStream output = cli.outputFile().isPresent() ? createStream(cli.outputFile().get(), "output")
                                                                :System.out;
        final PrintStream error = cli.errorFile().isPresent() ? createStream(cli.errorFile().get(), "error")
                                                              :System.err;
        final PrintStream log = cli.logFile().isPresent() ? createStream(cli.logFile().get(), "log")
                                                          :System.err;

        final ChatModel model = switch (cli.provider()) {
            case ProviderEnum.OPENAI        -> OpenAiChatModelProvider.createChatModel(OpenAiModelParameters.loadFromFile(cli.modelFile(), cli.overridingModelName()));
            case ProviderEnum.MISTRAL_AI    -> MistralAiChatModelProvider.createChatModel(MistralAiModelParameters.loadFromFile(cli.modelFile(), cli.overridingModelName()));
            case ProviderEnum.ANTHROPIC     -> AnthropicChatModelProvider.createChatModel(AnthropicModelParameters.loadFromFile(cli.modelFile(), cli.overridingModelName()));
            case ProviderEnum.GOOGLE_GEMINI -> GoogleGeminiChatModelProvider.createChatModel(GoogleGeminiModelParameters.loadFromFile(cli.modelFile(), cli.overridingModelName()));
            case ProviderEnum.CUSTOM        -> CustomChatModelProvider.createChatModel(CustomModelParameters.loadFromFile(cli.modelFile(), cli.overridingModelName()), log);
            case ProviderEnum.MOCK          -> MockChatModelProvider.createChatModel(new MockModelParameters());
        };

        final Optional<ToolManager> toolManager = cli.toolsDir().map(dir -> new ToolManager(dir));

        try {
            if (cli.chatMode()) {
                assert output == System.out;
                assert error == System.err;
                ChatMode.handleChat(model, cli.sysPrompt(), cli.userPrompt(), log, toolManager);
            } else {
                assert cli.userPrompt().isPresent();
                BatchMode.handleBatch(model, cli.sysPrompt(), cli.userPrompt().get(), output, toolManager);
            }
        } catch (final RuntimeException e) {
            error.println("Model failure (" + e.getMessage() + ")");
            e.printStackTrace(error);
            System.exit(ExitCode.MODEL_ERROR.getCode());
        }

        System.exit(ExitCode.SUCCESS.getCode());
    }

    private static PrintStream createStream(final Path file,
                                            final String description) {
        try {
            return new PrintStream(Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        } catch (final IOException e) {
            System.err.println("Error: Unable to write " + description + " file: " + file.toString() + " (" + e.getMessage() + ")");
            System.exit(ExitCode.FILE_ERROR.getCode());
        }
        return null;
    }
}
