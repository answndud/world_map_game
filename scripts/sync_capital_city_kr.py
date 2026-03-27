#!/usr/bin/env python3
"""
Sync Korean capital names into countries.json.

Source strategy:
- Base labels come from Wikidata country -> capital mapping in Korean.
- Manual overrides handle:
  - countries whose current seed still uses a legacy/alternate capital
  - Korean labels that include administrative suffixes instead of the
    common dictionary-style capital name used in Korean product copy
"""

from __future__ import annotations

import json
import sys
import urllib.parse
import urllib.request
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SEED_PATH = ROOT / "src/main/resources/data/countries.json"
USER_AGENT = "worldmap-capital-kr-sync/1.0"
QUERY_ENDPOINT = "https://query.wikidata.org/sparql"

MANUAL_KOREAN_CAPITAL_OVERRIDES: dict[str, str] = {
    "AND": "안도라라베야",
    "BDI": "부줌부라",
    "BOL": "라파스",
    "CHN": "베이징",
    "DNK": "코펜하겐",
    "GNQ": "말라보",
    "ISR": "예루살렘",
    "JPN": "도쿄",
    "KIR": "타라와",
    "KOR": "서울",
    "KWT": "쿠웨이트",
    "LKA": "콜롬보",
    "MCO": "모나코",
    "MNE": "포드고리차",
    "PRK": "평양",
    "SWZ": "음바바네",
    "VAT": "바티칸",
    "YEM": "사나",
    "ZAF": "프리토리아",
}

WIKIDATA_COUNTRY_CAPITAL_QUERY = """
SELECT DISTINCT ?iso3 ?capitalEn ?capitalLabel WHERE {
  ?country wdt:P31 wd:Q3624078 .
  FILTER NOT EXISTS { ?country wdt:P31 wd:Q3024240 }
  FILTER NOT EXISTS { ?country wdt:P31 wd:Q28171280 }
  ?country wdt:P298 ?iso3 .
  ?country wdt:P36 ?capital .
  ?capital rdfs:label ?capitalEn FILTER(LANG(?capitalEn) = "en")
  SERVICE wikibase:label { bd:serviceParam wikibase:language "ko,en". }
}
ORDER BY ?iso3 ?capitalEn
"""


def fetch_wikidata_capitals() -> dict[str, list[tuple[str, str]]]:
    url = QUERY_ENDPOINT + "?query=" + urllib.parse.quote(WIKIDATA_COUNTRY_CAPITAL_QUERY)
    request = urllib.request.Request(
        url,
        headers={
            "Accept": "application/sparql-results+json",
            "User-Agent": USER_AGENT,
        },
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        payload = json.load(response)

    rows: dict[str, list[tuple[str, str]]] = defaultdict(list)
    for binding in payload["results"]["bindings"]:
        rows[binding["iso3"]["value"]].append(
            (
                binding["capitalEn"]["value"],
                binding["capitalLabel"]["value"],
            )
        )
    return rows


def resolve_korean_capital(
    country: dict[str, object],
    wikidata_rows: dict[str, list[tuple[str, str]]],
) -> str:
    iso3 = str(country["iso3Code"])
    if iso3 in MANUAL_KOREAN_CAPITAL_OVERRIDES:
        return MANUAL_KOREAN_CAPITAL_OVERRIDES[iso3]

    capital_en = str(country["capitalCity"]).strip()
    candidates = wikidata_rows.get(iso3, [])

    for candidate_en, candidate_ko in candidates:
        if candidate_en.casefold() == capital_en.casefold():
            return candidate_ko

    if len(candidates) == 1:
        return candidates[0][1]

    raise RuntimeError(
        f"Unable to resolve Korean capital for {iso3} ({country['nameEn']} / {capital_en}). "
        f"Candidates: {candidates}"
    )


def sync_capital_city_kr() -> None:
    document = json.loads(SEED_PATH.read_text(encoding="utf-8"))
    wikidata_rows = fetch_wikidata_capitals()

    for country in document["countries"]:
        country["capitalCityKr"] = resolve_korean_capital(country, wikidata_rows)

    metadata = document["metadata"]
    metadata["datasetVersion"] = "stage2-expanded-2026-03-27"
    metadata["sourceName"] = "World Bank API + REST Countries + Wikidata"
    metadata["sourceUrl"] = "https://api.worldbank.org/v2/ , https://restcountries.com/ , https://query.wikidata.org/"
    metadata[
        "referenceNote"
    ] = (
        "referenceLatitude/referenceLongitude prefer REST Countries capitalInfo.latlng, then World Bank country "
        "latitude/longitude, then REST Countries latlng as fallback. population prefers World Bank latest value and "
        "falls back to REST Countries when unavailable. capitalCityKr uses Wikidata Korean labels and a small manual "
        "override set for dictionary-style Korean capital names and legacy seed capital choices."
    )

    SEED_PATH.write_text(
        json.dumps(document, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def main() -> int:
    try:
        sync_capital_city_kr()
    except Exception as exc:  # noqa: BLE001
        print(f"[sync_capital_city_kr] failed: {exc}", file=sys.stderr)
        return 1

    print(f"[sync_capital_city_kr] updated {SEED_PATH}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
