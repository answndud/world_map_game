(function () {
    const page = document.body.dataset.page;

    if (page === "location-start") {
        initStartPage();
    }

    if (page === "location-play") {
        initPlayPage();
    }
})();

function initStartPage() {
    const form = document.getElementById("location-start-form");
    const nicknameInput = document.getElementById("nickname");
    const messageBox = document.getElementById("location-start-message");
    const submitButton = document.getElementById("location-start-submit");
    const defaultButtonText = submitButton.textContent;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideLocationMessage(messageBox);
        submitButton.disabled = true;
        submitButton.textContent = "게임 준비 중...";
        showLocationMessage(messageBox, "게임 화면과 첫 번째 Stage를 준비하는 중입니다. 잠시만 기다려주세요.", "info");

        try {
            const response = await fetch("/api/games/location/sessions", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    nickname: nicknameInput.value
                })
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임 세션을 시작하지 못했습니다.");
            }

            window.location.assign(payload.playPageUrl);
        } catch (error) {
            submitButton.disabled = false;
            submitButton.textContent = defaultButtonText;
            showLocationMessage(messageBox, error.message, "error");
        }
    });
}

function initPlayPage() {
    const COLORS = {
        active: "rgba(148, 203, 235, 0)",
        activeStroke: "rgba(196, 233, 255, 0.92)",
        selected: "rgba(255, 74, 175, 0.2)",
        selectedSide: "rgba(156, 36, 101, 0.32)",
        selectedStroke: "rgba(255, 132, 222, 1)",
        wrong: "rgba(211, 106, 106, 0.84)",
        wrongSide: "rgba(135, 63, 63, 0.7)",
        correct: "rgba(88, 214, 141, 0.84)",
        correctSide: "rgba(37, 106, 70, 0.7)",
        inactive: "rgba(255, 255, 255, 0)",
        inactiveStroke: "rgba(255, 255, 255, 0.01)"
    };
    const DRAG_THRESHOLD = 18;

    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("location-answer-form");
    const statusBox = document.getElementById("location-game-status");
    const countryName = document.getElementById("target-country-name");
    const submitButton = document.getElementById("location-submit-button");
    const globeStage = document.getElementById("globe-stage");
    const feedback = document.getElementById("location-answer-feedback");
    const stageOverlay = document.getElementById("location-stage-overlay");
    const messageBox = document.getElementById("location-play-message");
    const heroCopy = document.getElementById("location-hero-copy");
    const stageHint = document.getElementById("location-stage-hint");
    const gameOverModal = document.getElementById("location-game-over-modal");
    const gameOverSummary = document.getElementById("location-game-over-summary");
    const restartButton = document.getElementById("location-restart-button");

    let currentState = null;
    let selectedCountryIso3Code = null;
    let highlightedCorrectIso3Code = null;
    let highlightedWrongIso3Code = null;
    let interactionLocked = false;
    let globe = null;
    let resizeObserver = null;
    let activeCountryIsoCodes = new Set();
    let polygonFeatures = [];
    let hitTestFeatures = [];
    let pointerIntent = {
        tracking: false,
        suppressSelection: false,
        startX: 0,
        startY: 0
    };

    window.addEventListener("pageshow", hideGameOverModal);
    restartButton?.addEventListener("click", restartCurrentSession);

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideLocationMessage(messageBox);

        if (!currentState) {
            showLocationMessage(messageBox, "현재 Stage를 아직 불러오지 못했습니다.", "error");
            return;
        }

        if (!selectedCountryIso3Code) {
            showLocationMessage(messageBox, "지구본에서 국가를 먼저 선택해주세요.", "error");
            return;
        }

        if (interactionLocked) {
            return;
        }

        try {
            lockInteraction(true);
            const response = await fetch(`/api/games/location/sessions/${sessionId}/answer`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    stageNumber: currentState.stageNumber,
                    selectedCountryIso3Code
                })
            });
            const payload = await response.json();

            if (!response.ok) {
                lockInteraction(false);
                throw new Error(payload.message || "정답 제출에 실패했습니다.");
            }

            renderStatus(statusBox, {
                gameLevel: payload.gameLevel,
                stageNumber: payload.nextStageNumber || currentState.stageNumber,
                difficultyLabel: payload.nextDifficultyLabel || currentState.difficultyLabel,
                clearedStageCount: payload.clearedStageCount,
                totalScore: payload.totalScore,
                livesRemaining: payload.livesRemaining
            });
            renderFeedback(feedback, payload);

            if (payload.correct) {
                highlightedCorrectIso3Code = payload.selectedCountryIso3Code;
                highlightedWrongIso3Code = null;
                renderStageOverlay(stageOverlay, "정답", `+${payload.awardedScore}`, "success");
                refreshGlobe();

                if (payload.outcome === "FINISHED") {
                    setTimeout(() => {
                        window.location.href = payload.resultPageUrl;
                    }, 1100);
                    return;
                }

                setTimeout(() => {
                    loadState().catch((error) => showLocationMessage(messageBox, error.message, "error"));
                }, 950);
                return;
            }

            highlightedCorrectIso3Code = null;
            highlightedWrongIso3Code = payload.selectedCountryIso3Code;
            renderStageOverlay(
                stageOverlay,
                payload.outcome === "GAME_OVER" ? "탈락" : "오답",
                payload.outcome === "GAME_OVER" ? "하트를 모두 잃었습니다" : `하트 ${payload.livesRemaining}개 남음`,
                "danger"
            );
            refreshGlobe();

            if (payload.outcome === "GAME_OVER") {
                lockInteraction(true);
                clearSelection();
                showGameOverModal(payload);
                return;
            }

            setTimeout(() => {
                lockInteraction(false);
                highlightedWrongIso3Code = null;
                stageOverlay.hidden = true;
                feedback.hidden = true;
                clearSelection();
                renderStatus(statusBox, {
                    stageNumber: currentState.stageNumber,
                    difficultyLabel: currentState.difficultyLabel,
                    clearedStageCount: payload.clearedStageCount,
                    totalScore: payload.totalScore,
                    livesRemaining: payload.livesRemaining
                });
                refreshGlobe();
            }, 950);
        } catch (error) {
            lockInteraction(false);
            showLocationMessage(messageBox, error.message, "error");
        }
    });

    showLocationMessage(messageBox, "세션과 지구본을 준비하는 중입니다. 첫 진입에서는 잠시 걸릴 수 있습니다.", "info");
    hideGameOverModal();
    initializePlayScreen()
        .catch((error) => showLocationMessage(messageBox, error.message, "error"));

    async function initializePlayScreen() {
        installPointerIntentDetection();

        const [statePayload, globePayload] = await Promise.all([
            fetchState(),
            fetchGlobePayload()
        ]);

        applyState(statePayload);
        activeCountryIsoCodes = globePayload.activeCountryIsoCodes;
        polygonFeatures = globePayload.polygonFeatures;
        hitTestFeatures = buildHitTestFeatures(polygonFeatures);

        await waitForNextPaint();
        createBaseGlobe();
        await waitForNextPaint();
        hydrateGlobePolygons();
        hideLocationMessage(messageBox);
    }

    async function fetchState() {
        const response = await fetch(`/api/games/location/sessions/${sessionId}/state`, {
            cache: "no-store"
        });
        const payload = await response.json();

        if (!response.ok) {
            throw new Error(payload.message || "현재 Stage를 불러오지 못했습니다.");
        }

        return payload;
    }

    function applyState(payload) {
        hideGameOverModal();
        feedback.hidden = true;
        stageOverlay.hidden = true;
        lockInteraction(false);
        clearSelection();
        highlightedCorrectIso3Code = null;
        highlightedWrongIso3Code = null;

        currentState = payload;
        countryName.textContent = payload.targetCountryName;
        renderLevelCopy(payload.gameLevel, heroCopy, stageHint);
        renderStatus(statusBox, payload);
        refreshGlobe();
    }

    async function loadState() {
        const payload = await fetchState();
        applyState(payload);
    }

    async function restartCurrentSession() {
        if (interactionLocked && gameOverModal.hidden) {
            return;
        }

        hideLocationMessage(messageBox);
        if (restartButton) {
            restartButton.disabled = true;
            restartButton.textContent = "재시작 중...";
        }

        try {
            const response = await fetch(`/api/games/location/sessions/${sessionId}/restart`, {
                method: "POST"
            });
            const payload = await response.json();

            if (!response.ok) {
                throw new Error(payload.message || "게임을 다시 시작하지 못했습니다.");
            }

            hideGameOverModal();
            await loadState();
        } catch (error) {
            showLocationMessage(messageBox, error.message, "error");
        } finally {
            if (restartButton) {
                restartButton.disabled = false;
                restartButton.textContent = "다시 시작하기";
            }
        }
    }

    async function fetchGlobePayload() {
        if (typeof Globe !== "function") {
            throw new Error("3D 지구본 라이브러리를 불러오지 못했습니다.");
        }

        const geoJsonResponse = await fetch("/data/active-countries.geojson?v=20260323-level1-110m", {
            cache: "no-store"
        });
        const geoJsonPayload = await geoJsonResponse.json();

        if (!geoJsonResponse.ok) {
            throw new Error("지구본 폴리곤 데이터를 불러오지 못했습니다.");
        }

        return {
            activeCountryIsoCodes: new Set(geoJsonPayload.features.map((feature) => feature.properties.iso3Code)),
            polygonFeatures: geoJsonPayload.features
        };
    }

    function createBaseGlobe() {
        globe = Globe()(globeStage)
            .globeImageUrl("/images/earth-blue-marble.jpg")
            .backgroundColor("rgba(0,0,0,0)")
            .showAtmosphere(true)
            .atmosphereColor("#55758f")
            .atmosphereAltitude(0.028)
            .polygonsData([])
            .polygonCapColor((feature) => polygonCapColor(feature))
            .polygonSideColor((feature) => polygonSideColor(feature))
            .polygonStrokeColor((feature) => polygonStrokeColor(feature))
            .polygonAltitude((feature) => polygonAltitude(feature))
            .polygonCapCurvatureResolution(6)
            .polygonsTransitionDuration(0)
            .onPolygonClick((feature, event, coords) => {
                handleCountrySelection(feature?.properties?.iso3Code, coords);
            })
            .onGlobeClick((coords) => {
                handleGlobeSurfaceClick(coords);
            });

        syncGlobeSize();
        installResizeSync();
        globe.pointOfView({lat: 18, lng: 24, altitude: 1.86}, 0);
        globe.controls().enableDamping = true;
        globe.controls().dampingFactor = 0.08;
        globe.controls().autoRotate = false;
        globe.controls().rotateSpeed = 0.62;
        globe.controls().minDistance = 140;
        globe.controls().maxDistance = 420;
    }

    function hydrateGlobePolygons() {
        if (!globe) {
            return;
        }

        globe
            .polygonsData(polygonFeatures)
            .polygonCapColor((feature) => polygonCapColor(feature))
            .polygonSideColor((feature) => polygonSideColor(feature))
            .polygonStrokeColor((feature) => polygonStrokeColor(feature))
            .polygonAltitude((feature) => polygonAltitude(feature));
    }

    function handleGlobeSurfaceClick(coords) {
        if (pointerIntent.suppressSelection) {
            pointerIntent.suppressSelection = false;
            return;
        }

        if (!currentState || interactionLocked || !coords) {
            return;
        }

        const matchedFeature = findFeatureByCoordinates(coords.lat, coords.lng);
        if (!matchedFeature) {
            return;
        }

        handleCountrySelection(matchedFeature.iso3Code, coords);
    }

    function handleCountrySelection(iso3Code, coords) {
        if (!currentState || interactionLocked || !iso3Code) {
            return;
        }

        if (!activeCountryIsoCodes.has(iso3Code)) {
            showLocationMessage(messageBox, "이번 모드에서 사용할 수 있는 국가만 선택할 수 있습니다.", "error");
            return;
        }

        hideLocationMessage(messageBox);
        selectedCountryIso3Code = iso3Code;
        submitButton.disabled = false;
        refreshGlobe();

        if (globe && coords) {
            globe.pointOfView({lat: coords.lat, lng: coords.lng, altitude: 1.45}, 600);
        }
    }

    function clearSelection() {
        selectedCountryIso3Code = null;
        submitButton.disabled = true;
        refreshGlobe();
    }

    function refreshGlobe() {
        if (!globe) {
            return;
        }

        globe
            .polygonCapColor((feature) => polygonCapColor(feature))
            .polygonSideColor((feature) => polygonSideColor(feature))
            .polygonStrokeColor((feature) => polygonStrokeColor(feature))
            .polygonAltitude((feature) => polygonAltitude(feature));
    }

    function lockInteraction(locked) {
        interactionLocked = locked;
        globeStage.classList.toggle("is-locked", locked);
        submitButton.disabled = locked || !selectedCountryIso3Code;
    }

    function showGameOverModal(payload) {
        if (!gameOverModal) {
            return;
        }

        gameOverSummary.textContent = `Stage ${payload.stageNumber}에서 하트를 모두 잃었습니다. 현재 점수는 ${payload.totalScore}점입니다. 홈으로 돌아가거나 바로 다시 시작할 수 있습니다.`;
        gameOverModal.hidden = false;
    }

    function hideGameOverModal() {
        if (!gameOverModal) {
            return;
        }

        gameOverModal.hidden = true;
    }

    function syncGlobeSize() {
        if (!globe) {
            return;
        }

        const rect = globeStage.getBoundingClientRect();
        const size = Math.floor(Math.min(rect.width, rect.height || rect.width));

        if (!size) {
            return;
        }

        globe.width(size).height(size);
    }

    function installResizeSync() {
        if (typeof ResizeObserver === "function") {
            resizeObserver = new ResizeObserver(() => syncGlobeSize());
            resizeObserver.observe(globeStage);
            return;
        }

        window.addEventListener("resize", syncGlobeSize);
    }

    function installPointerIntentDetection() {
        if (globeStage.dataset.pointerIntentReady === "true") {
            return;
        }

        globeStage.dataset.pointerIntentReady = "true";

        globeStage.addEventListener("pointerdown", (event) => {
            pointerIntent.tracking = true;
            pointerIntent.suppressSelection = false;
            pointerIntent.startX = event.clientX;
            pointerIntent.startY = event.clientY;
        });

        globeStage.addEventListener("pointermove", (event) => {
            if (!pointerIntent.tracking || pointerIntent.suppressSelection) {
                return;
            }

            const distance = Math.hypot(
                event.clientX - pointerIntent.startX,
                event.clientY - pointerIntent.startY
            );

            if (distance > DRAG_THRESHOLD) {
                pointerIntent.suppressSelection = true;
            }
        });

        globeStage.addEventListener("pointerup", () => {
            pointerIntent.tracking = false;
        });

        globeStage.addEventListener("pointercancel", () => {
            pointerIntent.tracking = false;
            pointerIntent.suppressSelection = false;
        });

        globeStage.addEventListener("pointerleave", () => {
            if (pointerIntent.tracking) {
                pointerIntent.suppressSelection = true;
            }
        });
    }

    function polygonCapColor(feature) {
        const iso3Code = feature.properties.iso3Code;
        const active = activeCountryIsoCodes.has(iso3Code);

        if (highlightedCorrectIso3Code === iso3Code) {
            return COLORS.correct;
        }

        if (highlightedWrongIso3Code === iso3Code) {
            return COLORS.wrong;
        }

        if (selectedCountryIso3Code === iso3Code) {
            return COLORS.selected;
        }

        return active ? COLORS.active : COLORS.inactive;
    }

    function polygonSideColor(feature) {
        const iso3Code = feature.properties.iso3Code;

        if (highlightedCorrectIso3Code === iso3Code) {
            return COLORS.correctSide;
        }

        if (highlightedWrongIso3Code === iso3Code) {
            return COLORS.wrongSide;
        }

        if (selectedCountryIso3Code === iso3Code) {
            return COLORS.selectedSide;
        }

        return "rgba(0, 0, 0, 0)";
    }

    function polygonStrokeColor(feature) {
        const iso3Code = feature.properties.iso3Code;

        if (highlightedCorrectIso3Code === iso3Code || highlightedWrongIso3Code === iso3Code) {
            return "rgba(255, 255, 255, 0.96)";
        }

        if (selectedCountryIso3Code === iso3Code) {
            return COLORS.selectedStroke;
        }

        return activeCountryIsoCodes.has(iso3Code)
            ? COLORS.activeStroke
            : COLORS.inactiveStroke;
    }

    function polygonAltitude(feature) {
        const iso3Code = feature.properties.iso3Code;

        if (highlightedCorrectIso3Code === iso3Code || highlightedWrongIso3Code === iso3Code) {
            return 0.018;
        }

        if (selectedCountryIso3Code === iso3Code) {
            return 0.015;
        }

        return activeCountryIsoCodes.has(iso3Code) ? 0.0025 : 0;
    }

    function buildHitTestFeatures(features) {
        return features.map((feature) => ({
            iso3Code: feature.properties.iso3Code,
            geometry: feature.geometry
        }));
    }

    function findFeatureByCoordinates(lat, lng) {
        return hitTestFeatures.find((feature) => geometryContainsPoint(feature.geometry, lat, lng)) || null;
    }

    function geometryContainsPoint(geometry, lat, lng) {
        if (!geometry) {
            return false;
        }

        if (geometry.type === "Polygon") {
            return polygonContainsPoint(geometry.coordinates, lat, lng);
        }

        if (geometry.type === "MultiPolygon") {
            return geometry.coordinates.some((polygon) => polygonContainsPoint(polygon, lat, lng));
        }

        return false;
    }

    function polygonContainsPoint(polygon, lat, lng) {
        if (!polygon.length) {
            return false;
        }

        if (!ringContainsPoint(polygon[0], lat, lng)) {
            return false;
        }

        return !polygon.slice(1).some((holeRing) => ringContainsPoint(holeRing, lat, lng));
    }

    function ringContainsPoint(ring, lat, lng) {
        let inside = false;
        const normalizedRing = ring.map(([ringLng, ringLat]) => [normalizeLongitude(ringLng, lng), ringLat]);

        for (let currentIndex = 0, previousIndex = normalizedRing.length - 1;
             currentIndex < normalizedRing.length;
             previousIndex = currentIndex++) {
            const [currentLng, currentLat] = normalizedRing[currentIndex];
            const [previousLng, previousLat] = normalizedRing[previousIndex];

            const intersects = ((currentLat > lat) !== (previousLat > lat))
                && (lng < ((previousLng - currentLng) * (lat - currentLat)) / ((previousLat - currentLat) || Number.EPSILON) + currentLng);

            if (intersects) {
                inside = !inside;
            }
        }

        return inside;
    }

    function normalizeLongitude(value, referenceLng) {
        let normalized = value;

        while (normalized - referenceLng > 180) {
            normalized -= 360;
        }

        while (normalized - referenceLng < -180) {
            normalized += 360;
        }

        return normalized;
    }
}

