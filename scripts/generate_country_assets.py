#!/usr/bin/env python3

from __future__ import annotations

import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
COUNTRY_SEED_PATH = ROOT / "src/main/resources/data/countries.json"
LEVEL_ONE_WORLD_GEOJSON_PATH = ROOT / "src/main/resources/static/data/world-countries-level1.geojson"
ACTIVE_GEOJSON_PATH = ROOT / "src/main/resources/static/data/active-countries.geojson"
LOCATION_LEVEL_ONE_COUNTRY_LIMIT = 72

REST_COUNTRIES_CORE_URL = (
    "https://restcountries.com/v3.1/all"
    "?fields=cca2,cca3,name,translations,capital,region,subregion,independent,status,population"
)
REST_COUNTRIES_COORDINATE_URL = (
    "https://restcountries.com/v3.1/all"
    "?fields=cca3,capitalInfo,latlng"
)
WORLD_BANK_COUNTRY_URL = "https://api.worldbank.org/v2/country?format=json&per_page=400"
WORLD_BANK_POPULATION_URL = (
    "https://api.worldbank.org/v2/country/all/indicator/SP.POP.TOTL?format=json&mrnev=1&per_page=20000"
)

KOREAN_NAME_OVERRIDES = {
    "KOR": "대한민국",
    "PRK": "북한",
    "USA": "미국",
    "GBR": "영국",
    "CZE": "체코",
    "COD": "콩고민주공화국",
    "COG": "콩고공화국",
}

CONTINENT_BY_REGION = {
    "Africa": "AFRICA",
    "Asia": "ASIA",
    "Europe": "EUROPE",
    "Oceania": "OCEANIA",
}

NORTH_AMERICA_SUBREGIONS = {
    "North America",
    "Caribbean",
    "Central America",
}


def fetch_json(url: str):
    result = subprocess.run(
        ["curl", "-sL", url],
        check=True,
        capture_output=True,
        text=True,
    )
    return json.loads(result.stdout)


def to_float(value):
    if value in (None, ""):
        return None
    return float(value)


def signed_area(ring):
    area = 0.0
    points = ring if ring[0] == ring[-1] else ring + [ring[0]]

    for (x1, y1), (x2, y2) in zip(points, points[1:]):
        area += (x1 * y2) - (x2 * y1)

    return area / 2.0


def rewind_ring(ring, clockwise: bool):
    area = signed_area(ring)
    should_reverse = (area > 0 and clockwise) or (area < 0 and not clockwise)

    if should_reverse:
        return list(reversed(ring))

    return ring


def rewind_geometry(geometry: dict):
    geometry_type = geometry["type"]

    if geometry_type == "Polygon":
        return {
            "type": "Polygon",
            "coordinates": [
                rewind_ring(ring, clockwise=index == 0)
                for index, ring in enumerate(geometry["coordinates"])
            ],
        }

    if geometry_type == "MultiPolygon":
        return {
            "type": "MultiPolygon",
            "coordinates": [
                [
                    rewind_ring(ring, clockwise=index == 0)
                    for index, ring in enumerate(polygon)
                ]
                for polygon in geometry["coordinates"]
            ],
        }

    return geometry


def resolve_geojson_iso3(properties: dict) -> str | None:
    for field in ("ISO_A3", "ADM0_A3", "SOV_A3", "GU_A3", "ISO3166-1-Alpha-3"):
        value = properties.get(field)
        if value and value != "-99":
            return value

    return None


def to_continent(rest_country: dict) -> str:
    region = rest_country.get("region")
    subregion = rest_country.get("subregion")

    if region in CONTINENT_BY_REGION:
        return CONTINENT_BY_REGION[region]

    if region == "Americas":
        if subregion == "South America":
            return "SOUTH_AMERICA"
        if subregion in NORTH_AMERICA_SUBREGIONS:
            return "NORTH_AMERICA"

    raise ValueError(f"Unsupported region/subregion: {region=} {subregion=}")


def to_name_kr(rest_country: dict) -> str:
    iso3 = rest_country["cca3"]

    if iso3 in KOREAN_NAME_OVERRIDES:
        return KOREAN_NAME_OVERRIDES[iso3]

    translations = rest_country.get("translations", {})
    korean = translations.get("kor", {})

    return korean.get("common") or korean.get("official") or rest_country["name"]["common"]


def pick_capital(rest_country: dict, world_bank_country: dict | None) -> str:
    if world_bank_country and world_bank_country.get("capitalCity"):
        capital = world_bank_country["capitalCity"].strip()
        if capital:
            return capital

    capitals = rest_country.get("capital") or []
    if capitals:
        return capitals[0]

    return rest_country["name"]["common"]


def pick_reference_point(rest_country: dict, world_bank_country: dict | None):
    capital_info = rest_country.get("capitalInfo", {}).get("latlng") or []
    if len(capital_info) == 2:
        return round(float(capital_info[0]), 4), round(float(capital_info[1]), 4), "CAPITAL_CITY"

    if world_bank_country:
        latitude = to_float(world_bank_country.get("latitude"))
        longitude = to_float(world_bank_country.get("longitude"))
        if latitude is not None and longitude is not None:
            return round(latitude, 4), round(longitude, 4), "CAPITAL_CITY"

    latlng = rest_country.get("latlng") or []
    if len(latlng) == 2:
        return round(float(latlng[0]), 4), round(float(latlng[1]), 4), "CAPITAL_CITY"

    raise ValueError(f"No reference point for {rest_country['cca3']}")


