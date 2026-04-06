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
        showLocationMessage(messageBox, "첫 문제를 준비하는 중입니다.", "info");

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
    const STAGE_FEEDBACK_DELAY_MS = 950;
    const FINISH_REDIRECT_DELAY_MS = 1100;
    const COMPACT_VIEWPORT_MAX_WIDTH = 760;
    const POINTER_DRAG_THRESHOLD_MOUSE = 18;
    const POINTER_DRAG_THRESHOLD_PEN = 24;
    const POINTER_DRAG_THRESHOLD_TOUCH = 30;
    const POINTER_SELECTION_SUPPRESSION_WINDOW_MS = 260;
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
    const sessionId = document.body.dataset.sessionId;
    const form = document.getElementById("location-answer-form");
    const statusBox = document.getElementById("location-game-status");
    const countryName = document.getElementById("target-country-name");
    const submitButton = document.getElementById("location-submit-button");
    const globeStage = document.getElementById("globe-stage");
    const feedback = document.getElementById("location-answer-feedback");
    const stageOverlay = document.getElementById("location-stage-overlay");
    const messageBox = document.getElementById("location-play-message");
    const stageHint = document.getElementById("location-stage-hint");
    const gameOverModal = document.getElementById("location-game-over-modal");
    const gameOverPanel = gameOverModal?.querySelector(".game-over-modal__panel");
    const gameOverSummary = document.getElementById("location-game-over-summary");
    const gameOverRecap = document.getElementById("location-game-over-recap");
    const restartButton = document.getElementById("location-restart-button");
    const pageShell = document.querySelector(".page-shell");
    const gameOverModalController = window.createGameOverModalController({
        modal: gameOverModal,
        panel: gameOverPanel,
        summaryTarget: gameOverSummary,
        restartButton,
        pageShell,
        buildSummaryText: (payload) =>
            `Stage ${payload.stageNumber}에서 종료되었습니다.`
    });

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
        startY: 0,
        pointerType: "mouse",
        selectionBlockedUntil: 0
    };

    window.addEventListener("pageshow", gameOverModalController.hide);
    restartButton?.addEventListener("click", restartCurrentSession);
    installBrowserSmokeHooks();

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
                    stageId: currentState.stageId,
                    expectedAttemptNumber: currentState.expectedAttemptNumber,
                    selectedCountryIso3Code
                })
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
            renderFeedback(feedback, payload);

            if (payload.correct) {
                highlightedCorrectIso3Code = payload.selectedCountryIso3Code;
                highlightedWrongIso3Code = null;
                renderStageOverlay(stageOverlay, "정답", `+${payload.awardedScore}`, "success");
                if (stageHint) {
                    stageHint.textContent = "정답입니다. 다음 문제로 넘어갑니다.";
                }
                refreshGlobe();

                if (payload.outcome === "FINISHED") {
                    setTimeout(() => {
                        window.location.href = payload.resultPageUrl;
                    }, FINISH_REDIRECT_DELAY_MS);
                    return;
                }

                setTimeout(() => {
                    loadState().catch((error) => {
                        lockInteraction(false);
                        showLocationMessage(messageBox, error.message, "error");
                    });
                }, STAGE_FEEDBACK_DELAY_MS);
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
            if (stageHint) {
                stageHint.textContent = payload.outcome === "GAME_OVER"
                    ? "하트를 모두 잃었습니다."
                    : "오답입니다. 잠시 뒤 다시 시도합니다.";
            }
            refreshGlobe();

            if (payload.outcome === "GAME_OVER") {
                lockInteraction(true);
                clearSelection();
                showGameOverModal(payload);
                return;
            }

            setTimeout(() => {
                loadState().catch((error) => {
                    lockInteraction(false);
                    showLocationMessage(messageBox, error.message, "error");
                });
            }, STAGE_FEEDBACK_DELAY_MS);
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
        renderLevelCopy(stageHint);
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
            focusPrimaryPlaySurface();
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
        globe.pointOfView({lat: 18, lng: 24, altitude: currentViewportGlobeTuning().initialAltitude}, 0);
        applyViewportGlobeControls();
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
        if (shouldIgnoreSelection()) {
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
        if (shouldIgnoreSelection() || !currentState || interactionLocked || !iso3Code) {
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
            const tuning = currentViewportGlobeTuning();
            globe.pointOfView({lat: coords.lat, lng: coords.lng, altitude: tuning.focusAltitude}, tuning.focusAnimationMs);
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
        if (gameOverRecap) {
            gameOverRecap.innerHTML = `
                <article class="recap-card">
                    <span class="subtitle">탈락 Stage</span>
                    <strong>Stage ${payload.stageNumber}</strong>
                </article>
                <article class="recap-card">
                    <span class="subtitle">클리어</span>
                    <strong>${payload.clearedStageCount}개</strong>
                </article>
                <article class="recap-card">
                    <span class="subtitle">총점</span>
                    <strong>${payload.totalScore}점</strong>
                </article>
            `;
        }
        gameOverModalController.show(payload);
    }

    function hideGameOverModal() {
        if (gameOverRecap) {
            gameOverRecap.innerHTML = "";
        }
        gameOverModalController.hide();
    }

    function focusPrimaryPlaySurface() {
        globeStage?.focus();
    }

    function installBrowserSmokeHooks() {
        const browserSmokeHook = window.__worldmapBrowserSmoke;
        if (!browserSmokeHook) {
            return;
        }

        browserSmokeHook.locationSelectCountry = (iso3Code) => {
            if (!currentState || interactionLocked || !iso3Code || !activeCountryIsoCodes.has(iso3Code)) {
                return false;
            }

            hideLocationMessage(messageBox);
            selectedCountryIso3Code = iso3Code;
            submitButton.disabled = false;
            refreshGlobe();
            return true;
        };

        browserSmokeHook.locationMarkDragSuppressed = () => {
            suppressSelectionAfterDrag();
            return true;
        };

        browserSmokeHook.locationReadControlSnapshot = () => {
            const controls = globe?.controls?.();
            if (!controls) {
                return null;
            }

            return {
                compactViewport: isCompactViewport(),
                rotateSpeed: controls.rotateSpeed,
                dampingFactor: controls.dampingFactor,
                zoomSpeed: controls.zoomSpeed,
                minDistance: controls.minDistance,
                maxDistance: controls.maxDistance
            };
        };
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
        applyViewportGlobeControls();
    }

    function installResizeSync() {
        if (typeof ResizeObserver === "function") {
            resizeObserver = new ResizeObserver(() => syncGlobeSize());
            resizeObserver.observe(globeStage);
            return;
        }

        window.addEventListener("resize", syncGlobeSize);
    }

    function applyViewportGlobeControls() {
        if (!globe) {
            return;
        }

        const controls = globe.controls();
        const tuning = currentViewportGlobeTuning();

        controls.enableDamping = true;
        controls.dampingFactor = tuning.dampingFactor;
        controls.autoRotate = false;
        controls.enablePan = false;
        controls.rotateSpeed = tuning.rotateSpeed;
        controls.zoomSpeed = tuning.zoomSpeed;
        controls.minDistance = tuning.minDistance;
        controls.maxDistance = tuning.maxDistance;
    }

    function currentViewportGlobeTuning() {
        if (isCompactViewport()) {
            return {
                initialAltitude: 2.02,
                focusAltitude: 1.62,
                focusAnimationMs: 720,
                rotateSpeed: 0.46,
                dampingFactor: 0.11,
                zoomSpeed: 0.76,
                minDistance: 165,
                maxDistance: 360
            };
        }

        return {
            initialAltitude: 1.86,
            focusAltitude: 1.45,
            focusAnimationMs: 600,
            rotateSpeed: 0.62,
            dampingFactor: 0.08,
            zoomSpeed: 0.95,
            minDistance: 140,
            maxDistance: 420
        };
    }

    function isCompactViewport() {
        return window.innerWidth <= COMPACT_VIEWPORT_MAX_WIDTH;
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
            pointerIntent.pointerType = event.pointerType || "mouse";
            pointerIntent.selectionBlockedUntil = 0;
        });

        globeStage.addEventListener("pointermove", (event) => {
            if (!pointerIntent.tracking || pointerIntent.suppressSelection) {
                return;
            }

            const distance = Math.hypot(
                event.clientX - pointerIntent.startX,
                event.clientY - pointerIntent.startY
            );

            if (distance > currentPointerDragThreshold()) {
                suppressSelectionAfterDrag();
            }
        });

        globeStage.addEventListener("pointerup", () => {
            pointerIntent.tracking = false;
            if (Date.now() >= pointerIntent.selectionBlockedUntil) {
                pointerIntent.suppressSelection = false;
            }
        });

        globeStage.addEventListener("pointercancel", () => {
            pointerIntent.tracking = false;
            pointerIntent.suppressSelection = false;
            pointerIntent.selectionBlockedUntil = 0;
        });

        globeStage.addEventListener("pointerleave", () => {
            if (pointerIntent.tracking) {
                suppressSelectionAfterDrag();
            }
        });
    }

    function currentPointerDragThreshold() {
        if (pointerIntent.pointerType === "touch") {
            return POINTER_DRAG_THRESHOLD_TOUCH;
        }

        if (pointerIntent.pointerType === "pen") {
            return POINTER_DRAG_THRESHOLD_PEN;
        }

        return POINTER_DRAG_THRESHOLD_MOUSE;
    }

    function suppressSelectionAfterDrag() {
        pointerIntent.suppressSelection = true;
        pointerIntent.selectionBlockedUntil = Date.now() + POINTER_SELECTION_SUPPRESSION_WINDOW_MS;
    }

    function shouldIgnoreSelection() {
        const blockedByDrag = pointerIntent.suppressSelection;
        const blockedByCooldown = Date.now() < pointerIntent.selectionBlockedUntil;

        if (blockedByDrag) {
            pointerIntent.suppressSelection = false;
        }

        return blockedByDrag || blockedByCooldown;
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

    if (payload.correct) {
        target.innerHTML = `
            <h3>정답입니다.</h3>
            <p>획득 점수: ${payload.awardedScore}</p>
        `;
        return;
    }

    const summary = `${payload.selectedCountryName} 선택은 오답입니다.`;
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
    return "잠시 뒤 같은 Stage를 다시 시도할 수 있습니다.";
}

function renderLevelCopy(stageHintTarget) {
    if (stageHintTarget) {
        stageHintTarget.textContent = "지구본에서 나라를 찾아 선택한 뒤 제출하세요.";
    }
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
