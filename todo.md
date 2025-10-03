- afficher la finishing reason en mode chat et batch
- ajouter le support de la finishing reason dans le custom AI server

- le code ATTACHMENT_ERROR est retourné pour un mauvais nom de modèle


add PDF attachment to
- chat mode
- interface mode
add unit tests
- local image
- remote image https://samplelib.com/lib/preview/jpeg/sample-green-100x75.jpg 
questions
- what about HTML attachment?

commands
- add command `/format json` to redisplay the last LLM answer (which should be JSON) preperly formatted

- tools
  - proper logging of tool calls
  - support tools in `CustomChatModel`

- aupport of attachments in `CustomChatModel`

- less urgent
  - clean up interface mode
    - see if/how I can display Markdown
  - look at HttpBuilder and log in/out payloads for all models
  - throw a BadParamaterValue exception when a Handlebars template is incorrect
  - analyse token count evaluation

How to manage the token count for `gemini-2.5-flash@custom.yaml`
```
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
