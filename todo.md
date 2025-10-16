
dans la partie context de 
on peut factoriser
{{#unless @last}},
            {{/unless}}


gérer les types des paramètres
+ ajouter l'UT correspondant

gérer l'optionnel des paramètres
+ ajouter l'UT correspondant

ajouter des tests

modifier examples\gpt-4.1-nano@custom.yaml pour qu'il prenne en compte ci-dessus


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
