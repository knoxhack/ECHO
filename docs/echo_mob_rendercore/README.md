# Echo Mob RenderCore Texture Boards

Each entity in the Echo ecosystem gets a production board under:

`docs/echo_mob_rendercore/entity_sheets/<modid>/<entity>.png`

These boards are the source art for the in-game RenderCore textures. The final
textures are cut from the bottom export row of each board:

- left panel: base texture
- middle panel: emissive/glow texture
- right panel: damage texture

The Python tools only crop, pad, and nearest-neighbor resize those export
panels. They must not draw final entity texture art.

## Workflow

1. Generate missing board prompts:

   ```powershell
   python tools\generate_echo_mob_rendercore_assets.py --prompts-only
   ```

2. Use Codex imagegen for each prompt in:

   `art_sources/echo_mob_rendercore/prompts/entity_boards/<modid>/<entity>.txt`

3. Copy each generated board into:

   `docs/echo_mob_rendercore/entity_sheets/<modid>/<entity>.png`

4. Cut available boards into in-game textures:

   ```powershell
   python tools\cut_echo_mob_board_textures.py --existing-only
   ```

5. Once all boards exist, regenerate the full manifest/resources:

   ```powershell
   python tools\generate_echo_mob_rendercore_assets.py
   python tools\validate_echo_mob_rendercore_assets.py
   ```

## Current Standard

Final atlas panels must follow Minecraft texture technique: clean UV islands,
hard square pixels, limited palette, aligned base/emissive/damage maps, no
blurred scaling, no perspective render, and no full-body sprite cutout in the
texture export area.
