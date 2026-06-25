#!/usr/bin/env python3
"""Import one bird's metadata and media into the Android asset library."""

from __future__ import annotations

import argparse
import json
import re
import shutil
import sys
import tempfile
from pathlib import Path

REQUIRED_METADATA = {
    "id",
    "romanized",
    "kanji",
    "scientific",
    "english",
    "czech",
}
SLUG_PATTERN = re.compile(r"^[a-z0-9][a-z0-9_-]*$")
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"}
AUDIO_EXTENSIONS = {".mp3"}


def parse_metadata(path: Path) -> dict[str, str]:
    metadata: dict[str, str] = {}
    for line_number, raw_line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = raw_line.strip()
        if not line:
            continue
        if "=" not in line:
            raise ValueError(f"{path}:{line_number}: expected key=value")
        key, value = (part.strip() for part in line.split("=", 1))
        if not key or not value:
            raise ValueError(f"{path}:{line_number}: key and value must be non-empty")
        metadata[key] = value

    missing = sorted(REQUIRED_METADATA - metadata.keys())
    if missing:
        raise ValueError(f"{path}: missing metadata keys: {', '.join(missing)}")
    try:
        int(metadata["id"])
    except ValueError as error:
        raise ValueError(f"{path}: id must be an integer") from error
    return metadata


def validate_media(path: Path, extensions: set[str], label: str) -> None:
    if not path.is_file():
        raise ValueError(f"{label} file does not exist: {path}")
    if path.suffix.lower() not in extensions:
        allowed = ", ".join(sorted(extensions))
        raise ValueError(f"{label} must use one of: {allowed}")


def build_entry(slug: str, image_name: str, metadata_name: str, audio_names: list[str]) -> dict:
    folder = f"birds/{slug}"
    return {
        "mp3": [
            {
                "name": Path(audio_name).stem,
                "file": f"{folder}/{audio_name}",
            }
            for audio_name in audio_names
        ],
        "jpg": f"{folder}/{image_name}",
        "txt": f"{folder}/{metadata_name}",
    }


def import_bird(
    assets_dir: Path,
    slug: str,
    metadata_path: Path,
    image_path: Path,
    audio_paths: list[Path],
    dry_run: bool = False,
) -> None:
    if not SLUG_PATTERN.fullmatch(slug):
        raise ValueError("slug must contain only lowercase letters, numbers, underscores, or dashes")

    metadata = parse_metadata(metadata_path)
    validate_media(image_path, IMAGE_EXTENSIONS, "image")
    if not audio_paths:
        raise ValueError("at least one MP3 audio file is required")
    for audio_path in audio_paths:
        validate_media(audio_path, AUDIO_EXTENSIONS, "audio")

    birds_json_path = assets_dir / "birds.json"
    birds = json.loads(birds_json_path.read_text(encoding="utf-8"))
    if slug in birds:
        raise ValueError(f"bird slug already exists: {slug}")

    bird_id = int(metadata["id"])
    existing_ids = {
        int(parse_metadata(assets_dir / entry["txt"])["id"])
        for entry in birds.values()
    }
    if bird_id in existing_ids:
        raise ValueError(f"bird id already exists: {bird_id}")

    destination = assets_dir / "birds" / slug
    if destination.exists():
        raise ValueError(f"destination already exists: {destination}")

    image_name = image_path.name
    metadata_name = f"{slug}.txt"
    audio_names = [path.name for path in audio_paths]
    if len(set(audio_names)) != len(audio_names):
        raise ValueError("audio filenames must be unique")

    birds[slug] = build_entry(slug, image_name, metadata_name, audio_names)

    if dry_run:
        print(f"Validated {slug} (id={bird_id}); no files changed.")
        return

    destination.mkdir(parents=True)
    try:
        shutil.copy2(metadata_path, destination / metadata_name)
        shutil.copy2(image_path, destination / image_name)
        for audio_path in audio_paths:
            shutil.copy2(audio_path, destination / audio_path.name)

        with tempfile.NamedTemporaryFile(
            mode="w",
            encoding="utf-8",
            dir=assets_dir,
            prefix="birds.",
            suffix=".json.tmp",
            delete=False,
        ) as temporary_file:
            json.dump(birds, temporary_file, ensure_ascii=False, indent=2)
            temporary_file.write("\n")
            temporary_path = Path(temporary_file.name)
        temporary_path.replace(birds_json_path)
    except Exception:
        shutil.rmtree(destination, ignore_errors=True)
        raise

    print(f"Imported {slug} (id={bird_id}) into {destination}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--slug", required=True, help="lowercase asset folder name")
    parser.add_argument("--metadata", required=True, type=Path, help="key=value TXT file")
    parser.add_argument("--image", required=True, type=Path, help="JPG, JPEG, or PNG image")
    parser.add_argument(
        "--audio",
        required=True,
        type=Path,
        nargs="+",
        help="one or more MP3 files",
    )
    parser.add_argument(
        "--assets-dir",
        type=Path,
        default=Path("app/src/main/assets"),
        help="Android assets directory",
    )
    parser.add_argument("--dry-run", action="store_true", help="validate without changing files")
    arguments = parser.parse_args()

    try:
        import_bird(
            assets_dir=arguments.assets_dir.resolve(),
            slug=arguments.slug,
            metadata_path=arguments.metadata.resolve(),
            image_path=arguments.image.resolve(),
            audio_paths=[path.resolve() for path in arguments.audio],
            dry_run=arguments.dry_run,
        )
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
