# Publishing the GitHub Wiki

The `wiki/` directory in this repository is a **source folder** only.
GitHub Wiki content is stored in a separate Git repository at:

- `https://github.com/<owner>/<repo>.wiki.git`

For this repository, that is:

- `https://github.com/knoxhack/Echo.wiki.git`

## One-time bootstrap

```bash
git clone https://github.com/knoxhack/Echo.wiki.git /tmp/Echo.wiki
cp -f wiki/*.md /tmp/Echo.wiki/
cd /tmp/Echo.wiki
git add .
git commit -m "docs: bootstrap wiki pages"
git push origin master
```

## Ongoing sync

Use the helper script from repo root:

```bash
bash tools/sync_wiki.sh /path/to/local/Echo.wiki
```

Then commit and push from the wiki repo.
