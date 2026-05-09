#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 /path/to/Echo.wiki"
  exit 1
fi

wiki_repo="$1"

if [ ! -d "$wiki_repo/.git" ]; then
  echo "Target is not a git repository: $wiki_repo"
  exit 1
fi

mkdir -p "$wiki_repo"
cp -f wiki/*.md "$wiki_repo/"

echo "Copied wiki/*.md to $wiki_repo"
echo "Next steps:"
echo "  cd $wiki_repo"
echo "  git add ."
echo "  git commit -m 'docs: sync wiki content'"
echo "  git push origin master"
