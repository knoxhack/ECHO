# Addon Development

## Integration Rules

1. Integrate via `echocore` public services.
2. Register terminal navigation through `echoterminal` public profile APIs.
3. Keep chapter logic, persistence, and reward authority inside the owning addon.
4. Avoid direct mutation of another chapter's save domain.

## Recommended Addon Skeleton

- Module metadata and Gradle wiring
- Chapter-owned progression state
- Worldgen/content registrations
- Terminal navigation registration
- Optional recipe provider registration
- GameTests / validation hooks

## Compatibility Philosophy

- Be tolerant of missing optional addons.
- Fail gracefully on duplicate registration attempts.
- Preserve forward compatibility by relying on contract types, not internals.
