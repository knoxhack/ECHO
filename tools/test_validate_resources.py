import tempfile
import unittest
from pathlib import Path

import validate_resources


class GlobalLootModifierPathTests(unittest.TestCase):
    def test_singular_loot_modifier_directory_is_rejected(self) -> None:
        resource_root = self.resource_root()
        singular = resource_root / "data/examplemod/loot_modifier"
        singular.mkdir(parents=True)
        (singular / "seed_capsules.json").write_text('{"type":"neoforge:add_table"}', encoding="utf-8")

        errors: list[str] = []
        validate_resources.check_global_loot_modifier_paths("examplemod", resource_root, errors)

        self.assertTrue(
            any(error.startswith("SINGULAR_LOOT_MODIFIER_DIR") for error in errors),
            errors,
        )

    def test_plural_loot_modifiers_directory_is_accepted(self) -> None:
        resource_root = self.resource_root()
        plural = resource_root / "data/examplemod/loot_modifiers"
        plural.mkdir(parents=True)
        (plural / "seed_capsules.json").write_text('{"type":"neoforge:add_table"}', encoding="utf-8")

        errors: list[str] = []
        validate_resources.check_global_loot_modifier_paths("examplemod", resource_root, errors)

        self.assertEqual([], errors)

    def resource_root(self) -> Path:
        scratch_parent = validate_resources.ROOT / "build/validator-tests"
        scratch_parent.mkdir(parents=True, exist_ok=True)
        temp_dir = tempfile.TemporaryDirectory(dir=scratch_parent)
        self.addCleanup(temp_dir.cleanup)
        return Path(temp_dir.name)


if __name__ == "__main__":
    unittest.main()
