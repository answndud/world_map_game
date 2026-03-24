# AI Agent Operating Model

## 목적

이 문서는 WorldMap 프로젝트를 `AI 에이전트와 함께 개발할 때의 운영 기준`을 정리한다.

초점은 두 가지다.

1. 개발용 AI 운영
   - Codex / Claude Code 같은 에이전트를 어떻게 쓸지
   - `AGENTS.md`, `docs/`, `skills`, `subagents`를 어떻게 나눌지
2. 서비스용 AI 기능 운영
   - 추천 기능에서 AI를 어디까지 서비스 안에 넣지 않을지
   - 어디까지를 서버가 결정하고, 어디서 AI를 오프라인 개선 도구로 쓸지

이 문서는 `2026-03-22` 기준 공식 문서와 공식 블로그를 읽고 정리한 설계 메모다.

## 먼저 결론

이 프로젝트는 처음부터 복잡한 멀티 에이전트 구조를 도입할 필요가 없다.

권장 방향은 아래다.

1. 개발은 `단일 메인 에이전트 + 필요한 경우에만 보조 에이전트`로 시작한다.
2. `AGENTS.md`는 짧고 방향성만 주는 문서로 유지한다.
3. 상세 규칙과 근거는 `docs/`에 쌓는다.
4. 스킬은 넓은 만능 스킬이 아니라 `좁고 반복적인 작업`에만 만든다.
5. 실제 서비스 런타임에는 LLM을 넣지 않고, AI는 설문/시나리오 개선용 오프라인 워크플로우로 제한한다.
6. 멀티 에이전트는 `병렬화 이득이 명확할 때만` 쓴다.

## 현재 구현 상태

현재 실제로 만든 커스텀 스킬은 아래 하나다.

- `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync`
  - 기능 작업 이후 `docs/WORKLOG.md`, `docs/PORTFOLIO_PLAYBOOK.md`, `README.md`, `blog/` 업데이트를 판단하고 동기화하는 스킬

나머지 추천 스킬은 아직 설계 후보 상태다.

## 공식 레퍼런스에서 읽은 핵심 신호

### OpenAI

#### 1. Responses API가 에이전트 구축의 기본 방향

- OpenAI는 2025-03-11 공개 글에서 `Responses API represents the future direction for building agents`라고 설명했다.
- 즉, 서비스 안에 실제 에이전트형 LLM 기능을 넣을 일이 생기면 예전 Assistants 중심 사고보다 `Responses API 중심`으로 보는 것이 맞다.

### 2. 에이전트의 기본 구성은 모델, 도구, 지침

- OpenAI의 "A practical guide to building agents"는 에이전트의 핵심 요소를 `Model`, `Tools`, `Instructions` 세 가지로 정리한다.
- 이건 개발용 에이전트 운영에도 그대로 적용된다.
  - Model: 어떤 모델을 어떤 작업에 쓸 것인가
  - Tools: 어떤 도구를 허용할 것인가
  - Instructions: 어떤 규칙과 가드레일을 줄 것인가

### 3. 단일 에이전트로 시작하고 필요할 때만 멀티 에이전트

- OpenAI 가이드는 `starting with a single agent and evolving to multi-agent systems only when needed`라고 정리한다.
- 즉, 처음부터 멀티 에이전트를 고집하는 것은 좋은 기본값이 아니다.

### 4. AGENTS.md는 백과사전보다 지도여야 함

- OpenAI의 2026-02-11 글 "Harness engineering"은 `give Codex a map, not a 1,000-page instruction manual`이라고 설명한다.
- 같은 글에서 `AGENTS.md`는 짧은 목차 역할, 실제 지식 저장소는 구조화된 `docs/` 디렉터리여야 한다고 권장한다.
- 이 프로젝트에도 이 원칙을 그대로 적용하는 편이 맞다.

### 5. 에이전트 품질은 코드보다 환경과 피드백 루프에 더 크게 좌우됨

- 같은 글은 에이전트 협업에서 중요한 것이 코드 작성 자체보다 `tooling, abstractions, feedback loops`라고 말한다.
- 따라서 테스트, 문서, 실행 절차, 기록 체계가 곧 에이전트 품질이다.

