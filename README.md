# Usage
```bash
mvn clean package
java -jar target/AITestCaseGeneration-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "Hello!" --system-prompt-string "You are a humorist. You always answer with jokes." --provider OpenAI --model-file gpt-4.1-nano@openai.yaml
```

# CLI
| parameter                                       | description                                |
| ----------------------------------------------- | ------------------------------------------ |
| `--system-prompt-string <system-prompt-string>` | system prompt as a string                  |
| `--system-prompt-file <system-prompt-file>`     | system prompt as the content of a file     |
| `--user-prompt-string <user-prompt-string>`     | user prompt as a string                    |
| `--user-prompt-file <user-prompt-file>`         | user prompt as the content of a file       |
| `--output-file <output-file>`                   | output file (stdout by default)            |
| `--error-file error-file>`                      | error file (stderr by default)             |
| `--provider <provider>`                         | provider                                   |
| `--chat-mode`                                   | chat mode                                  |
| `--model-file <model_file>`                     | file defining the model and its parameters |
| `--help`                                        | display help and exit                      |

# Parameters per provider

A model and its parameters are defined by a YAML file, see for example [gpt-4.1-nano@openai.yaml](gpt-4.1-nano@openai.yaml).  
It is indicated on the command line with the `--model-file <model_file>` parameter.

Its content depends on the provider.  
The provider is indicated on the command line with the `--provider <provider>` parameter.

## OpenAI
| parameter           | description                                              | compulsory   |
| ------------------- | -------------------------------------------------------- | ------------ |
| modelName           | name of the model                                        | yes          |
| baseUrl             | base URL of the provider                                 | no           |
| apiKeyEnvVar        | name of the environment variable containing the API key  | yes          |
| organizationId      | ID of the organization containing the model              | no           |
| projectId           | ID of the project containing the model                   | no           |
| temperature         | temperature of the model                                 | no           |
| seed                | random seed of the model                                 | no           |
| topP                | top P value of the model                                 | no           |
| maxCompletionTokens | maximum number of tokens the model should generate       | no           |

## Mistral AI
| parameter           | description                                              | compulsory   |
| ------------------- | -------------------------------------------------------- | ------------ |
| modelName           | name of the model                                        | yes          |
| baseUrl             | base URL of the provider                                 | no           |
| apiKeyEnvVar        | name of the environment variable containing the API key  | yes          |
| temperature         | temperature of the model                                 | no           |
| seed                | random seed of the model                                 | no           |
| topP                | top P value of the model                                 | no           |
| maxTokens           | maximum number of tokens the model should generate       | no           |

## custom
| parameter           | description                                              | compulsory   |
| ------------------- | -------------------------------------------------------- | ------------ |
| modelName           | name of the model                                        | yes          |
| url                 | URL of the provider                                      | yes          |
| apiKeyEnvVar        | name of the environment variable containing the API key  | yes          |

## mock

A mock provider used for testing.
