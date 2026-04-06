(function () {
    var storageKey = "worldmap-theme";

    function normalizeTheme(value) {
        return value === "dark" ? "dark" : "light";
    }

    function readStoredTheme() {
        try {
            return window.localStorage.getItem(storageKey);
        } catch (error) {
            return null;
        }
    }

    function storeTheme(theme) {
        try {
            window.localStorage.setItem(storageKey, theme);
        } catch (error) {
            // Ignore storage failures and keep the theme applied in-memory.
        }
    }

    function updateButtons(theme) {
        document.querySelectorAll("[data-theme-toggle]").forEach(function (button) {
            var label = button.querySelector("[data-theme-toggle-label]");
            if (label) {
                label.textContent = theme === "light" ? "라이트" : "다크";
            }

            button.setAttribute("aria-pressed", theme === "light" ? "true" : "false");
            button.setAttribute("title", theme === "dark" ? "현재 다크 모드, 클릭하면 라이트 모드로 전환" : "현재 라이트 모드, 클릭하면 다크 모드로 전환");
        });
    }

    function applyTheme(theme) {
        var normalizedTheme = normalizeTheme(theme);
        document.documentElement.dataset.theme = normalizedTheme;
        updateButtons(normalizedTheme);
    }

    function toggleTheme() {
        var currentTheme = normalizeTheme(document.documentElement.dataset.theme || readStoredTheme());
        var nextTheme = currentTheme === "dark" ? "light" : "dark";

        storeTheme(nextTheme);
        applyTheme(nextTheme);
    }

    function init() {
        var storedTheme = normalizeTheme(readStoredTheme());
        applyTheme(storedTheme);

        document.querySelectorAll("[data-theme-toggle]").forEach(function (button) {
            button.addEventListener("click", toggleTheme);
        });

        window.addEventListener("storage", function (event) {
            if (event.key === storageKey) {
                applyTheme(normalizeTheme(event.newValue));
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
}());