### Anthropic / Claude

#### 6. 서브에이전트는 각자 독립된 컨텍스트와 제한된 도구를 가져야 함

- Claude Code의 subagents 문서는 각 subagent가 `its own context window`, `custom system prompt`, `specific tool access`를 가진다고 설명한다.
- 또한 best practice로 아래를 제시한다.
  - 한 에이전트는 하나의 구체적 작업에 집중
  - description은 명확하게 작성
  - tool access는 최소 권한으로 제한
  - 프로젝트 수준 설정은 버전 관리에 포함

### 7. 멀티 에이전트는 병렬 탐색형 작업에는 강하지만, 대부분의 코딩 작업에는 기본값이 아님

- Anthropic의 2025-06-13 글 "How we built our multi-agent research system"은 멀티 에이전트가 `breadth-first queries`와 병렬 탐색에 강하다고 설명한다.
- 동시에 같은 글은 `most coding tasks involve fewer truly parallelizable tasks than research`라고 지적한다.
- 따라서 WorldMap에서는 멀티 에이전트를 `리서치, 자료 수집, 리뷰, 병렬 문서화`에 우선 쓰고, 핵심 구현은 단일 흐름으로 가는 편이 낫다.

### 8. 멀티 에이전트는 비용과 토큰 사용량이 커진다

- 같은 Anthropic 글은 대략 `agents use about 4x more tokens than chat`, `multi-agent systems use about 15x more tokens than chats`라고 설명한다.
- 즉, 병렬화 이득이 없는 작업에 멀티 에이전트를 쓰는 것은 낭비일 수 있다.

### 9. 프롬프트 엔지니어링 전에 성공 기준과 평가가 먼저 있어야 함

- Anthropic의 prompt engineering overview는 아래 세 가지를 먼저 요구한다.
  - 명확한 성공 기준
  - 그 기준을 검증할 방법
  - 개선할 첫 프롬프트 초안
- 설문 개선용 AI 워크플로우를 붙일 때도 이 순서를 따라야 한다.

### 10. 반복 프롬프트는 템플릿과 변수로 관리해야 함

- Anthropic 문서는 반복되는 프롬프트는 `prompt templates and variables`로 관리하라고 권장한다.
- 이 프로젝트의 설문 비판 / 시나리오 생성 프롬프트도 하드코딩 문자열보다 템플릿으로 분리하는 편이 맞다.

### 11. 가드레일은 입력 검증, 프롬프트 설계, 모니터링의 다층 방어여야 함

- Anthropic의 가드레일 문서는 input validation, prompt engineering, continuous monitoring을 함께 쓰라고 권장한다.
- OpenAI 가이드도 가드레일을 layered defense로 설명한다.

## 이 프로젝트에 적용한 운영 원칙

### 원칙 1. `AGENTS.md`는 짧게, `docs/`는 깊게

이 프로젝트에서는 아래처럼 역할을 나눈다.

- `AGENTS.md`
  - 에이전트가 시작할 때 읽는 짧은 지도
  - 문서 우선순위와 작업 절차 요약
- `docs/`
  - 실제 설계 근거, 단계, AI 운영 정책, 작업 기록
- `blog/`
  - 초보자 기준의 공개용 설명 문서
  - 의미 있는 기능 조각은 구현과 같은 턴에 같이 남기는 공개 설명 레이어

즉, `AGENTS.md`에 모든 세부 규칙을 우겨넣지 않는다.

### 원칙 2. 개발용 에이전트는 기본적으로 단일 흐름

기본값은 아래다.

- 메인 에이전트 1개가 현재 작업을 끝까지 책임진다.
- 병렬화가 명확히 유리한 경우에만 보조 에이전트를 붙인다.

이 프로젝트에서 병렬화가 유리한 경우는 대체로 아래뿐이다.

- 공식 문서 / 레퍼런스 조사
- 국가 데이터 출처 조사와 정규화
- 구현 후 read-only 리뷰
- 구현 완료 후 블로그/문서 동시 작성

반대로 아래 작업은 기본적으로 단일 흐름이 더 낫다.

