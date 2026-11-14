"""Offline tests for release version, code and changelog management."""
import importlib.util
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "release_prepare", Path(__file__).parent / "release/prepare.py"
)
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleasePreparationTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        (self.root / "app").mkdir()
        (self.root / "app/build.gradle.kts").write_text(
            'android {\n    defaultConfig {\n'
            '        versionCode = 10\n'
            '        versionName = "1.7.1"\n'
            '    }\n}\n', encoding="utf-8"
        )
        (self.root / "CHANGELOG.md").write_text(
            '# Changelog\n\n## Unreleased\n\n- New V-Design\n\n'
            '## 1.7.1\n\n- Older fix\n', encoding="utf-8"
        )

    def test_prepares_release_commit_source_and_notes(self):
        notes = self.root / "artifacts/RELEASE_NOTES.md"
        self.assertEqual(11, release.prepare(self.root, "1.8.0", notes))
        gradle = (self.root / "app/build.gradle.kts").read_text()
        self.assertEqual(("1.8.0", 11), release.read_version(gradle))
        changelog = (self.root / "CHANGELOG.md").read_text()
        self.assertIn("## Unreleased\n\n## 1.8.0\n\n- New V-Design", changelog)
        self.assertIn("## 1.7.1\n\n- Older fix", changelog)
        self.assertIn("Plume Launcher Advanced v1.8.0", notes.read_text())
        self.assertIn("- New V-Design", notes.read_text())

    def test_beta_and_rc_sort_before_stable(self):
        self.assertLess(release.version_key("1.8.0-beta2"), release.version_key("1.8.0-rc1"))
        self.assertLess(release.version_key("1.8.0-rc1"), release.version_key("1.8.0"))
        self.assertGreater(release.version_key("2.0.0-beta1"), release.version_key("1.7.1"))

    def test_existing_or_lower_version_rejected_without_editing(self):
        original = (self.root / "app/build.gradle.kts").read_text()
        for name in ("1.7.1", "1.6.0", "1.7.1-beta2", "v1.8.0", "1.8", "1.8.0;touch /tmp/x"):
            with self.subTest(name=name), self.assertRaises(ValueError):
                release.prepare(self.root, name)
        self.assertEqual(original, (self.root / "app/build.gradle.kts").read_text())

    def test_missing_release_notes_rejected(self):
        (self.root / "CHANGELOG.md").write_text(
            "# Changelog\n\n## Unreleased\n\n## 1.7.1\n- Older\n"
        )
        with self.assertRaisesRegex(ValueError, "release-note bullet"):
            release.prepare(self.root, "1.8.0")

    def test_no_duplicate_headings_or_decreasing_codes(self):
        path = self.root / "CHANGELOG.md"
        path.write_text(path.read_text().replace("## 1.7.1", "## 1.8.0\n- duplicated\n\n## 1.7.1"))
        with self.assertRaisesRegex(ValueError, "already exists"):
            release.prepare(self.root, "1.8.0")
        path.write_text("# Changelog\n## Unreleased\n- Good\n## 1.7.1\n- Old\n")
        (self.root / "app/build.gradle.kts").write_text(
            'versionName = "1.7.1"\nversionCode = 2100000000\n'
        )
        with self.assertRaisesRegex(ValueError, "versionCode"):
            release.prepare(self.root, "1.8.0")


if __name__ == "__main__":
    unittest.main()
