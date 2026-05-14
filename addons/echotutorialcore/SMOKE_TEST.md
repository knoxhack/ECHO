# ECHO: TutorialCore Smoke Test

Quick verification steps to confirm TutorialCore loads and operates correctly.

## 1. Module Load

- Start the game (client or dedicated server).
- Check logs for: `ECHO: TutorialCore online. Ashfall deep, but not confusing.`

## 2. Data Reload

- Run `/reload`.
- Check logs for: `TutorialCore reloaded: X cards, Y hints, Z flows, W tooltips.`
- Values should be non-zero (default content is bundled).

## 3. Commands

- Run `/echotutorialcore guide_mode normal`
- Expected: `Guide mode set to NORMAL.`
- Run `/echotutorialcore progress`
- Expected: progress summary with flags, cards, flows counts.
- Run `/echotutorialcore list_cards`
- Expected: list of registered tutorial cards.
- Run `/echotutorialcore list_hints`
- Expected: list of registered tutorial hints.
- Run `/echotutorialcore debug`
- Expected: counts for cards, hints, flows.

## 4. Player Progress

- Join the world.
- Expected log: progress flag `entered_world` is marked automatically.
- Run `/echotutorialcore progress`
- Expected: at least 1 progress flag.

## 5. Guide Mode Persistence

- Set guide mode to `assisted`.
- Disconnect and reconnect.
- Run `/echotutorialcore progress`
- Expected: guide mode is still `ASSISTED`.

## 6. Optional Integrations

If other ECHO addons are present, check logs for integration messages:
- `ECHO: TutorialCore integrated with Terminal.`
- `ECHO: TutorialCore integrated with PowerGrid.`
- etc.

If an addon is absent, TutorialCore must not crash.

## 7. Client Display

- On client, trigger a hint with `/echotutorialcore hint @p echotutorialcore:no_power`
- Expected: chat message `[ECHO-7] Machine Offline: No EP input detected...`

## 8. Config

- Verify `echotutorialcore-common.toml` and `echotutorialcore-client.toml` are generated in the config folder.

## Known MVP Limitations

- Custom tutorial toast UI is not implemented; chat fallback is used.
- Terminal Guide page UI is scaffolded but not fully wired.
- Deep condition evaluation for hints (inventory checks, region checks) is partially scaffolded.
