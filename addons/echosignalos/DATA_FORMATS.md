# SignalOS Data Formats

SignalOS loads datapack JSON from each namespace under:

- `data/<namespace>/signalos/apps/*.json`
- `data/<namespace>/signalos/data_records/*.json`
- `data/<namespace>/signalos/drive_templates/*.json`
- `data/<namespace>/signalos/chapters/*.json`
- `data/<namespace>/signalos/missions/*.json`
- `data/<namespace>/signalos/archives/*.json`

## App

```json
{
  "title": "Field Files",
  "type": "files",
  "summary": "Browse recovered field records.",
  "order": 25,
  "accentColor": 6737151,
  "icon": "minecraft:compass",
  "permission": "user"
}
```

Known V1 app types include `home`, `files`, `notes`, `logs`, `network`, `settings`, `data_vault`, `echo_link`, `missions`, `archives`, `rewards`, and `diagnostics`. Unknown types can appear as app metadata but do not receive a rich built-in renderer yet.

## Data Record

```json
{
  "title": "Desktop Shell",
  "type": "record",
  "source": "SignalOS Core",
  "order": 0,
  "archived": false,
  "lines": [
    "SignalOS now boots into a desktop shell.",
    "Server racks expose installed data drives to the current operator network."
  ]
}
```

`body` can be used instead of `lines` when the record is a single string. If both are present, `lines` wins and is joined with newlines.

## Drive Template

```json
{
  "label": "Field Drive",
  "records": [
    {
      "id": "example:drive/field_boot",
      "title": "Field Boot",
      "type": "record",
      "source": "Example Drive Template",
      "body": "Portable data drives can carry records into a rack-backed SignalOS network.",
      "order": 0
    }
  ]
}
```

Drive template records use the same fields as data records. Each embedded record can provide an explicit `id`; if omitted, SignalOS derives one from the template id and record index.

## Chapter

```json
{
  "title": "Field Ops",
  "section": "progress",
  "order": 10,
  "accentColor": 65535,
  "icon": "minecraft:compass",
  "visible": true,
  "pages": ["missions", "archives", "rewards", "diagnostics"]
}
```

## Mission

```json
{
  "chapter": "example:field_ops",
  "title": "Secure Cache",
  "description": "Recover the first support cache.",
  "objectives": ["Find shelter", "Open SignalOS"],
  "order": 10,
  "icon": "minecraft:chest",
  "completionAdvancement": "minecraft:story/root",
  "rewardClaim": true,
  "displayRewards": [
    { "item": "minecraft:bread", "count": 4, "label": "Emergency rations" }
  ]
}
```

## Archive

```json
{
  "chapter": "example:field_ops",
  "title": "Field Brief",
  "group": "Briefings",
  "status": "OPEN",
  "order": 10,
  "locked": false,
  "lines": ["SignalOS records should be short, searchable, and chapter-owned."]
}
```

## Validation

SignalOS rejects malformed JSON, duplicate data IDs, invalid identifiers, blank objectives or archive lines, missing chapter references, reward counts below one, and reward items that do not resolve in the loaded item registry. Java and script-registered chapters can satisfy JSON mission/archive chapter references.
