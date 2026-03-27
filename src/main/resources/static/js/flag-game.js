(function () {
    const page = document.body.dataset.page;

    if (page === "flag-start") {
        initStartPage();
    }

    if (page === "flag-play") {
        initPlayPage();
    }
})();

function initStartPage() {
    const form = document.getElementById("flag-start-form");
    const nicknameInput = document.getElementById("flag-nickname");
    const messageBox = document.getElementById("flag-start-message");
    const submitButton = document.getElementById("flag-start-submit");
    const defaultButtonText = submitButton.textContent;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideFlagMessage(messageBox);
        submitButton.disabled = true;
        submitButton.textContent = "게임 준비 중...";
        showFlagMessage(messageBox, "첫 번째 Stage와 국기 보기를 준비하는 중입니다. 잠시만 기다려주세요.", "info");

        try {
            const response = await fetch("/api/games/flag/sessions", {
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
            showFlagMessage(messageBox, error.message, "error");
        }
    });
}

function initPlayPage() {
    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("flag-answer-form");
    const statusBox = document.getElementById("flag-game-status");
    const flagImage = document.getElementById("flag-target-image");
    const stageCopy = document.getElementById("flag-stage-copy");
    const optionsBox = document.getElementById("flag-options");
    const feedback = document.getElementById("flag-answer-feedback");
    const overlay = document.getElementById("flag-stage-overlay");
    const messageBox = document.getElementById("flag-play-message");
    const selectionLabel = document.getElementById("flag-selection-label");
    const stageHint = document.getElementById("flag-stage-hint");
    const submitButton = document.getElementById("flag-submit-button");
    const nextStageButton = document.getElementById("flag-next-stage-button");
    const gameOverModal = document.getElementById("flag-game-over-modal");
    const restartButton = document.getElementById("flag-restart-button");

    let currentState = null;
    let interactionLocked = false;

    window.addEventListener("pageshow", hideGameOverModal);
    restartButton?.addEventListener("click", restartCurrentSession);
    nextStageButton?.addEventListener("click", () => {
        hideFlagMessage(messageBox);
        nextStageButton.disabled = true;
        loadState()
            .catch((error) => showFlagMessage(messageBox, error.message, "error"))
            .finally(() => {
                nextStageButton.disabled = false;
            });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideFlagMessage(messageBox);

        if (!currentState) {
            showFlagMessage(messageBox, "현재 Stage를 아직 불러오지 못했습니다.", "error");
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
            const response = await fetch(`/api/games/flag/sessions/${sessionId}/answer`, {
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

            hideFlagFeedback(feedback);
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
                    : "오답입니다. 같은 Stage에서 다시 국가를 골라보세요."
            );

            if (payload.outcome === "GAME_OVER") {
                lockInteraction(true);
                showGameOverModal();
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
            showFlagMessage(messageBox, error.message, "error");
        }
    });

    showFlagMessage(messageBox, "Stage와 국기 보기를 불러오는 중입니다.", "info");
    hideGameOverModal();
    loadState()
        .then(() => hideFlagMessage(messageBox))
        .catch((error) => showFlagMessage(messageBox, error.message, "error"));

    async function loadState() {
        const response = await fetch(`/api/games/flag/sessions/${sessionId}/state`, {
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
        hideFlagFeedback(feedback);
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
            const response = await fetch(`/api/games/flag/sessions/${sessionId}/restart`, {
                method: "POST"
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 다시 시작하지 못했습니다.");
            }

            hideGameOverModal();
            hideFlagFeedback(feedback);
            overlay.hidden = true;
            hideNextStageAction();
            showFlagMessage(messageBox, "같은 세션을 Stage 1부터 다시 시작했습니다.", "success");
            await loadState();
        } catch (error) {
            showFlagMessage(messageBox, error.message, "error");
        } finally {
            restartButton.disabled = false;
        }
    }

    function createAnswerPayload() {
        const selectedOption = form.querySelector("input[name='flag-option']:checked");
        if (!selectedOption) {
            showFlagMessage(messageBox, "보기 하나를 먼저 선택해주세요.", "error");
            return null;
        }

        return {
            stageNumber: currentState.stageNumber,
            selectedOptionNumber: Number(selectedOption.value)
        };
    }

    function renderQuestion(payload) {
        flagImage.src = payload.targetFlagRelativePath;
        flagImage.alt = `문제 국기 Stage ${payload.stageNumber}`;
        stageCopy.textContent = "이 국기는 어느 나라일까요?";
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
            <label class="option-card" data-option-number="${option.optionNumber}" data-option-label="${escapeHtml(option.countryName)}">
                <input type="radio" name="flag-option" value="${option.optionNumber}" data-option-label="${escapeHtml(option.countryName)}">
                <span class="subtitle">Choice ${option.optionNumber}</span>
                <strong>${escapeHtml(option.countryName)}</strong>
            </label>
        `).join("");

        target.querySelectorAll("input[name='flag-option']").forEach((input) => {
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
            <h3>국기 판정 완료</h3>
            <p>획득 점수: ${payload.awardedScore}</p>
        `;
    }

    function hideFlagFeedback(target) {
        target.hidden = true;
        target.innerHTML = "";
    }

    function renderStageOverlay(target, title, body, tone) {
        target.hidden = false;
        target.dataset.tone = tone;
        target.innerHTML = `
            <p class="subtitle">${escapeHtml(title)}</p>
            <strong>${escapeHtml(body)}</strong>
        `;
    }

    function setSelectionState(text) {
        selectionLabel.textContent = text;
    }

    function setStageHint(text) {
        stageHint.textContent = text;
    }

    function resetHudGuidance(payload) {
        setSelectionState("국가 보기 하나를 고르면 여기서 확인합니다.");
        setStageHint(`현재 ${payload.difficultyLabel} 구간입니다. 국기를 보고 정답 국가를 고르세요.`);
    }

    function clearAnswerInput() {
        form.querySelectorAll("input[name='flag-option']").forEach((input) => {
            input.checked = false;
        });
        optionsBox.querySelectorAll(".option-card").forEach((card) => {
            card.classList.remove("is-selected");
        });
        submitButton.disabled = true;
    }

    function lockInteraction(locked) {
        interactionLocked = locked;
        submitButton.disabled = locked || !canSubmitCurrentAnswer();
        optionsBox.querySelectorAll("input[name='flag-option']").forEach((input) => {
            input.disabled = locked;
        });
    }

    function canSubmitCurrentAnswer() {
        return Boolean(form.querySelector("input[name='flag-option']:checked"));
    }

    function showNextStageAction() {
        submitButton.hidden = true;
        nextStageButton.hidden = false;
    }

    function hideNextStageAction() {
        submitButton.hidden = false;
        nextStageButton.hidden = true;
    }

    function showGameOverModal() {
        gameOverModal.hidden = false;
    }

    function hideGameOverModal() {
        gameOverModal.hidden = true;
    }
}

function showFlagMessage(target, message, tone) {
    target.hidden = false;
    target.dataset.tone = tone;
    target.textContent = message;
}

function hideFlagMessage(target) {
    target.hidden = true;
    target.textContent = "";
    delete target.dataset.tone;
}

function renderHearts(livesRemaining) {
    return new Array(3).fill("♡")
        .map((heart, index) => index < livesRemaining ? "♥" : heart)
        .join(" ");
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
