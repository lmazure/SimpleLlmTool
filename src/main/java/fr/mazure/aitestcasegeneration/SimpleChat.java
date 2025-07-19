package fr.mazure.aitestcasegeneration;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import fr.mazure.aitestcasegeneration.provider.base.InvalidModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.MissingEnvironmentVariable;
import fr.mazure.aitestcasegeneration.provider.base.MissingModelParameter;
import fr.mazure.aitestcasegeneration.provider.custom.CustomChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.custom.CustomModelParameters;
import fr.mazure.aitestcasegeneration.provider.mistralai.MistralAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.mistralai.MistralAiModelParameters;
import fr.mazure.aitestcasegeneration.provider.mock.MockChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.mock.MockModelParameters;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiModelParameters;

public class SimpleChat {

    interface Assistant {
        String chat(final String userMessage);
    }

    public static void main(final String[] args) throws MissingEnvironmentVariable, IOException, MissingModelParameter, InvalidModelParameter {

        final CommandLine.Parameters cli = CommandLine.parseCommandLine(args);

        PrintStream output = System.out;
        if (cli.outputFile().isPresent()) {
            try {
                output = new PrintStream(Files.newOutputStream(cli.outputFile().get(), StandardOpenOption.CREATE_NEW));
            } catch (final IOException e) {
                System.err.println("Error: Unable to write output file: " + cli.outputFile().get().toString());
                System.exit(ExitCode.FILE_ERROR.getCode());
            }
        }

        PrintStream error = System.err;
        if (cli.errorFile().isPresent()) {
            try {
                error = new PrintStream(Files.newOutputStream(cli.errorFile().get(), StandardOpenOption.CREATE_NEW));
            } catch (final IOException e) {
                System.err.println("Error: Unable to write error file: " + cli.errorFile().get().toString());
                System.exit(ExitCode.FILE_ERROR.getCode());
            }
        }

        final ChatModel model = switch (cli.provider()) {
            case ProviderEnum.OPENAI     -> OpenAiChatModelProvider.createChatModel(OpenAiModelParameters.loadFromFile(cli.modelFile()));
            case ProviderEnum.MISTRAL_AI -> MistralAiChatModelProvider.createChatModel(MistralAiModelParameters.loadFromFile(cli.modelFile()));
            case ProviderEnum.CUSTOM     -> CustomChatModelProvider.createChatModel(CustomModelParameters.loadFromFile(cli.modelFile()));
            case ProviderEnum.MOCK       -> MockChatModelProvider.createChatModel(new MockModelParameters());
        };

        final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(2);

        if (cli.sysPrompt().isPresent()) {
            final SystemMessage systemPrompt = new SystemMessage(cli.sysPrompt().get());
            memory.add(systemPrompt);
        }

        if (cli.chatMode()) {
            assert output == System.out;
            assert error == System.err;
            handleChatMode(model, cli.userPrompt(), cli.sysPrompt());
            return;
        } else {
            assert cli.userPrompt().isPresent();
            handleBatchMode(output, error, model, cli.userPrompt().get(), cli.sysPrompt());
        }
    }

    private static void handleChatMode(final ChatModel model,
                                       final Optional<String> userPrompt,
                                       final Optional<String> sysPrompt) throws IOException {
        System.out.println("Type '/exit' to exit");

        final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

        if (sysPrompt.isPresent()) {
            final SystemMessage systemPrompt = new SystemMessage(sysPrompt.get());
            memory.add(systemPrompt);
            System.out.println("System prompt: " + sysPrompt.get());
        }
        final Assistant assistant = AiServices.builder(Assistant.class)
                                              .chatModel(model)
                                              .chatMemory(memory)
                                              .build();
        final Terminal terminal = TerminalBuilder.builder()
                                                 .system(true)
                                                 .build();

        final LineReader reader = LineReaderBuilder.builder()
                                                   .terminal(terminal)
                                                   .build();

        String prefilledText = userPrompt.orElse("");

        while (true) {
            final String input = reader.readLine("Enter text: ", null, prefilledText);
            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("/exit")) {
                terminal.close();
                System.exit(ExitCode.SUCCESS.getCode());
            }
            final String answer = assistant.chat(input);
            System.out.println(answer);
            prefilledText = "";
        }
    }

    private static void handleBatchMode(final PrintStream output,
                                        final PrintStream error,
                                        final ChatModel model,
                                        final String userPrompt,
                                        final Optional<String> sysPrompt) {
        final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(2);

        if (sysPrompt.isPresent()) {
            final SystemMessage systemPrompt = new SystemMessage(sysPrompt.get());
            memory.add(systemPrompt);
        }

        final Assistant assistant = AiServices.builder(Assistant.class)
                                              .chatMemory(memory)
                                              .chatModel(model)
                                              .build();

        try {
            final String answer = assistant.chat(userPrompt);
            output.println(answer);
        } catch (final RuntimeException e) {
            error.println("Model failure");
            e.printStackTrace(error);
            System.exit(ExitCode.MODEL_ERROR.getCode());
        }

        System.exit(ExitCode.SUCCESS.getCode());
    }
}
