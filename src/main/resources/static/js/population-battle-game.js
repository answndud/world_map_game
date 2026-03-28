(function () {
    const page = document.body.dataset.page;

    if (page === "population-battle-start") {
        initPopulationBattleStartPage();
    }

    if (page === "population-battle-play") {
        initPopulationBattlePlayPage();
    }
})();

function initPopulationBattleStartPage() {
    const form = document.getElementById("population-battle-start-form");
    const nicknameInput = document.getElementById("population-battle-nickname");
    const messageBox = document.getElementById("population-battle-start-message");
    const submitButton = document.getElementById("population-battle-start-submit");
    const defaultButtonText = submitButton.textContent;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePopulationBattleMessage(messageBox);
        submitButton.disabled = true;
        submitButton.textContent = "게임 준비 중...";
        showPopulationBattleMessage(messageBox, "첫 번째 비교 Stage를 준비하는 중입니다. 잠시만 기다려주세요.", "info");

        try {
            const response = await fetch("/api/games/population-battle/sessions", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({nickname: nicknameInput.value})
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 시작하지 못했습니다.");
            }

            window.location.assign(payload.playPageUrl);
        } catch (error) {
            submitButton.disabled = false;
            submitButton.textContent = defaultButtonText;
            showPopulationBattleMessage(messageBox, error.message, "error");
        }
    });
}

