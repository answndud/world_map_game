(function () {
    if (document.body.dataset.page !== "ranking") {
        return;
    }

    const refreshButton = document.getElementById("ranking-refresh-button");
    const statusBox = document.getElementById("ranking-auto-refresh-status");
    const lastUpdatedBox = document.getElementById("ranking-last-updated");
    const messageBox = document.getElementById("ranking-refresh-message");
    const tableBodies = Array.from(document.querySelectorAll("tbody[data-game-mode][data-scope]"));
    const refreshIntervalMs = 15000;
    let refreshTimer = null;
    let refreshInFlight = false;

    refreshButton?.addEventListener("click", () => refreshLeaderboards(true));
    window.addEventListener("visibilitychange", handleVisibilityChange);

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
        messageBox.textContent = message;
    }

    function hideRankingMessage() {
        if (!messageBox) {
            return;
        }

        messageBox.hidden = true;
        messageBox.textContent = "";
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

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }
})();
