async function readJson(path) {
  const response = await fetch(path, { cache: "no-store" });
  if (!response.ok) {
    throw new Error(`Failed to load ${path}: ${response.status}`);
  }
  return response.json();
}

let cachedCatalogPromise = null;

export async function loadSharedCatalog() {
  if (!cachedCatalogPromise) {
    cachedCatalogPromise = Promise.all([
      readJson("/generated/data/countries.json"),
      readJson("/generated/data/flag-assets.json")
    ]).then(([countriesPayload, flagAssetsPayload]) => {
      const countries = Array.isArray(countriesPayload?.countries) ? countriesPayload.countries : [];
      const flagAssets = Array.isArray(flagAssetsPayload?.assets) ? flagAssetsPayload.assets : [];
      const continentSet = new Set(countries.map((country) => country.continent).filter(Boolean));

      return {
        countries,
        flagAssets,
        summary: {
          countryCount: countries.length,
          flagAssetCount: flagAssets.length,
          continentCount: continentSet.size,
          retainedGameCount: 4
        }
      };
    });
  }

  return cachedCatalogPromise;
}

export async function loadSharedSummary() {
  const catalog = await loadSharedCatalog();
  return catalog.summary;
}

export async function loadSharedCountries() {
  const catalog = await loadSharedCatalog();
  return catalog.countries;
}
