# Work Log

## 목적

이 문서는 AI와 함께 개발하면서도, 나중에 내가 직접 설명할 수 있도록 작업 흔적을 남기는 용도다.

중요한 것은 "무엇을 바꿨는가"보다 아래를 기록하는 것이다.

- 왜 바꿨는가
- 어떤 흐름이 생겼는가
- 무엇이 아직 헷갈리는가

## 작성 규칙

- 의미 있는 기능 작업마다 1개 항목을 추가한다.
- 짧아도 되지만, 흐름과 이유는 반드시 적는다.
- 이해가 부족한 부분은 숨기지 말고 `아직 약한 부분`에 적는다.

## 템플릿

```md
## YYYY-MM-DD - 작업 이름

- 단계:
- 목적:
- 변경 파일:
- 요청 흐름:
- 데이터 / 상태 변화:
- 핵심 도메인 개념:
- 예외 / 엣지 케이스:
- 테스트:
- 배운 점:
- 아직 약한 부분:
- 면접용 30초 요약:
```

## 2026-03-22 - 문서 운영 구조 생성

- 단계: 0. 문서와 규칙 정리
- 목적: AI와 함께 개발해도 구현 순서, 이해 포인트, 작업 기록이 남도록 기본 문서 구조를 만든다.
- 변경 파일:
  - `README.md`
  - `AGENTS.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 아직 애플리케이션 요청 흐름은 없고, 개발 프로세스와 문서 흐름을 먼저 고정했다.
- 데이터 / 상태 변화: 코드 상태보다 프로젝트 운영 방식이 정리되었다. 앞으로는 기능 단위로 작업 후 문서를 같이 갱신해야 한다.
- 핵심 도메인 개념: 서버 주도 게임 상태 관리, 단계별 개발, 설명 가능한 구조, 작업 로그 누적
- 예외 / 엣지 케이스: 문서가 너무 많아져 관리가 어려워질 수 있으므로 역할을 분리했다. README는 개요, Playbook은 순서, Work Log는 작업 기록만 담당한다.
- 테스트: 없음
- 배운 점: AI 보조 개발에서는 코드보다 문서 운영 체계가 먼저 있어야 나중에 설명이 가능하다.
- 아직 약한 부분: 실제 구현이 시작되면 문서 갱신이 번거롭게 느껴질 수 있다. 이후 단계에서 너무 무거운 절차인지 검증이 필요하다.
- 면접용 30초 요약: 기능 구현 전에 프로젝트의 개발 순서, 문서화 기준, 작업 로그 규칙을 먼저 정리해 두어 AI와 함께 개발해도 설계 의도와 구현 흐름을 직접 설명할 수 있게 만들었다.

## 2026-03-22 - blog 워크스페이스 추가

- 단계: 0. 문서와 규칙 정리
- 목적: 참고 프로젝트의 `blog/` 폴더처럼, 초보자 기준으로 자세하게 설명하는 공개용 글 구조를 현재 프로젝트에도 추가한다.
- 변경 파일:
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/00_rebuild_guide.md`
  - `blog/_post_template.md`
  - `blog/01-why-worldmap-game-platform-domain.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 아직 애플리케이션 요청은 없고, 개발용 문서와 공개용 설명 문서를 분리하는 흐름을 먼저 만들었다.
- 데이터 / 상태 변화: 코드 상태 변화는 없고, 이제 프로젝트 문서가 `개발 SSOT`와 `블로그형 설명 문서`로 분리되었다.
- 핵심 도메인 개념: 서버 주도 게임 플랫폼, 단계별 학습, 글 템플릿 기반 설명, 공개용 연재 구조
- 예외 / 엣지 케이스: 실제 코드가 없는 상태에서 코드 설명 글을 과하게 쓰면 공허해질 수 있으므로, 현재는 방향 글과 템플릿까지만 만들고 구현 글은 코드 생성 이후에 채우도록 했다.
- 테스트: 없음
- 배운 점: 초보자 기준의 자세한 문서는 하나의 장문보다, 연재형 번호 문서와 템플릿으로 쪼개는 편이 훨씬 관리하기 쉽다.
- 아직 약한 부분: 이후 실제 기능 구현이 시작되면 `blog/`와 `docs/`의 중복이 생길 수 있다. 어느 문서가 근거 문서인지 계속 분명히 해야 한다.
- 면접용 30초 요약: 개발용 문서와 별도로 `blog/` 워크스페이스를 만들어, 각 기능을 초보자 기준으로 문제 정의부터 테스트와 취업 포인트까지 단계별 글로 정리할 수 있는 구조를 만들었다.

## 2026-03-22 - AI agent 운영 모델 조사 및 정리

- 단계: 0. 문서와 규칙 정리
- 목적: OpenAI, Anthropic의 공식 문서와 블로그를 바탕으로 이 프로젝트에 맞는 `AGENTS.md`, `docs`, `skills`, `subagents`, 런타임 LLM 구성 원칙을 정리한다.
- 변경 파일:
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `AGENTS.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 외부 공식 레퍼런스를 조사한 뒤, 그 결과를 현재 저장소 운영 원칙으로 번역해 문서화했다.
- 데이터 / 상태 변화: 코드 변화는 없고, 프로젝트의 AI 보조 개발 방식과 런타임 LLM 도입 원칙이 명시되었다.
- 핵심 도메인 개념: `AGENTS.md는 짧은 지도`, `docs는 시스템 오브 레코드`, `단일 메인 에이전트 우선`, `병렬화가 명확할 때만 멀티 에이전트`, `추천 결과는 서버가 결정하고 LLM은 설명만 담당`
- 예외 / 엣지 케이스: 공식 문서의 멀티 에이전트 권장 사례는 주로 리서치/탐색형 작업에 강하므로, 이를 게임 백엔드 구현에 그대로 일반화하면 오히려 복잡도만 늘어날 수 있다.
- 테스트: 없음
- 배운 점: 에이전트 품질은 모델 자체보다도 도구 접근, 문서 구조, 기록 체계, 피드백 루프 같은 저장소 운영 방식에 크게 좌우된다.
- 아직 약한 부분: 실제 Codex용 커스텀 스킬은 아직 만들지 않았다. 어떤 스킬을 언제 만드는 게 정말 반복 가치가 있는지는 첫 구현 단계에서 다시 검증해야 한다.
- 면접용 30초 요약: OpenAI와 Anthropic의 공식 문서를 조사해, 이 저장소에서는 멀티 에이전트를 기본값으로 두지 않고 `짧은 AGENTS.md + 구조화된 docs + 제한된 목적의 skills` 구조로 운영하는 것이 가장 설명 가능하고 유지보수하기 좋다고 정리했다.