function waitForNextPaint() {
    return new Promise((resolve) => {
        window.requestAnimationFrame(() => {
            window.setTimeout(resolve, 0);
        });
    });
}

function renderStatus(target, payload) {
    target.innerHTML = `
        <article class="stat-card">
            <span class="subtitle">Mode</span>
            <strong>${formatLocationGameLevel(payload.gameLevel)}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">Stage</span>
            <strong>${payload.stageNumber}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">Difficulty</span>
            <strong>${payload.difficultyLabel}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">Score</span>
            <strong>${payload.totalScore}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">Hearts</span>
            <strong class="heart-row">${renderHearts(payload.livesRemaining)}</strong>
        </article>
        <article class="stat-card">
            <span class="subtitle">Clear</span>
            <strong>${payload.clearedStageCount}</strong>
        </article>
    `;
}

function renderHearts(livesRemaining) {
    return Array.from({length: 3}, (_, index) => {
        const active = index < livesRemaining;
        return `<span class="heart ${active ? "is-active" : "is-empty"}">${active ? "♥" : "♡"}</span>`;
    }).join("");
}

function renderFeedback(target, payload) {
    target.hidden = false;
    const summary = payload.correct
        ? `${payload.selectedCountryName} 선택이 정답입니다.`
        : `${payload.selectedCountryName} 선택은 오답입니다.`;
    const followUp = payload.outcome === "WRONG"
        ? wrongFollowUp(payload)
        : payload.outcome === "GAME_OVER"
            ? "하트를 모두 잃었습니다."
            : payload.outcome === "FINISHED"
                ? "이번 러닝이 종료되었습니다."
                : "다음 Stage로 자동 이동합니다.";

    target.innerHTML = `
        <h3>${summary}</h3>
        <p>${followUp}</p>
        <p>획득 점수: ${payload.awardedScore}</p>
        <p>현재 총점: ${payload.totalScore}</p>
    `;
}

