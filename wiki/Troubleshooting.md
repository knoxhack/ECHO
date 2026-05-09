# Troubleshooting

## Common Issues

### Missing or mismatched jars

Run the copy task and ensure only one current jar exists per module in the modpack profile.

### Release verification failures

- Re-run resource and gameplay validators.
- Confirm all required modules for your chosen addon set are present.
- Run with `--warning-mode all` for better diagnostics.

### Old world behavior drift

Legacy worlds may require new chunk generation to surface current POI/resource distribution updates.

## Bug Report Template

```text
Version / mod list:
World age and seed:
What you expected:
What happened:
Steps to reproduce:
Screenshots or crash report:
Coordinates / biome / POI:
```
