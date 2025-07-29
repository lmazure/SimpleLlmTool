package fr.mazure.aitestcasegeneration;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
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

        try {
            if (cli.chatMode()) {
                assert output == System.out;
                assert error == System.err;
                ChatMode.handleChat(model, cli.sysPrompt(), cli.userPrompt());
            } else {
                assert cli.userPrompt().isPresent();
                BatchMode.handleBatch(output, model, cli.sysPrompt(), cli.userPrompt().get());
            }
        } catch (final RuntimeException e) {
            error.println("Model failure");
            e.printStackTrace(error);
            System.exit(ExitCode.MODEL_ERROR.getCode());
        }

        System.exit(ExitCode.SUCCESS.getCode());
    }
}
