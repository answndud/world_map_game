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

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePopulationMessage(messageBox);

        try {
            const response = await fetch("/api/games/population/sessions", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({nickname: nicknameInput.value})
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임 세션을 시작하지 못했습니다.");
            }

            window.location.href = payload.playPageUrl;
        } catch (error) {
            showPopulationMessage(messageBox, error.message);
        }
    });
}

function initPlayPage() {
    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("population-answer-form");
    const statusBox = document.getElementById("population-game-status");
    const countryName = document.getElementById("population-target-country-name");
    const yearLabel = document.getElementById("population-year");
    const optionsBox = document.getElementById("population-options");
    const feedback = document.getElementById("population-answer-feedback");
    const nextRoundButton = document.getElementById("load-next-population-round-button");
    const messageBox = document.getElementById("population-play-message");

    let currentRound = null;

    nextRoundButton.addEventListener("click", async () => {
        await loadRound();
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePopulationMessage(messageBox);

        if (!currentRound) {
            showPopulationMessage(messageBox, "현재 라운드를 아직 불러오지 못했습니다.");
            return;
        }

        const selectedOption = form.querySelector("input[name='population-option']:checked");

        if (!selectedOption) {
            showPopulationMessage(messageBox, "보기 하나를 선택해주세요.");
            return;
        }

        try {
            const response = await fetch(`/api/games/population/sessions/${sessionId}/answer`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    roundNumber: currentRound.roundNumber,
                    selectedOptionNumber: Number(selectedOption.value)
                })
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "정답 제출에 실패했습니다.");
            }

            renderPopulationFeedback(feedback, payload);
            renderPopulationStatus(statusBox, {
                roundNumber: payload.nextRoundNumber || payload.roundNumber,
                totalRounds: currentRound.totalRounds,
                answeredRoundCount: payload.answeredRoundCount,
                totalScore: payload.totalScore
            });

            if (payload.gameStatus === "FINISHED") {
                nextRoundButton.hidden = true;
                form.hidden = true;
                showPopulationMessage(messageBox, "게임이 종료되었습니다. 결과 페이지로 이동하세요.");
                messageBox.insertAdjacentHTML(
                    "beforeend",
                    `<br><a class="primary-link inline-action" href="${payload.resultPageUrl}">결과 페이지 보기</a>`
                );
                return;
            }

            currentRound = null;
            nextRoundButton.hidden = false;
        } catch (error) {
            showPopulationMessage(messageBox, error.message);
        }
    });

    loadRound().catch((error) => showPopulationMessage(messageBox, error.message));

    async function loadRound() {
        hidePopulationMessage(messageBox);
        feedback.hidden = true;
        nextRoundButton.hidden = true;

        const response = await fetch(`/api/games/population/sessions/${sessionId}/round`);
        const payload = await response.json();

        if (!response.ok) {
            throw new Error(payload.message || "현재 라운드를 불러오지 못했습니다.");
        }

        currentRound = payload;
        countryName.textContent = payload.targetCountryName;
        yearLabel.textContent = `기준 연도: ${payload.populationYear}`;
        renderPopulationStatus(statusBox, payload);
        renderPopulationOptions(optionsBox, payload.options);
    }
}

function renderPopulationStatus(target, payload) {
    const nextRound = payload.roundNumber || 1;
    const remainingRounds = payload.totalRounds - payload.answeredRoundCount;

    target.innerHTML = `
        <article class="stat-card">
            <span class="subtitle">진행 라운드</span>
            <strong>${nextRound} / ${payload.totalRounds}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">누적 점수</span>
            <strong>${payload.totalScore}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">완료 수</span>
            <strong>${payload.answeredRoundCount}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">남은 라운드</span>
            <strong>${remainingRounds}</strong>
        </article>
    `;
}

function renderPopulationOptions(target, options) {
    target.innerHTML = options.map((option) => `
        <label class="option-card">
            <input type="radio" name="population-option" value="${option.optionNumber}">
            <span>${option.population.toLocaleString()}명</span>
        </label>
    `).join("");
}

function renderPopulationFeedback(target, payload) {
    target.hidden = false;
    target.innerHTML = `
        <h3>${payload.targetCountryName} 결과</h3>
        <p>내 선택: ${Number(payload.selectedPopulation).toLocaleString()}명</p>
        <p>정답: ${Number(payload.correctPopulation).toLocaleString()}명</p>
        <p>판정: ${payload.correct ? "Correct" : "Wrong"}</p>
        <p>획득 점수: ${payload.awardedScore}</p>
        <p>현재 총점: ${payload.totalScore}</p>
    `;
}

function showPopulationMessage(target, message) {
    target.hidden = false;
    target.innerHTML = message;
}

function hidePopulationMessage(target) {
    target.hidden = true;
    target.innerHTML = "";
}