- 도메인 모델링
- 상태 전이 로직 구현
- 트랜잭션 경계 설계
- 테스트 고치기와 코드 수정이 강하게 엮인 작업

### 원칙 3. 스킬은 좁고 반복적인 작업에만 만든다

스킬은 "무엇이든 해주는 도우미"가 아니라, 반복적으로 같은 구조가 나오는 작업에만 만드는 것이 맞다.

이 프로젝트에서 바로 가치가 있는 스킬은 아래 4개다.

## 추천 스킬 구성

### 1. `worldmap-doc-sync`

목적:

- 기능 작업 후 문서를 빠뜨리지 않게 한다.

트리거 예시:

- 게임 기능을 구현했다
- 새로운 API를 추가했다
- 테스트와 함께 설명 문서를 업데이트해야 한다

해야 할 일:

- `docs/PORTFOLIO_PLAYBOOK.md` 상태 갱신
- `docs/WORKLOG.md` 템플릿에 맞춰 기록
- 공개 설명이 필요한 기능 조각이면 `blog/NN-*.md`를 같은 턴에 작성 또는 갱신
- 필요하면 `README.md` 갱신 포인트 제안

번들 리소스 후보:

- `references/doc-impact-map.md`
- `references/worklog-entry-template.md`
- `references/blog-update-rules.md`

구현 상태:

- 구현 완료
- 프로젝트 로컬 설치 위치: `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync`
- `agents/openai.yaml`에서 `allow_implicit_invocation: false`로 설정해 명시적으로만 사용

이 스킬은 문서 누락 방지를 위해 가장 먼저 만든다.

### 2. `worldmap-country-data`

목적:

- 국가 시드 데이터 수집, 정규화, 검증 절차를 일관되게 만든다.

트리거 예시:

- 국가 데이터 CSV/JSON을 추가한다
- 위경도, ISO 코드, 인구수 필드를 정리한다
- 데이터 출처와 필드 정의를 문서화한다

해야 할 일:

- 입력 포맷 검증
- 필드명 통일
- 누락값 / 중복값 점검
- 출처 기록 템플릿 적용

번들 리소스 후보:

- `scripts/validate_country_seed.*`
- `references/country-schema.md`
- `references/data-provenance-checklist.md`

### 3. `worldmap-game-domain`

목적:

- 게임 기능 구현 시 세션, 라운드, 점수, 상태 전이 규칙을 놓치지 않게 한다.

트리거 예시:

- 위치 찾기 게임 API 구현
- 인구수 게임 판정 로직 구현
- 세션 / 라운드 엔티티 변경

해야 할 일:

- `game_session`, `game_round` 책임 점검
- 상태 전이 체크
- 정답 판정이 서버에 남아 있는지 확인
- 테스트 체크리스트 적용

번들 리소스 후보:

- `references/game-state-machine.md`
- `references/testing-checklist.md`

### 4. `worldmap-ai-recommendation`

목적:

- 추천 기능의 오프라인 설문 개선과 시나리오 평가 방식을 안정적으로 관리한다.

트리거 예시:

- 설문 문항 후보 초안 생성
- 서브 에이전트로 페르소나 시나리오 생성
- 설문 버전과 엔진 버전 품질 비교 평가

해야 할 일:

- 설문 버전 실험 규칙 관리
- 페르소나 시나리오 세트 관리
- 기대 후보 / 기대 만족도 기준 정리
- 서브 에이전트 평가 체크리스트 관리

번들 리소스 후보:

- `references/prompt-template.md`
- `references/eval-cases.md`
- `assets/fallback-messages.json`

## 나중에 추가할 스킬

처음부터 다 만들지 말고, 필요할 때 추가한다.

- `worldmap-ranking-redis`
  - Redis 키 전략, Sorted Set 갱신, 일간/전체 랭킹 규칙
- `worldmap-blog-post`
  - 구현 완료 후 `blog/NN-*.md` 글 초안 생성
- `worldmap-thymeleaf-leaflet-ui`
  - SSR + JS + Leaflet 상호작용 패턴 정리

## 추천 에이전트 구성

### A. Codex 기준

이 저장소에서는 우선 아래 조합이 적절하다.

#### 1. 메인 에이전트

역할:

