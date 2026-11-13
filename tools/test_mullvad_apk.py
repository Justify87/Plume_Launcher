"""Offline synthetic tests for required integrated Mullvad APK contents."""
import importlib.util
import tempfile
import unittest
import zipfile
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "check_apk", Path(__file__).parent / "mullvad/check_apk.py"
)
checker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(checker)


def arm64_elf(machine=183):
    header = bytearray(20)
    header[0:4] = b"\x7fELF"
    header[4] = 2
    header[5] = 1
    header[18:20] = machine.to_bytes(2, "little")
    return bytes(header)


class IntegratedApkStructureTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.path = Path(self.temp.name) / "integrated.apk"
        self.entries = {
            "AndroidManifest.xml": b"manifest",
            "classes.dex": b"dex\n035\x00",
            "lib/arm64-v8a/libmullvad_jni.so": arm64_elf(),
            "assets/relays.json": b'{"countries":[]}',
        }

    def write(self):
        with zipfile.ZipFile(self.path, "w") as apk:
            for name, data in self.entries.items():
                apk.writestr(name, data)

    def test_valid_arm64_apk(self):
        self.write()
        self.assertEqual(checker.check_apk(str(self.path)), 4)

    def test_missing_native_library(self):
        del self.entries["lib/arm64-v8a/libmullvad_jni.so"]
        self.write()
        with self.assertRaisesRegex(ValueError, "missing mandatory"):
            checker.check_apk(str(self.path))

    def test_zero_length_native_library(self):
        self.entries["lib/arm64-v8a/libmullvad_jni.so"] = b""
        self.write()
        with self.assertRaisesRegex(ValueError, "empty mandatory"):
            checker.check_apk(str(self.path))

    def test_wrong_native_architecture(self):
        self.entries["lib/arm64-v8a/libmullvad_jni.so"] = arm64_elf(62)
        self.write()
        with self.assertRaisesRegex(ValueError, "ARM64 ELF"):
            checker.check_apk(str(self.path))

    def test_invalid_relay_asset(self):
        self.entries["assets/relays.json"] = b"{"
        self.write()
        with self.assertRaisesRegex(ValueError, "not valid JSON"):
            checker.check_apk(str(self.path))


if __name__ == "__main__":
    unittest.main()
