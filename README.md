# Usage
build the project
```bash
mvn clean package
```

use OpenAI provider
```bash
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider OpenAI --model-file examples/gpt-4.1-nano@openai.yaml
```

use Mistral AI provider
```bash
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Why is the sky blue?" --system-prompt-string "You are a scientific." --provider "Mistral AI" --model-file examples/mistral-large-latest@mistralai.yaml
```

use Anthropic provider
```bash
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider Anthropic --model-file examples/claude-3-5-sonnet@anthropic.yaml
```

use Google Gemini provider
```bash
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider "Google Gemini" --model-file examples/gemini-2.5-flash@google.yaml
```

use custom provider
```bash
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider custom --model-file examples/gpt-4.1-nano@custom.yaml
```

use mock provider
```bash
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider mock --model-file non-existing-file.yaml
```

# CLI
| parameter                                       | description                                |
| ----------------------------------------------- | ------------------------------------------ |
| `--system-prompt-string <system-prompt-string>` | system prompt as a string                  |
| `--system-prompt-file <system-prompt-file>`     | system prompt as the content of a file     |
| `--user-prompt-string <user-prompt-string>`     | user prompt as a string                    |
| `--user-prompt-file <user-prompt-file>`         | user prompt as the content of a file       |
| `--output-file <output-file>`                   | output file (stdout by default)            |
| `--error-file <error-file>`                     | error file (stderr by default)             |
| `--log-file <log-file>`                         | log file (stderr by default)               |
| `--provider <provider>`                         | provider                                   |
| `--model-name <model-name>`                     | overriding model name                      |
| `--chat-mode`                                   | chat mode                                  |
| `--model-file <model_file>`                     | file defining the model and its parameters |
| `--help`                                        | display help and exit                      |

If `<output-file>` already exists, the text is appended to it.  
If `<error-file>` already exists, the text is appended to it.  
If `<log-file>` already exists, the text is appended to it.  
If `<model-name>` is provided, it overrides the model name in the model file.

# Parameters per provider

A model and its parameters are defined by a YAML file, see for example [gpt-4.1-nano@openai.yaml](examples/gpt-4.1-nano@openai.yaml).  
It is indicated on the command line with the `--model-file <model_file>` parameter.

Its content depends on the provider.  
The provider is indicated on the command line with the `--provider <provider>` parameter.

## OpenAI
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

## Mistral AI
| parameter           | description                                              | type   | compulsory   |
| ------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`         | name of the model                                        | string | yes          |
| `baseUrl`           | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`      | name of the environment variable containing the API key  | string | yes          |
| `temperature`       | temperature of the model                                 | float  | no           |
| `seed`              | random seed of the model                                 | int    | no           |
| `topP`              | top P value of the model                                 | float  | no           |
| `maxTokens`         | maximum number of tokens the model should generate       | int    | no           |

## Anthropic
| parameter           | description                                              | type   | compulsory   |
| ------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`         | name of the model                                        | string | yes          |
| `baseUrl`           | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`      | name of the environment variable containing the API key  | string | yes          |
| `temperature`       | temperature of the model                                 | float  | no           |
| `topP`              | top P value of the model                                 | float  | no           |
| `topK`              | top K value of the model                                 | int    | no           |
| `maxTokens`         | maximum number of tokens the model should generate       | int    | no           |

## Google Gemini
| parameter           | description                                              | type   | compulsory   |
| ------------------- | -------------------------------------------------------- | ------ | ------------ |
| `modelName`         | name of the model                                        | string | yes          |
| `baseUrl`           | base URL of the provider                                 | string | no           |
| `apiKeyEnvVar`      | name of the environment variable containing the API key  | string | yes          |
| `temperature`       | temperature of the model                                 | float  | no           |
| `topP`              | top P value of the model                                 | float  | no           |
| `topK`              | top K value of the model                                 | int    | no           |
| `maxTokens`         | maximum number of tokens the model should generate       | int    | no           |

## custom
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
| `logRequests`           | whether to log the requests                                   | bool   | no           |
| `logResponses`          | whether to log the responses                                  | bool   | no           |

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

### Example
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

## mock

A mock provider used for testing.
