# [Spring Boot 포트폴리오] 15. 페르소나 평가로 survey v2 개정안 만들기

## 이번 글의 핵심 질문

오프라인 평가 시나리오를 만들었다면, 그 다음에는 실제로 현재 추천 엔진을 한 번 돌려 봐야 한다.

이번 단계의 질문은 이것이다.

“현재 추천 엔진이 어떤 페르소나에서는 잘 맞고, 어떤 페르소나에서는 왜 어긋나는지 어떻게 읽어야 할까?”

이번 글에서는 14개 페르소나 시나리오를 코드 기반 baseline으로 고정하고, 그 결과를 바탕으로 `survey v2` 개정안을 정리했다.

## 왜 이 단계가 필요한가

문서만으로 “이 설문이 괜찮다”라고 말하는 건 약하다.

실제로는 최소한 아래가 있어야 한다.

1. 평가 시나리오를 코드로 다시 돌릴 수 있어야 한다.
2. 현재 baseline이 어느 정도인지 숫자로 말할 수 있어야 한다.
3. 어떤 시나리오가 약한지 보고 다음 개정안을 만들 수 있어야 한다.

그래서 이번 단계에서는 설문 개선 루프를 실제 baseline 평가로 한 번 닫았다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaScenario.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
- `/Users/alex/project/worldmap/docs/recommendation/SURVEY_V2_PROPOSAL.md`

## baseline은 어떻게 고정했는가

이번에는 페르소나 표를 그냥 Markdown에만 두지 않았다.

`RecommendationOfflinePersonaFixtures`로 14개 시나리오를 테스트 코드 안에도 옮겼다.

그리고 `RecommendationOfflinePersonaCoverageTest`에서 아래를 확인한다.

1. 현재 엔진이 14개 시나리오 중 최소 11개에서 기대 후보 1개 이상을 top 3에 포함하는가
2. 핵심 anchor 시나리오(`P01`, `P02`, `P14`)는 여전히 원하는 방향으로 결과가 나오는가

이렇게 해야 다음에 가중치를 바꾸더라도 “어디가 좋아지고, 어디가 깨졌는가”를 바로 볼 수 있다.

## 현재 baseline에서 강한 시나리오

현재 엔진은 아래 영역에서 강했다.

- `P01`: 싱가포르, 아랍에미리트, 미국
- `P02`: 말레이시아, 태국, 브라질
- `P07`: 대한민국, 싱가포르, 브라질
- `P14`: 말레이시아, 태국, 호주

즉, 지금 엔진은 `동남아 저물가`, `고도시 영어권`, `음식 중심 대도시` 축은 비교적 잘 맞는다.

## 현재 baseline에서 약한 시나리오

반대로 아래는 먼저 손봐야 한다.

### `P04`

- 기대: 독일, 덴마크, 캐나다
- 실제: 우루과이, 칠레, 스페인

이건 복지 우선 사용자에게서 `복지 그 자체`보다 기후/물가 균형이 너무 앞서는 문제다.

### `P06`

- 기대: 포르투갈, 스페인, 체코
- 실제: 우루과이, 아일랜드, 말레이시아

이건 `저예산 제약`이 아직 충분히 강하지 않다는 신호다.

### `P13`

- 기대: 영국, 캐나다, 네덜란드
- 실제: 미국, 싱가포르, 아랍에미리트

이건 영어와 도시성 점수가 너무 강해서, `온화한 기후` 선호가 밀리는 문제다.

## 그래서 v2는 무엇을 먼저 바꾸는가

이번 제안은 질문 수를 늘리는 방향이 아니다.

먼저 아래 둘만 손본다.

1. `engine-v2`
   - climate mismatch penalty 추가
   - 영어 점수 비중 재조정
   - 저예산 초과 penalty 강화
2. `survey-v2`
   - budget helper text를 더 명확하게
   - welfare 라벨을 `복지 / 공공 서비스`로 정밀화

즉, 문항 구조를 뒤엎기 전에 `현재 6문항 구조를 유지한 채 설명과 가중치부터 다듬는 것`이 먼저다.

## 왜 이 로직은 테스트와 문서를 같이 가져가야 하는가

설문 개선은 감으로만 하면 안 된다.

이번 단계에서 중요한 건 두 가지다.

1. 테스트는 현재 baseline을 고정한다.
2. 문서는 왜 그 baseline이 부족한지와 다음 조정 방향을 설명한다.

즉, 테스트는 “현재 상태의 품질 하한”을 지키고, 문서는 “왜 다음 버전을 바꾸는가”를 설명하는 역할을 맡는다.

## 면접에서는 이렇게 설명하면 된다

“추천 품질 개선을 감으로 하지 않기 위해, 14개 페르소나 시나리오를 테스트 코드로 옮겨 baseline을 고정했습니다. 현재 엔진은 11개 시나리오에서 기대 후보를 top 3 안에 포함하고, 약한 케이스는 복지형, 저예산 안전형, 온화한 고도시 다양성형이었습니다. 그래서 survey v2에서는 질문 수를 늘리기보다 먼저 climate mismatch penalty, 영어 가중치, 저예산 penalty를 조정하는 방향으로 개정안을 만들었습니다.”

## 다음 글

다음 단계는 바로 production 점수식을 바꾸는 것이 아니다.

먼저 현재 `engine-v1`의 14개 페르소나 top 3 결과를 snapshot으로 고정한다.

그래야 다음 `engine-v2` 실험에서 “coverage 숫자”뿐 아니라 “어떤 시나리오의 top 3 순서가 어떻게 바뀌었는지”까지 안전하게 비교할 수 있다.
