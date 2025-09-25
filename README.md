# SimpleLlmTool
SimpleLlmTool has two objectives:
- experimenting with [LangChain4j](https://docs.langchain4j.dev/) and understand what that framework can do
- having a handy tool to exploit LLMs from the command line, both in batch mode and interactively (i.e. in chat mode)

## How to build the project
1) Store your API keys in, for example, an `.env` file.  
   You should provide the following environment variables:
   - `OPENAI_API_KEY`
   - `MISTRALAI_API_KEY`
   - `ANTHROPIC_API_KEY`
   - `GOOGLE_GEMINI_API_KEY`

2) Build the project using
   ```bash
   export $(cat .env)
   mvn clean package
   ```
   If you want to save money or if you do not have the necessary API keys, you can skip the end-to-end tests accessing the LLM by running
   ```bash
   mvn clean package -Dgroups='!e2e'
   ```

## How to run SimpleLlmTool

SimpleLlmTool accepts the following parameters on the command line:

| parameter                                       | description                                                                          |
| ----------------------------------------------- | ------------------------------------------------------------------------------------ |
| `--system-prompt-string <system-prompt-string>` | system prompt as a string                                                            |
| `--system-prompt-file <system-prompt-file>`     | system prompt as the content of a file                                               |
| `--user-prompt-string <user-prompt-string>`     | user prompt as a string                                                              |
| `--user-prompt-file <user-prompt-file>`         | user prompt as the content of a file                                                 |
| `--output-file <output-file>`                   | output file (stdout by default)<br>If `<output-file>` already exists, the text is appended to it.|
| `--error-file <error-file>`                     | error file (stderr by default)<br>If `<error-file>` already exists, the text is appended to it.  |
| `--log-file <log-file>`                         | log file (stderr by default)<br>If `<log-file>` already exists, the text is appended to it.      |
| `--log-level <log-level>`                       | log level (info by default)<br>If `--log-level <log-level>` is not provided, the default log level is `warn`.<br>`<log-level>` can be `trace`, `debug`, `info`, `warn`, or `error`. |
| `--tools-dir <tools-dir>`                       | directory containing the tools (see [below](#tools))<br>If `--tools-dir <tools-dir>` is not provided, no tools are available. |
| `--provider <provider>`                         | provider<br>`<provider>` can be `OpenAI`, `Mistral AI`, `Anthropic`, `Google Gemini`, `custom`, or `mock` |
| `--model-file <model_file>`                     | file defining the model and its parameters (see [below](#how-to-write-a-model-file)) |
| `--model-name <model-name>`                     | overriding model name<br>If `--model-name <model-name>` is provided, it overrides the model name in the model file. |
| `--chat-mode`                                   | chat mode                                                                            |
| `--help`                                        | display help and exit                                                                |

## How to write a model file

A model and its parameters are defined by a YAML file, see for example [gpt-4.1-nano@openai.yaml](examples/gpt-4.1-nano@openai.yaml).  
It is indicated on the command line with the `--model-file <model_file>` parameter.

Its content depends on the provider.  
The provider is indicated on the command line with the `--provider <provider>` parameter.

### OpenAI
| parameter             | description                                              | type   | compulsory   |
| --------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`           | name of the model                                        | string | yes          |
| `baseUrl`             | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`        | name of the environment variable containing the API key  | string | yes          |
| `organizationId`      | ID of the organization containing the model              | string | no           |
| `projectId`           | ID of the project containing the model                   | string | no           |
| `temperature`         | temperature of the model                                 | float  | no           |
| `seed`                | random seed of the model                                 | int    | no           |
| `topP`                | top P value of the model                                 | float  | no           |
| `maxCompletionTokens` | maximum number of tokens the model should generate       | int    | no           |

### Mistral AI
| parameter           | description                                              | type   | compulsory   |
| ------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`         | name of the model                                        | string | yes          |
| `baseUrl`           | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`      | name of the environment variable containing the API key  | string | yes          |
| `temperature`       | temperature of the model                                 | float  | no           |
| `seed`              | random seed of the model                                 | int    | no           |
| `topP`              | top P value of the model                                 | float  | no           |
| `maxTokens`         | maximum number of tokens the model should generate       | int    | no           |

### Anthropic
| parameter           | description                                              | type   | compulsory   |
| ------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`         | name of the model                                        | string | yes          |
| `baseUrl`           | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`      | name of the environment variable containing the API key  | string | yes          |
| `temperature`       | temperature of the model                                 | float  | no           |
| `topP`              | top P value of the model                                 | float  | no           |
| `topK`              | top K value of the model                                 | int    | no           |
| `maxTokens`         | maximum number of tokens the model should generate       | int    | no           |

### Google Gemini
| parameter           | description                                              | type   | compulsory   |
| ------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`         | name of the model                                        | string | yes          |
| `baseUrl`           | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`      | name of the environment variable containing the API key  | string | yes          |
| `temperature`       | temperature of the model                                 | float  | no           |
| `topP`              | top P value of the model                                 | float  | no           |
| `topK`              | top K value of the model                                 | int    | no           |
| `maxTokens`         | maximum number of tokens the model should generate       | int    | no           |

### custom
| parameter               | description                                                   | type   | compulsory   |
| ----------------------- | ------------------------------------------------------------- | ------ | ------------ |
| `modelName`             | name of the model                                             | string | yes          |
| `url`                   | URL of the provider                                           | string | yes          |
| `apiKeyEnvVar`          | name of the environment variable containing the API key       | string | yes          |
| `payloadTemplate`       | payload template for the API calls                            | string | yes          |
| `httpHeaders`           | HTTP headers to send with the API calls†                      | list   | yes          |
| - header name           | name of the HTTP header                                       | string | yes          |
| - header value template | template for the value of the HTTP header                     | string | yes          |
| `answerPath`            | JSON path to the field containing the answer                  | string | yes          |
| `inputTokenPath`        | JSON path to the field containing the number of input tokens  | string | yes          |
| `outputTokenPath`       | JSON path to the field containing the number of output tokens | string | yes          |

† The HTTP header `Content-Type: application/json` is added automatically.

`payloadTemplate` and header value templates are evaluated using [Handlebars](https://jknack.github.io/handlebars.java/gettingStarted.html).

The following variables are available:
- `messages`: the list of messages  
    each message has a `role` and a `content`
- `modelName`: the name of the model
- `apiKey`: the API key

The following helpers are available:
- `isSystem` (boolean): tests if this is a system message (a.k.a system prompt)
- `isUser` (boolean): tests if this is a user message (a.k.a user prompt)
- `isModel` (boolean): tests if this is a model message
- `convertToJsonString` (string): converts a string to a JSON string (including the double quotes) by escaping the special characters

#### Example
- this YAML extract:
    ```yaml
    payloadTemplate: |
        {
            "model": "{{modelName}}",
            "messages": [
                {{#each messages}}{
                    "role": "{{#if (isSystem role)}}system{{/if}}{{#if (isUser role)}}user{{/if}}{{#if (isModel role)}}assistant{{/if}}",
                    "content": {{convertToJsonString content}}
                }{{#unless @last}},
                {{/unless}}{{/each}}
            ],
            "temperature": 0.7,
            "seed": 42
        }
    httpHeaders:
      Authorization: Bearer {{apiKey}}
    ```

- these messages:

    | role   | content                             |
    | ------ | ----------------------------------- |
    | system | you are a helpful assistant         |
    | user   | what is the weather?                |
    | model  | i don't have access to weather data |
    | user   | what day is it?                     |
    | model  | april fools' day                    |
    | user   | so, tell me a joke!                 |

- the model name: `gpt-4.1`.
- the API key: `sec_DEADBEEF`

will generate this JSON:
```json
{
  "model": "gpt-4.1",
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful assistant"
    },
    {
      "role": "user",
      "content": "What is the weather?"
    },
    {
      "role": "assistant",
      "content": "I don't have access to weather data"
    },
    {
      "role": "user",
      "content": "What day is it?"
    },
    {
      "role": "assistant",
      "content": "April fools' day"
    },
    {
      "role": "user",
      "content": "So, tell me a joke!"
    }
  ],
  "temperature": 0.7,
  "seed": 42
}
```
and this HTTP header:
```http
Authorization: Bearer sec_DEADBEEF
```

### mock

A mock provider used for testing.

## Examples of usage

### Use Claude Sonnet 4 in interactive mode
```bash
export $(cat .env)
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --provider Anthropic --model-file examples/claude-4-sonnet@anthropic.yaml --chat-mode
```

### Total mess
use OpenAI provider
```bash
export $(cat .env)
source tools/.venv/Scripts/Activate
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider OpenAI --model-file examples/gpt-4.1-nano@openai.yaml
```

use Mistral AI provider
```bash
export $(cat .env)
source tools/.venv/Scripts/Activate
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Why is the sky blue?" --system-prompt-string "You are a scientific." --provider "Mistral AI" --model-file examples/mistral-large-latest@mistralai.yaml
```

use Anthropic provider
```bash
export $(cat .env)
source tools/.venv/Scripts/Activate
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider Anthropic --model-file examples/claude-4-sonnet@anthropic.yaml
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "What is the weather in Paris?" --system-prompt-string "You always provide an English anwer, followed by a precise translation in French" --provider Anthropic --model-file examples/claude-4-sonnet@anthropic.yaml --tools-dir tools
```

use Google Gemini provider
```bash
export $(cat .env)
source tools/.venv/Scripts/Activate
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider "Google Gemini" --model-file examples/gemini-2.5-flash@google.yaml
```

use custom provider
```bash
export $(cat .env)
source tools/.venv/Scripts/Activate
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider custom --model-file examples/gpt-4.1-nano@custom.yaml
```

use mock provider
```bash
export $(cat .env)
source tools/.venv/Scripts/Activate
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider mock --model-file non-existing-file.yaml
```

# Tools

If `--tools-dir <tools-dir>` is provided, it should be a directory containing Python scripts.  
Each script is a tool.

Each script should, when called with the `--description` parameter, return the description of the tool formatted as:
- first line: the description of the tool
- following lines: one line per parameter, each line formatted as `parameter_name<tab>parameter_description` where
    - `parameter_name` is the name of the parameter,
    - `<tab>` is a tab character, and
    - `parameter_description` is the description of the parameter.

Each script should
- if successfully executed, output the result of the execution as a raw string and have an exit code of 0
- if in error, output an error message and have an exit code not equal to 0

See examples in the [`tools`](tools) directory.

## Experiments

Some experiments with SimpleLlmTool…

### Document review

Reviewing a document (e.g. a Markdown file) stored in a GitLab repository and proposing corrections in a GitLab merge request.  
See the [README](experimentations/document_review/README.md) for more details.

### Extract names to XML

Extracting names from a text and formatting them as an XML extract (something that I could exploit later on for [my homepage](https://mazure.fr))  
See the [README](experimentations/extract_names_to_xml_fr/README.md) for more details.  
