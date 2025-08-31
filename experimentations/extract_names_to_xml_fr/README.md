# Extract person names and format them to XML (French)

```bash
export $(cat .env)
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-string "the text" --system-prompt-file experimentations/extract_names_to_xml_fr/system-prompt.txt --provider "Mistral AI" --model-file examples/mistral-large-latest@mistralai.yaml
```

