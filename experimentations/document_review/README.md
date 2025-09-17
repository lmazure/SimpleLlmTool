# Review documents and propose corrections in a GitLab merge request

```bash
export $(cat .env)
java -jar target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --user-prompt-file //wsl.localhost/Ubuntu-22.04/home/laurent/code/GitLab/henixdevelopment/squash/doc/squashtm-doc-en/docs/admin-guide/manage-milestones/manage-milestones.md --system-prompt-file experimentations/document_review/system-prompt-en.txt --provider "Anthropic" --model-file examples/claude-3-5-sonnet@anthropic.yaml
```
and in the GitLabDocumentation repo
```bash
python gitlab_review.py  https://gitlab.com/lmazure_TestGroup/testreview manage-milestones.md ../SimpleLlmTool/findings.json
```