- 현재 기능의 최종 구현 책임
- 설계 판단
- 코드 수정
- 테스트 실행
- 문서 갱신

기본값으로 항상 메인이다.

#### 2. `explorer` 계열

적합한 작업:

- 공식 문서 조사
- 코드베이스 구조 파악
- 특정 파일 / 클래스 위치 찾기
- read-only 비교 분석

사용 원칙:

- 바로 다음 코드 수정이 explorer 결과에 막혀 있지 않을 때만 병렬로 쓴다.

#### 3. `worker` 계열

적합한 작업:

- 서로 다른 파일 집합을 가진 병렬 구현
- 한정된 문서 작성
- 독립적인 테스트 보강

사용 원칙:

- 쓰기 범위가 겹치지 않을 때만 사용
- 상태 전이, 트랜잭션, 엔티티 설계처럼 결합도가 높은 작업에는 기본적으로 쓰지 않음

### B. Claude Code 기준

Claude Code를 같이 쓴다면 아래처럼 `focused subagent`만 두는 편이 좋다.

#### 1. `architecture-auditor`

- read-only
- 책임: 현재 구조가 README / Playbook의 설계 원칙과 맞는지 점검

#### 2. `country-data-curator`

- 데이터 폴더와 검증 스크립트만 수정 가능
- 책임: 국가 데이터 정규화와 검증

#### 3. `test-reviewer`

- read-only 또는 test 디렉터리만 수정 가능
- 책임: 핵심 비즈니스 로직 테스트 누락 점검

#### 4. `blog-writer`

- `blog/`와 `docs/WORKLOG.md`만 수정 가능
- 책임: 구현 이후 설명 글 초안 작성

Claude 공식 문서 기준으로도, description을 명확히 쓰고 tool access를 최소 권한으로 제한하는 편이 맞다.

## 이 프로젝트에서 멀티 에이전트를 써도 되는 경우 / 안 되는 경우

### 써도 되는 경우

- 여러 공식 레퍼런스를 병렬 조사하는 작업
- 국가 데이터 출처를 여러 소스에서 비교하는 작업
- 구현이 끝난 뒤 read-only 리뷰와 문서 초안을 병렬로 만드는 작업
- 파일 쓰기 범위가 완전히 분리된 구현 작업

### 기본적으로 쓰지 않는 경우

- 핵심 도메인 모델 첫 설계
- 컨트롤러 / 서비스 / 엔티티 책임 분리 결정
- 게임 상태 전이 버그 수정
- 여러 파일에 걸친 리팩터링인데 추상화 경계가 아직 불안정한 경우

이 판단은 Anthropic의 "대부분의 코딩 작업은 병렬화 이득이 작다"는 관찰과 맞닿아 있다.

## 서비스와 분리한 오프라인 AI 개선 구조

이 프로젝트는 추천 기능 때문에 AI를 활용할 수 있다.
하지만 AI는 서비스 런타임 호출이 아니라 `설문/시나리오 개선 워크플로우`에만 둔다.

### 권장 구조

- `RecommendationEngine`
  - 설문 응답을 점수화하고 상위 국가를 계산
- `SurveyVersionCatalog`
  - 설문 문항과 선택지 버전 관리
- `PersonaEvalSet`
  - 오프라인 평가 시나리오 관리
- `AiSurveyImprovementLoop`
  - 서브 에이전트로 문항 초안, 비판, 시나리오 비교를 수행

### 왜 이렇게 나누는가

- 추천 결과 자체는 서버가 결정한다.
- 런타임 과금과 외부 의존성이 없다.
- 같은 입력에 같은 결과를 테스트로 고정하기 쉽다.
- AI 품질 실험은 개발 단계에서만 별도로 돌릴 수 있다.

## OpenAI와 Claude를 어디에 쓸 것인가

이 프로젝트에서 OpenAI나 Claude는 서비스 기능보다 `개발용 품질 개선 도구`에 가깝다.

권장 사용처는 아래다.

- 설문 문항 초안 생성
- 질문 중복 / 모호성 비판
- 페르소나 시나리오 대량 생성
- 기대 후보 국가와 만족도 기준 비교

