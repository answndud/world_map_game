# [Spring Boot 게임 플랫폼 포트폴리오] 104. demo-lite의 마지막 retained surface로 compact recommendation loop를 여는 이유

## 1. 이번 글에서 닫을 문제

`demo-lite`에 수도, 국기, 인구 비교 배틀까지 붙인 뒤에도,
retained surface 하나가 여전히 비어 있었습니다.

> 추천은 retained route에만 있고, 실제 결과 loop는 아직 없는 상태

이 상태로는 demo-lite가 “게임 3종 체험판”까지는 설명돼도,
현재 제품의 또 다른 축인 `설문 기반 추천`은 보여 주지 못합니다.

그래서 이번 조각의 목표는 `recommendation`도 shell이 아니라 실제 결과 loop로 바꾸는 것이었습니다.

다만 메인 앱의 20문항과 feedback 저장, dashboard review를 그대로 옮기지는 않습니다.
free-tier static app에 맞게 **compact deterministic slice**만 남기는 것이 이번 설계의 핵심입니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/features/recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
- [demo-lite/tests/recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)
- [demo-lite/src/app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [demo-lite/src/routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [demo-lite/src/style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)

문서도 같이 맞췄습니다.

- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/DEMO_LITE_DECOMPOSITION_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_DECOMPOSITION_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 compact 설문으로 줄였나

메인 앱 추천은 현재 20문항입니다.

- 기후
- 계절
- 속도
- crowd
- 비용·품질
- 주거
- 환경
- 이동
- 영어
- 정착 친화도
- 안전
- 공공 서비스
- 디지털
- 음식
- 다양성
- 문화
- work-life
- settlement
- future base
- 기타 보정 축

이걸 demo-lite에 그대로 옮기면,
free-tier 공개용 별도 앱이 오히려 너무 무거워집니다.

그래서 이번에는 아래 6문항만 retained했습니다.

1. 기후
2. 생활 속도
3. 비용·품질 기준
4. 영어 지원 필요도
5. 안전 우선도
6. 도시 편의 vs 자연 접근성

즉 “현재 추천이 어떤 trade-off를 보고 나라를 고르는가”는 유지하되,
문항 수와 운영 부가는 과감히 줄였습니다.

## 4. 무엇을 유지하고 무엇을 버렸나

### 유지한 것

- deterministic scoring
- top 3 결과
- 나라별 간단한 headline
- 수도/인구 같은 기본 metadata
- 내가 고른 답 요약

### 버린 것

- 20문항 full parity
- feedback token
- 만족도 저장
- `/api/recommendation/feedback`
- dashboard review
- persona baseline

즉 demo-lite recommendation은 “메인 추천의 축소판”이지,
“메인 추천과 완전히 같은 제품”은 아닙니다.

## 5. 데이터는 어떻게 만들었나

이번 조각도 Spring Boot recommendation service를 재사용하지 않습니다.

대신 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js) 안에
대표 국가 프로필만 얇게 다시 정의했습니다.

예를 들어 각 프로필은 아래 축만 가집니다.

- `climate`
- `pace`
- `priceLevel`
- `englishSupport`
- `safety`
- `urbanity`

그리고 generated [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)과 `iso3Code`로 join해서,

- 나라 이름
- 영어 이름
- 수도
- 인구

같은 결과 카드 metadata를 가져옵니다.

즉 shared contract는 여전히 **정적 국가 데이터**이고,
추천 profile table만 demo-lite 안에서 local-state용으로 다시 정의한 것입니다.

## 6. 왜 이 로직을 `app.js`가 아니라 `recommendation.js`에 뒀나

[app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)의 책임은 여전히 route shell입니다.

- 현재 route 결정
- 공통 header/navigation 렌더링
- route별 mount 연결

반면 추천 loop의 책임은 다릅니다.

- 질문 카탈로그
- 답 선택 상태
- profile join
- 점수 계산
- top 3 정렬
- 결과 화면 렌더링

이건 route shell이 아니라 **추천 규칙**이므로,
[recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 모았습니다.

## 7. 새 브라우저 흐름

메인 Spring Boot 앱의 요청 흐름은 바뀌지 않았습니다.

이번 조각에서 새로 닫은 건 demo-lite의 브라우저 흐름입니다.

1. 브라우저가 [index.html](/Users/alex/project/worldmap/demo-lite/index.html)을 연다
2. [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)가 현재 hash route를 읽는다
3. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 `#/recommendation`이면 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)를 mount한다
4. [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 generated countries JSON을 읽어 local profile과 join한다
5. 사용자가 6문항에 답하면 브라우저 안에서 top 3 결과를 계산해 바로 결과 화면으로 바꾼다

즉 이 조각도 API 요청이 아니라 **브라우저 안에서 닫는 deterministic recommendation loop**입니다.

## 8. 어떤 시나리오를 먼저 고정했나

이번 추천은 질문 수를 줄였기 때문에,
최소한 대표 성향이 엉뚱하게 뒤집히지 않는지 먼저 테스트해야 했습니다.

그래서 [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)에서 아래 두 시나리오를 먼저 고정했습니다.

### 8-1. warm city

- 따뜻한 기후
- 빠른 도시 리듬
- 품질 우선
- 영어 높음
- 안전 높음
- 도시 편의 우선

이 경우 `싱가포르`가 1위가 되도록 고정했습니다.

### 8-2. cool nature

- 선선한 기후
- 느긋한 생활 리듬
- 적당한 비용 균형
- 영어 높음
- 안전 높음
- 자연 접근성 우선

이 경우 `뉴질랜드` 또는 `캐나다`가 1위권으로 올라오고,
`스웨덴`도 top 3에 남는지를 고정했습니다.

즉 질문 수를 줄여도 현재 제품의 추천 방향성이 완전히 무너지지 않는지 먼저 본 것입니다.

## 9. 테스트는 무엇으로 닫았나

이번에도 Node 내장 테스트로 pure function을 먼저 고정했습니다.

```bash
cd demo-lite
npm test
```

[recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)는 아래를 확인합니다.

- 답 요약과 top 3 결과 생성
- warm city 시나리오에서 `SGP` 1위
- cool nature 시나리오에서 `NZL` 또는 `CAN` 상위

그리고 정적 앱 전체는 아래로 다시 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
“compact 질문 축으로도 deterministic top 3가 무너지지 않는가”와
“static build가 계속 깨지지 않는가”였습니다.

## 10. 지금 남은 것

이제 demo-lite retained surface는 전부 playable합니다.

- `capital`
- `flag`
- `population-battle`
- `recommendation`

남은 것은 기능 확장보다 polish 쪽입니다.

예를 들면:

1. 브라우저 단위 recent play/history
2. cross-mode summary
3. recommendation compact survey의 copy 다듬기

즉 다음 조각은 “새 retained surface 추가”가 아니라,
이미 열린 demo-lite 전체의 마감도를 높이는 작업이 될 가능성이 큽니다.

## 11. 30초 답변

demo-lite의 마지막 retained surface로 compact recommendation loop를 열었습니다. 메인 Spring Boot 추천 엔진은 건드리지 않고, generated countries와 local profile table을 `iso3Code`로 join해서 6문항 설문과 deterministic top 3 결과를 브라우저 안에서 계산합니다. 핵심은 free-tier 공개용 앱에서도 추천 결과까지 shell이 아니라 실제 결과 loop로 닫았다는 점이고, 이를 `recommendation.test.mjs`와 `npm run build`로 함께 고정했다는 것입니다.