function wrongFollowUp(payload) {
    return "같은 Stage를 다시 시도하세요.";
}

function renderLevelCopy(gameLevel, heroCopyTarget, stageHintTarget) {
    if (heroCopyTarget) {
        heroCopyTarget.textContent = "플레이 중 지구본 위 나라 이름은 표시되지 않습니다. 국가를 클릭하면 지구본 위에서만 선택 하이라이트가 보이고, 실제 국가명은 제출 후 판정 단계에서만 공개됩니다. 현재는 상위 72개 주요 국가를 대상으로 먼저 감을 익히는 기본 모드만 운영합니다.";
    }

    if (stageHintTarget) {
        stageHintTarget.textContent = "지구본을 회전해 해당 국가를 찾은 뒤 클릭하세요. 현재는 상위 72개 주요 국가를 대상으로 먼저 안정성과 클릭 정확도를 맞추고 있습니다.";
    }
}

function formatLocationGameLevel(gameLevel) {
    return "기본 탐색";
}

function renderStageOverlay(target, title, description, tone) {
    target.hidden = false;
    target.dataset.tone = tone;
    target.innerHTML = `
        <strong>${title}</strong>
        <span>${description}</span>
    `;
}

function showLocationMessage(target, message, tone = "info") {
    target.hidden = false;
    target.textContent = message;
    target.dataset.tone = tone;
}

function hideLocationMessage(target) {
    target.hidden = true;
    target.textContent = "";
    delete target.dataset.tone;
}