def build_country_seed():
    rest_country_core_rows = fetch_json(REST_COUNTRIES_CORE_URL)
    rest_country_coordinate_rows = fetch_json(REST_COUNTRIES_COORDINATE_URL)
    world_bank_country_rows = fetch_json(WORLD_BANK_COUNTRY_URL)[1]
    world_bank_population_rows = fetch_json(WORLD_BANK_POPULATION_URL)[1]

    rest_country_coordinate_by_iso3 = {
        row["cca3"]: row for row in rest_country_coordinate_rows if row.get("cca3")
    }
    rest_countries = []
    for row in rest_country_core_rows:
        if not row.get("cca3"):
            continue
        coordinate_row = rest_country_coordinate_by_iso3.get(row["cca3"], {})
        merged = dict(row)
        merged["capitalInfo"] = coordinate_row.get("capitalInfo")
        merged["latlng"] = coordinate_row.get("latlng")
        rest_countries.append(merged)

    sovereign_countries = sorted(
        [
            country
            for country in rest_countries
            if country.get("status") == "officially-assigned" and country.get("independent") is True
        ],
        key=lambda country: country["cca3"],
    )

    world_bank_country_by_iso3 = {
        row["id"]: row
        for row in world_bank_country_rows
        if row.get("id")
    }

    world_bank_population_by_iso3 = {}
    for row in world_bank_population_rows:
        iso3 = row.get("countryiso3code")
        value = row.get("value")
        if iso3 and value is not None and iso3 not in world_bank_population_by_iso3:
            world_bank_population_by_iso3[iso3] = int(value)

    countries = []
    fallback_population_count = 0
    fallback_reference_count = 0

    for country in sovereign_countries:
        iso3 = country["cca3"]
        world_bank_country = world_bank_country_by_iso3.get(iso3)

        latitude, longitude, reference_type = pick_reference_point(country, world_bank_country)
        if not country.get("capitalInfo", {}).get("latlng") and (
            not world_bank_country or not world_bank_country.get("latitude") or not world_bank_country.get("longitude")
        ):
            fallback_reference_count += 1

        population = world_bank_population_by_iso3.get(iso3)
        if population is None:
            population = int(country["population"])
            fallback_population_count += 1

        countries.append(
            {
                "iso2Code": country["cca2"],
                "iso3Code": iso3,
                "nameKr": to_name_kr(country),
                "nameEn": country["name"]["common"],
                "continent": to_continent(country),
                "capitalCity": pick_capital(country, world_bank_country),
                "referenceLatitude": latitude,
                "referenceLongitude": longitude,
                "referenceType": reference_type,
                "population": population,
            }
        )

    document = {
        "metadata": {
            "datasetVersion": "stage2-expanded-2026-03-23",
            "sourceName": "World Bank API + REST Countries",
            "sourceUrl": "https://api.worldbank.org/v2/ , https://restcountries.com/",
            "populationIndicator": "SP.POP.TOTL",
            "populationYear": 2024,
            "referenceNote": (
                "referenceLatitude/referenceLongitude prefer REST Countries capitalInfo.latlng, "
                "then World Bank country latitude/longitude, then REST Countries latlng as fallback. "
                "population prefers World Bank latest value and falls back to REST Countries when unavailable."
            ),
        },
        "countries": countries,
    }

    COUNTRY_SEED_PATH.write_text(json.dumps(document, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return len(countries), fallback_population_count, fallback_reference_count


def build_active_geojson():
    seed_document = json.loads(COUNTRY_SEED_PATH.read_text(encoding="utf-8"))
    ranked_countries = sorted(
        seed_document["countries"],
        key=lambda country: (-country["population"], country["nameKr"])
    )
    active_iso3 = {country["iso3Code"] for country in ranked_countries[:LOCATION_LEVEL_ONE_COUNTRY_LIMIT]}
    world_geojson = json.loads(LEVEL_ONE_WORLD_GEOJSON_PATH.read_text(encoding="utf-8"))

    filtered_features = []
    for feature in world_geojson["features"]:
        properties = feature["properties"]
        iso3 = resolve_geojson_iso3(properties)
        name = properties.get("NAME_EN") or properties.get("name") or properties.get("NAME")

        if iso3 not in active_iso3:
            continue

        filtered_features.append(
            {
                "type": "Feature",
                "properties": {
                    "name": name,
                    "iso3Code": iso3,
                },
                "geometry": feature["geometry"],
            }
        )

    final_geojson = {"type": "FeatureCollection", "features": filtered_features}
    final_geojson["features"] = [
        {
            **feature,
            "geometry": rewind_geometry(feature["geometry"]),
        }
        for feature in final_geojson["features"]
    ]
    ACTIVE_GEOJSON_PATH.write_text(
        json.dumps(final_geojson, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8"
    )
    return len(filtered_features), len(final_geojson["features"])


def main():
    country_count, fallback_population_count, fallback_reference_count = build_country_seed()
    raw_feature_count, final_feature_count = build_active_geojson()

    print(f"Generated {country_count} country seed entries.")
    print(f"World Bank population fallback count: {fallback_population_count}")
    print(f"Reference coordinate fallback count: {fallback_reference_count}")
    print(f"Generated {raw_feature_count} raw GeoJSON features.")
    print(f"Generated {final_feature_count} simplified GeoJSON features.")


if __name__ == "__main__":
    main()
