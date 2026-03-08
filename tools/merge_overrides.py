#!/usr/bin/env python3

import argparse
import json
import subprocess
from collections import OrderedDict
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_BASE_JSON = REPO_ROOT / "src/main/resources/assets/vintagedamageindicators/defaults/overrides.json"
SELECT_OPTION_SCRIPT = REPO_ROOT / "tools/select_option.sh"
PACKAGE_GROUP_OVERRIDES = [
    "com.wildmobsmod.entity",
    "net.minecraft.entity",
    "drzhark.mocreatures",
    "com.Oceancraft",
    "com.wildmobsmod",
    "thaumcraft.common.entities",
    "twilightforest.entity",
    "com.emoniph.witchery",
    "lycanite.lycanitesmobs",
    "am2.entities",
    "atomicstryker.minions",
    "tuhljin.automagy",
    "vazkii.botania",
    "georgetsak.camouflagecreepers",
    "diversity.entity",
    "mysticalmobs.common",
    "com.ilya3point999k.thaumicconcilium",
    "com.kentington.thaumichorizons",
    "zeldaswordskills.entity.mobs",
    "toast.specialMobs.entity"
]
IMPORT_OPTION = "Merge package"
SKIP_OPTION = "Skip package"
IMPORT_ALL_OPTION = "Merge all remaining packages"
SKIP_ALL_OPTION = "Skip remaining"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Merge override entries into an overrides.json file. "
            "Selected imported mobs replace entries with the same className."
        )
    )
    parser.add_argument("input_json", help="JSON file containing overrides to import")
    parser.add_argument(
        "-b",
        "--base",
        default=str(DEFAULT_BASE_JSON),
        help="Base overrides JSON to merge into",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="merged.json",
        help="Output file path. Defaults to merged.json in the current working directory.",
    )
    parser.add_argument(
        "--import-all",
        action="store_true",
        help="Import every package without prompting",
    )
    return parser.parse_args()


def load_overrides(path: Path) -> list[dict]:
    try:
        with path.open("r", encoding="utf-8") as handle:
            data = json.load(handle)
    except FileNotFoundError as exc:
        raise SystemExit(f"JSON file not found: {path}") from exc
    except json.JSONDecodeError as exc:
        raise SystemExit(f"Failed to parse JSON from {path}: {exc}") from exc

    if not isinstance(data, list):
        raise SystemExit(f"Expected a JSON array in {path}")
    for index, entry in enumerate(data):
        if not isinstance(entry, dict):
            raise SystemExit(f"Entry {index} in {path} is not a JSON object")
        class_name = entry.get("className")
        if not isinstance(class_name, str) or not class_name.strip():
            raise SystemExit(f"Entry {index} in {path} is missing a valid className")
    return data


def get_package_name(class_name: str) -> str:
    if "." not in class_name:
        return "<default package>"

    package_name = class_name.rsplit(".", 1)[0]
    for override_prefix in sorted(PACKAGE_GROUP_OVERRIDES, key=len, reverse=True):
        if package_name == override_prefix or package_name.startswith(override_prefix + "."):
            return override_prefix
    return package_name


def group_by_package(entries: list[dict]) -> OrderedDict[str, list[dict]]:
    grouped: OrderedDict[str, list[dict]] = OrderedDict()
    for entry in entries:
        package_name = get_package_name(entry["className"])
        grouped.setdefault(package_name, []).append(entry)
    return grouped


def merge_entries(base_entries: list[dict], imported_entries: list[dict]) -> list[dict]:
    merged_by_class: OrderedDict[str, dict] = OrderedDict()
    for entry in base_entries:
        merged_by_class[entry["className"]] = entry
    for entry in imported_entries:
        merged_by_class[entry["className"]] = entry
    return list(merged_by_class.values())


def select_option(title: str, *options: str) -> int:
    if not options:
        raise ValueError("select_option requires at least one option")
    try:
        result = subprocess.run(
            ["/usr/bin/env", "bash", str(SELECT_OPTION_SCRIPT), title, *options],
            check=False,
        )
    except FileNotFoundError as exc:
        raise SystemExit(f"Missing selector helper: {SELECT_OPTION_SCRIPT}") from exc

    if result.returncode == 130:
        raise SystemExit(130)
    if result.returncode < 0:
        raise SystemExit(f"Selector helper terminated by signal {-result.returncode}")
    if result.returncode >= len(options):
        raise SystemExit(f"Selector helper failed with exit code {result.returncode}")
    return result.returncode


def choose_imports(grouped_entries: OrderedDict[str, list[dict]], import_all: bool) -> list[dict]:
    if import_all:
        selected_entries: list[dict] = []
        for entries in grouped_entries.values():
            selected_entries.extend(entries)
        return selected_entries

    selected_entries: list[dict] = []
    merge_all_remaining = False
    for package_name, entries in grouped_entries.items():
        if merge_all_remaining:
            selected_entries.extend(entries)
            continue

        choice = select_option(
            f"Import package {package_name} ({len(entries)} entr{'y' if len(entries) == 1 else 'ies'})?",
            IMPORT_OPTION,
            SKIP_OPTION,
            IMPORT_ALL_OPTION,
            SKIP_ALL_OPTION,
        )
        if choice == 0:
            selected_entries.extend(entries)
        elif choice == 2:
            selected_entries.extend(entries)
            merge_all_remaining = True
        elif choice == 3:
            break

    return selected_entries


def write_overrides(path: Path, entries: list[dict]) -> None:
    with path.open("w", encoding="utf-8") as handle:
        json.dump(entries, handle, indent=2, ensure_ascii=True)
        handle.write("\n")


def main() -> int:
    args = parse_args()
    input_path = Path(args.input_json)
    base_path = Path(args.base)
    output_path = Path(args.output)

    base_entries = load_overrides(base_path)
    imported_entries = load_overrides(input_path)
    grouped_entries = group_by_package(imported_entries)
    selected_entries = choose_imports(grouped_entries, args.import_all)
    merged_entries = merge_entries(base_entries, selected_entries)
    write_overrides(output_path, merged_entries)

    base_classes = {entry["className"] for entry in base_entries}
    replaced_classes = {entry["className"] for entry in selected_entries if entry["className"] in base_classes}
    added_count = len(selected_entries) - len(replaced_classes)
    print(
        f"Wrote {len(merged_entries)} overrides to {output_path} "
        f"({len(replaced_classes)} replaced, {added_count} added, {len(selected_entries)} imported)."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
