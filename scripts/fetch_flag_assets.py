#!/usr/bin/env python3

import json
from pathlib import Path
from urllib.request import urlopen

TARGET_ISO3_CODES = [
    "JPN",
    "FRA",
    "DEU",
    "ITA",
    "IRL",
    "BEL",
    "POL",
    "UKR",
    "AUT",
    "NLD",
    "EST",
    "LTU",
    "GBR",
    "ESP",
    "PRT",
    "CHE",
    "KOR",
    "CHN",
    "IND",
    "THA",
    "SGP",
    "TUR",
    "IDN",
    "USA",
    "CAN",
    "MEX",
    "BRA",
    "ARG",
    "CHL",
    "COL",
    "ZAF",
    "EGY",
    "MAR",
    "KEN",
    "AUS",
    "NZL",
]

REPO_ROOT = Path(__file__).resolve().parents[1]
COUNTRY_DATA_PATH = REPO_ROOT / "src/main/resources/data/countries.json"
FLAG_ASSET_MANIFEST_PATH = REPO_ROOT / "src/main/resources/data/flag-assets.json"
FLAG_IMAGE_DIR = REPO_ROOT / "src/main/resources/static/images/flags"


def load_iso2_mapping():
    document = json.loads(COUNTRY_DATA_PATH.read_text())
    return {country["iso3Code"]: country["iso2Code"].lower() for country in document["countries"]}


def download_flag_svg(iso2_code: str) -> str:
    url = f"https://flagcdn.com/{iso2_code}.svg"
    with urlopen(url) as response:
        return response.read().decode("utf-8")


def main():
    iso2_mapping = load_iso2_mapping()
    FLAG_IMAGE_DIR.mkdir(parents=True, exist_ok=True)

    assets = []

    for iso3_code in TARGET_ISO3_CODES:
        iso2_code = iso2_mapping.get(iso3_code)
        if iso2_code is None:
            raise SystemExit(f"countries.json에 없는 ISO3 코드입니다: {iso3_code}")

        svg = download_flag_svg(iso2_code)
        file_name = f"{iso3_code.lower()}.svg"
        output_path = FLAG_IMAGE_DIR / file_name
        output_path.write_text(svg)

        assets.append(
            {
                "iso3Code": iso3_code,
                "relativePath": f"/images/flags/{file_name}",
                "format": "svg",
                "source": "flagcdn.com",
                "licenseNote": "source-snapshot",
            }
        )

    manifest = {"assets": assets}
    FLAG_ASSET_MANIFEST_PATH.write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n"
    )

    print(f"Downloaded {len(assets)} flag assets into {FLAG_IMAGE_DIR}")


if __name__ == "__main__":
    main()
