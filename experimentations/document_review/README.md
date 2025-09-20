# Review documents and propose corrections in a GitLab merge request

Your LLM API should be stored as the adequate variable in the `.env` file at the root of this repository.

Your GitLab API should be stored as `GITLAB_API_KEY` in the `.env` file of this directory.

Being in this directory, run:
```bash
python -m venv .venv
source .venv/Scripts/activate
pip install -r requirements.txt
./review_doc.sh https://gitlab.com project_path/project_name example.md
```
