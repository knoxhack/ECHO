# Build and Release

## Build Entire Workspace

```powershell
.\gradlew.bat buildEchoWorkspace
```

## Run Full Verification Gate

```powershell
.\gradlew.bat verifyEchoRelease --warning-mode all
```

## Copy Built Jars to Modpack Profile

```powershell
.\gradlew.bat copyEchoJarsToModpack
```

## Additional Validation

```powershell
python -m pip install -r tools\requirements.txt
python tools\validate_resources.py
python tools\validate_gameplay_data.py
```

## Version Contract

- Tags follow `v<major>.<minor>.<patch>` (optional prerelease suffix).
- Tag version must match module release versions for that cut.
- Release title should mirror the exact tag.
