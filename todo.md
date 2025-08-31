commands
- add command to list `/tools dlist` to list tools with their parameters
- add command `/format json` to redisplay the last LLM answer (which should be JSON) preperly formatted

- tools
  - proper logging of tool calls
  - support tools in `CustomChatModel`

- do not run expensive test while daily coding
- add pointers between READMEs

- less
  - clean up interface mode
    - see if/how I can display Markdown
  - look at HttpBuilder and log in/out payloads for all models
  - throw a BadParamaterValue exception when a Handlebars template is incorrect
  - analyse token count evaluation
