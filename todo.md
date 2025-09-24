readme
- fix the header level
- add hyperlinks for the "see below"

add image attachment to
- chat mode
- interface mode
add PDF attachment to
- chat mode
- interface mode

commands
- add command `/format json` to redisplay the last LLM answer (which should be JSON) preperly formatted

- tools
  - proper logging of tool calls
  - support tools in `CustomChatModel`

- less
  - clean up interface mode
    - see if/how I can display Markdown
  - look at HttpBuilder and log in/out payloads for all models
  - throw a BadParamaterValue exception when a Handlebars template is incorrect
  - analyse token count evaluation
