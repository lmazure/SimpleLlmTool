- add the unit tests for
  - java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar     --provider custom     --model-file examples/gemini-2.5-flash@custom.yaml     --tools-dir tools     --system-prompt-string "You always provide an English anwer, followed by a precise translation in French"     --user-prompt-string "What is the weather in Paris?" --log-level info
  - java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar     --provider custom     --model-file examples/gpt-5-nano\@custom.yaml     --tools-dir tools     --system-prompt-string "You always provide an English anwer, followed by a precise translation in French"     --user-prompt-string "What is the weather in Paris?" --log-level info
  - java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar     --provider custom     --model-file examples/gpt-5-nano\@custom.yaml     --tools-dir tools     --system-prompt-string "Use the tool to perform computation"     --user-prompt-string "What is 123456789 + 987654321?" --log-level info
  - java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar     --provider custom     --model-file examples/gemini-2.5-flash\@custom.yaml     --tools-dir tools     --system-prompt-string "Use the tool to perform computation"     --user-prompt-string "What is 123456789 + 987654321?" --log-level inf
- test boolean parameters
- test float parameters
- add unit tests for all parameter types
- test optional parameters


----------------------------------------




- afficher (en log info) les tokens et la finishing reason en mode batch
- compléter et tester gpt-4.1-nano@custom.yaml + corriger le test testBasicCustom
- corriger le parsing de la finishReason dans parseApiResponse (mettre un switch case qui throw sur une valeur non prévue en indiquant celle-ci)

- le code ATTACHMENT_ERROR est retourné pour un mauvais nom de modèle

- understand why attaching PDF URL does not work

questions
- what about HTML attachment?

commands
- add command `/format json` to redisplay the last LLM answer (which should be JSON) preperly formatted

- tools
  - proper logging of tool calls

- aupport of attachments in `CustomChatModel`

- less urgent
  - clean up interface mode
    - see if/how I can display Markdown
  - look at HttpBuilder and log in/out payloads for all models
  - throw a BadParamaterValue exception when a Handlebars template is incorrect
  - analyse token count evaluation

How to manage the token count for `gemini-2.5-flash@custom.yaml`, it seems we will need a formula
```
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "I do not have a name. I am a large language model, trained by Google."
          }
        ],
        "role": "model"
      },
      "finishReason": "STOP",
      "index": 0
    }
  ],
  "usageMetadata": {
    "promptTokenCount": 6,
    "candidatesTokenCount": 18,
    "totalTokenCount": 290,
    "promptTokensDetails": [
      {
        "modality": "TEXT",
        "tokenCount": 6
      }
    ],
    "thoughtsTokenCount": 266
  },
  "modelVersion": "gemini-2.5-flash",
  "responseId": "CijgaNeAA7Xm7M8PpYSfoQ4"
}
```