## 2026-03-22 - 첫 번째 커스텀 스킬 `worldmap-doc-sync` 구현

- 단계: 0. 문서와 규칙 정리
- 목적: 기능 구현 후 문서 업데이트를 빠뜨리지 않도록, WorldMap 전용 문서 동기화 스킬을 실제로 만든다.
- 변경 파일:
  - `/Users/alex/.codex/skills/worldmap-doc-sync/SKILL.md`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/agents/openai.yaml`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/references/doc-impact-map.md`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/references/worklog-entry-template.md`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/references/blog-update-rules.md`
  - `AGENTS.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 기능 작업이 끝난 뒤 어떤 문서를 업데이트해야 하는지 판단하고, Work Log를 기본값으로 갱신한 뒤 Playbook, README, blog 필요 여부를 체크하는 후처리 흐름을 스킬로 고정했다.
- 데이터 / 상태 변화: 저장소 코드 상태는 바뀌지 않았지만, 이제 WorldMap 프로젝트에는 재사용 가능한 문서 동기화 스킬이 생겼고 AI 운영 방식이 추천 수준에서 구현 수준으로 한 단계 진전됐다.
- 핵심 도메인 개념: 문서 역할 분리, post-task documentation workflow, `AGENTS.md`는 짧은 지도, `docs/`는 근거 문서, `blog/`는 공개용 설명 문서
- 예외 / 엣지 케이스: 작은 수정까지 모든 상위 문서를 갱신하면 오히려 소음이 커질 수 있으므로, 이 스킬은 `WORKLOG는 기본`, `Playbook/README/blog는 조건부`라는 규칙으로 문서 업데이트 범위를 제한한다.
- 테스트: skill-creator의 `quick_validate.py`를 임시 가상환경에서 실행해 구조 검증 완료
- 배운 점: 스킬은 막연한 지침보다 특정 저장소의 문서 역할과 업데이트 순서를 고정하는 좁은 워크플로우일 때 가장 실용적이다.
- 아직 약한 부분: 실제 기능 구현 이후 이 스킬을 몇 번 사용해 보면서 체크리스트가 너무 무겁지 않은지 검증이 필요하다.
- 면접용 30초 요약: AI와 함께 개발할 때 가장 먼저 생기는 문제는 문서 누락이어서, WorldMap 전용 `worldmap-doc-sync` 스킬을 만들어 기능 작업 후 Work Log, Playbook, README, blog 업데이트 범위를 일관되게 판단하고 기록하도록 운영 체계를 고정했다.

## 2026-03-22 - `worldmap-doc-sync`를 프로젝트 로컬 스킬로 전면 수정

- 단계: 0. 문서와 규칙 정리
- 목적: `worldmap-doc-sync`가 모든 프로젝트에 보이는 전역 스킬이 아니라, WorldMap 저장소 안에서만 사용되는 프로젝트 로컬 스킬이 되도록 위치와 설정을 수정한다.
- 변경 파일:
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/SKILL.md`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/agents/openai.yaml`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/references/doc-impact-map.md`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/references/worklog-entry-template.md`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/references/blog-update-rules.md`
  - `AGENTS.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 전역 위치에 있던 스킬을 제거하고, 저장소 루트의 `.agents/skills/` 아래로 옮긴 뒤 문서에서 현재 경로와 사용 방식을 프로젝트 기준으로 다시 고정했다.
- 데이터 / 상태 변화: 이제 `worldmap-doc-sync`는 WorldMap 저장소 내부에만 존재하며, `agents/openai.yaml`에서 `allow_implicit_invocation: false`로 설정되어 이 프로젝트에서도 명시적으로만 사용된다.
- 핵심 도메인 개념: 프로젝트 로컬 스킬, explicit invocation, 전역 설정과 저장소 설정 분리, 문서 역할 일관성
- 예외 / 엣지 케이스: 기존 Work Log에는 전역 경로로 처음 생성했던 기록이 남아 있다. 이는 현재 상태가 아니라 역사 기록이므로, 최신 경로는 이 항목과 운영 문서를 기준으로 본다.
- 테스트: skill validator를 새 프로젝트 로컬 경로에 대해 다시 실행 예정
- 배운 점: 스킬은 저장 위치 자체가 동작 범위를 결정하므로, 프로젝트 전용 워크플로우는 처음부터 전역이 아니라 저장소 내부 `.agents/skills/`에 두는 편이 맞다.
- 아직 약한 부분: 현재 세션이 이미 시작된 뒤 경로를 옮긴 것이므로, UI나 자동 발견 상태는 새 세션에서 한 번 더 확인하는 것이 안전하다.
- 면접용 30초 요약: 처음에는 문서 동기화 스킬을 전역으로 만들었지만, 프로젝트 전용 규칙을 다른 저장소에 전파하면 오히려 혼란이 커질 수 있어서, 스킬을 WorldMap 저장소 내부 `.agents/skills/`로 옮기고 명시 호출 전용으로 바꿔 범위를 이 프로젝트로만 제한했다.

## 2026-03-22 - 1단계 스프링부트 프로젝트 뼈대 완료

- 단계: 1. 스프링부트 프로젝트 뼈대
- 목적: Spring Boot 3 기반의 실행 가능한 서버 뼈대, SSR 메인 페이지, 프로파일 분리, 개발용 DB/Redis 환경 구성을 한 번에 잡는다.
- 변경 파일:
  - `build.gradle`
  - `compose.yaml`
  - `HELP.md`
  - `src/main/java/com/worldmap/WorldMapApplication.java`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/java/com/worldmap/web/view/ModeCardView.java`
  - `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
  - `src/main/java/com/worldmap/common/response/ApiErrorResponse.java`
  - `src/main/java/com/worldmap/auth/package-info.java`
  - `src/main/java/com/worldmap/country/package-info.java`
  - `src/main/java/com/worldmap/game/package-info.java`
  - `src/main/java/com/worldmap/ranking/package-info.java`
  - `src/main/java/com/worldmap/recommendation/package-info.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/02-spring-boot-bootstrap.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 `/`로 접근하면 `HomeController`가 모드 카드, 핵심 원칙, 로드맵 데이터를 모델에 담아 `home.html`을 렌더링한다. 아직 영속 상태 변화는 없고, 현재 단계는 서버 뼈대와 렌더링 구조를 고정하는 데 집중했다.
