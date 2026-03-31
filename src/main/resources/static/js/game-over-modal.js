(function () {
    function createGameOverModalController(config) {
        const modal = config?.modal;
        const panel = config?.panel || modal?.querySelector(".game-over-modal__panel");
        const summaryTarget = config?.summaryTarget;
        const restartButton = config?.restartButton;
        const pageShell = config?.pageShell;
        const buildSummaryText = config?.buildSummaryText;

        function focusEntry() {
            (restartButton || panel)?.focus();
        }

        function getFocusableElements() {
            if (!modal) {
                return [];
            }

            return Array.from(modal.querySelectorAll("a[href], button:not([disabled]), [tabindex]:not([tabindex='-1'])"))
                .filter((element) => !element.hidden);
        }

        function handleKeydown(event) {
            if (!modal || modal.hidden) {
                return;
            }

            if (event.key === "Escape") {
                event.preventDefault();
                focusEntry();
                return;
            }

            if (event.key !== "Tab") {
                return;
            }

            const focusableElements = getFocusableElements();
            if (focusableElements.length === 0) {
                event.preventDefault();
                panel?.focus();
                return;
            }

            const firstElement = focusableElements[0];
            const lastElement = focusableElements[focusableElements.length - 1];

            if (event.shiftKey && document.activeElement === firstElement) {
                event.preventDefault();
                lastElement.focus();
                return;
            }

            if (!event.shiftKey && document.activeElement === lastElement) {
                event.preventDefault();
                firstElement.focus();
            }
        }

        function show(payload) {
            if (!modal) {
                return;
            }

            if (summaryTarget && typeof buildSummaryText === "function") {
                summaryTarget.textContent = buildSummaryText(payload);
            }

            modal.hidden = false;
            if (pageShell) {
                pageShell.inert = true;
            }

            document.removeEventListener("keydown", handleKeydown);
            document.addEventListener("keydown", handleKeydown);
            focusEntry();
        }

        function hide() {
            if (!modal) {
                return;
            }

            modal.hidden = true;
            if (pageShell) {
                pageShell.inert = false;
            }

            document.removeEventListener("keydown", handleKeydown);
        }

        return {
            show,
            hide
        };
    }

    window.createGameOverModalController = createGameOverModalController;
})();