function initPopulationBattlePlayPage() {
    const STAGE_FEEDBACK_DELAY_MS = 950;
    const FINISH_REDIRECT_DELAY_MS = 1100;
    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("population-battle-answer-form");
    const statusBox = document.getElementById("population-battle-game-status");
    const stageCopy = document.getElementById("population-battle-stage-copy");
    const optionsBox = document.getElementById("population-battle-options");
    const feedback = document.getElementById("population-battle-answer-feedback");
    const overlay = document.getElementById("population-battle-stage-overlay");
    const messageBox = document.getElementById("population-battle-play-message");
    const selectionLabel = document.getElementById("population-battle-selection-label");
    const stageHint = document.getElementById("population-battle-stage-hint");
    const submitButton = document.getElementById("population-battle-submit-button");
    const gameOverModal = document.getElementById("population-battle-game-over-modal");
    const gameOverSummary = document.getElementById("population-battle-game-over-summary");
    const restartButton = document.getElementById("population-battle-restart-button");

    let currentState = null;
    let interactionLocked = false;

    window.addEventListener("pageshow", hideGameOverModal);
    restartButton?.addEventListener("click", restartCurrentSession);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePopulationBattleMessage(messageBox);

        if (!currentState) {
            showPopulationBattleMessage(messageBox, "현재 Stage를 아직 불러오지 못했습니다.", "error");
            return;
        }

        if (interactionLocked) {
            return;
        }

        const answerPayload = createAnswerPayload();
        if (!answerPayload) {
            return;
        }

        try {
            lockInteraction(true);
            const response = await fetch(`/api/games/population-battle/sessions/${sessionId}/answer`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(answerPayload)
            });
            const payload = await response.json();

            if (!response.ok) {
                lockInteraction(false);
                throw new Error(payload.message || "정답 제출에 실패했습니다.");
            }

            renderStatus(statusBox, {
                stageNumber: payload.nextStageNumber || currentState.stageNumber,
                difficultyLabel: payload.nextDifficultyLabel || currentState.difficultyLabel,
                clearedStageCount: payload.clearedStageCount,
                totalScore: payload.totalScore,
                livesRemaining: payload.livesRemaining
            });

            if (payload.correct) {
                renderFeedback(feedback, payload);
                renderStageOverlay(overlay, "정답", `+${payload.awardedScore}`, "success");
                setSelectionState("정답 처리 완료");
                setStageHint("정답입니다. 잠시 뒤 다음 Stage로 자동 이동합니다.");

                if (payload.outcome === "FINISHED") {
                    setTimeout(() => {
                        window.location.href = payload.resultPageUrl;
                    }, FINISH_REDIRECT_DELAY_MS);
                    return;
                }

                setTimeout(() => {
                    loadState().catch((error) => {
                        lockInteraction(false);
                        showPopulationBattleMessage(messageBox, error.message, "error");
                    });
                }, STAGE_FEEDBACK_DELAY_MS);
                return;
            }

            hidePopulationBattleFeedback(feedback);
            renderStageOverlay(
                overlay,
                payload.outcome === "GAME_OVER" ? "탈락" : "오답",
                payload.outcome === "GAME_OVER" ? "하트를 모두 잃었습니다" : `하트 ${payload.livesRemaining}개 남음`,
                "danger"
            );
            setSelectionState(`직전 선택: ${payload.selectedCountryName}`);
            setStageHint(
                payload.outcome === "GAME_OVER"
                    ? "하트를 모두 잃었습니다. 다음 행동을 선택하세요."
                    : "오답입니다. 잠시 뒤 같은 Stage에서 다시 더 큰 인구의 나라를 고를 수 있습니다."
            );

            if (payload.outcome === "GAME_OVER") {
                lockInteraction(true);
                showGameOverModal(payload);
                return;
            }

            currentState = {
                ...currentState,
                totalScore: payload.totalScore,
                livesRemaining: payload.livesRemaining,
                clearedStageCount: payload.clearedStageCount
            };

            setTimeout(() => {
                overlay.hidden = true;
                clearAnswerInput();
                resetHudGuidance(currentState);
                lockInteraction(false);
            }, STAGE_FEEDBACK_DELAY_MS);
        } catch (error) {
            lockInteraction(false);
            showPopulationBattleMessage(messageBox, error.message, "error");
        }
    });

    showPopulationBattleMessage(messageBox, "Stage와 좌우 비교 보기를 불러오는 중입니다.", "info");
    hideGameOverModal();
    loadState()
        .then(() => hidePopulationBattleMessage(messageBox))
        .catch((error) => showPopulationBattleMessage(messageBox, error.message, "error"));

    async function loadState() {
        const response = await fetch(`/api/games/population-battle/sessions/${sessionId}/state`, {
            cache: "no-store"
        });
        const payload = await response.json();

        if (!response.ok) {
            throw new Error(payload.message || "현재 Stage를 불러오지 못했습니다.");
        }

        currentState = payload;
        renderQuestion(payload);
        renderStatus(statusBox, payload);
        renderOptions(optionsBox, payload.options || []);
        hidePopulationBattleFeedback(feedback);
        overlay.hidden = true;
        hideGameOverModal();
        clearAnswerInput();
        lockInteraction(false);
        submitButton.disabled = true;
        resetHudGuidance(payload);
    }

    async function restartCurrentSession() {
        try {
            restartButton.disabled = true;
            const response = await fetch(`/api/games/population-battle/sessions/${sessionId}/restart`, {
                method: "POST"
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 다시 시작하지 못했습니다.");
            }

            hideGameOverModal();
            hidePopulationBattleFeedback(feedback);
            overlay.hidden = true;
            showPopulationBattleMessage(messageBox, "같은 세션을 Stage 1부터 다시 시작했습니다.", "success");
            await loadState();
        } catch (error) {
            showPopulationBattleMessage(messageBox, error.message, "error");
        } finally {
            restartButton.disabled = false;
        }
    }

    function createAnswerPayload() {
        const selectedOption = form.querySelector("input[name='population-battle-option']:checked");
        if (!selectedOption) {
            showPopulationBattleMessage(messageBox, "보기 하나를 먼저 선택해주세요.", "error");
            return null;
        }

        return {
            stageNumber: currentState.stageNumber,
            selectedOptionNumber: Number(selectedOption.value)
        };
    }

    function renderQuestion(payload) {
        stageCopy.textContent = payload.questionPrompt;
    }

    function renderStatus(target, payload) {
        target.innerHTML = `
            <article class="stat-card">
                <span class="subtitle">현재 Stage</span>
                <strong>${payload.stageNumber}</strong>
            </article>
            <article class="stat-card">
                <span class="subtitle">난이도</span>
                <strong>${payload.difficultyLabel}</strong>
            </article>
            <article class="stat-card">
                <span class="subtitle">점수</span>
                <strong>${payload.totalScore}</strong>
            </article>
            <article class="stat-card">
                <span class="subtitle">클리어</span>
                <strong>${payload.clearedStageCount}</strong>
            </article>
            <article class="stat-card">
                <span class="subtitle">남은 하트</span>
                <strong>${payload.livesRemaining}</strong>
            </article>
        `;
    }

    function renderOptions(target, options) {
        target.innerHTML = options.map((option) => `
            <label class="option-card" data-option-number="${option.optionNumber}" data-option-label="${escapeHtml(option.countryName)}">
                <input type="radio" name="population-battle-option" value="${option.optionNumber}" data-option-label="${escapeHtml(option.countryName)}">
                <span class="subtitle">선택 ${option.optionNumber}</span>
                <strong>${escapeHtml(option.countryName)}</strong>
            </label>
        `).join("");

        target.querySelectorAll("input[name='population-battle-option']").forEach((input) => {
            input.addEventListener("change", () => {
                setSelectionState(`현재 선택: ${input.dataset.optionLabel}`);
                submitButton.disabled = false;
            });
        });
    }

    function renderFeedback(target, payload) {
        target.hidden = false;

        if (payload.correct) {
            target.innerHTML = `
                <h3>정답 처리 완료</h3>
                <p>획득 점수: +${payload.awardedScore}</p>
            `;
            return;
        }

        target.innerHTML = `
            <h3>정답 기록</h3>
            <p>내 선택: ${escapeHtml(payload.selectedCountryName)} (${formatPopulation(payload.selectedCountryPopulation)})</p>
            <p>정답: ${escapeHtml(payload.correctCountryName)} (${formatPopulation(payload.correctCountryPopulation)})</p>
            <p>획득 점수: +${payload.awardedScore}</p>
        `;
    }

    function hidePopulationBattleFeedback(target) {
        target.hidden = true;
        target.innerHTML = "";
    }

    function renderStageOverlay(target, title, copy, tone) {
        target.hidden = false;
        target.dataset.tone = tone;
        target.innerHTML = `<strong>${escapeHtml(title)}</strong><span>${escapeHtml(copy)}</span>`;
    }

    function clearAnswerInput() {
        optionsBox.querySelectorAll("input[name='population-battle-option']").forEach((input) => {
            input.checked = false;
            input.closest(".option-card")?.classList.remove("is-selected");
        });
        submitButton.disabled = true;
    }

    function lockInteraction(locked) {
        interactionLocked = locked;
        submitButton.disabled = locked || !hasCurrentSelection();
        optionsBox.querySelectorAll("input[name='population-battle-option']").forEach((input) => {
            input.disabled = locked;
        });
    }

    function setSelectionState(text) {
        selectionLabel.textContent = text;
        optionsBox.querySelectorAll("input[name='population-battle-option']").forEach((input) => {
            input.closest(".option-card")?.classList.toggle("is-selected", input.checked);
        });
    }

    function setStageHint(text) {
        stageHint.textContent = text;
    }

    function resetHudGuidance(payload) {
        setSelectionState("좌우 보기 중 하나를 고르면 여기서 확인합니다.");
        setStageHint(payload.questionPrompt);
    }

    function showGameOverModal(payload) {
        gameOverSummary.textContent = `하트를 모두 잃었습니다. 최종 점수 ${payload.totalScore}점으로 종료되었습니다.`;
        gameOverModal.hidden = false;
    }

    function hideGameOverModal() {
        if (gameOverModal) {
            gameOverModal.hidden = true;
        }
    }

    function hasCurrentSelection() {
        return Boolean(form.querySelector("input[name='population-battle-option']:checked"));
    }
}

function showPopulationBattleMessage(target, message, tone = "info") {
    target.hidden = false;
    target.dataset.tone = tone;
    target.textContent = message;
}

function hidePopulationBattleMessage(target) {
    target.hidden = true;
    target.textContent = "";
}

function formatPopulation(value) {
    if (value == null) {
        return "-";
    }

    return new Intl.NumberFormat("ko-KR").format(value) + "명";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
