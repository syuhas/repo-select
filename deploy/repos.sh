#!/bin/bash

# Define file paths
REPO_LIST_FILE="$WORKSPACE/github_repos.txt"
BRANCHES_LIST_FILE="$WORKSPACE/github_branches.json"

# Fetch repositories (Modify the API call to include full .git URL)
curl -s -H "Accept: application/vnd.github+json" \
     -H "Authorization: Bearer $TOKEN" \
     "https://api.github.com/user/repos?per_page=100" | \
     jq -r '.[] | {name: .name, git_url: .clone_url}' > "$REPO_LIST_FILE"

echo "Repositories saved to $REPO_LIST_FILE"

# Initialize JSON object
echo "{" > "$BRANCHES_LIST_FILE"

# Read repo list and fetch branches
while read -r repo_data; do
    repo_name=$(echo "$repo_data" | jq -r '.name')
    git_url=$(echo "$repo_data" | jq -r '.git_url')

    # Construct GitHub API URL correctly
    branchesApiUrl="https://api.github.com/repos/$GITHUB_USER/$repo_name/branches"

    echo "Fetching branches from: $branchesApiUrl"

    # Fetch branches
    branches=$(curl -s -H "Accept: application/vnd.github+json" \
                    -H "Authorization: Bearer $TOKEN" \
                    "$branchesApiUrl" | jq -r '[.[].name]')

    # Handle empty branch list
    if [[ "$branches" == "[]" ]]; then
        branches='["main"]'
    fi

    # Append to JSON
    echo "\"$repo_name\": { \"branches\": $branches, \"git_url\": \"$git_url\" }," >> "$BRANCHES_LIST_FILE"

done < "$REPO_LIST_FILE"

# Remove last comma and close JSON
sed -i '$ s/,$//' "$BRANCHES_LIST_FILE"
echo "}" >> "$BRANCHES_LIST_FILE"

echo "Branches saved to $BRANCHES_LIST_FILE"
