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
    const rankingPanels = Array.from(document.querySelectorAll("[data-ranking-panel]"));
    const modeButtons = Array.from(document.querySelectorAll("[data-ranking-mode]"));
    const scopeButtons = Array.from(document.querySelectorAll("[data-ranking-scope]"));
    const boardPanels = new Map(rankingPanels.map((panel) => [panel.dataset.rankingPanel, panel]));
    const boardBodies = new Map(
        rankingPanels
            .map((panel) => {
                const tbody = panel.querySelector("tbody[data-game-mode][data-scope]");
                return tbody ? [panel.dataset.rankingPanel, tbody] : null;
            })
            .filter(Boolean)
    );
    const boardRefreshMeta = new Map();
    const refreshIntervalMs = 15000;
    const initialRenderedAt = new Date();
    let activeMode = modeButtons.find((button) => button.classList.contains("is-active"))?.dataset.rankingMode ?? "location";
    let activeScope = scopeButtons.find((button) => button.classList.contains("is-active"))?.dataset.rankingScope ?? "ALL";
    let refreshTimer = null;
    let refreshInFlight = false;
    let queuedRefresh = null;

    rankingPanels.forEach((panel) => {
        boardRefreshMeta.set(
            panel.dataset.rankingPanel,
            panel.dataset.initialRendered === "true"
                ? {date: initialRenderedAt, prefix: "SSR 초기 로드"}
                : {date: null, prefix: "아직 불러오지 않음"}
        );
    });

    refreshButton?.addEventListener("click", () => queueRefresh(currentBoardKey(), true));
    window.addEventListener("visibilitychange", handleVisibilityChange);
    modeButtons.forEach((button) => button.addEventListener("click", () => switchMode(button.dataset.rankingMode)));
    scopeButtons.forEach((button) => button.addEventListener("click", () => switchScope(button.dataset.rankingScope)));
    syncActiveBoardUi();
    setRefreshState("현재 보드 15초 간격 ON");
    startAutoRefresh();

    function startAutoRefresh() {
        stopAutoRefresh();
        refreshTimer = window.setInterval(() => {
            if (document.visibilityState === "visible") {
                queueRefresh(currentBoardKey(), false);
            }
        }, refreshIntervalMs);
    }

    function stopAutoRefresh() {
        if (refreshTimer) {
            window.clearInterval(refreshTimer);
            refreshTimer = null;
        }
    }

    function queueRefresh(boardKey, manual) {
        if (!boardKey || !boardBodies.has(boardKey)) {
            return;
        }

        queuedRefresh = {
            boardKey,
            manual: Boolean(manual) || queuedRefresh?.manual === true
        };

        if (refreshInFlight) {
            setRefreshState(manual ? "현재 보드 수동 새로고침 대기..." : "현재 보드 자동 갱신 대기...");
            return;
        }

        void processQueuedRefresh();
    }

    async function processQueuedRefresh() {
        const nextRefresh = queuedRefresh;

        if (!nextRefresh) {
            return;
        }

        queuedRefresh = null;
        refreshInFlight = true;
        setRefreshButtonBusy(true);
        setRefreshState(nextRefresh.manual ? "현재 보드 수동 새로고침 중..." : "현재 보드 자동 갱신 중...");
        hideRankingMessage();

        try {
            const panel = boardPanels.get(nextRefresh.boardKey);
            const tbody = boardBodies.get(nextRefresh.boardKey);

            if (!panel || !tbody) {
                return;
            }

            const payload = await fetchLeaderboard(tbody.dataset.gameMode, tbody.dataset.scope);
            renderLeaderboardRows(tbody, payload.entries);
            syncPanelCopy(panel, payload);
            boardRefreshMeta.set(nextRefresh.boardKey, {
                date: new Date(),
                prefix: nextRefresh.manual ? "수동 갱신 완료" : "자동 갱신 완료"
            });
            syncActiveBoardUi();
            setRefreshState("현재 보드 15초 간격 ON");
        } catch (error) {
            if (nextRefresh.boardKey === currentBoardKey()) {
                setRefreshState("현재 보드 자동 갱신 오류");
                showRankingMessage(error instanceof Error ? error.message : "랭킹을 새로고침하지 못했습니다.");
            }
        } finally {
            refreshInFlight = false;
            setRefreshButtonBusy(false);

            if (queuedRefresh) {
                void processQueuedRefresh();
            }
        }
    }

    function switchMode(nextMode) {
        if (!nextMode || nextMode === activeMode) {
            return;
        }

        activeMode = nextMode;
        syncActiveBoardUi();
        queueRefresh(currentBoardKey(), false);
    }

    function switchScope(nextScope) {
        if (!nextScope || nextScope === activeScope) {
            return;
        }

        activeScope = nextScope;
        syncActiveBoardUi();
        queueRefresh(currentBoardKey(), false);
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

        syncLastUpdatedForActiveBoard();
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
            if (!date) {
                lastUpdatedBox.textContent = prefix;
                return;
            }

            lastUpdatedBox.textContent = `${prefix} · ${date.toLocaleTimeString("ko-KR", {hour12: false})}`;
        }
    }

    function syncLastUpdatedForActiveBoard() {
        const refreshMeta = boardRefreshMeta.get(currentBoardKey());

        if (!refreshMeta) {
            return;
        }

        setLastUpdated(refreshMeta.date, refreshMeta.prefix);
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
            queueRefresh(currentBoardKey(), false);
            startAutoRefresh();
            return;
        }

        stopAutoRefresh();
        setRefreshState("탭 비활성화로 현재 보드 자동 갱신 일시정지");
    }

    function currentBoardKey() {
        return `${activeMode}:${activeScope}`;
    }

    async function fetchLeaderboard(gameMode, scope) {
        const response = await fetch(`/api/rankings/${gameMode}?scope=${scope}&limit=10`, {
            cache: "no-store"
        });
        const payload = await readJsonSafely(response);

        if (!response.ok) {
            throw new Error(payload?.message || "랭킹을 새로고침하지 못했습니다.");
        }

        if (!payload || !Array.isArray(payload.entries)) {
            throw new Error("랭킹 응답 형식이 올바르지 않습니다.");
        }

        return payload;
    }

    async function readJsonSafely(response) {
        const contentType = response.headers.get("content-type") || "";

        if (!contentType.includes("application/json")) {
            return null;
        }

        try {
            return await response.json();
        } catch (error) {
            return null;
        }
    }

    function syncPanelCopy(panel, payload) {
        if (!panel || payload.scope !== "DAILY") {
            return;
        }

        const copyBase = panel.dataset.copyBase;

        if (!copyBase || !payload.targetDate) {
            return;
        }

        panel.dataset.copy = `${copyBase} 기준 날짜: ${payload.targetDate}`;
    }

    function setRefreshButtonBusy(isBusy) {
        if (!refreshButton) {
            return;
        }

        refreshButton.disabled = isBusy;
        refreshButton.textContent = isBusy ? "새로고침 중..." : "지금 새로고침";
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