- 데이터 / 상태 변화: 로컬 실행에서는 Docker Compose 기반 PostgreSQL / Redis를 붙일 수 있는 상태가 되었고, 테스트에서는 H2 메모리 DB를 사용하도록 분리했다. 프로젝트 상태는 `문서 전용 저장소`에서 `실행 가능한 Spring Boot 3 애플리케이션` 단계로 올라갔다.
- 핵심 도메인 개념: SSR + API 혼합 구조, 프로파일 분리, 패키지 구조 선고정, 공통 API 예외 응답, Docker Compose 기반 로컬 개발 환경
- 예외 / 엣지 케이스: Spring Boot 4로 생성된 초기 스캐폴드를 그대로 쓰지 않고 사용자가 요청한 Spring Boot 3 기준으로 수정했다. 테스트가 로컬 Docker 환경에 의존하지 않도록 `application-test.yml`에서 H2와 Docker Compose 비활성화 설정을 따로 둔 것이 핵심이었다.
- 테스트: `./gradlew test` 통과
- 배운 점: 뼈대 단계에서 버전, 패키지 구조, 프로파일, 예외 처리, 테스트 전략을 같이 고정해 두면 이후 기능 구현 속도보다 설명 가능성이 크게 좋아진다.
- 아직 약한 부분: 현재는 JPA/Redis 의존성만 연결된 상태이고 실제 엔티티와 저장 로직은 아직 없다. 다음 단계에서 country 시드 구조를 넣으면서 persistence 설정이 자연스럽게 이어지는지 다시 검증해야 한다.
- 면접용 30초 요약: WorldMap 프로젝트의 첫 단계에서는 Spring Boot 3.5 기반 서버 뼈대와 Thymeleaf SSR 홈 화면을 만들고, 로컬/테스트 프로파일을 분리했으며, 이후 게임 API 확장을 대비해 공통 예외 응답 구조와 패키지 경계를 먼저 고정했다.

