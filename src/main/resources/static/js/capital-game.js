(function () {
    const page = document.body.dataset.page;

    if (page === "capital-start") {
        initStartPage();
    }

    if (page === "capital-play") {
        initPlayPage();
    }
})();

function initStartPage() {
    const form = document.getElementById("capital-start-form");
    const nicknameInput = document.getElementById("capital-nickname");
    const messageBox = document.getElementById("capital-start-message");
    const submitButton = document.getElementById("capital-start-submit");
    const defaultButtonText = submitButton.textContent;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideCapitalMessage(messageBox);
        submitButton.disabled = true;
        submitButton.textContent = "게임 준비 중...";
        showCapitalMessage(messageBox, "첫 번째 Stage와 보기 구성을 준비하는 중입니다. 잠시만 기다려주세요.", "info");

        try {
            const response = await fetch("/api/games/capital/sessions", {
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
            showCapitalMessage(messageBox, error.message, "error");
        }
    });
}

function initPlayPage() {
    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("capital-answer-form");
    const statusBox = document.getElementById("capital-game-status");
    const countryName = document.getElementById("capital-target-country-name");
    const stageCopy = document.getElementById("capital-stage-copy");
    const optionsBox = document.getElementById("capital-options");
    const feedback = document.getElementById("capital-answer-feedback");
    const overlay = document.getElementById("capital-stage-overlay");
    const messageBox = document.getElementById("capital-play-message");
    const selectionLabel = document.getElementById("capital-selection-label");
    const stageHint = document.getElementById("capital-stage-hint");
    const submitButton = document.getElementById("capital-submit-button");
    const nextStageButton = document.getElementById("capital-next-stage-button");
    const gameOverModal = document.getElementById("capital-game-over-modal");
    const gameOverSummary = document.getElementById("capital-game-over-summary");
    const restartButton = document.getElementById("capital-restart-button");

    let currentState = null;
    let interactionLocked = false;

    window.addEventListener("pageshow", hideGameOverModal);
    restartButton?.addEventListener("click", restartCurrentSession);
    nextStageButton?.addEventListener("click", () => {
        hideCapitalMessage(messageBox);
        nextStageButton.disabled = true;
        loadState()
            .catch((error) => showCapitalMessage(messageBox, error.message, "error"))
            .finally(() => {
                nextStageButton.disabled = false;
            });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideCapitalMessage(messageBox);

        if (!currentState) {
            showCapitalMessage(messageBox, "현재 Stage를 아직 불러오지 못했습니다.", "error");
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
            const response = await fetch(`/api/games/capital/sessions/${sessionId}/answer`, {
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
                setSelectionState(`선택 완료: ${payload.selectedCapitalCity}`);
                setStageHint("정답입니다. 결과를 확인한 뒤 다음 Stage 버튼으로 직접 넘어가세요.");

                if (payload.outcome === "FINISHED") {
                    setTimeout(() => {
                        window.location.href = payload.resultPageUrl;
                    }, 1100);
                    return;
                }

                showNextStageAction();
                return;
            }

            hideCapitalFeedback(feedback);
            renderStageOverlay(
                overlay,
                payload.outcome === "GAME_OVER" ? "탈락" : "오답",
                payload.outcome === "GAME_OVER" ? "하트를 모두 잃었습니다" : `하트 ${payload.livesRemaining}개 남음`,
                "danger"
            );
            setSelectionState(`직전 선택: ${payload.selectedCapitalCity}`);
            setStageHint(
                payload.outcome === "GAME_OVER"
                    ? "하트를 모두 잃었습니다. 다음 행동을 선택하세요."
                    : "오답입니다. 같은 Stage에서 다시 수도를 골라보세요."
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
            }, 950);
        } catch (error) {
            lockInteraction(false);
            showCapitalMessage(messageBox, error.message, "error");
        }
    });

    showCapitalMessage(messageBox, "Stage와 수도 보기 구성을 불러오는 중입니다.", "info");
    hideGameOverModal();
    loadState()
        .then(() => hideCapitalMessage(messageBox))
        .catch((error) => showCapitalMessage(messageBox, error.message, "error"));

    async function loadState() {
        const response = await fetch(`/api/games/capital/sessions/${sessionId}/state`, {
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
        hideCapitalFeedback(feedback);
        overlay.hidden = true;
        hideNextStageAction();
        hideGameOverModal();
        clearAnswerInput();
        lockInteraction(false);
        submitButton.disabled = true;
        resetHudGuidance(payload);
    }

    async function restartCurrentSession() {
        try {
            restartButton.disabled = true;
            const response = await fetch(`/api/games/capital/sessions/${sessionId}/restart`, {
                method: "POST"
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 다시 시작하지 못했습니다.");
            }

            hideGameOverModal();
            hideCapitalFeedback(feedback);
            overlay.hidden = true;
            hideNextStageAction();
            showCapitalMessage(messageBox, "같은 세션을 Stage 1부터 다시 시작했습니다.", "success");
            await loadState();
        } catch (error) {
            showCapitalMessage(messageBox, error.message, "error");
        } finally {
            restartButton.disabled = false;
        }
    }

    function createAnswerPayload() {
        const selectedOption = form.querySelector("input[name='capital-option']:checked");
        if (!selectedOption) {
            showCapitalMessage(messageBox, "보기 하나를 먼저 선택해주세요.", "error");
            return null;
        }

        return {
            stageNumber: currentState.stageNumber,
            selectedOptionNumber: Number(selectedOption.value)
        };
    }

    function renderQuestion(payload) {
        countryName.textContent = payload.targetCountryName;
        stageCopy.textContent = "아래 보기 중 이 국가의 수도를 고르세요.";
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
            <label class="option-card" data-option-number="${option.optionNumber}" data-option-label="${escapeHtml(option.capitalCity)}">
                <input type="radio" name="capital-option" value="${option.optionNumber}" data-option-label="${escapeHtml(option.capitalCity)}">
                <span class="subtitle">Choice ${option.optionNumber}</span>
                <strong>${escapeHtml(option.capitalCity)}</strong>
            </label>
        `).join("");

        target.querySelectorAll("input[name='capital-option']").forEach((input) => {
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
        target.innerHTML = `
            <h3>${escapeHtml(payload.targetCountryName)} 결과</h3>
            <p>내 선택 수도: ${escapeHtml(payload.selectedCapitalCity)}</p>
            <p>정답 수도: ${escapeHtml(payload.correctCapitalCity)}</p>
            <p>획득 점수: ${payload.awardedScore}</p>
            <p>현재 총점: ${payload.totalScore}</p>
        `;
    }

    function hideCapitalFeedback(target) {
        target.hidden = true;
        target.innerHTML = "";
    }

    function showNextStageAction() {
        nextStageButton.hidden = false;
        nextStageButton.disabled = false;
        submitButton.disabled = true;
        optionsBox.querySelectorAll("input[name='capital-option']").forEach((input) => {
            input.disabled = true;
        });
    }

    function hideNextStageAction() {
        nextStageButton.hidden = true;
        nextStageButton.disabled = true;
    }

    function clearAnswerInput() {
        optionsBox.querySelectorAll("input[name='capital-option']").forEach((input) => {
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
        setStageHint(`${payload.difficultyLabel} 구간입니다. 가장 맞는 수도를 고른 뒤 제출하세요.`);
    }

    function canSubmitCurrentAnswer() {
        if (!currentState) {
            return false;
        }
        return Boolean(form.querySelector("input[name='capital-option']:checked"));
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
        gameOverSummary.textContent = `Stage ${payload.stageNumber}에서 탈락했습니다. 현재 총점 ${payload.totalScore}점, 다시 시작하면 같은 세션으로 Stage 1부터 이어집니다.`;
        gameOverModal.hidden = false;
    }

    function hideGameOverModal() {
        gameOverModal.hidden = true;
    }

    function lockInteraction(locked) {
        interactionLocked = locked;
        submitButton.disabled = locked || !canSubmitCurrentAnswer();
        nextStageButton.disabled = locked || nextStageButton.hidden;
        optionsBox.querySelectorAll("input[name='capital-option']").forEach((input) => {
            input.disabled = locked;
        });
    }
}

function showCapitalMessage(target, message, tone = "info") {
    target.hidden = false;
    target.textContent = message;
    target.dataset.tone = tone;
}

function hideCapitalMessage(target) {
    target.hidden = true;
    target.textContent = "";
    delete target.dataset.tone;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
