#!/bin/sh

set -e

if [ $# -ne 3 ]; then
    echo "Usage: $0 GITLAB_SERVER_URL GITLAB_PROJECT FILE_TO_REVIEW"
    exit 1
fi

gitlab_server_url=$1
gitlab_project=$2
file_to_review=$3
local_file_copy="tmp/file_to_be_reviewed.md"
findings_file=tmp/findings.json

# Get LLM API key
export $(cat ../../.env)

# Get GitLab API key
export $(cat .env)

mkdir -p ./tmp
if [ -f $findings_file ]; then
    rm $findings_file
fi


# Get local copy of the file to be reviewed
## Encode project name
encoded_gitlab_project=${gitlab_project//\//%2F}

## Get project info to find default branch
default_branch=$(curl -s -H "PRIVATE-TOKEN: $GITLAB_API_KEY" "https://gitlab.com/api/v4/projects/${encoded_gitlab_project}" | \
                 grep -o '"default_branch":"[^"]*"' | \
                 cut -d'"' -f4)

# Encode file path
encoded_file_path=${file_to_review//\//%2F}

curl -H "PRIVATE-TOKEN: $GITLAB_API_KEY" \
    "https://gitlab.com/api/v4/projects/${encoded_gitlab_project}/repository/files/${encoded_file_path}/raw?ref=${default_branch}" > $local_file_copy

# Review the file
java -jar ../../target/SimpleLlmTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
     --system-prompt-file system-prompt-en.txt \
     --user-prompt-file $local_file_copy \
     --provider "Anthropic" --model-file ../../examples/claude-4-sonnet@anthropic.yaml \
     --output-file $findings_file

# Create a GitLab MR to fix the problems
python gitlab_review.py  $gitlab_server_url/$gitlab_project $file_to_review $findings_file
