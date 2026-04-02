(function () {
    const page = document.body.dataset.page;

    if (page === "population-start") {
        initStartPage();
    }

    if (page === "population-play") {
        initPlayPage();
    }
})();

function initStartPage() {
    const form = document.getElementById("population-start-form");
    const nicknameInput = document.getElementById("population-nickname");
    const messageBox = document.getElementById("population-start-message");
    const submitButton = document.getElementById("population-start-submit");
    const defaultButtonText = submitButton.textContent;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePopulationMessage(messageBox);
        submitButton.disabled = true;
        submitButton.textContent = "게임 준비 중...";
        showPopulationMessage(messageBox, "첫 번째 Stage와 입력 모드를 준비하는 중입니다. 잠시만 기다려주세요.", "info");

        try {
            const response = await fetch("/api/games/population/sessions", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    nickname: nicknameInput.value
                })
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 시작하지 못했습니다.");
            }

            window.location.assign(payload.playPageUrl);
        } catch (error) {
            submitButton.disabled = false;
            submitButton.textContent = defaultButtonText;
            showPopulationMessage(messageBox, error.message, "error");
        }
    });
}

function initPlayPage() {
    const STAGE_FEEDBACK_DELAY_MS = 950;
    const FINISH_REDIRECT_DELAY_MS = 1100;
    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("population-answer-form");
    const statusBox = document.getElementById("population-game-status");
    const countryName = document.getElementById("population-target-country-name");
    const yearLabel = document.getElementById("population-year");
    const optionsBox = document.getElementById("population-options");
    const feedback = document.getElementById("population-answer-feedback");
    const overlay = document.getElementById("population-stage-overlay");
    const messageBox = document.getElementById("population-play-message");
    const selectionLabel = document.getElementById("population-selection-label");
    const stageHint = document.getElementById("population-stage-hint");
    const submitButton = document.getElementById("population-submit-button");
    const gameOverModal = document.getElementById("population-game-over-modal");
    const gameOverPanel = gameOverModal?.querySelector(".game-over-modal__panel");
    const gameOverSummary = document.getElementById("population-game-over-summary");
    const restartButton = document.getElementById("population-restart-button");
    const pageShell = document.querySelector(".page-shell");
    const gameOverModalController = window.createGameOverModalController({
        modal: gameOverModal,
        panel: gameOverPanel,
        summaryTarget: gameOverSummary,
        restartButton,
        pageShell,
        buildSummaryText: (payload) =>
            `Stage ${payload.stageNumber}에서 탈락했습니다. 현재 총점 ${payload.totalScore}점, 다시 시작하면 같은 세션으로 Stage 1부터 이어집니다.`
    });

    let currentState = null;
    let interactionLocked = false;

    window.addEventListener("pageshow", gameOverModalController.hide);
    restartButton?.addEventListener("click", restartCurrentSession);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePopulationMessage(messageBox);

        if (!currentState) {
            showPopulationMessage(messageBox, "현재 Stage를 아직 불러오지 못했습니다.", "error");
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
            const response = await fetch(`/api/games/population/sessions/${sessionId}/answer`, {
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
                        showPopulationMessage(messageBox, error.message, "error");
                    });
                }, STAGE_FEEDBACK_DELAY_MS);
                return;
            }

            hidePopulationFeedback(feedback);
            renderStageOverlay(
                overlay,
                payload.outcome === "GAME_OVER" ? "탈락" : "오답",
                payload.outcome === "GAME_OVER" ? "하트를 모두 잃었습니다" : `하트 ${payload.livesRemaining}개 남음`,
                "danger"
            );
            setSelectionState(wrongSelectionSummary(payload));
            setStageHint(
                payload.outcome === "GAME_OVER"
                    ? "하트를 모두 잃었습니다. 다음 행동을 선택하세요."
                    : "오답입니다. 잠시 뒤 같은 Stage를 다시 추정할 수 있습니다."
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
            showPopulationMessage(messageBox, error.message, "error");
        }
    });

    showPopulationMessage(messageBox, "Stage와 입력 모드를 불러오는 중입니다.", "info");
    hideGameOverModal();
    loadState()
        .then(() => hidePopulationMessage(messageBox))
        .catch((error) => showPopulationMessage(messageBox, error.message, "error"));

    async function loadState() {
        const response = await fetch(`/api/games/population/sessions/${sessionId}/state`, {
            cache: "no-store"
        });
        const payload = await response.json();

        if (!response.ok) {
            throw new Error(payload.message || "현재 Stage를 불러오지 못했습니다.");
        }

        currentState = payload;
        renderQuestion(payload);
        renderStatus(statusBox, payload);
        configureAnswerMode(payload);
        renderOptions(optionsBox, payload.options || []);
        hidePopulationFeedback(feedback);
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
            const response = await fetch(`/api/games/population/sessions/${sessionId}/restart`, {
                method: "POST"
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 다시 시작하지 못했습니다.");
            }

            hideGameOverModal();
            hidePopulationFeedback(feedback);
            overlay.hidden = true;
            showPopulationMessage(messageBox, "같은 세션을 Stage 1부터 다시 시작했습니다.", "success");
            await loadState();
            focusFirstPlayableOption();
        } catch (error) {
            showPopulationMessage(messageBox, error.message, "error");
        } finally {
            restartButton.disabled = false;
        }
    }

    function createAnswerPayload() {
        const selectedOption = form.querySelector("input[name='population-option']:checked");
        if (!selectedOption) {
            showPopulationMessage(messageBox, "보기 하나를 먼저 선택해주세요.", "error");
            return null;
        }

        return {
            stageNumber: currentState.stageNumber,
            stageId: currentState.stageId,
            expectedAttemptNumber: currentState.expectedAttemptNumber,
            selectedOptionNumber: Number(selectedOption.value)
        };
    }

    function configureAnswerMode(payload) {
        optionsBox.hidden = false;
    }

    function renderQuestion(payload) {
        countryName.textContent = payload.targetCountryName;
        yearLabel.textContent = `기준 연도: ${payload.populationYear} · 가장 가까운 인구 규모 구간을 고르세요.`;
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
                <span class="subtitle">누적 점수</span>
                <strong>${payload.totalScore}</strong>
            </article>
            <article class="stat-card">
                <span class="subtitle">클리어 수</span>
                <strong>${payload.clearedStageCount}</strong>
            </article>
            <article class="stat-card">
                <span class="subtitle">하트</span>
                <strong class="heart-row">${renderHearts(payload.livesRemaining)}</strong>
            </article>
        `;
    }

    function renderOptions(target, options) {
        target.innerHTML = options.map((option) => `
            <label class="option-card" data-option-number="${option.optionNumber}" data-option-label="${option.label}">
                <input type="radio" name="population-option" value="${option.optionNumber}" data-option-label="${option.label}">
                <span class="subtitle">Choice ${option.optionNumber}</span>
                <strong>${option.label}</strong>
            </label>
        `).join("");

        target.querySelectorAll("input[name='population-option']").forEach((input) => {
            input.addEventListener("change", () => {
                target.querySelectorAll(".option-card").forEach((card) => {
                    card.classList.toggle("is-selected", card.dataset.optionNumber === input.value);
                });
                setSelectionState(`선택 중: ${input.dataset.optionLabel}`);
                setStageHint("선택 제출을 누르면 서버가 정답 여부와 점수를 판정합니다.");
                submitButton.disabled = interactionLocked || !canSubmitCurrentAnswer();
            });
        });
    }

    function renderFeedback(target, payload) {
        target.hidden = false;

        if (payload.correct) {
            target.innerHTML = `
                <h3>${payload.targetCountryName} 정답</h3>
                <p>획득 점수: ${payload.awardedScore}</p>
            `;
            return;
        }

        target.innerHTML = `
            <h3>${payload.targetCountryName} 결과</h3>
            <p>내 선택 구간: ${payload.selectedOptionLabel}</p>
            <p>정답 구간: ${payload.correctOptionLabel}</p>
            <p>실제 인구: ${formatPopulation(payload.correctPopulation)}</p>
            <p>획득 점수: ${payload.awardedScore}</p>
            <p>현재 총점: ${payload.totalScore}</p>
        `;
    }

    function hidePopulationFeedback(target) {
        target.hidden = true;
        target.innerHTML = "";
    }

    function clearAnswerInput() {
        optionsBox.querySelectorAll("input[name='population-option']").forEach((input) => {
            input.checked = false;
            input.disabled = false;
        });

        optionsBox.querySelectorAll(".option-card").forEach((card) => {
            card.classList.remove("is-selected");
        });

        submitButton.disabled = true;
    }

    function resetHudGuidance(payload) {
        setSelectionState("아직 선택하지 않았습니다.");
        setStageHint(`${payload.difficultyLabel} 구간입니다. 가장 가까운 인구 규모대를 고른 뒤 제출하세요.`);
    }

    function wrongSelectionSummary(payload) {
        return `직전 선택: ${payload.selectedOptionLabel}`;
    }

    function canSubmitCurrentAnswer() {
        if (!currentState) {
            return false;
        }
        return Boolean(form.querySelector("input[name='population-option']:checked"));
    }

    function setSelectionState(text) {
        if (selectionLabel) {
            selectionLabel.textContent = text;
        }
    }

    function setStageHint(text) {
        if (stageHint) {
            stageHint.textContent = text;
        }
    }

    function renderStageOverlay(target, title, detail, tone) {
        target.hidden = false;
        target.dataset.tone = tone;
        target.innerHTML = `<strong>${title}</strong><span>${detail}</span>`;
    }

    function renderHearts(livesRemaining) {
        return Array.from({length: 3}, (_, index) => {
            const active = index < livesRemaining;
            return `<span class="heart ${active ? "is-active" : "is-empty"}">♥</span>`;
        }).join("");
    }

    function showGameOverModal(payload) {
        gameOverModalController.show(payload);
    }

    function hideGameOverModal() {
        gameOverModalController.hide();
    }

    function lockInteraction(locked) {
        interactionLocked = locked;
        submitButton.disabled = locked || !canSubmitCurrentAnswer();
        optionsBox.querySelectorAll("input[name='population-option']").forEach((input) => {
            input.disabled = locked;
        });
    }

    function focusFirstPlayableOption() {
        optionsBox.querySelector("input[name='population-option']:not([disabled])")?.focus();
    }
}

function formatPopulation(population) {
    return `${Number(population).toLocaleString()}명`;
}

function showPopulationMessage(target, message, tone = "info") {
    target.hidden = false;
    target.textContent = message;
    target.dataset.tone = tone;
}

function hidePopulationMessage(target) {
    target.hidden = true;
    target.textContent = "";
    delete target.dataset.tone;
}