## 2026-03-22 - 2단계 국가 데이터와 시드 적재 완료

- 단계: 2. 국가 데이터와 시드 적재
- 목적: 게임 출제와 추천 기능이 공통으로 사용할 국가 데이터를 DB에 적재하고, 출처와 형식을 설명할 수 있는 상태로 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/WorldMapApplication.java`
  - `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
  - `src/main/java/com/worldmap/common/exception/ResourceNotFoundException.java`
  - `src/main/java/com/worldmap/country/domain/Continent.java`
  - `src/main/java/com/worldmap/country/domain/Country.java`
  - `src/main/java/com/worldmap/country/domain/CountryReferenceType.java`
  - `src/main/java/com/worldmap/country/domain/CountryRepository.java`
  - `src/main/java/com/worldmap/country/application/CountryCatalogService.java`
  - `src/main/java/com/worldmap/country/application/CountryDetailView.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedInitializer.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedProperties.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedValidator.java`
  - `src/main/java/com/worldmap/country/application/CountrySummaryView.java`
  - `src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java`
  - `src/main/java/com/worldmap/country/web/CountryApiController.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/resources/data/countries.json`
  - `src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/03-country-seed-loading.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 애플리케이션 시작 시 `CountrySeedInitializer`가 `country` 테이블이 비어 있는지 확인하고, 비어 있으면 JSON 시드 파일을 읽는다. `CountrySeedValidator`가 ISO 코드 중복, 좌표 범위, 필수값을 검증한 뒤 `CountryRepository`로 저장한다. 이후 사용자는 `GET /api/countries`와 `GET /api/countries/{iso3Code}`로 출제 가능한 국가 데이터를 조회할 수 있다.
- 데이터 / 상태 변화: 처음 실행 시 `country` 테이블에 17개 국가가 적재되고, 이후 재실행에서는 기존 데이터가 있으면 시드를 다시 넣지 않는다. 위치 게임용 좌표는 현재 `대표 좌표 1개` 구조이며, 실제 국경 폴리곤 데이터는 아직 도입하지 않았다.
- 핵심 도메인 개념: 국가 시드와 운영 데이터 분리, 대표 좌표(`referenceLatitude`, `referenceLongitude`)와 실제 국경 데이터의 차이, 빈 테이블일 때만 적재하는 초기화 전략, 조회 API와 저장 로직 분리
- 예외 / 엣지 케이스: 잘못된 시드 파일이 들어오면 애플리케이션 시작 단계에서 실패하도록 설계했다. 좌표는 국가 경계가 아니라 단일 대표점이므로, 향후 Level 2나 정교한 판정에서는 GeoJSON 같은 경계 데이터가 필요하다. 알 수 없는 ISO3 코드는 `404`로 응답한다.
- 테스트: `./gradlew test` 통과, 시드 적재 확인 / 재실행 시 중복 적재 방지 / 국가 상세 조회 API / 존재하지 않는 국가 `404` 응답 검증
- 배운 점: 시드 데이터는 단순 JSON 파일이 아니라 “출처, 기준 연도, 좌표 의미”를 설명할 수 있어야 면접과 포트폴리오에서 설득력이 생긴다. 또한 초기 적재 로직은 편하지만, 운영 환경에서는 마이그레이션 전략과 어떻게 분리할지도 계속 생각해야 한다.
- 아직 약한 부분: 현재 대표 좌표는 World Bank API의 capital city 좌표를 사용한다. 즉 위치 찾기 게임의 정답 기준점이 국가의 경계나 중심점이 아니라 `수도 기준 대표점`이라는 한계가 있다. 이후 게임 점수 정책을 만들 때 이 trade-off를 더 명확히 설명할 준비가 필요하다.
- 면접용 30초 요약: 국가 데이터를 `country` 테이블에 미리 적재해 게임과 추천 기능의 공통 기준 데이터를 만들었고, 시작 시 JSON 시드 파일을 검증한 뒤 빈 테이블에만 넣도록 구성했다. 인구수는 World Bank API 기준 2024 데이터를 사용했고, 위치 판정용 좌표는 첫 버전에서는 대표점 하나만 저장해 복잡도를 낮췄다.
