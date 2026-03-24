(function () {
    const form = document.getElementById("recommendation-feedback-form");

    if (!form) {
        return;
    }

    const scoreInput = document.getElementById("recommendation-satisfaction-score");
    const scoreButtons = Array.from(document.querySelectorAll(".rating-score-button"));
    const submitButton = document.getElementById("recommendation-feedback-submit");
    const messageBox = document.getElementById("recommendation-feedback-message");
    let submitted = false;

    scoreButtons.forEach((button) => {
        button.addEventListener("click", () => {
            if (submitted) {
                return;
            }

            scoreInput.value = button.dataset.score;
            scoreButtons.forEach((candidate) => {
                candidate.classList.toggle("is-selected", candidate === button);
                candidate.setAttribute("aria-pressed", String(candidate === button));
            });
            submitButton.disabled = false;
            hideFeedbackMessage(messageBox);
        });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (submitted) {
            return;
        }

        if (!scoreInput.value) {
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
            scoreButtons.forEach((button) => {
                button.disabled = true;
            });
            submitButton.textContent = "전송 완료";
            showFeedbackMessage(messageBox, `감사합니다. ${body.satisfactionScore}점 피드백을 저장했습니다.`, "success");
        } catch (error) {
            submitButton.disabled = false;
            submitButton.textContent = "만족도 보내기";
            showFeedbackMessage(messageBox, error.message, "error");
        }
    });
})();

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
