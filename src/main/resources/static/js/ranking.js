(function () {
    if (document.body.dataset.page !== "ranking") {
        return;
    }

    const refreshButton = document.getElementById("ranking-refresh-button");
    const statusBox = document.getElementById("ranking-auto-refresh-status");
    const lastUpdatedBox = document.getElementById("ranking-last-updated");
    const messageBox = document.getElementById("ranking-refresh-message");
    const activeTitleBox = document.getElementById("ranking-active-title");
    const activeCopyBox = document.getElementById("ranking-active-copy");
    const tableBodies = Array.from(document.querySelectorAll("tbody[data-game-mode][data-scope]"));
    const rankingPanels = Array.from(document.querySelectorAll("[data-ranking-panel]"));
    const modeButtons = Array.from(document.querySelectorAll("[data-ranking-mode]"));
    const scopeButtons = Array.from(document.querySelectorAll("[data-ranking-scope]"));
    const refreshIntervalMs = 15000;
    let activeMode = modeButtons.find((button) => button.classList.contains("is-active"))?.dataset.rankingMode ?? "location";
    let activeScope = scopeButtons.find((button) => button.classList.contains("is-active"))?.dataset.rankingScope ?? "ALL";
    let refreshTimer = null;
    let refreshInFlight = false;

    refreshButton?.addEventListener("click", () => refreshLeaderboards(true));
    window.addEventListener("visibilitychange", handleVisibilityChange);
    modeButtons.forEach((button) => button.addEventListener("click", () => switchMode(button.dataset.rankingMode)));
    scopeButtons.forEach((button) => button.addEventListener("click", () => switchScope(button.dataset.rankingScope)));
    syncActiveBoardUi();
    setLastUpdated(new Date(), "SSR 초기 로드");
    startAutoRefresh();

    function startAutoRefresh() {
        stopAutoRefresh();
        refreshTimer = window.setInterval(() => {
            if (document.visibilityState === "visible") {
                refreshLeaderboards(false);
            }
        }, refreshIntervalMs);
    }

    function stopAutoRefresh() {
        if (refreshTimer) {
            window.clearInterval(refreshTimer);
            refreshTimer = null;
        }
    }

    async function refreshLeaderboards(manual) {
        if (refreshInFlight) {
            return;
        }

        refreshInFlight = true;
        setRefreshState(manual ? "수동 새로고침 중..." : "자동 갱신 중...");
        hideRankingMessage();

        try {
            const responses = await Promise.all(tableBodies.map(async (tbody) => {
                const gameMode = tbody.dataset.gameMode;
                const scope = tbody.dataset.scope;
                const response = await fetch(`/api/rankings/${gameMode}?scope=${scope}&limit=10`, {
                    cache: "no-store"
                });
                const payload = await response.json();

                if (!response.ok) {
                    throw new Error(payload.message || "랭킹을 새로고침하지 못했습니다.");
                }

                return {tbody, payload};
            }));

            responses.forEach(({tbody, payload}) => renderLeaderboardRows(tbody, payload.entries));
            setLastUpdated(new Date(), manual ? "수동 갱신 완료" : "자동 갱신 완료");
            setRefreshState("15초 간격 ON");
        } catch (error) {
            setRefreshState("자동 갱신 오류");
            showRankingMessage(error.message);
        } finally {
            refreshInFlight = false;
        }
    }

    function switchMode(nextMode) {
        if (!nextMode || nextMode === activeMode) {
            return;
        }

        activeMode = nextMode;
        syncActiveBoardUi();
    }

    function switchScope(nextScope) {
        if (!nextScope || nextScope === activeScope) {
            return;
        }

        activeScope = nextScope;
        syncActiveBoardUi();
    }

    function syncActiveBoardUi() {
        const activeKey = currentBoardKey();

        modeButtons.forEach((button) => {
            const isActive = button.dataset.rankingMode === activeMode;
            button.classList.toggle("is-active", isActive);
            button.setAttribute("aria-pressed", String(isActive));
        });

        scopeButtons.forEach((button) => {
            const isActive = button.dataset.rankingScope === activeScope;
            button.classList.toggle("is-active", isActive);
            button.setAttribute("aria-pressed", String(isActive));
        });

        rankingPanels.forEach((panel) => {
            const isActive = panel.dataset.rankingPanel === activeKey;
            panel.hidden = !isActive;

            if (isActive) {
                if (activeTitleBox) {
                    activeTitleBox.textContent = panel.dataset.title || "실시간 랭킹";
                }

                if (activeCopyBox) {
                    activeCopyBox.textContent = panel.dataset.copy || "";
                }
            }
        });
    }

    function renderLeaderboardRows(tbody, entries) {
        if (!entries.length) {
            tbody.innerHTML = `<tr><td colspan="5">아직 기록이 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = entries.map((entry) => `
            <tr>
                <td>${entry.rank}</td>
                <td>${escapeHtml(entry.playerNickname)}</td>
                <td>${entry.totalScore}</td>
                <td>${entry.clearedStageCount}</td>
                <td>${entry.totalAttemptCount}</td>
            </tr>
        `).join("");
    }

    function setRefreshState(text) {
        if (statusBox) {
            statusBox.textContent = text;
        }
    }

    function setLastUpdated(date, prefix) {
        if (lastUpdatedBox) {
            lastUpdatedBox.textContent = `${prefix} · ${date.toLocaleTimeString("ko-KR", {hour12: false})}`;
        }
    }

    function showRankingMessage(message) {
        if (!messageBox) {
            return;
        }

        messageBox.hidden = false;
        messageBox.dataset.tone = "error";
        messageBox.textContent = message;
    }

    function hideRankingMessage() {
        if (!messageBox) {
            return;
        }

        messageBox.hidden = true;
        messageBox.textContent = "";
        delete messageBox.dataset.tone;
    }

    function handleVisibilityChange() {
        if (document.visibilityState === "visible") {
            refreshLeaderboards(false);
            startAutoRefresh();
            return;
        }

        stopAutoRefresh();
        setRefreshState("탭 비활성화로 자동 갱신 일시정지");
    }

    function currentBoardKey() {
        return `${activeMode}:${activeScope}`;
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }
})();
