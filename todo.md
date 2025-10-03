- ajouter des pointeurs vers les listes de modèles

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

- less urgent
  - clean up interface mode
    - see if/how I can display Markdown
  - look at HttpBuilder and log in/out payloads for all models
  - throw a BadParamaterValue exception when a Handlebars template is incorrect
  - analyse token count evaluation