즉, 모델은 “사용자에게 보여 줄 실시간 설명 생성기”가 아니라 “설문과 평가 자산을 더 빠르게 만드는 오프라인 도구”다.

## 지금 문서 구조에서 바꾸면 좋은 점

현재 구조는 이미 나쁘지 않다.
다만 OpenAI의 Codex 운영 방식과 맞추려면 장기적으로 아래 방향이 더 좋다.

### 유지

- `README.md`
- `AGENTS.md`
- `docs/PORTFOLIO_PLAYBOOK.md`
- `docs/WORKLOG.md`
- `blog/`

### 추가 권장

- `docs/AI_AGENT_OPERATING_MODEL.md`
  - 이 문서
- `docs/references/`
  - 외부 기술 레퍼런스 요약
- `docs/exec-plans/active/`
  - 현재 큰 작업의 실행 계획
- `docs/exec-plans/completed/`
  - 완료한 큰 작업 기록

즉, `AGENTS.md`를 계속 키우기보다 `docs/`로 분해해 가는 편이 맞다.

## 하지 말아야 할 것

- 처음부터 멀티 에이전트 구조를 표준처럼 도입하지 않는다.
- 모든 작업에 skill을 만들지 않는다.
- 서비스의 추천 결과 계산을 모델에게 맡기지 않는다.
- prompt를 코드 문자열에 여기저기 흩뿌리지 않는다.
- 문서와 테스트 없이 AI가 만든 코드만 쌓지 않는다.

## 추천 실행 순서

### 1단계

- 현재 `AGENTS.md`는 유지
- 이 문서를 기준으로 AI 운영 원칙을 고정
- `worldmap-doc-sync` 스킬을 문서 동기화 기본 도구로 사용
- 실제 기능 구현은 계속 `단일 메인 에이전트` 중심으로 진행

### 2단계

- 스프링부트 뼈대 생성
- 국가 데이터 작업 직전에 `worldmap-country-data` 스킬 설계

### 3단계

- 위치 찾기 게임 구현 직전에 `worldmap-game-domain` 스킬 설계
- 추천 기능 직전에 `worldmap-ai-recommendation` 스킬 설계

### 4단계

- 필요성이 확인되면 그때만 `ranking`, `blog-post` 스킬 추가
- 병렬 작업이 늘어날 때만 보조 에이전트 구성을 구체화

## 내가 최종 추천하는 운영안

가장 현실적인 권장안은 아래다.

1. 개발용 에이전트 운영은 `AGENTS.md + docs/ + Work Log` 중심으로 간다.
2. 구현은 기본적으로 단일 메인 에이전트가 맡는다.
3. 스킬은 4개만 먼저 준비한다.
   - `worldmap-doc-sync`
   - `worldmap-country-data`
   - `worldmap-game-domain`
   - `worldmap-ai-recommendation`
4. 서비스 런타임에는 LLM을 붙이지 않는다.
5. 멀티 에이전트는 `리서치 / 리뷰 / 병렬 문서화 / 설문 시나리오 생성`에 한정한다.

이 프로젝트에 가장 중요한 것은 "에이전트를 많이 쓰는 것"이 아니라 `에이전트가 만든 결과를 내가 끝까지 설명할 수 있게 만드는 것`이다.

## 참고한 공식 레퍼런스

### OpenAI

- [New tools for building agents](https://openai.com/index/new-tools-for-building-agents/)
- [A practical guide to building agents](https://openai.com/business/guides-and-resources/a-practical-guide-to-building-ai-agents/)
- [Agents SDK](https://developers.openai.com/api/docs/guides/agents-sdk)
- [Harness engineering: leveraging Codex in an agent-first world](https://openai.com/index/harness-engineering/)
- [Introducing Codex](https://openai.com/index/introducing-codex/)

### Anthropic

- [Subagents](https://code.claude.com/docs/en/sub-agents)
- [How we built our multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system)
- [Prompt engineering overview](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/overview)
- [Tool use with Claude](https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview)
- [Console prompting tools](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prompting-tools)
- [Mitigate jailbreaks and prompt injections](https://platform.claude.com/docs/en/test-and-evaluate/strengthen-guardrails/mitigate-jailbreaks)
