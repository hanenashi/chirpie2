import tempfile
import unittest
from pathlib import Path

from import_bird import build_entry, parse_metadata


class ImportBirdTest(unittest.TestCase):
    def test_parse_metadata_requires_all_fields(self):
        with tempfile.TemporaryDirectory() as directory:
            metadata = Path(directory) / "bird.txt"
            metadata.write_text("id=52\nenglish=Test Bird\n", encoding="utf-8")

            with self.assertRaisesRegex(ValueError, "missing metadata keys"):
                parse_metadata(metadata)

    def test_build_entry_uses_android_asset_paths(self):
        self.assertEqual(
            {
                "mp3": [
                    {"name": "sample_call", "file": "birds/sample/sample_call.mp3"}
                ],
                "jpg": "birds/sample/sample.jpg",
                "txt": "birds/sample/sample.txt",
            },
            build_entry("sample", "sample.jpg", "sample.txt", ["sample_call.mp3"]),
        )


if __name__ == "__main__":
    unittest.main()
