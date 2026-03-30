(function () {
    const form = document.getElementById("recommendation-feedback-form");

    if (!form) {
        return;
    }

    const scoreInputs = Array.from(form.querySelectorAll("input[name='satisfactionScore']"));
    const submitButton = document.getElementById("recommendation-feedback-submit");
    const messageBox = document.getElementById("recommendation-feedback-message");
    let submitted = false;

    scoreInputs.forEach((input) => {
        input.addEventListener("change", () => {
            if (submitted) {
                return;
            }

            syncSelectedScore(scoreInputs);
            submitButton.disabled = !hasSelectedScore(scoreInputs);
            hideFeedbackMessage(messageBox);
        });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (submitted) {
            return;
        }

        if (!hasSelectedScore(scoreInputs)) {
            showFeedbackMessage(messageBox, "만족도 점수를 먼저 선택해주세요.", "error");
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "보내는 중...";

        try {
            const payload = Object.fromEntries(new FormData(form).entries());
            const response = await fetch("/api/recommendation/feedback", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            });
            const body = await response.json();

            if (!response.ok) {
                throw new Error(body.message || "만족도 저장에 실패했습니다.");
            }

            submitted = true;
            scoreInputs.forEach((input) => {
                input.disabled = true;
            });
            submitButton.textContent = "전송 완료";
            showFeedbackMessage(messageBox, `감사합니다. ${body.satisfactionScore}점 피드백을 저장했습니다.`, "success");
        } catch (error) {
            submitButton.disabled = !hasSelectedScore(scoreInputs);
            submitButton.textContent = "만족도 보내기";
            showFeedbackMessage(messageBox, error.message, "error");
        }
    });
})();

function hasSelectedScore(scoreInputs) {
    return scoreInputs.some((input) => input.checked);
}

function syncSelectedScore(scoreInputs) {
    scoreInputs.forEach((input) => {
        input.closest(".rating-score-option")?.classList.toggle("is-selected", input.checked);
    });
}

function showFeedbackMessage(target, message, tone) {
    target.hidden = false;
    target.dataset.tone = tone;
    target.textContent = message;
}

function hideFeedbackMessage(target) {
    target.hidden = true;
    target.textContent = "";
    delete target.dataset.tone;
}
