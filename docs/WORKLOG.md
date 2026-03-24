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

## 2026-03-23 - 3단계 국가 위치 찾기 게임 Level 1 완료

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 서버가 세션, 라운드, 점수, 진행 상태를 직접 관리하는 첫 번째 실제 게임 흐름을 완성한다.
- 변경 파일:
  - `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionStatus.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameRound.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameRoundRepository.java`
  - `src/main/java/com/worldmap/game/location/application/LocationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStartView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameCurrentRoundView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameRoundResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/StartLocationGameRequest.java`
  - `src/main/java/com/worldmap/game/location/web/SubmitLocationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGamePageController.java`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/04-location-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 시작 페이지에서 닉네임을 입력하면 `POST /api/games/location/sessions`가 세션과 라운드들을 만든다. 플레이 페이지는 `GET /round`로 현재 문제를 받고, 좌표와 `roundNumber`를 함께 `POST /answer`로 보낸다. 서버는 현재 라운드와 요청 라운드를 비교해 중복 제출을 막고, 거리 계산과 점수 계산을 수행한 뒤 세션과 라운드를 함께 갱신한다. 마지막 라운드가 끝나면 결과 페이지에서 세션 요약과 라운드별 기록을 보여준다.
- 데이터 / 상태 변화: 새로 `location_game_session`, `location_game_round` 테이블이 생기고, 상태는 `READY -> IN_PROGRESS -> FINISHED`로 바뀐다. 각 답안 제출 시 세션 총점과 완료 라운드 수가 증가하고, 라운드에는 제출 좌표, 거리, 판정, 점수가 저장된다.
- 핵심 도메인 개념: 세션과 라운드 분리, 서버 주도 정답 판정, `roundNumber` 기반 중복 제출 방지, 거리 계산과 점수 계산의 정책 분리, SSR 페이지와 API의 역할 분리
- 예외 / 엣지 케이스: 존재하지 않는 세션은 `404`, 현재 라운드와 다른 라운드 번호 제출은 `409`, 범위를 벗어난 좌표는 `400`으로 처리한다. 현재 UI는 지도 클릭이 아니라 좌표 입력형 셸이며, 정답 기준도 국가 경계가 아니라 대표 좌표 1개라는 한계가 있다.
- 테스트: `./gradlew test` 통과, 점수 정책 단위 테스트와 전체 게임 흐름 통합 테스트 추가
- 배운 점: 게임형 서비스에서도 결국 중요한 것은 시각 효과보다 상태 전이와 데이터 무결성이다. 특히 “중복 제출이 다음 라운드를 잘못 먹지 않게 하는가” 같은 예외 흐름이 백엔드 설계의 질을 크게 좌우한다.
- 아직 약한 부분: 현재 프론트는 좌표를 직접 입력하는 형태라 사용자 경험이 거칠다. 다음 단계나 별도 UI 고도화 단계에서 Leaflet 지도 클릭을 붙이되, 지금 만든 API와 세션 구조는 그대로 유지된다는 점을 더 또렷하게 설명할 필요가 있다.
- 면접용 30초 요약: 위치 찾기 게임 Level 1에서는 서버가 게임 세션과 라운드를 직접 저장하고, 답안 제출 시 거리와 점수를 계산해 상태를 갱신하도록 만들었다. 답안 요청에 `roundNumber`를 포함해 중복 제출을 방지했고, 결과 페이지에서는 라운드별 거리와 점수를 다시 확인할 수 있게 해 서버 주도 게임 구조를 명확히 보여 주었다.

## 2026-03-23 - 4단계 국가 인구수 맞추기 게임 Level 1 완료

- 단계: 4. 국가 인구수 맞추기 게임 Level 1
- 목적: 위치 게임에서 만든 세션 구조를 재사용하면서, 보기형 퀴즈 방식의 두 번째 모드를 추가한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/GameSessionStatus.java`
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStartView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameRound.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameRoundRepository.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationOptionView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStartView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameCurrentRoundView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameOptionGenerator.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationRoundOptions.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameRoundResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/web/StartPopulationGameRequest.java`
  - `src/main/java/com/worldmap/game/population/web/SubmitPopulationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGamePageController.java`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/05-population-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 시작 페이지에서 닉네임을 입력하면 `POST /api/games/population/sessions`가 세션과 라운드를 만든다. 라운드 생성 시 서버는 `PopulationGameOptionGenerator`로 보기 4개를 만들고, 플레이 페이지는 `GET /round`로 문제와 보기를 받아 렌더링한다. 사용자가 `selectedOptionNumber`를 제출하면 서버는 정답 여부와 점수를 계산하고 세션/라운드를 함께 갱신한다. 마지막 라운드가 끝나면 결과 페이지에서 라운드별 선택값과 정답값을 보여준다.
- 데이터 / 상태 변화: `population_game_session`, `population_game_round` 테이블이 추가됐고, 위치 게임과 인구수 게임 모두 `BaseGameSession`의 공통 세션 구조를 사용하게 됐다. 각 인구수 라운드에는 정답 인구수, 보기 4개, 정답 보기 번호, 사용자가 선택한 보기 번호, 점수가 저장된다.
- 핵심 도메인 개념: 공통 세션 구조 재사용, 모드별 라운드 분리, 보기 생성 규칙, 모드별 점수 정책, “공통화는 세션까지만 / 라운드는 모드별로”라는 경계 설정
- 예외 / 엣지 케이스: 국가 수가 4개 미만이면 보기형 게임 자체를 시작할 수 없다. 답안 요청의 라운드 번호가 현재 세션 라운드와 다르면 `409`로 막고, 보기 번호가 1~4 범위를 벗어나면 `400`으로 처리한다. 보기 생성은 비슷한 인구 규모를 우선 고르지만, seed 데이터가 적기 때문에 매우 정교한 난이도 조절은 아직 아니다.
- 테스트: `./gradlew test` 통과, 옵션 생성 단위 테스트와 인구수 게임 전체 흐름 통합 테스트 추가, 기존 위치 게임 테스트도 유지
- 배운 점: 두 번째 게임을 추가해 보니 “공통으로 보이는 것”과 “정말 공통인 것”이 다르다는 점이 분명해졌다. 세션 필드와 상태 전이는 공통이지만, 라운드 데이터와 점수 정책은 모드별로 남기는 편이 훨씬 설명 가능하고 유지보수하기 좋다.
- 아직 약한 부분: 현재 인구수 Level 1은 보기형이어서 판정이 단순하다. 이후 Level 2 수치 입력형으로 넘어갈 때 오차율 기반 점수 계산과 보기형 구조를 어떻게 같이 설명할지 더 다듬을 필요가 있다.
- 면접용 30초 요약: 인구수 맞추기 게임 Level 1에서는 위치 게임에서 검증한 세션 구조를 `BaseGameSession`으로 재사용하고, 라운드와 점수 정책만 모드별로 분리했다. 서버가 비슷한 인구 규모의 보기 4개를 만들고 정답 여부를 판정하도록 해, 공통 구조와 모드별 로직의 경계를 설명 가능한 형태로 정리했다.

## 2026-03-23 - 3단계 위치 찾기 게임 Level 1 요구사항 정합화

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 좌표 입력형 프로토타입을 실제 요구사항인 `3D 지구본 국가 선택형`으로 맞추고, 답안 모델을 좌표가 아닌 국가 코드 기준으로 정리한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/domain/LocationGameRound.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameRoundResultView.java`
  - `src/main/java/com/worldmap/game/location/web/SubmitLocationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/static/data/world-countries.geojson`
  - `src/main/resources/static/images/earth-blue-marble.jpg`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/04-location-game-level-1.md`
  - `blog/05-population-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 시작 페이지에서 닉네임을 입력하면 `POST /api/games/location/sessions`가 세션과 라운드를 만든다. 플레이 페이지는 `GET /round`로 현재 문제 국가명을 받고, 동시에 `/api/countries`와 `/data/world-countries.geojson`을 내려받아 지구본에 활성 국가를 표시한다. 사용자가 국가 폴리곤을 클릭하면 프론트는 `selectedCountryIso3Code`를 선택 상태로 들고 있다가 `roundNumber`와 함께 `POST /answer`로 보낸다. 서버는 현재 라운드와 요청 라운드를 비교하고, 선택한 ISO3가 실제 시드 국가인지 검증한 뒤 세션과 라운드를 갱신한다.
- 데이터 / 상태 변화: `location_game_round`는 이제 제출 좌표와 거리 대신 `selectedCountryIso3Code`, `selectedCountryName`, `correct`, `awardedScore`를 저장한다. `targetLatitude`, `targetLongitude`는 향후 Level 2나 다른 시각화 확장을 위해 남겨뒀지만, 현재 Level 1 판정에는 사용하지 않는다. 프론트 정적 자산으로 GeoJSON과 지구 텍스처가 추가됐다.
- 핵심 도메인 개념: 3D 지구본은 입력 UI일 뿐이고, 정답 판정의 기준은 서버가 가진 국가 ISO 코드다. 프론트는 국가 폴리곤을 렌더링하지만 정답 여부를 계산하지 않는다. `roundNumber` 기반 중복 제출 방지와 `selectedCountryIso3Code` 정규화가 서버 무결성의 핵심이다.
- 예외 / 엣지 케이스: 존재하지 않는 세션은 `404`, 현재 라운드와 다른 `roundNumber` 제출은 `409`, 시드에 없는 국가 ISO3를 제출하면 `400`이다. 프론트에서는 시드에 포함된 국가만 활성화하지만, 서버도 동일 검증을 수행해 클라이언트 조작을 막는다. 현재는 17개 시드 국가만 활성화되어 있어 실제 전체 국가 찾기 난도와는 차이가 있다.
- 테스트: `./gradlew test` 통과. `LocationGameScoringPolicyTest`에서 국가 코드 일치/불일치 점수 정책을 검증했고, `LocationGameFlowIntegrationTest`에서 세션 시작, 현재 라운드 조회, 정답 제출, 중복 제출 차단, 게임 종료 흐름을 확인했다.
- 배운 점: 요구사항이 바뀌면 화면만 바꾸는 것이 아니라 요청 모델과 판정 기준까지 같이 바뀐다. 이번 수정은 “좌표 게임”에서 “국가 선택 게임”으로 문제 정의가 이동한 사례라서, 서비스와 DTO를 함께 재정의해야 일관성이 생겼다.
- 아직 약한 부분: 현재 지구본 렌더링은 CDN의 `three`와 `globe.gl`에 의존한다. 또한 GeoJSON은 정적 파일이라 모바일 성능이나 폴리곤 단순화 전략을 아직 검토하지 않았다. 이후 랭킹 단계 전에 프론트 성능과 asset 관리 전략을 한 번 더 점검할 필요가 있다.
- 면접용 30초 요약: 위치 찾기 게임 요구사항이 실제로는 3D 지구본에서 나라를 선택하는 방식이어서, 답안 모델을 좌표에서 국가 ISO 코드로 다시 정의했습니다. 프론트는 지구본과 국가 폴리곤을 렌더링하고, 서버는 선택한 ISO3가 정답 국가와 일치하는지만 판정해 세션과 라운드를 갱신합니다. 이렇게 해서 시각 효과와 판정 로직을 분리한 서버 주도 구조를 더 명확하게 설명할 수 있게 됐습니다.

## 2026-03-23 - 위치 찾기 지구본 렌더링 성능 안정화

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 플레이 페이지에서 지구본이 뜨지 않고 브라우저가 멈추는 문제를 해결한다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/data/active-countries.geojson`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `blog/04-location-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 플레이 페이지 진입 시 프론트는 더 이상 전 세계 고해상도 폴리곤 전체를 내려받지 않는다. `/api/countries`로 활성 국가 목록을 받고, `/data/active-countries.geojson`으로 현재 시드 국가만 담은 경량 자산을 내려받아 지구본을 렌더링한다.
- 데이터 / 상태 변화: 정적 자산이 `world-countries.geojson` 중심에서 `active-countries.geojson` 중심으로 바뀌었다. 새 자산은 시드 17개 국가만 포함하고 좌표 정밀도를 낮춰 파일 크기를 줄였다.
- 핵심 도메인 개념: Level 1의 실제 선택 가능 범위는 시드 국가다. 따라서 렌더링 자산도 같은 범위로 맞추는 것이 기능 요구사항과 성능 요구사항을 동시에 만족시키는 방법이다.
- 예외 / 엣지 케이스: 전체 세계 폴리곤을 렌더링하면 브라우저 메인 스레드가 멈출 수 있다. 현재는 선택 가능한 국가만 렌더링하므로 비활성 국가 클릭 개념 자체가 사라졌고, 대신 “왜 모든 국가가 안 보이느냐”는 UX 설명이 필요하다.
- 테스트: `./gradlew test` 통과. 로컬 서버 재기동 후 `/games/location/play/{sessionId}`가 `globe.gl` 스크립트와 경량 GeoJSON 경로를 포함하는지 확인했고, 세션 시작/라운드 조회/정답 제출 API도 재검증했다.
- 배운 점: 프론트 성능 문제는 종종 코드 로직보다 “어떤 자산을 얼마나 브라우저에 올리느냐”의 문제다. 이번 수정은 정답 판정 모델만큼 렌더링 대상 범위를 맞추는 것도 설계의 일부라는 점을 보여준다.
- 아직 약한 부분: 현재 경량화는 “활성 국가만 추리기 + 좌표 정밀도 낮추기” 수준이다. 추후 전체 국가를 지원하려면 단순 필터링이 아니라 폴리곤 단순화, LOD, 타일 전략 같은 추가 설계가 필요하다.
- 면접용 30초 요약: 위치 찾기 게임을 3D 지구본으로 바꾼 뒤 브라우저가 멈추는 문제가 있어서, 전체 세계 폴리곤 대신 실제 시드에 포함된 국가만 별도 GeoJSON으로 분리했습니다. 이렇게 해서 요구사항 범위와 렌더링 범위를 맞추고, 지구본은 정상적으로 뜨면서도 서버 판정 구조는 그대로 유지할 수 있게 했습니다.

## 2026-03-23 - 위치 찾기 지구본 레이아웃과 색상 개선

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 지구본이 태양처럼 붉게 보이고 화면 오른쪽으로 치우쳐 보이는 문제를 해결한다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/static/data/active-countries.geojson`
  - `docs/WORKLOG.md`
- 요청 흐름: 플레이 페이지 진입 시 프론트는 이제 더 단순한 Natural Earth 110m 기반 `active-countries.geojson`을 불러온다. 지구본 생성 후 `globeStage`의 실제 크기를 읽어 정사각형 캔버스를 맞추고, `ResizeObserver`로 레이아웃 변화에 따라 다시 동기화한다.
- 데이터 / 상태 변화: `active-countries.geojson`은 17개 시드 국가를 모두 포함하도록 다시 생성했다. 이전 고해상도 데이터에서 France 코드가 `-99`로 들어가 누락되던 문제도 함께 사라졌다.
- 핵심 도메인 개념: Level 1에서 보여 줄 국가 범위와 실제 선택 가능한 국가 범위를 일치시키는 것이 중요하다. 또한 3D 지구본은 “상호작용 컴포넌트”이므로, 시각 스타일보다 먼저 캔버스 크기와 데이터 복잡도를 안정적으로 맞춰야 한다.
- 예외 / 엣지 케이스: 정사각형 캔버스를 맞추지 않으면 지구가 잘리거나 한쪽으로 밀려 보일 수 있다. 기본 폴리곤 색이 너무 강하면 텍스처보다 오버레이가 먼저 보여 지구가 아니라 단색 구체처럼 보인다. 시드 국가만 보여 주는 구조이므로 모든 나라가 보이지 않는 것은 현재 Level 1의 의도된 제한이다.
- 테스트: `./gradlew test` 통과. 새 `active-countries.geojson` 크기가 약 70KB인지 확인했고, 로컬 서버 재기동 후 `location-game.js`가 새 경로를 참조하는지와 결과 페이지가 200으로 렌더링되는지 확인했다.
- 배운 점: 브라우저에서 보이는 “이상한 3D 화면”은 종종 WebGL 자체보다 데이터 밀도, 캔버스 크기, 기본 색 설계가 원인이다. 특히 기본 상태를 강한 빨강으로 두면 사용자는 정답/선택/일반 상태를 구분하기 어렵다.
- 아직 약한 부분: 아직 실제 브라우저 상호작용 테스트는 수동 확인이 필요하다. 다음에는 데스크톱 브라우저와 모바일 브라우저에서 드래그 감도, 확대 축소, 선택 정확도까지 점검해야 한다.
- 면접용 30초 요약: 지구본이 오른쪽으로 밀리고 붉은 구체처럼 보이는 문제를 해결하기 위해, 먼저 국가 데이터 자체를 더 단순한 110m 해상도 자산으로 교체하고, 캔버스를 컨테이너 크기에 맞는 정사각형으로 동기화했습니다. 그리고 기본 국가 색을 저채도 파란색으로 낮추고 선택/정답 상태에만 강한 색을 써서, 텍스처가 보이면서도 상호작용 상태가 명확한 화면으로 바꿨습니다.

## 2026-03-23 - 위치 게임 아케이드 리부트와 우주 테마 재설계 계획 수립

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 현재 동작하는 프로토타입을 포트폴리오 대표 기능으로 끌어올리기 위해, 게임 루프와 사이트 비주얼 방향을 다시 설계한다.
- 변경 파일:
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 이번 작업은 코드 구현보다 먼저 “앞으로 어떤 요청 흐름을 만들 것인가”를 고정하는 단계다. 새 방향에서는 사용자가 세션을 시작하면 서버가 `하트 3개`, `현재 Stage`, `총점`을 가진 세션을 만들고, 플레이 중에는 `GET /state`로 HUD 상태를 조회하며 `POST /answer`로 선택한 국가를 제출한다. 정답이면 점수와 Stage가 갱신되고, 오답이면 같은 Stage를 유지한 채 하트만 줄어든다.
- 데이터 / 상태 변화: 아직 실제 엔티티 변경은 없지만, 목표 도메인 모델을 `세션 / Stage / Attempt` 구조로 재정의했다. 또한 사이트 전체의 디자인 방향을 `따뜻한 카드형`에서 `차가운 우주 HUD`로 전환하기로 결정했다.
- 핵심 도메인 개념: 서버 주도 하트 관리, Stage 기반 진행, Attempt 기록, 게임오버 상태, HUD 중심 게임 UI, 나라 이름 비노출 규칙
- 예외 / 엣지 케이스: 위치 게임은 “국가를 찾는 것”이 목적이므로 플레이 중 tooltip이나 국가명 label을 보여주면 게임성이 깨진다. 또한 `한 문제 = 한 번 제출` 구조로는 재시도와 게임오버를 표현하기 어려워 현재 도메인 모델을 그대로 유지하면 요구사항과 어긋난다.
- 테스트: 없음. 이번 작업은 설계 문서와 개발 순서 정리 단계다.
- 배운 점: 사용자 경험을 게임답게 만들려면 CSS 수정만으로는 부족하고, 게임 루프와 상태 모델부터 다시 정의해야 한다. 특히 하트, 재시도, 자동 다음 단계 같은 감각은 프론트 연출 이전에 백엔드 상태 전이 설계가 먼저다.
- 아직 약한 부분: 점수 공식과 Stage 종료 규칙은 초안을 정했지만 실제 플레이 템포를 보며 다시 조정할 가능성이 높다. 또한 인구수 게임과 공통 세션 구조를 어디까지 공유할지도 리부트 과정에서 다시 판단해야 한다.
- 면접용 30초 요약: 위치 찾기 게임 프로토타입은 서버 주도 구조는 맞았지만 게임성이 약해서, 하트 3개와 재시도, 게임오버, 자동 Stage 진행이 있는 아케이드 루프로 다시 설계했습니다. 이 과정에서 기존 `세션 + 라운드`만으로는 부족하다고 보고 `세션 / Stage / Attempt` 구조와 우주 HUD 비주얼 방향을 문서로 먼저 고정해, 이후 구현이 설계 의도와 분리되지 않게 만들었습니다.

## 2026-03-23 - 위치 게임 아케이드 리부트 1차 구현

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 문서로만 정리했던 아케이드 규칙을 실제 코드로 옮겨, `하트 3개 + 같은 Stage 재시도 + 게임오버` 흐름이 서버 주도로 동작하게 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/GameSessionStatus.java`
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStage.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageStatus.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttempt.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStateView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerOutcome.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/location/web/SubmitLocationAnswerRequest.java`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 사용자가 시작 페이지에서 세션을 만들면 서버가 `location_game_session`과 여러 개의 `location_game_stage`를 생성한다. 플레이 화면은 `GET /api/games/location/sessions/{sessionId}/state`로 현재 Stage, 하트, 점수, 문제 국가를 받는다. 사용자가 지구본에서 국가를 클릭해 `POST /answer`를 보내면 서버는 현재 Stage 번호를 검증하고, `LocationGameAttempt`를 1건 저장한 뒤 정답 여부에 따라 `LocationGameStage` 상태와 `LocationGameSession`의 하트/점수/진행 상태를 갱신한다. 정답이면 다음 Stage 번호가 바뀌고, 오답이면 같은 Stage를 유지하며 하트만 감소한다.
- 핵심 도메인 개념: `LocationGameSession`은 게임 전체 상태를, `LocationGameStage`는 한 문제의 진행을, `LocationGameAttempt`는 그 안의 개별 시도를 표현한다. 이 분리 덕분에 “같은 문제를 세 번 틀리고 게임오버가 났다”는 흐름을 데이터로 그대로 설명할 수 있다.
- 예외 상황 또는 엣지 케이스: 현재 세션의 Stage 번호와 다른 `stageNumber`를 보내면 `409`로 막는다. 시드에 없는 ISO3를 보내면 `400`이다. 하트가 0이 되는 마지막 오답은 Attempt로 남기고, 같은 Stage를 `FAILED`로 마감한다. 플레이 중 tooltip은 제거했기 때문에 나라 이름은 클릭 후 액션 바에서만 확인 가능하다.
- 테스트 내용: `./gradlew test` 통과. `LocationGameScoringPolicyTest`에서 Stage/시도/하트에 따른 점수 공식을 검증했고, `LocationGameFlowIntegrationTest`에서 전체 클리어 흐름, 오답 시 하트 감소, 세 번 오답 시 `GAME_OVER`를 검증했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임을 `세션 + 라운드 1회 제출형`에서 `세션 / Stage / Attempt` 구조로 바꿔 하트 3개와 재시도형 아케이드 루프를 서버가 직접 관리하도록 만들었습니다. 프론트는 지구본에서 나라를 고르고 제출/취소 UI를 보여주기만 하고, 실제 하트 감소, 점수 계산, 게임오버 판정은 모두 서버에서 처리합니다.
- 아직 내가 이해가 부족한 부분: 현재 UI는 여전히 기존 사이트 톤 위에 얹힌 1차 플레이 셸이라, “차가운 우주 HUD” 느낌의 전면 리디자인은 다음 작업에서 별도로 정리해야 한다. 또한 모바일에서 지구본 드래그와 선택 감도는 실사용 테스트가 더 필요하다.

## 2026-03-23 - 공통 우주 테마 1차 적용

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강, 4. 공통 UI 톤 정리
- 목적: 따뜻하고 둥근 카드형 사이트 인상을 걷어내고, 프로젝트 전체를 `cold space HUD` 방향으로 통일한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 로직은 바꾸지 않고, 같은 API와 SSR 진입점을 유지한 채 화면 표현 계층만 재설계했다. 홈 화면은 모드 허브처럼 보이도록 바꿨고, 시작 화면은 규칙 카드, 결과 화면은 디브리프 배너 중심으로 정리했다.
- 핵심 도메인 개념: 이 작업은 도메인이나 API가 아니라 `표현 계층의 공통 디자인 시스템`을 정리한 것이다. 기능별 페이지를 따로 꾸미기보다 `공통 CSS 토큰`을 먼저 바꾸면 이후 랭킹, 추천, 마이페이지도 같은 언어로 확장할 수 있다.
- 예외 상황 또는 엣지 케이스: 구현 순서상 원래는 Step 4에서 테마를 통일할 예정이었지만, 위치 게임 리부트의 플레이 감각을 보여주려면 메인/시작/결과 화면의 온도감부터 먼저 바꾸는 편이 자연스러워 이번에 앞당겨 적용했다. 다만 위치 게임 플레이 HUD 자체는 아직 1차 수준이라 추가 polish가 남아 있다.
- 테스트 내용: `./gradlew test` 통과. 서버 재시작 후 `/`, `/games/location/start`, `/css/site.css` 응답 `200` 확인.
- 면접에서 30초 안에 설명하는 요약: 기능 로직을 바꾸지 않고도 서비스 인상이 크게 달라질 수 있기 때문에, 공통 CSS 변수와 화면 구조를 먼저 우주 HUD 톤으로 다시 잡았습니다. 이 작업은 단순 색상 교체가 아니라, 홈과 시작/결과 화면의 정보 구조를 게임 허브처럼 다시 배열해 프로젝트 전체의 정체성을 통일한 단계입니다.
- 아직 내가 이해가 부족한 부분: 현재는 CSS와 템플릿 중심의 1차 리디자인이라, 브라우저별 폰트 로딩 체감과 모바일에서의 실제 시각 밀도는 직접 플레이하며 한 번 더 조정할 필요가 있다.

## 2026-03-23 - 국가 데이터 194개 확장과 시드 동기화 전환

- 단계: 2. 국가 데이터와 시드 적재 보강
- 목적: 위치 게임에서 17개 국가만 선택 가능한 제약을 줄이고, `country` 테이블과 `active-countries.geojson`이 최대한 넓은 같은 범위를 보도록 만든다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/data/countries.json`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/java/com/worldmap/country/domain/Country.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedInitializer.java`
  - `src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java`
  - `src/main/resources/templates/location-game/play.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `blog/03-country-seed-loading.md`
  - `blog/04-location-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 애플리케이션 시작 시 `CountrySeedInitializer`가 `countries.json`을 읽고 검증한 뒤, 기존 `country` 테이블을 ISO3 기준으로 조회한다. 이후 시드에 있는 국가는 추가 또는 갱신하고, 시드에 없는 국가는 삭제해 DB 범위를 현재 정적 자산 범위와 맞춘다. 플레이 화면은 계속 `/api/countries`와 `/data/active-countries.geojson`을 함께 읽는데, 이제 두 경로 모두 독립국 194개를 기준으로 응답한다.
- 데이터 / 상태 변화: 국가 시드는 `World Bank API + REST Countries` 조합으로 다시 만들었고, 인구수는 2024 기준 최신치에 맞췄다. `active-countries.geojson`도 독립국 194개만 포함하도록 다시 생성했다. 기존 “빈 테이블이면 적재, 아니면 생략” 구조에서 “앱 시작 시 현재 시드와 동기화” 구조로 바뀌면서 로컬 DB에 17개만 남아 있던 상태도 자동으로 194개로 확장된다.
- 핵심 도메인 개념: 이 단계의 핵심은 단순히 JSON 숫자를 늘리는 것이 아니라 `시드 파일`, `RDB`, `프론트 정적 자산`이 같은 국가 집합을 보게 만드는 것이다. 키는 `ISO3 코드`이며, 이 코드로 국가 데이터를 식별하고 동기화한다.
- 예외 상황 또는 엣지 케이스: 전 세계 지도 자산에는 영토와 특수 지역이 섞여 있고 일부 국가는 ISO3가 `-99`로 들어가므로, “파일에 있는 모든 폴리곤”을 그대로 국가 시드로 쓰면 데이터 설명력이 떨어진다. 그래서 이번 단계는 최대 범위를 추구하되, 게임과 인구 데이터가 함께 설명 가능한 `독립국 194개`를 기준으로 잘랐다. France와 Norway처럼 원본 GeoJSON에서 ISO3가 비정상인 케이스는 이름 매핑으로 보정했다.
- 테스트 내용: `./gradlew test` 통과. `CountrySeedIntegrationTest`에서 시작 시 194개 동기화, 기존 잘못된 데이터 복원, 존재하지 않는 가짜 국가 삭제를 검증했다. 서버 재시작 후 `/api/countries`가 `194`, `/data/active-countries.geojson`의 feature 수가 `194`, `/api/countries/FRA`가 정상 응답하는 것도 확인했다.
- 배운 점: 시드 데이터는 “초기 적재”보다 “현재 자산과 동기화”가 중요할 때가 있다. 특히 이 프로젝트처럼 게임 출제 범위를 프론트 GeoJSON과 함께 다루는 경우에는, 시드와 DB와 정적 자산이 조금만 어긋나도 실제 플레이에서 바로 문제가 난다.
- 아직 약한 부분: 현재는 독립국 194개를 기준으로 잘랐지만, 향후 Level 2에서 영토와 소국까지 확장할지, 국가명 한글 번역을 어떤 기준으로 더 정제할지는 추가 판단이 필요하다.
- 면접용 30초 요약: 위치 게임의 국가 수가 너무 적어 보여서 단순히 JSON만 늘리는 대신, `World Bank API + REST Countries`로 국가 시드를 재생성하고 앱 시작 시 ISO3 기준으로 DB를 동기화하도록 바꿨습니다. 그래서 이제 `/api/countries`와 `active-countries.geojson`이 모두 독립국 194개를 기준으로 움직이고, 기존 로컬 DB에 17개만 남아 있어도 서버 재시작만 하면 자동으로 같은 범위로 맞춰집니다.

## 2026-03-23 - 위치 게임 제출 전 국가명 비노출 규칙 보강

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 사용자가 지구본에서 클릭한 나라 이름이 제출 전 액션 바에 보이면 사실상 추가 힌트가 되므로, 제출 직전까지는 선택 상태만 보여주도록 규칙을 더 엄격하게 만든다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/play.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 국가 폴리곤을 클릭하면 프론트는 여전히 `selectedCountryIso3Code`를 내부 상태로 저장하지만, 화면에는 더 이상 실제 국가명을 렌더링하지 않는다. 액션 바에는 `국가 선택됨` 같은 상태만 보이고, 사용자가 `POST /answer`를 보낸 뒤에야 서버 응답의 `selectedCountryName`을 이용해 정답/오답 피드백에서 이름을 보여준다.
- 데이터 / 상태 변화: 서버 API와 DB 스키마는 바뀌지 않았다. 바뀐 것은 프론트의 정보 공개 시점이며, “클릭 직후”에서 “제출 후 판정 단계”로 한 단계 늦춰졌다.
- 핵심 도메인 개념: 위치 게임의 핵심은 “국가를 찾는 행위”이므로, 사용자에게 너무 빠른 텍스트 힌트를 주면 게임성이 무너진다. 따라서 `선택 상태`와 `선택 국가명`을 같은 시점에 공개하지 않고 분리하는 것이 UX 규칙상 중요하다.
- 예외 상황 또는 엣지 케이스: 사용자는 여전히 잘못된 국가를 선택할 수 있고 취소도 가능하다. 다만 제출 전에는 액션 바에서 “내가 정확히 무엇을 골랐는지”를 텍스트로 재확인할 수 없으므로, 지구본 하이라이트와 취소 버튼이 더 중요해졌다.
- 테스트 내용: `./gradlew test` 통과. 로컬 플레이 화면에서 클릭 후 액션 바에 이름 대신 상태 텍스트만 보이는지 수동 확인 필요.
- 배운 점: 같은 “이름 비노출” 규칙이라도 hover tooltip만 끄는 것과, 제출 전 선택 텍스트까지 숨기는 것은 게임성에 미치는 강도가 다르다. 실제 플레이 감각을 맞추려면 정보 공개 타이밍도 설계 요소로 봐야 한다.
- 아직 약한 부분: 현재는 국가명을 숨겼지만, 제출 전 “선택 실수 방지”를 위한 시각적 확인 수단은 하이라이트 정도뿐이다. 이후에는 카메라 줌, 선택 애니메이션, 취소 UX를 더 다듬어야 한다.
- 면접용 30초 요약: 위치 게임은 나라를 찾는 것이 목적이라, 클릭 직후 액션 바에 국가명을 보여주는 것도 사실상 힌트라고 판단했습니다. 그래서 프론트는 선택 ISO 코드를 내부 상태로만 들고 있고, 화면에는 제출 전까지 `국가 선택됨`만 보여주며, 실제 이름은 서버 판정 응답 이후에만 공개하도록 바꿨습니다.

## 2026-03-23 - 위치 게임 시작 지연 체감 개선

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: `/games/location/start`에서 세션 시작 후 플레이 화면 진입이 오래 걸리고, 그동안 회색 화면처럼 멈춰 보이는 문제를 줄인다.
- 변경 파일:
  - `src/main/resources/static/vendor/globe.gl.min.js`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `docs/WORKLOG.md`
- 요청 흐름: 시작 페이지에서 사용자가 `세션 시작하기`를 누르면 프론트가 버튼을 바로 비활성화하고 로딩 메시지를 보여준다. 플레이 페이지에서는 `GET /state`와 지구본 자산 fetch를 병렬로 시작하고, 상태 텍스트를 먼저 렌더링한 뒤 첫 페인트 이후에만 `Globe.gl` 초기화를 진행한다. 지구본도 바로 모든 폴리곤을 붙이지 않고, 베이스 지구본을 먼저 만들고 그 다음 단계에서 국가 폴리곤을 붙인다.
- 데이터 / 상태 변화: 백엔드 세션 생성 속도 자체는 원래 빠른 편이었다. 이번 변경은 서버 데이터가 아니라 프론트 초기화 순서를 바꿔 “멈춘 것처럼 보이는 시간”을 줄이는 데 집중했다. 또한 CDN 의존을 줄이기 위해 `globe.gl`를 로컬 정적 자산으로 옮겼다.
- 핵심 도메인 개념: 이 작업의 핵심은 게임 상태가 아니라 `초기 렌더링 경로`다. 세션 생성 API가 빠르더라도 브라우저가 무거운 3D 초기화를 첫 페인트 전에 몰아서 하면 사용자는 서버가 느리다고 느낄 수 있다.
- 예외 상황 또는 엣지 케이스: 국가 수가 194개로 늘면서 폴리곤 수가 많아졌기 때문에, 지구본 초기화는 여전히 기기 성능 영향을 받는다. 이번에는 “무조건 더 빠르게”보다 “네트워크 변수 제거 + 로딩 메시지 유지 + 무거운 작업을 뒤로 미루기”에 초점을 맞췄다.
- 테스트 내용: `./gradlew test` 통과. 서버 재시작 후 세션 생성, 플레이 페이지 HTML, `/api/countries`, `/data/active-countries.geojson` 응답 시간이 모두 짧은 것을 확인했고, 실제 플레이 페이지 HTML에 로딩 문구가 기본 노출되는 것도 확인했다.
- 배운 점: 사용자가 느끼는 “느림”은 종종 백엔드 처리시간보다도 첫 화면이 언제 보이느냐에 더 크게 좌우된다. 특히 WebGL 같은 무거운 초기화는 첫 페인트 뒤로 보내야 체감 품질이 좋아진다.
- 아직 약한 부분: 현재는 `globe.gl`만 로컬 자산으로 옮겼다. 폰트, 텍스처, 지구본 폴리곤 자체의 체감 로딩은 모바일 기기 기준으로 한 번 더 점검해야 한다.
- 면접용 30초 요약: 세션 생성 API를 측정해 보니 서버는 거의 즉시 응답해서 병목이 아니었습니다. 대신 플레이 화면에서 3D 지구본과 194개 국가 폴리곤을 한 번에 초기화하는 구간이 체감 지연을 만들고 있었기 때문에, 로딩 메시지를 먼저 보여주고 상태를 먼저 렌더링한 뒤 지구본을 단계적으로 초기화하도록 프론트 흐름을 바꿨습니다.

## 2026-03-23 - 지구본 렌더링 경량화와 경계선 가시성 조정

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 사용자가 “하늘색 원만 보이고 국가 경계가 안 보인다”고 느끼는 문제와, 국가 수 확대 이후 지구본 초기화가 무거워진 문제를 같이 줄인다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/resources/static/js/location-game.js`
  - `docs/WORKLOG.md`
- 요청 흐름: 플레이 페이지가 `/data/active-countries.geojson`을 내려받는 구조는 유지하되, 자산 자체를 더 강하게 단순화해서 브라우저가 처리할 좌표 수를 줄였다. 지구본 초기화 시에는 `polygonCapCurvatureResolution`을 거칠게 조정해 폴리곤 표면 분할 수를 줄이고, 폴리곤 fill은 거의 투명하게, stroke는 더 선명하게 바꿔 실제 국가 경계가 먼저 보이도록 했다.
- 데이터 / 상태 변화: `active-countries.geojson`은 여전히 194개 국가를 포함하지만 파일 크기가 약 `863KB -> 461KB`로 줄었다. 프론트 렌더링에서는 밝은 하늘색 면 덩어리보다 얇은 국경선 중심으로 보이도록 시각 균형을 바꿨다.
- 핵심 도메인 개념: 이 작업은 국가 수를 줄인 것이 아니라 “같은 194개를 더 가벼운 기하 데이터와 덜 비싼 표면 곡률로 그린다”는 최적화다. 즉 기능 범위는 유지하면서 렌더링 비용만 줄이는 방향이다.
- 예외 상황 또는 엣지 케이스: GeoJSON을 더 많이 단순화하면 작은 섬나라 윤곽이 거칠어질 수 있다. 이번에는 194개 보존을 우선했고, 클릭 정확도와 성능 사이에서 현재 기준점을 다시 잡았다.
- 테스트 내용: `./gradlew test` 통과. 재생성 후 `active-countries.geojson` feature 수가 194개인지와 파일 크기가 줄어든 것을 확인하고, 서버 재시작까지 완료했다.
- 배운 점: “국가 수가 많아졌다”와 “렌더링이 무거워졌다”는 별개의 문제가 아니라 같은 변경의 양면일 수 있다. 이럴 때는 기능을 되돌리기보다, 데이터 단순화와 렌더링 옵션 튜닝으로 같은 범위를 더 효율적으로 보여주는 쪽이 낫다.
- 아직 약한 부분: 실제 사용자 브라우저에서 클릭 정확도와 경계선 가독성은 한 번 더 수동 확인이 필요하다. 특히 소국과 섬나라가 너무 거칠게 보이지 않는지 체크해야 한다.
- 면접용 30초 요약: 국가 수를 194개로 늘린 뒤 지구본이 단색 구처럼 보이고 무거워져서, 기능 범위는 유지한 채 렌더링 비용만 줄이는 방향으로 다시 튜닝했습니다. GeoJSON을 더 단순화해 파일 크기를 줄이고, Globe.gl의 폴리곤 곡률 해상도를 낮춰 계산량을 줄였으며, fill보다 stroke를 강조해 실제 나라 경계가 먼저 보이도록 바꿨습니다.

## 2026-03-23 - 위치 게임 endless Stage화와 선택 안정화, 공통 헤더 추가

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 위치 게임이 5 Stage에서 끝나는 구조를 없애고, Stage가 올라갈수록 더 어려워지며, 회전 중 오선택이 덜 나는 실제 아케이드 루프로 보강한다. 동시에 어느 화면에서나 메인으로 돌아갈 수 있는 공통 헤더를 추가한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPlan.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStateView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/game/location/application/LocationGameDifficultyPolicyTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 세션 시작 시 서버는 더 이상 5개 Stage를 미리 만들지 않고 Stage 1만 생성한다. 사용자가 `POST /answer`로 정답을 맞히면 `LocationGameService`가 현재 Stage를 `CLEARED`로 바꾸기 전에 다음 Stage를 새로 생성하고 `totalRounds`를 확장해, 세션이 끝나지 않고 계속 진행되게 만든다. 현재 상태 조회 `GET /state`는 `difficultyLabel`까지 내려주고, 프론트는 이를 HUD에 표시한다. 프론트에서는 지구본 드래그와 클릭을 구분해 회전 후 오선택되는 클릭을 무시한다.
- 데이터 / 상태 변화: `location_game_session`의 총 라운드는 고정값이 아니라 “현재까지 계획된 Stage 수”가 됐다. Stage를 맞힐 때마다 다음 Stage가 추가되고, 하트가 0이 되기 전까지 세션은 `IN_PROGRESS`를 유지한다. 난이도는 새 테이블을 만들지 않고 현재 `country.population` 데이터를 기준으로 `주요 국가 -> 지역 확장 -> 글로벌 -> 전체 국가` 순으로 후보 풀 크기를 넓히는 방식으로 구현했다. GeoJSON은 선택 정확도를 위해 단순화 강도를 완화해 약 `1.55MB` 수준으로 다시 생성했다.
- 핵심 도메인 개념: endless 모드에서도 “다음 Stage를 누가 만들 것인가”는 서버 책임이다. 그래야 프론트 조작만으로 Stage를 건너뛰거나 쉬운 국가만 반복 선택하는 흐름을 막을 수 있다. 또 현재 데이터 구조에서 난이도 상승을 가장 설명 가능하게 구현하는 방법은 “추가 메타데이터를 억지로 붙이는 것”이 아니라 이미 가진 인구 데이터를 기준으로 익숙한 국가 풀에서 덜 익숙한 국가 풀로 점진적으로 넓히는 것이다.
- 예외 상황 또는 엣지 케이스: 드래그 후 pointer up 순간이 클릭으로 해석되면 회전하다가 원치 않는 국가가 선택될 수 있다. 그래서 프론트에서 이동 거리 10px 이상인 포인터 상호작용은 선택으로 인정하지 않게 막았다. 나라를 모두 한 번씩 소진한 뒤에도 endless run을 유지해야 하므로, 전체 국가를 다 쓴 이후에는 직전 국가만 피하면서 재등장할 수 있게 했다.
- 테스트 내용: `./gradlew test` 통과. `LocationGameFlowIntegrationTest`에서 7 Stage 연속 정답 후에도 게임이 끝나지 않고 8번째 Stage가 준비되는지 확인했고, 기존 하트 감소 / 3회 오답 게임오버 테스트를 유지했다. `LocationGameDifficultyPolicyTest`에서 Stage가 올라갈수록 후보 풀 크기가 커지고, 후반부에는 194개 전체를 쓰는지 검증했다.
- 면접에서 30초 안에 설명하는 요약: 원래 위치 게임은 5 Stage를 미리 만들어 놓고 끝나는 구조였는데, 그 방식으로는 아케이드 감각이 약해서 정답을 맞힐 때마다 서버가 다음 Stage를 계속 생성하는 endless 구조로 바꿨습니다. 난이도는 별도 복잡한 메타데이터 없이 현재 가진 인구 데이터를 기준으로 주요 국가 풀에서 전체 국가 풀로 점차 넓히도록 설계했고, 프론트는 회전 드래그와 클릭을 구분해 오선택을 줄였습니다. 화면 쪽은 모든 페이지에서 홈으로 복귀할 수 있도록 공통 헤더도 같이 넣었습니다.
- 아직 내가 이해가 부족한 부분: 현재 난이도는 “익숙한 국가에서 덜 익숙한 국가로 넓힌다”는 1차 정책이라, 대륙/면적/섬나라 여부까지 섞은 더 정교한 난이도 모델은 다음 단계에서 검토가 필요하다. 또한 GeoJSON 정확도를 높이면서 파일이 다시 커졌기 때문에, 저사양 브라우저에서의 체감 성능은 실제 플레이로 한 번 더 확인해야 한다.

## 2026-03-23 - 지구본 폴리곤 링 방향 오류 수정

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 국가 선택 화면에서 나라 내부가 아니라 바깥 영역이 채워지며, 지구본 전체가 이상하게 덮이는 렌더링 버그를 해결한다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 위치 게임 플레이 화면은 계속 `/data/active-countries.geojson`을 읽어 `Globe.gl`에 폴리곤을 넘긴다. 이번 문제는 프론트 이벤트가 아니라 GeoJSON 자산의 외곽 링 방향이 원본 세계 지도와 반대로 뒤집힌 상태였기 때문에, `scripts/generate_country_assets.py`에서 단순화 후 최종 GeoJSON을 한 번 더 읽어 외곽 링은 시계 방향, 홀은 반대 방향으로 다시 맞춘 뒤 저장하도록 바꿨다.
- 데이터 / 상태 변화: 국가 수 194개는 그대로 유지했고, 바뀐 것은 폴리곤 좌표의 순서다. 수정 전에는 단순화 결과의 외곽 링이 모두 반대 방향이라 Globe가 “국가 내부” 대신 “국가 바깥쪽 구면”을 캡으로 채우는 현상이 있었다. 수정 후에는 외곽 링 방향이 원본 `world-countries.geojson`과 같은 방향으로 맞춰졌다.
- 핵심 도메인 개념: 이 버그는 색상이나 클릭 이벤트 문제가 아니라 `기하 데이터 해석 규칙` 문제다. 같은 좌표 집합이어도 링 방향이 바뀌면 3D 구면에서 안쪽/바깥쪽 판정이 달라질 수 있다는 점을 설명할 수 있어야 한다.
- 예외 상황 또는 엣지 케이스: GeoJSON은 단순화나 클린업 과정에서 링 방향이 바뀔 수 있다. 그래서 이번 수정은 일회성 수동 편집이 아니라 생성 스크립트에 보정 로직을 넣어, 앞으로 자산을 다시 만들더라도 같은 오류가 반복되지 않게 했다.
- 테스트 내용: `./gradlew test` 통과. 추가로 `active-countries.geojson`의 외곽 링 부호를 검사했을 때 194개 전부 원본과 같은 방향으로 정렬된 것을 확인했고, 서버도 재시작했다.
- 면접에서 30초 안에 설명하는 요약: 지구본이 이상하게 보인 원인은 JS 색상보다도 GeoJSON의 링 방향이 뒤집힌 데 있었습니다. 단순화된 국가 폴리곤의 외곽 링이 원본과 반대 방향이어서 Globe가 나라 내부가 아니라 바깥 영역을 채우고 있었고, 그래서 자산 생성 스크립트에 링 방향 보정 단계를 넣어 다시 생성 가능하게 고쳤습니다.
- 아직 내가 이해가 부족한 부분: 현재는 원본 세계 지도와 같은 방향으로 맞춰 정상 렌더링을 회복했지만, 향후 다른 지도 소스를 가져올 때도 같은 방향 규칙이 항상 유지되는지는 자산별 검증이 더 필요하다.

## 2026-03-23 - 위치 게임 폴리곤 캡 렌더링 안정화

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 국가 면이 지글거리거나 큰 삼각형처럼 흔들려 보이는 현상과, 일부 국가가 잘 안 눌리는 체감을 줄이기 위해 기본 렌더링을 `면 중심`에서 `경계선 중심`으로 바꾼다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면이 `active-countries.geojson`을 읽는 구조는 유지한다. 다만 `Globe.gl`에 넘긴 뒤 기본 상태에서는 국가 cap을 사실상 투명하게 두고 stroke만 강하게 렌더링하도록 바꿨다. hover, selected, correct, wrong 상태에서만 cap을 약하게 드러내고, 기본 altitude를 조금 올려 지구 텍스처와 거의 같은 표면에서 생기던 z-fighting을 줄였다.
- 데이터 / 상태 변화: 서버 상태나 DB는 바뀌지 않았다. 바뀐 것은 프론트의 폴리곤 시각화 정책이다. 이전에는 모든 국가가 반투명 cap을 가지고 있어 복잡한 MultiPolygon에서 triangulation 결과가 그대로 드러났고, 지금은 선택 전 기본 상태에서 cap을 그리지 않아 시각 노이즈가 크게 줄어든다.
- 핵심 도메인 개념: 위치 게임에서 중요한 것은 “나라를 읽고 클릭할 수 있는가”이지, 모든 국가 면을 항상 칠해 놓는 것이 아니다. 따라서 기본 상태는 경계선을 우선해 가독성을 확보하고, 상태 변화가 생길 때만 면을 노출하는 쪽이 게임 UX에 더 맞다.
- 예외 상황 또는 엣지 케이스: 클릭 실수를 줄이려고 넣어둔 드래그 판정이 너무 민감하면 큰 나라도 안 눌리는 느낌이 날 수 있다. 그래서 드래그 임계값을 10px에서 18px로 완화해, 약간의 손 떨림은 클릭으로 인정하도록 조정했다.
- 테스트 내용: `./gradlew test` 통과. 런타임에서는 새 JS가 반영되도록 서버 재시작이 필요하다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 지구본은 폴리곤이 많고 나라가 복잡해서, 모든 국가 면을 반투명으로 항상 그리면 오히려 삼각형 캡과 깜빡임이 보였습니다. 그래서 기본 렌더링을 경계선 중심으로 바꾸고, hover나 선택 같은 상태에서만 면을 잠깐 드러내도록 바꿔 가독성과 선택 안정성을 높였습니다.
- 아직 내가 이해가 부족한 부분: 현재 방식은 시각적 안정성을 우선한 1차 조정이라, 이후에는 나라 크기별로 hover altitude를 달리 주거나, 모바일 터치 환경에서 별도 threshold를 분리할지 검토가 더 필요하다.

## 2026-03-23 - 위치 게임 지도 자산을 Natural Earth 50m로 교체

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 기존 `world-countries.geojson` 기반 자산이 나라를 지나치게 잘게 쪼개고, 클릭/hover가 불안정하며, 브라질·중국 같은 대국에서도 넓은 영역이 반응하지 않는 문제를 줄인다.
- 변경 파일:
  - `src/main/resources/static/data/world-countries.geojson`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/resources/static/js/location-game.js`
  - `scripts/generate_country_assets.py`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 계속 `/data/active-countries.geojson`을 읽지만, 이제 이 파일은 기존 데이터셋이 아니라 Natural Earth 50m 국가 경계를 기반으로 생성된다. `generate_country_assets.py`는 새 소스의 `ISO_A3/ADM0_A3/SOV_A3/GU_A3` 필드를 보고 현재 시드 194개와 매칭한 뒤, 별도 mapshaper 단순화 없이 필터링과 링 방향 보정만 수행해 최종 active GeoJSON을 만든다.
- 데이터 / 상태 변화: `active-countries.geojson`은 여전히 194개 국가를 담지만, 국가 조각 수가 크게 줄었다. 예를 들어 브라질은 이전 자산에서 34조각이던 것이 새 자산에서는 17조각, 중국은 26조각에서 13조각 수준으로 내려갔다. South Sudan처럼 `ADM0_A3`와 `ISO_A3`가 다른 국가는 `ISO_A3`를 우선 사용하도록 매칭 순서도 조정했다.
- 핵심 도메인 개념: 위치 게임에서 중요한 것은 “국가 수가 많다”보다 “국가 경계가 설명 가능하고 클릭 가능하다”다. 따라서 데이터셋 선택도 단순 용량 문제가 아니라 `웹 렌더링 안정성`과 `상호작용 정확도` 기준으로 결정해야 한다.
- 예외 상황 또는 엣지 케이스: Natural Earth 110m는 충분히 가볍지만 194개 전체를 온전히 담지 못해서 제외했고, 10m는 너무 무거워 50m를 선택했다. 즉 이번 선택은 “194개 유지”와 “폴리곤 안정성” 사이에서 잡은 중간 해상도다.
- 테스트 내용: `./gradlew test` 통과. 생성 후 `active-countries.geojson` feature 수가 194개인지, 누락 국가가 없는지, 주요 국가 조각 수가 줄었는지 스크립트로 확인했고 서버도 재시작했다.
- 면접에서 30초 안에 설명하는 요약: 기존 위치 게임 지도는 국가 수는 많았지만 폴리곤이 너무 지저분해서 실제 클릭 안정성이 떨어졌습니다. 그래서 지구본 코드를 계속 미세 조정하기보다, 웹용 국가 경계 데이터로 많이 쓰이는 Natural Earth 50m를 기준으로 active GeoJSON을 다시 만들었고, 194개 국가 범위는 유지하면서 국가 조각 수를 줄여 hover와 클릭 안정성을 높였습니다.
- 아직 내가 이해가 부족한 부분: 현재는 데이터셋을 더 안정적인 쪽으로 바꾼 단계라, 실제 사용자 브라우저에서 어느 정도까지 클릭 체감이 좋아졌는지는 한 번 더 플레이 기준 확인이 필요하다. 특히 캐나다, 미국, 노르웨이처럼 다도해/북극권 조각이 많은 국가는 추가 검증이 필요하다.

## 2026-03-23 - 위치 게임 Level 1 범위를 상위 72개 국가로 축소

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 세션 진입 지연과 클릭 불안정이 계속 남아 있어, Level 1의 화면 범위를 먼저 줄여 플레이 가능한 기준선부터 확보한다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 위치 게임 시작 시 서버는 이제 전체 `country` 194건이 아니라 인구 기준 상위 72개만 후보로 사용한다. 프론트는 `/api/countries` 전체 목록을 더 이상 받지 않고, `active-countries.geojson`만 받아 그 안의 ISO 코드 집합으로 선택 가능 국가를 판단한다. 그래서 세션 시작 후 플레이 화면 초기화 시 네트워크 요청 수와 자산량이 같이 줄어든다.
- 데이터 / 상태 변화: DB의 `country` 시드는 여전히 194개를 유지한다. 바뀐 것은 위치 게임 Level 1의 활성 국가 범위이며, `active-countries.geojson`도 72개 feature로 줄었다. 게임오버 시에는 결과 화면에 머무르지 않고 시작 화면으로 바로 되돌아가 새로 시작하게 바꿨다.
- 핵심 도메인 개념: “시드는 넓게, Level 1 플레이 범위는 작게”를 분리하는 것이 중요하다. 전체 국가 데이터는 추천/인구수/향후 Level 2를 위해 유지하되, 현재 사용자 경험이 불안정하면 Level 1은 작은 집합으로 먼저 안정화하는 편이 맞다.
- 예외 상황 또는 엣지 케이스: 지금 72개로 줄였다고 해서 위치 게임 아키텍처가 바뀐 것은 아니다. 서버는 여전히 Stage 생성과 정답 판정을 담당하고, 이후 성능이 확보되면 100개 이상으로 다시 넓힐 수 있다.
- 테스트 내용: `./gradlew test` 통과. 재생성 후 `active-countries.geojson` feature 수가 72개로 줄었는지 확인했고, 서버 재시작까지 완료했다.
- 면접에서 30초 안에 설명하는 요약: 194개 전체를 한 번에 지구본에 올리니 클릭 안정성과 초기 로딩이 계속 흔들려서, Level 1은 인구 기준 상위 72개 주요 국가로 범위를 줄였습니다. 대신 전체 194개 시드는 DB에 그대로 유지해 다른 기능과 이후 확장은 막지 않았고, 프론트는 전체 국가 목록 요청을 없애고 active GeoJSON만 읽도록 줄여 초기화 비용도 같이 낮췄습니다.
- 아직 내가 이해가 부족한 부분: 72개가 현재 기준선으로는 현실적이지만, 어느 시점부터 100개 이상으로 다시 늘릴 수 있는지는 브라우저 성능과 클릭 정확도 테스트가 더 필요하다.

## 2026-03-23 - 위치 게임 Level 1 자산을 110m로 낮추고 좌표 기반 직접 선택 판정 추가

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 세션 진입이 오래 걸리고, 일부 국가가 잘 클릭되지 않으며, `Globe.gl` 폴리곤 이벤트만으로는 선택 안정성이 부족한 문제를 줄인다.
- 변경 파일:
  - `src/main/resources/static/data/world-countries-level1.geojson`
  - `src/main/resources/static/data/active-countries.geojson`
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 계속 `GET /state`와 `GET /data/active-countries.geojson`을 병렬로 받는다. 이번에는 Level 1용 지도 소스를 `Natural Earth 110m`로 분리해 `active-countries.geojson`을 다시 만들었고, 프론트는 `onPolygonClick` 하나에만 의존하지 않고 `onGlobeClick`으로 받은 위도/경도 좌표를 GeoJSON 폴리곤 내부 포함 판정에 한 번 더 통과시켜 선택 국가를 결정한다.
- 데이터 / 상태 변화: DB의 국가 시드 194개는 그대로다. 바뀐 것은 Level 1 활성 지도 자산과 선택 방식이다. `active-countries.geojson`은 약 `1.52MB -> 157KB`로 줄었고, 브라질/중국/미국 같은 다각형 복잡도가 큰 국가도 더 낮은 해상도 경계를 사용하게 됐다. 게임오버 시에는 프론트에서 지구본 입력을 즉시 잠그고 시작 화면으로 복귀한다.
- 핵심 도메인 개념: 이 작업의 핵심은 “보이는 폴리곤”과 “선택 판정”을 분리한 것이다. 사용자는 여전히 지구본에서 나라를 고르지만, 클릭 성공 여부를 3D 라이브러리 이벤트 하나에 맡기지 않고 `클릭 좌표 -> GeoJSON 포함 판정 -> ISO 코드 제출`로 한 단계 더 명시적으로 처리한다.
- 예외 상황 또는 엣지 케이스: 반경계선, 작은 섬, 날짜 변경선 근처 국가는 단순 평면 point-in-polygon 판정이 흔들릴 수 있다. 그래서 현재 로직은 클릭 좌표 기준으로 경도 값을 정규화해 anti-meridian을 완화하지만, 캐나다/러시아 같은 극지·다도해 국가는 추가 확인이 필요하다.
- 테스트 내용: `./gradlew test` 통과. 로컬 서버 재기동 후 `POST /api/games/location/sessions`는 약 `0.10s`, `GET /data/active-countries.geojson`은 약 `157KB / 0.015s`로 확인했다. 또 API 기준으로 3회 오답 제출 시 `GAME_OVER`, `livesRemaining = 0`이 내려오는 것도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임이 느리고 일부 나라가 잘 안 눌린 원인은 서버보다 프론트 지도 자산과 선택 방식에 있었습니다. 그래서 Level 1용 국가 경계를 더 낮은 해상도의 Natural Earth 110m로 분리해 초기 로딩을 크게 줄였고, Globe.gl의 기본 폴리곤 클릭만 믿지 않고 지구본 클릭 좌표를 GeoJSON 내부 판정으로 다시 검사해 선택 안정성을 높였습니다.
- 아직 내가 이해가 부족한 부분: 현재 직접 hit-test는 브라우저에서 충분히 설명 가능한 단순 알고리즘이지만, 구면 좌표 특성과 홀/섬 처리까지 완전한 GIS 수준으로 다루는 것은 아니다. 이후 Level 2 확장 전에는 작은 섬나라와 극지권 국가 기준으로 한 번 더 확인이 필요하다.

## 2026-03-23 - 위치 게임 GAME_OVER 선택 모달과 시작 버튼 문구 수정

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 하트가 모두 소진됐을 때 바로 화면을 강제로 넘기지 않고, 사용자가 탈락을 인지한 뒤 `홈으로` 또는 `다시 시작`을 직접 고를 수 있게 만든다. 동시에 시작 화면 CTA를 더 자연스러운 문구로 바꾼다.
- 변경 파일:
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버는 여전히 3회 오답 후 `GAME_OVER`를 내려준다. 바뀐 것은 프론트 후처리다. 플레이 화면은 `GAME_OVER` 응답을 받으면 지구본 입력을 잠그고, 자동 리다이렉트 대신 탈락 모달을 띄운다. 사용자는 그 안에서 `/` 또는 `/games/location/start`로 이동을 직접 선택한다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. UI 상태만 바뀌었다. 이전에는 `GAME_OVER -> 자동 시작 페이지 이동`이었고, 지금은 `GAME_OVER -> 입력 잠금 -> 탈락 모달 표시 -> 사용자 선택` 흐름이다.
- 핵심 도메인 개념: 이 변경은 게임 규칙을 바꾼 것이 아니라 피드백 UX를 바꾼 것이다. 즉 `언제 게임이 끝나는가`는 계속 서버가 결정하고, 프론트는 그 종료 사실을 사용자가 이해하기 쉬운 방식으로 표현한다.
- 예외 상황 또는 엣지 케이스: 탈락 모달이 떠 있는 동안 다시 선택하거나 제출하면 안 되므로, 게임오버 시 `globe-stage` 입력 자체를 잠그게 했다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과. 기존 `./gradlew test` 상태도 유지했다.
- 면접에서 30초 안에 설명하는 요약: 게임오버 자체는 서버가 그대로 결정하지만, 사용성 측면에서는 즉시 화면을 넘겨 버리면 왜 끝났는지 체감이 약했습니다. 그래서 프론트에서 `GAME_OVER` 응답을 받으면 입력을 잠그고 탈락 모달을 보여준 뒤, 사용자가 홈으로 갈지 바로 다시 시작할지 선택하게 바꿨습니다.
- 아직 내가 이해가 부족한 부분: 현재는 모달 안에서 단순 이동만 제공한다. 이후에는 최근 점수, 도달 Stage, 다시 시작 시 닉네임 유지 여부까지 넣을지 결정이 필요하다.

## 2026-03-23 - 새 게임 시작 시 이전 GAME_OVER 모달 상태 초기화

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: `다시 시작하기` 후 새 게임을 시작해도 이전 탈락 모달이 그대로 보이는 문제를 막는다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 새 플레이 화면에 진입하거나 `pageshow`가 다시 발생하면 프론트가 `GAME_OVER` 모달을 먼저 숨긴 뒤 상태를 불러오도록 바꿨다. 또한 CSS에 `[hidden] { display: none !important; }`를 넣어 hidden 속성이 클래스 스타일보다 항상 우선되게 고정했다.
- 데이터 / 상태 변화: 서버 상태와 DB는 변하지 않았다. 바뀐 것은 모달 표시 상태 초기화 시점이다.
- 핵심 도메인 개념: `GAME_OVER`는 서버 도메인 상태이고, 모달 노출 여부는 프론트 UI 상태다. 새 세션이 시작되면 이전 UI 상태를 반드시 초기화해야 서버 상태와 화면 상태가 어긋나지 않는다.
- 예외 상황 또는 엣지 케이스: 브라우저의 뒤로 가기/앞으로 가기 캐시에서 이전 DOM 상태가 살아 있을 수 있어 `pageshow` 시점에도 모달을 강제로 숨기게 했다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과.
- 면접에서 30초 안에 설명하는 요약: 탈락 모달을 추가한 뒤에는 새 게임이 시작될 때 이전 UI 상태를 초기화하는 것도 중요해졌습니다. 그래서 플레이 화면 진입 시와 브라우저 `pageshow` 시점에 모달을 다시 숨기고, CSS hidden 속성을 강제해 이전 탈락 상태가 새 세션으로 이어지지 않게 했습니다.
- 아직 내가 이해가 부족한 부분: 브라우저별 BFCache 동작 차이는 추가 확인이 필요하다. 현재는 가장 단순한 `pageshow` 초기화로 대응했다.

## 2026-03-23 - 위치 게임 다시 시작을 같은 세션 리셋 방식으로 변경

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 탈락 후 `다시 시작하기`를 눌렀을 때 시작 페이지로 돌아가 새 세션을 만드는 대신, 현재 세션을 그대로 초기화해 같은 플레이 화면에서 바로 다시 시작하게 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 프론트에서 `다시 시작하기`를 누르면 `POST /api/games/location/sessions/{sessionId}/restart`가 호출된다. 서버는 같은 `sessionId`의 attempt와 stage를 지우고, 세션의 상태/하트/점수/현재 Stage를 초기값으로 리셋한 뒤 Stage 1을 다시 만든다. 프론트는 새 URL로 이동하지 않고 같은 플레이 화면에서 `GET /state`를 다시 불러와 게임을 이어서 보여준다.
- 데이터 / 상태 변화: 이 변경 전에는 탈락 후 새 시작 흐름으로 넘어가며 사실상 새 세션을 만드는 UX였다. 지금은 같은 세션 row를 재사용하고, 관련 stage/attempt 데이터만 비운 뒤 다시 시작한다. 따라서 `sessionId`는 유지되고, 점수/하트/클리어 카운트만 초기화된다.
- 핵심 도메인 개념: “다시 시작”은 도메인 관점에서 `새 게임 생성`이 아니라 `종료된 세션의 초기화`다. 그래서 이 로직은 컨트롤러가 아니라 서비스에서 attempt 삭제, stage 삭제, session reset, 새 Stage 생성 순서를 묶어 처리해야 한다.
- 예외 상황 또는 엣지 케이스: 삭제 후 같은 세션에 Stage 1을 다시 넣기 때문에, attempt를 먼저 지우고 stage를 지운 뒤 flush까지 해줘야 unique constraint와 FK 충돌을 피할 수 있다. 또한 진행 중인 세션은 실수로 초기화되지 않도록 terminal 상태에서만 restart를 허용한다.
- 테스트 내용: `LocationGameFlowIntegrationTest`에 restart 시나리오를 추가해, 3회 오답으로 게임오버 후 `POST /restart`를 호출하면 같은 `sessionId`가 유지되고, lives=3 / score=0 / stage=1 / attempt 비움 상태로 복구되는지 확인했다. `./gradlew test` 통과.
- 면접에서 30초 안에 설명하는 요약: 탈락 후 다시 시작을 새 세션 생성으로 처리하면 같은 런의 흐름이 끊기고 프론트도 다시 시작 페이지를 거쳐야 했습니다. 그래서 별도 restart API를 두고, 서비스에서 기존 attempt와 stage를 지운 뒤 같은 sessionId의 상태를 초기화하고 Stage 1을 다시 생성하도록 바꿨습니다.
- 아직 내가 이해가 부족한 부분: 현재는 restart 시 이전 플레이 기록을 세션 안에 남기지 않는다. 나중에 “한 세션 안의 여러 러닝”까지 분석하고 싶다면 run 단위를 따로 분리할지 고민이 필요하다.

## 2026-03-23 - 인구수 게임 Level 1 리부트 기획 시작

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 기획
- 목적: 현재 인구수 게임이 `고정 5라운드 숫자 보기형 퀴즈`에 머물러 있어, 위치 게임처럼 하트 기반 아케이드 모드로 다시 설계할 기준 문서를 만든다.
- 변경 파일:
  - `docs/POPULATION_GAME_ARCADE_REBOOT.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 이번 작업은 코드 구현이 아니라 리부트 방향 고정이다. 현재 구현을 기준으로 `start -> get round -> submit answer -> next round button -> result` 흐름이 어디서 게임성을 잃는지 분석하고, 앞으로는 `start -> get state -> submit answer -> auto next stage / retry -> game over -> restart` 구조로 옮기기로 정리했다.
- 데이터 / 상태 변화: 실제 DB 스키마는 아직 바뀌지 않았다. 다만 다음 리부트에서 `PopulationGameRound 1회 제출형`만으로는 부족하다고 판단했고, 위치 게임처럼 `Session / Stage / Attempt` 구조로 갈 가능성을 플레이북과 설계 문서에 명시했다.
- 핵심 도메인 개념: 인구수 게임도 위치 게임과 마찬가지로 “문제를 제출하고 끝나는 폼”이 아니라 “서버가 하트, 점수, 진행 상태를 관리하는 게임 루프”가 되어야 한다. 따라서 현재의 `5문제 고정`, `100/0 점수`, `다음 라운드 버튼`은 프로토타입 단계의 흔적으로 본다.
- 예외 상황 또는 엣지 케이스: 현재 Level 1은 정확 숫자 4개 보기라 읽기 피로도가 높다. 리부트 시 Level 1은 구간형 또는 압축 표현형으로 다시 정의하고, 정확 수치 / 오차율 입력은 Level 2로 올리는 것이 더 자연스럽다고 판단했다.
- 테스트 내용: 이번 작업은 문서 설계 단계라 테스트는 실행하지 않았다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임은 공통 세션 구조를 검증하는 데는 성공했지만, 실제 게임으로는 아직 약했습니다. 그래서 위치 게임을 리부트했던 것처럼 인구수 게임도 하트, 재시도, 자동 다음 Stage, 게임오버, 같은 세션 재시작이 있는 아케이드 루프로 다시 설계하기로 했고, 그 기준 문서를 먼저 만들었습니다.
- 아직 내가 이해가 부족한 부분: Level 1을 `구간형`으로 갈지, `압축된 근사 수치 보기형`으로 갈지는 아직 확정하지 않았다. 둘 다 장단점이 있어 실제 구현 전에 한 번 더 선택해야 한다.

## 2026-03-23 - 인구수 게임 Level 1 아케이드 리부트 1차 구현

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트
- 목적: 기존 `고정 5라운드 + 다음 라운드 버튼 + 100/0 점수` 구조를 버리고, 위치 게임과 같은 서버 주도 아케이드 루프로 옮긴다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStage.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStageStatus.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameAttempt.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameDifficultyPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameDifficultyPlan.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStateView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerOutcome.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/SubmitPopulationAnswerRequest.java`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/POPULATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 시작 페이지에서 닉네임을 제출하면 `POST /api/games/population/sessions`가 같은 `BaseGameSession` 위에 `PopulationGameSession`을 만들고, 서버가 첫 `PopulationGameStage`와 보기 4개를 생성한다. 플레이 화면은 `GET /state`로 현재 Stage, 하트, 점수, 보기 4개를 받고, `POST /answer`로 선택한 보기 번호를 제출한다. 서비스는 `PopulationGameScoringPolicy`와 `PopulationGameDifficultyPolicy`를 이용해 정답 여부, 점수, 다음 Stage 생성 여부를 계산하고, `PopulationGameAttempt`를 저장한다. 하트가 모두 사라지면 `GAME_OVER`가 되며, `POST /restart`로 같은 `sessionId`를 초기화해 Stage 1부터 다시 시작한다.
- 데이터 / 상태 변화: `population_game_round` 1회 제출형 구조 대신 `population_game_stage`, `population_game_attempt` 구조를 사용하게 됐다. 세션에는 `livesRemaining`이 추가됐고, 상태 전이는 `READY -> IN_PROGRESS -> GAME_OVER` 중심으로 바뀌었다. Stage는 정답 시 `CLEARED`, 하트 소진 시 `FAILED`가 되고, 정답일 때마다 다음 Stage가 추가되어 `totalRounds`는 “현재까지 계획된 Stage 수” 의미로 확장된다.
- 핵심 도메인 개념: 인구수 게임도 위치 게임처럼 “한 문제를 제출하고 끝나는 퀴즈”가 아니라 “한 Stage 안에서 여러 번 시도할 수 있는 게임”으로 보는 것이 핵심이다. 그래서 정답 데이터와 보기 4개는 `PopulationGameStage`에 두고, 사용자의 실제 선택 기록은 `PopulationGameAttempt`로 분리했다. 이 로직이 컨트롤러가 아니라 서비스에 있어야 하는 이유는, 답안 제출 한 번이 `세션 하트 감소`, `점수 계산`, `Stage 상태 변경`, `다음 Stage 생성`, `Attempt 저장`을 한 트랜잭션 안에서 함께 일으키기 때문이다.
- 예외 상황 또는 엣지 케이스: 진행 중이 아닌 세션에는 답안을 제출할 수 없고, 현재 Stage 번호와 다른 값을 제출하면 충돌로 막는다. 재시작은 종료된 세션에서만 허용하며, 기존 attempt/stage를 먼저 지우고 flush한 뒤 같은 sessionId를 초기화해 unique constraint 충돌을 피한다. 아직 보기 표현은 `정확 숫자 4개`라서 읽기 피로도가 남아 있고, 이 부분은 다음 단계에서 다시 다듬어야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest` 통과, `./gradlew test` 전체 통과, `node --check src/main/resources/static/js/population-game.js` 통과. 로컬 실행 후 `POST /api/games/population/sessions`, `GET /state`, 결과 페이지 `200` 응답도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임도 위치 게임처럼 서버가 하트, 점수, 진행 상태를 관리하도록 리부트했습니다. 기존 5라운드 퀴즈 구조를 `세션 / Stage / Attempt`로 바꾸고, 오답이면 같은 Stage 재시도, 하트 3개 소진 시 게임오버, 같은 sessionId 재시작, 정답 시 자동 다음 Stage 생성까지 모두 서비스에서 처리하도록 옮겼습니다.
- 아직 내가 이해가 부족한 부분: 이번 1차 리부트는 게임 루프와 상태 전이에 집중했고, 보기 표현은 아직 정확 숫자 4개다. Level 1을 구간형으로 바꿀지 압축 수치형으로 유지할지는 실제 플레이 피드백을 보고 한 번 더 판단해야 한다.

## 2026-03-23 - 인구수 게임 오답 시 하단 결과 카드 비노출 처리

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 오답을 냈을 때 하단에 정답/선택값 결과 카드까지 뜨면 아케이드 게임보다 해설형 퀴즈처럼 느껴져, 실패 피드백을 오버레이 중심으로 단순화한다.
- 변경 파일:
  - `src/main/resources/static/js/population-game.js`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions/{sessionId}/answer` 응답을 받은 뒤, 프론트는 이제 정답일 때만 하단 feedback 카드를 렌더링한다. 오답 또는 게임오버일 때는 하단 카드를 비우고, 상단 오버레이만 보여준 뒤 같은 Stage 재시도 또는 게임오버 모달로 넘어간다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. 바뀐 것은 프론트 피드백 강도와 노출 방식이다.
- 핵심 도메인 개념: 오답 피드백은 “상태 전이 알림”만 주고, 상세 결과 카드는 정답 보상에만 쓰는 편이 현재 아케이드 루프에 더 맞는다. 즉 서버가 판정한 결과는 같지만, 프론트는 `correct` 여부에 따라 다른 UI 채널로 보여준다.
- 예외 상황 또는 엣지 케이스: 게임오버도 오답의 연장선이므로, 하단 카드 대신 게임오버 모달만 띄우도록 유지한다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test` 전체 통과 상태 유지.
- 면접에서 30초 안에 설명하는 요약: 오답 때마다 하단 결과 카드까지 뜨면 게임 흐름이 끊겨서, 인구수 게임은 정답일 때만 결과 카드를 보여주고 오답은 오버레이만 남기도록 바꿨습니다. 서버 응답은 그대로 두고, 프론트에서 `correct` 여부에 따라 피드백 채널만 분기한 것입니다.
- 아직 내가 이해가 부족한 부분: 지금은 오답 상세 해설을 숨겼지만, 나중에 학습형 모드와 아케이드 모드를 분리할 때는 결과 노출 정책을 다시 나눌 필요가 있다.

## 2026-03-23 - 인구수 게임 오답 시 화면 재조회 제거

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 오답 뒤 같은 Stage를 다시 시도하는 상황인데도 프론트가 `GET /state`를 다시 호출해 화면이 깜빡이고 새로고침처럼 보이는 문제를 제거한다.
- 변경 파일:
  - `src/main/resources/static/js/population-game.js`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions/{sessionId}/answer` 응답이 `WRONG`일 때는 이제 추가 `GET /state` 요청을 보내지 않는다. 프론트는 서버 응답에 포함된 `livesRemaining`, `totalScore`, `clearedStageCount`만 현재 HUD에 반영하고, 같은 보기 목록은 그대로 둔 채 선택 상태만 지우고 다시 입력을 받는다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. 바뀐 것은 오답 후 프론트의 후처리 경로다. 이전에는 `WRONG -> overlay -> GET /state -> 전체 재렌더링`, 지금은 `WRONG -> overlay -> 선택 초기화 -> 같은 화면 유지` 흐름이다.
- 핵심 도메인 개념: 같은 Stage 재시도는 상태상 “새 문제 로딩”이 아니라 “현재 문제 유지”이므로, 프론트도 전체 재조회보다 현재 상태 보정이 맞다. 즉 서버는 여전히 정답 판정과 하트 감소를 관리하지만, 프론트는 Stage가 안 바뀐 경우 불필요한 새 요청을 줄여야 한다.
- 예외 상황 또는 엣지 케이스: 정답일 때는 다음 Stage와 보기 4개가 바뀌므로 여전히 `GET /state`를 다시 호출한다. `GAME_OVER`는 재시도가 아니라 종료이므로 모달 흐름을 유지한다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 오답은 같은 Stage를 다시 푸는 상황인데도 화면 전체를 다시 불러오고 있어서 UX가 깜빡였습니다. 그래서 오답일 때는 서버 재조회 없이 응답에 담긴 하트/점수만 HUD에 반영하고, 선택만 초기화해서 같은 문제를 자연스럽게 이어가도록 바꿨습니다.
- 아직 내가 이해가 부족한 부분: 지금은 “같은 Stage면 재조회하지 않는다”가 충분하지만, 나중에 서버가 오답 시 힌트나 옵션 순서를 바꾸는 규칙을 넣으면 이 분기 기준을 다시 검토해야 한다.

## 2026-03-23 - 인구수 게임 정답 후 Next Stage 수동 진행으로 변경

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 정답 직후 자동으로 다음 문제로 넘어가면 결과를 볼 시간이 너무 짧고, 사용자가 리듬을 직접 조절하기 어렵다. 그래서 정답 후에는 `다음 Stage` 버튼으로 직접 진행하게 바꾼다.
- 변경 파일:
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/static/js/population-game.js`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions/{sessionId}/answer` 응답이 `CORRECT`면, 프론트는 하단 결과 카드와 성공 오버레이를 보여주고 자동 `GET /state`를 호출하지 않는다. 대신 `선택 제출` 옆에 `다음 Stage` 버튼을 노출하고, 사용자가 이 버튼을 눌렀을 때만 `GET /state`로 다음 문제를 받아온다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. 정답 시점에 이미 다음 Stage는 서버가 생성해 두고 있으므로, 바뀐 것은 프론트 전환 타이밍뿐이다. 이전에는 `CORRECT -> 잠시 후 자동 state 재조회`, 지금은 `CORRECT -> Next Stage 버튼 노출 -> 버튼 클릭 시 state 재조회` 흐름이다.
- 핵심 도메인 개념: “다음 Stage 생성” 책임은 여전히 서버에 있고, “언제 화면을 넘길지”만 프론트 UX 정책으로 분리한 것이다. 즉 상태 전이는 서버가 유지하고, 사용자의 템포 조절은 클라이언트가 맡는다.
- 예외 상황 또는 엣지 케이스: `GAME_OVER`는 여전히 모달 흐름을 유지하고, 정답이더라도 `FINISHED`가 생기는 모드라면 결과 화면으로 넘어가는 분기를 그대로 쓸 수 있다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임은 정답을 맞힌 직후 결과를 읽을 시간이 필요해서 자동 다음 Stage 이동을 없앴습니다. 서버는 여전히 정답 시 다음 Stage를 생성하지만, 프론트는 `다음 Stage` 버튼을 보여주고 사용자가 눌렀을 때만 새 상태를 받아오도록 바꿔 UX 템포를 분리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 정답마다 사용자가 직접 넘기는 방식이지만, 나중에 속도감을 더 중시하는 모드가 생기면 자동 전환 옵션을 따로 둘지 다시 판단해야 한다.

## 2026-03-24 - 인구수 게임 Level 1을 구간형 보기로 전환

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 현재 Level 1은 숫자 4개를 그대로 읽는 방식보다, 사용자가 `어느 인구 규모대인지`를 빠르게 판단하는 구간형이 더 게임답고 설명하기 쉽다. 그래서 서버가 인구 규모 구간 4개를 만들고 프론트는 구간 라벨만 보여주도록 바꾼다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/application/PopulationScaleBand.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationScaleBandCatalog.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationOptionLabelFormatter.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameOptionGenerator.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationOptionView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/POPULATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions`와 `GET /state` 흐름은 유지되지만, 서버는 이제 target population이 속한 `PopulationScaleBand`를 찾고 그 주변 4개 구간을 보기로 생성한다. 플레이 화면은 `PopulationOptionView.label`만 렌더링하고, `POST /answer` 응답에는 `selectedOptionLabel`, `correctOptionLabel`도 같이 들어와 정답 카드와 결과 화면에서 구간 중심으로 보여준다.
- 데이터 / 상태 변화: DB 스키마는 바꾸지 않았고, 기존 option population 칼럼에는 각 구간의 시작값(lower bound)을 저장한다. 즉 저장 구조는 유지하면서 의미만 “정확 숫자”에서 “구간 키”로 바뀌었다. 실제 국가 인구수는 여전히 `targetPopulation`으로 따로 유지해 결과 설명과 Level 2 확장에 쓸 수 있다.
- 핵심 도메인 개념: Level 1은 “정확 수치 기억력”보다 “인구 규모 감각”을 보는 편이 맞다. 그래서 서버가 `PopulationScaleBandCatalog`로 전역 구간 체계를 관리하고, 각 Stage는 그중 어떤 4개 구간을 보기로 썼는지 저장한다. 이 방식이 서비스에 있어야 하는 이유는, 같은 target country라도 어떤 구간 창(window)을 줄지와 정답 옵션 번호를 서버가 일관되게 결정해야 하기 때문이다.
- 예외 상황 또는 엣지 케이스: 구간이 고정되어 있기 때문에 어떤 국가는 경계값 근처에서 체감 난이도가 높아질 수 있다. 예를 들어 7천만 바로 아래/위 국가는 인접 구간 구분이 어렵게 느껴질 수 있어, 다음 단계에서 경계 조정 여지가 있다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test --tests com.worldmap.game.population.application.PopulationGameOptionGeneratorTest --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest` 통과.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임 Level 1은 정확 숫자 4개를 읽게 하면 피로도가 높아서, 서버가 인구 규모 구간 4개를 생성하는 방식으로 바꿨습니다. 구간 정의는 공통 카탈로그로 두고 Stage에는 구간 키를 저장해, 플레이 화면과 결과 화면 모두 같은 구간 언어로 설명 가능하게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 구간 경계는 첫 버전이라 실제 플레이 감각 기준으로는 더 다듬을 수 있다. 특히 7천만, 1억 5천만 같은 경계 근처 국가들이 체감상 어느 정도 헷갈리는지는 플레이 데이터를 보고 다시 판단해야 한다.

## 2026-03-24 - 인구수 게임 HUD / 결과 디브리프 1차 polish

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 현재 인구수 게임은 루프는 게임답게 바뀌었지만, 플레이 중 “내가 무엇을 골랐는지”와 결과 화면의 요약 밀도가 부족했다. 그래서 플레이 화면에 선택 상태와 진행 가이드를 보강하고, 결과 화면에는 핵심 러닝 지표를 추가해 설명 가능한 디브리프 화면으로 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 여전히 `GET /api/games/population/sessions/{sessionId}/state`로 현재 Stage를 받고, `POST /answer`로 판정을 요청한다. 다만 프론트는 이제 선택한 보기 라벨을 로컬 HUD에 즉시 반영하고, 서버 응답이 오면 정답/오답에 맞는 진행 가이드를 같은 화면에서 갱신한다. 결과 화면은 `GET /api/games/population/sessions/{sessionId}` 응답에 서버가 계산한 `totalAttemptCount`, `firstTryClearCount`를 추가로 받아 디브리프 카드에 표시한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았고, 결과 요약 값은 기존 `PopulationGameStage`, `PopulationGameAttempt` 기록을 읽어 서비스에서 계산한다. 즉 영속 데이터는 그대로 두고, “면접에서 설명하기 좋은 요약값”만 조회 시점에 조합한 것이다.
- 핵심 도메인 개념: 선택 상태 표시와 다음 행동 가이드는 프론트의 책임이지만, 총 시도 수나 1트 클리어 수 같은 러닝 요약은 서버가 계산해 내려줘야 한다. 이유는 이 값들이 Stage/Attempt 전체 기록을 기준으로 일관되게 계산돼야 하고, 나중에 랭킹이나 전적 페이지로 확장해도 같은 기준을 재사용할 수 있기 때문이다. 그래서 UI 문구는 JS가 관리하고, 디브리프 지표는 서비스가 만든다.
- 예외 상황 또는 엣지 케이스: 오답 후 같은 Stage를 다시 시도할 때는 `GET /state`를 재호출하지 않으므로, 선택 상태와 진행 가이드를 프론트에서 직접 초기화해야 한다. 또한 결과 요약은 아직 “총 시도 수 / 1트 클리어 수”까지만 넣었고, 연속 정답 보너스나 평균 시도 수 같은 값은 다음 polish 범위로 남겨뒀다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest` 통과, `./gradlew test` 전체 통과. 통합 테스트에는 인구수 결과 응답의 `totalAttemptCount`, `firstTryClearCount` 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임의 게임 루프는 이미 서버 주도로 정리돼 있었고, 이번에는 사용자가 현재 선택 상태와 결과를 더 잘 읽을 수 있게 polish 했습니다. 플레이 화면은 선택한 구간과 다음 행동을 바로 보여주고, 결과 화면은 Stage/Attempt 기록에서 계산한 총 시도 수와 1트 클리어 수를 서버가 내려줘서 디브리프 화면으로 설명 가능하게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 HUD 문구와 결과 지표는 첫 버전이라, 실제 플레이 기준으로 어떤 정보가 과하고 어떤 정보가 부족한지는 한 번 더 조정할 수 있다. 특히 모바일에서는 선택 상태 카드와 버튼 간격이 얼마나 읽기 좋은지 추가 확인이 필요하다.

## 2026-03-24 - 위치 게임 HUD / 결과 디브리프 1차 polish

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 위치 게임은 클릭 안정화와 재시작 루프는 갖췄지만, 플레이 중 “지금 무엇을 해야 하는지”와 결과 화면의 요약 밀도가 아직 약했다. 그래서 제출 전후 행동을 안내하는 HUD를 추가하고, 결과 화면에는 런 전체를 짧게 요약하는 지표를 넣는다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 기존처럼 `GET /api/games/location/sessions/{sessionId}/state`로 현재 Stage와 난이도를 받고, `POST /answer`로 선택한 ISO3 코드를 제출한다. 프론트는 이제 제출 전에는 여전히 국가명을 숨기되, 클릭 완료 / 정답 / 오답 / 게임오버 시점마다 `진행 가이드` 텍스트를 바꿔 사용자가 다음 행동을 바로 이해할 수 있게 했다. 결과 화면은 `GET /api/games/location/sessions/{sessionId}/result` 응답에 서버가 계산한 `totalAttemptCount`, `firstTryClearCount`를 포함해 디브리프 카드에 표시한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 새 요약값은 기존 `LocationGameStage`, `LocationGameAttempt` 기록을 읽어 서비스가 계산한다. 즉 영속 데이터는 그대로 두고, 결과 조회 응답만 더 설명 가능하게 만든 것이다.
- 핵심 도메인 개념: 위치 게임은 제출 전 국가명을 숨겨야 하므로, 플레이 HUD는 “선택 여부와 다음 행동”만 알려주는 것이 맞고, 정답/오답 이후에만 실제 국가명을 feedback 카드에서 공개한다. 반면 총 시도 수와 1트 클리어 수 같은 러닝 요약은 Stage/Attempt 전체 기록을 기준으로 서버가 계산해야 일관성이 생긴다. 그래서 행동 안내는 JS, 러닝 지표는 서비스로 역할을 분리했다.
- 예외 상황 또는 엣지 케이스: 선택 취소를 눌렀을 때는 제출 버튼뿐 아니라 진행 가이드도 현재 Stage 기본 상태로 되돌려야 한다. 또한 게임오버 직후에는 선택 상태를 비우되, 안내 문구는 모달 선택을 유도하는 방향으로 남기는 편이 흐름상 더 자연스럽다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과, `./gradlew test --tests com.worldmap.game.location.LocationGameFlowIntegrationTest` 통과, `./gradlew test` 전체 통과. 통합 테스트에는 위치 게임 결과 응답의 `totalAttemptCount`, `firstTryClearCount` 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임은 국가명을 숨긴 채 플레이해야 해서, 사용자가 헷갈리지 않게 행동 안내를 별도로 두는 게 중요했습니다. 이번에는 프론트에 선택 상태와 진행 가이드를 추가하고, 결과 화면에는 서버가 계산한 총 시도 수와 1트 클리어 수를 넣어서 한 판의 러닝을 더 짧게 설명할 수 있게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 가이드 문구는 1차 버전이라, 실제 플레이 감각 기준으로 문장이 너무 많거나 적은지 더 조정할 수 있다. 모바일에서 선택 상태 카드와 버튼들이 한 줄에 얼마나 자연스럽게 배치되는지도 추가 확인이 필요하다.

## 2026-03-24 - Redis 랭킹 1차 구현

- 단계: 5. Redis 랭킹 시스템
- 목적: 게임오버 시점 점수를 단순히 결과 페이지에서만 끝내지 않고, `RDB에 영속 저장 + Redis Sorted Set 반영 + 상위 N명 조회`까지 이어지는 랭킹 vertical slice를 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardGameLevel.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardScope.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardRankingPolicy.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardEntryView.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardView.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardApiController.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameAttemptRepository.java`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 위치 게임과 인구수 게임은 `POST /answer`에서 하트를 모두 잃어 세션이 종료되면, 서비스가 현재 run의 점수/클리어 수/총 시도 수를 모아 `LeaderboardService`에 전달한다. `LeaderboardService`는 우선 `leaderboard_record`에 종료 run을 저장하고, 트랜잭션 커밋 이후 Redis Sorted Set의 전체 키와 일간 키에 record id를 반영한다. 조회 시에는 `GET /api/rankings/{gameMode}`가 먼저 Redis에서 상위 record id를 가져오고, 그 id로 RDB 상세 정보를 읽어 응답을 만든다. `/ranking` 화면은 같은 서비스를 직접 호출해 전체/일간 top 10을 SSR로 보여준다.
- 데이터 / 상태 변화: 이제 게임 세션 외에 `leaderboard_record`가 생겼다. 중요한 점은 랭킹이 `세션`을 그대로 재사용하지 않고, 종료된 `run 결과`를 별도 레코드로 저장한다는 것이다. 이유는 현재 게임들이 같은 `sessionId`로 재시작될 수 있어서, 세션 자체를 랭킹 row로 쓰면 이전 게임 기록이 덮어써질 수 있기 때문이다.
- 핵심 도메인 개념: Redis는 정렬과 top N 조회를 빠르게 하기 위한 read model이고, 진실 공급원은 RDB의 `leaderboard_record`다. 그래서 저장은 항상 RDB가 먼저고, Redis 반영은 `after commit` 뒤에 수행한다. 또한 Redis key가 비어 있거나 유실되면 `LeaderboardService`가 RDB 상위 기록을 다시 읽어 키를 재구성하도록 만들어, “Redis가 날아가도 랭킹은 복구 가능하다”는 설명이 가능해졌다.
- 예외 상황 또는 엣지 케이스: 같은 세션을 재시작하면 새로운 run이 시작되므로, 랭킹도 별도 레코드로 남겨야 한다. 같은 종료 시점을 두 번 반영하지 않도록 `runSignature`를 두었다. Redis 반영이 실패해도 RDB에는 기록이 남으므로, 다음 조회 시 fallback으로 복구할 수 있다. 현재 일간 랭킹은 `finishedAt.toLocalDate()` 기준으로 계산하고, 레벨은 아직 `LEVEL_1`만 지원한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과, `./gradlew test` 전체 통과. 통합 테스트에서는 실제 게임오버를 발생시켜 랭킹 레코드 생성, `/api/rankings/location` 조회, `/api/rankings/population`의 DB fallback 복구, `/ranking` 페이지 렌더링을 확인했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹은 세션을 그대로 재사용하지 않고, 종료된 게임 run을 `leaderboard_record`로 따로 저장했습니다. 게임오버가 되면 먼저 RDB에 기록하고, 커밋이 끝난 뒤 Redis Sorted Set의 전체/일간 키에 record id를 반영합니다. 조회는 Redis에서 상위 id를 빠르게 가져오고, 상세 정보는 RDB에서 채워서 보여줍니다.
- 아직 내가 이해가 부족한 부분: 현재는 Level 1 전체/일간 top 10까지만 구현했고, 동점 처리 규칙과 실시간 갱신 체감은 더 다듬을 수 있다. 이후 SSE를 붙일 때 지금의 SSR 화면을 어떻게 부드럽게 갱신할지도 한 번 더 설계해야 한다.

## 2026-03-24 - 랭킹 페이지 짧은 주기 폴링 추가

- 단계: 5. Redis 랭킹 시스템
- 목적: `/ranking`이 처음 렌더링된 뒤 멈춰 있는 정적 화면처럼 보이지 않게 하고, Redis 랭킹의 장점을 사용자가 바로 체감할 수 있도록 짧은 주기 자동 갱신을 붙인다.
- 변경 파일:
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/js/ranking.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `/ranking`은 SSR로 처음 렌더링된 뒤, 브라우저가 `ranking.js`에서 15초마다 `/api/rankings/location`, `/api/rankings/population`의 전체/일간 API를 다시 호출한다. 응답을 받으면 프론트가 각 표의 `tbody`만 다시 그려 전체 페이지 새로고침 없이 랭킹을 갱신한다. 탭이 비활성화되면 폴링을 잠시 멈추고, 다시 활성화되면 즉시 한 번 새로고침한 뒤 주기를 재개한다.
- 데이터 / 상태 변화: 서버 도메인과 DB 스키마는 바뀌지 않았다. 바뀐 것은 `/ranking` 페이지의 조회 후처리 방식이다. SSR 초기 HTML은 그대로 유지하고, 이후 갱신은 API 응답으로 표만 교체한다.
- 핵심 도메인 개념: 이 단계에서는 SSE보다 짧은 주기 폴링이 더 설명하기 쉽다. 이유는 기존 랭킹 API를 그대로 재사용하면서도 “Redis로 빨리 읽어온 상위 N명을 화면에 주기적으로 반영한다”는 흐름을 단순하게 보여줄 수 있기 때문이다. 즉 저장과 정렬의 핵심은 여전히 서버/Redis에 있고, 프론트는 이미 만들어진 API를 주기적으로 읽어 표현만 갱신한다.
- 예외 상황 또는 엣지 케이스: 자동 갱신 중 에러가 나면 기존 표 내용은 유지하고 상단 메시지 박스에 오류만 띄운다. 같은 탭이 백그라운드에 있을 때는 불필요한 호출을 줄이기 위해 폴링을 멈춘다. 현재는 모든 보드를 한 번에 갱신하므로, 이후 모드/범위 필터가 들어가면 호출 범위를 다시 좁힐 수 있다.
- 테스트 내용: `node --check src/main/resources/static/js/ranking.js` 통과, `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과, `./gradlew test` 전체 통과. 랭킹 통합 테스트에는 `/ranking` 페이지가 새로고침 버튼을 포함해 렌더링되는지 확인을 추가했다. 랭킹 전용 테스트를 전체 테스트와 동시에 돌렸을 때는 Gradle 결과 파일 충돌이 있었지만, 단독 재실행으로 정상 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹 1차는 저장과 조회 구조까지였고, 이번에는 `/ranking` 화면에 15초 간격 폴링을 붙여 실시간 갱신 체감을 만들었습니다. 프론트는 기존 랭킹 API를 주기적으로 다시 읽어 표만 바꾸고, 탭 비활성화 시에는 호출을 멈춰 불필요한 트래픽도 줄였습니다.
- 아직 내가 이해가 부족한 부분: 지금은 짧은 주기 폴링으로 충분하지만, 이후 사용자가 많아지거나 더 즉각적인 반영이 필요하면 SSE와 어떤 기준으로 갈아탈지 판단 기준을 더 명확히 해야 한다.

## 2026-03-24 - blog 동시 작성 규칙 정리와 랭킹 글 백필

- 단계: 0. 문서와 규칙 정리 / 5. Redis 랭킹 시스템 설명 보강
- 목적: 최근 랭킹 기능은 코드, 플레이북, 워크로그까지는 남았지만 `blog/` 공개 설명 글이 같은 턴에 작성되지 않았다. 그래서 “의미 있는 기능 조각은 docs와 함께 blog도 동시에 남긴다”는 규칙을 문서와 로컬 스킬에 명시하고, 누락된 랭킹 글 2개를 실제 코드 기준으로 백필한다.
- 변경 파일:
  - `AGENTS.md`
  - `.agents/skills/worldmap-doc-sync/SKILL.md`
  - `.agents/skills/worldmap-doc-sync/references/blog-update-rules.md`
  - `.agents/skills/worldmap-doc-sync/references/doc-impact-map.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/06-redis-leaderboard-vertical-slice.md`
  - `blog/07-leaderboard-polling-refresh.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 이번 변경은 애플리케이션 런타임 흐름을 바꾸지 않는다. 대신 저장소의 문서 흐름을 바꾼다. 앞으로 의미 있는 기능 조각이 끝나면 `코드 + 테스트 + docs + blog`를 같은 턴 기본값으로 삼고, `worldmap-doc-sync`도 그 기준으로 문서 영향 범위를 판단한다. 동시에 랭킹 관련 blog 글은 이미 구현된 `LeaderboardService`, `/api/rankings/*`, `/ranking`, `ranking.js` 흐름을 각각 “저장/조회 구조”와 “화면 갱신 구조”로 나눠 설명한다.
- 데이터 / 상태 변화: DB 스키마나 Redis 키는 바뀌지 않았다. 바뀐 것은 저장소의 기록 정책이다. `docs/`는 내부 SSOT 역할을 유지하고, `blog/`는 의미 있는 기능 조각마다 같은 턴에 따라오는 공개 설명 레이어로 기준이 강화됐다.
- 핵심 도메인 개념: 이 프로젝트는 AI 도움을 받아 개발하더라도 사용자가 구현 이유를 직접 설명할 수 있어야 한다. 그래서 `WORKLOG`만으로 내부 메모를 남기는 데 그치지 않고, 초보자도 읽을 수 있는 공개 설명 글까지 같은 시점에 남겨야 이해 공백이 줄어든다. 특히 랭킹처럼 “RDB source of truth + Redis read model + polling UI”가 겹치는 주제는 블로그 분리가 설명력을 크게 높인다.
- 예외 상황 또는 엣지 케이스: 모든 변경에 블로그를 강제하면 문서가 과도하게 불어난다. 그래서 버튼 문구, 오타, 아주 작은 CSS 조정처럼 설명 가치가 낮은 수정은 예외로 두고, 이런 경우에는 블로그를 생략한 이유를 워크로그나 최종 설명에 짧게 남기도록 규칙을 잡았다.
- 테스트 내용: 코드 동작 변경은 없는 문서 작업이므로 애플리케이션 테스트는 별도로 실행하지 않았다. 대신 새 blog 글은 이미 통과했던 `LeaderboardIntegrationTest`, `./gradlew test`, `node --check src/main/resources/static/js/ranking.js` 결과를 근거로 작성했다.
- 면접에서 30초 안에 설명하는 요약: 이 프로젝트는 AI 도움을 받더라도 나중에 제가 직접 설명할 수 있어야 해서, 의미 있는 기능 조각은 코드와 내부 문서뿐 아니라 blog 글도 같은 턴에 같이 남기도록 운영 규칙을 강화했습니다. 이번에는 빠져 있던 Redis 랭킹 저장 구조와 랭킹 폴링 UI 글을 백필해, 랭킹 기능을 문서와 공개 글 기준으로 모두 설명 가능하게 맞췄습니다.
- 아직 내가 이해가 부족한 부분: 어디까지를 “설명 가치가 있는 기능 조각”으로 보고 blog를 반드시 써야 하는지는 아직 약간의 판단 여지가 있다. 특히 작은 UX 수정이 누적돼 게임 루프 체감이 크게 달라지는 경우, 어느 시점에서 하나의 blog 글로 묶는 것이 가장 좋은지 경험이 더 필요하다.

## 2026-03-24 - 랭킹 화면 필터와 동점 규칙 노출

- 단계: 5. Redis 랭킹 시스템
- 목적: 랭킹 저장 구조와 15초 폴링은 이미 있었지만, 위치/인구수와 전체/일간 보드를 한 화면에 모두 펼쳐 놓으니 읽기 순서가 약했다. 그래서 active 보드 하나만 크게 보는 필터 UI로 정리하고, 동점 처리 기준도 화면에 직접 노출해 랭킹 규칙을 더 설명 가능하게 만든다.
- 변경 파일:
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/js/ranking.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/08-ranking-filter-and-tie-rule.md`
- 요청 흐름 / 데이터 흐름: `/ranking`은 여전히 SSR로 처음 렌더링되고, 브라우저는 `ranking.js`에서 15초마다 기존 `/api/rankings/location`, `/api/rankings/population` API를 다시 호출한다. 달라진 점은 UI가 이제 active 보드 하나만 보여준다는 것이다. `위치/인구수`, `전체/일간` 버튼은 프론트 로컬 상태만 바꾸고, 폴링은 같은 API 응답으로 각 `tbody`를 갱신한다. 즉 정렬 결과 계산은 계속 서버가 맡고, 어떤 보드를 보여줄지는 프론트가 맡는다.
- 데이터 / 상태 변화: DB 스키마와 Redis 키 전략은 바뀌지 않았다. `leaderboard_record`, `rankingScore`, Redis Sorted Set 구조도 그대로 유지된다. 바뀐 것은 `/ranking`의 표현 구조와 안내 정보다. 화면에 동점 규칙을 직접 적어, `rankingScore desc -> finishedAt asc` 기준이 UI에서도 읽히게 만들었다.
- 핵심 도메인 개념: 이번 변경은 도메인 변경이 아니라 표현 변경이다. 중요한 점은 “필터 전환은 프론트가 해도 되지만, 정렬과 동점 처리 규칙은 서버가 계속 가져가야 한다”는 경계를 지킨 것이다. 그래서 새 API를 늘리지 않고 기존 랭킹 API를 재사용했고, SSR 템플릿은 4개 보드를 준비하되 active 보드만 노출하도록 바꿨다.
- 예외 상황 또는 엣지 케이스: 현재 daily 보드 설명 문구의 기준 날짜는 SSR 시점 값을 사용한다. 같은 날 안에서는 충분하지만, 자정을 넘기는 장시간 탭 유지 상황까지 완전히 매끈하게 맞추려면 API 응답의 `targetDate`를 기준으로 설명 문구도 다시 갱신하도록 한 단계 더 손볼 수 있다. 또 필터 전환은 로컬 UI 상태이므로 URL deep link까지는 아직 지원하지 않는다.
- 테스트 내용: `node --check src/main/resources/static/js/ranking.js` 통과, `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과. 통합 테스트에는 `/ranking` 페이지가 `게임 모드`, `동점 처리` 문구를 포함해 렌더링되는지 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹 저장과 조회 구조는 이미 만들었고, 이번에는 화면을 더 설명 가능하게 정리했습니다. 기존 API는 그대로 두고 프론트에서 `위치/인구수`, `전체/일간` 필터로 active 보드 하나만 크게 보여주게 바꿨고, 동점 처리 규칙도 화면에 명시해서 `rankingScore` 기준이 UI에서도 읽히게 했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 active 보드 전환이 로컬 상태라 충분하지만, 나중에 URL 공유나 deep link가 필요해지면 query parameter와 어떻게 연결할지 한 번 더 설계해야 한다. 또한 daily 보드 설명 문구 날짜를 폴링 응답 기준으로 더 정교하게 갱신할지 여부도 판단이 남아 있다.

## 2026-03-24 - 랭킹 단계 마감과 polling 유지 결정

- 단계: 5. Redis 랭킹 시스템 마감
- 목적: 랭킹은 이미 `RDB 저장 + Redis Sorted Set + fallback 조회 + polling UI`까지 구현됐기 때문에, 더 복잡한 SSE를 바로 붙이기보다 현재 전략을 명시적으로 닫는 편이 맞다. 그래서 화면과 문서에 “현재는 15초 polling으로 운영하고, SSE/WebSocket은 9단계 고도화 범위”라는 결정을 남긴다.
- 변경 파일:
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/07-leaderboard-polling-refresh.md`
- 요청 흐름 / 데이터 흐름: 런타임 데이터 흐름은 바뀌지 않는다. `/ranking`은 여전히 SSR 후 15초마다 랭킹 API를 다시 읽는다. 이번 변경은 그 선택을 제품에 명시한 것이다. 사용자는 화면 상단에서 현재 전달 방식이 `15초 Polling`임을 바로 읽을 수 있고, 문서상으로도 SSE/WebSocket은 `9단계 실시간성 고도화`로 넘긴다고 정리했다.
- 데이터 / 상태 변화: DB 스키마, Redis 키, API 응답은 바뀌지 않았다. 바뀐 것은 단계 상태와 설명 문구다. 플레이북에서는 5단계를 `Done`으로 닫고, 실시간 전송 고도화는 9단계로 이관했다.
- 핵심 도메인 개념: 지금 중요한 것은 “실시간 같아 보이는 랭킹 체감”이지, 가장 복잡한 실시간 기술을 먼저 붙이는 것이 아니다. 현재 구조에서는 polling이 이미 Redis read model과 잘 맞고, 설명도 쉽다. 따라서 지금은 polling으로 마감하고, 실제로 더 낮은 지연이나 서버 push가 필요해질 때만 SSE/WebSocket으로 올리는 판단이 더 합리적이다.
- 예외 상황 또는 엣지 케이스: 현재 문구는 전략 결정 자체를 드러내기 위한 것이고, 사용자가 polling 주기까지 바꾸거나 URL에 전략을 선택하는 기능은 없다. 이후 사용량이 늘거나 자정 경계 같은 더 빠른 반영이 중요해지면 그때 SSE로 넘어갈 근거를 다시 모아야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과. 통합 테스트에는 `/ranking`이 `15초 Polling` 문구를 포함하는지 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹은 이미 저장/조회/복구 구조가 완성돼 있었기 때문에, 이번에는 전략을 마감하는 작업을 했습니다. 현재 MVP에서는 15초 polling으로 충분하다고 판단해 그 결정을 화면과 문서에 남겼고, 더 복잡한 SSE/WebSocket은 9단계 실시간성 고도화 범위로 분리했습니다.
- 아직 내가 이해가 부족한 부분: 언제 polling이 더 이상 충분하지 않고 SSE/WebSocket으로 넘어가야 하는지에 대한 정량 기준은 아직 약하다. 이후 사용자 수, 업데이트 빈도, 서버 리소스 데이터를 본 뒤 판단 기준을 더 구체화해야 한다.

## 2026-03-24 - 설문 기반 추천 엔진 1차 vertical slice

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 기능을 바로 LLM에게 맡기지 않고, 설문 답변만으로 deterministic하게 top 3 국가를 계산하는 서버 엔진을 먼저 만든다. 이번 조각에서는 설문 페이지, 답변 타입, 국가 프로필 카탈로그, 가중치 계산 서비스, 결과 SSR 화면까지 연결한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfile.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationOptionView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationPreferenceSummaryView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCandidateView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyResultView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/static/css/site.css`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/09-survey-recommendation-engine.md`
- 요청 흐름 / 데이터 흐름: 사용자는 `GET /recommendation/survey`로 설문 페이지를 열고 6개 문항을 고른 뒤 `POST /recommendation/survey`를 보낸다. 컨트롤러는 `RecommendationSurveyForm`으로 입력을 검증하고, 이를 `RecommendationSurveyAnswers` 불변 객체로 바꿔 `RecommendationSurveyService.recommend()`에 넘긴다. 서비스는 `CountryRepository`의 기본 국가 정보와 `RecommendationCountryProfileCatalog`의 추천 전용 속성을 합쳐 점수를 계산하고, 상위 3개 결과를 `RecommendationSurveyResultView`로 만들어 SSR 결과 페이지에 넘긴다.
- 데이터 / 상태 변화: 아직 DB 스키마는 바뀌지 않았다. 이번 단계의 “답변 저장 구조”는 DB 엔티티가 아니라 `폼 객체 -> 불변 답변 객체` 구조다. 이유는 지금 단계의 핵심이 추천 규칙 확정이기 때문이다. 국가 기본 데이터는 기존 `country` 테이블을 재사용하고, 추천용 속성만 별도 프로필 카탈로그로 시작했다.
- 핵심 도메인 개념: 추천 계산은 LLM이 아니라 서버가 해야 한다. 그래서 설문 입력 자체도 enum 기반으로 타입을 고정했고, 추천 후보 비교는 기후/생활 속도/물가/환경/영어/최우선 기준을 점수화해 deterministic하게 처리했다. 즉 이번 결과 화면은 “자연어 설명”이 아니라 “서버가 계산한 근거”를 먼저 보여주는 1차 엔진이다.
- 예외 상황 또는 엣지 케이스: 현재 추천 프로필은 12개 국가로 시작하는 수작업 카탈로그라, 후보 풀이 아직 제한적이다. 또한 설문 답변은 현재 세션 기록으로 저장하지 않으므로 나중에 추천 이력이나 재조회 기능을 붙일 때는 별도 저장 모델이 필요하다. 영문/현지 언어 적응 난이도도 지금은 영어 친화도 한 축으로 단순화했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.web.HomeControllerTest` 통과. 서비스 테스트는 warm/fast/high/city/high english/diversity 조합에서 `싱가포르`가 1위로 나오는지 확인했고, 통합 테스트는 설문 페이지 렌더링과 설문 제출 후 결과 페이지 SSR 렌더링을 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 기능은 처음부터 계산과 설명을 분리했습니다. 이번 단계에서는 설문 답변을 enum 기반으로 고정하고, 서버가 국가 프로필 카탈로그와 비교해 가중치 점수로 top 3를 계산하도록 만들었습니다. 즉 추천 결과 자체는 deterministic하게 서버가 만들고, LLM은 다음 단계에서 그 결과를 설명하는 역할만 맡게 됩니다.
- 아직 내가 이해가 부족한 부분: 현재 국가 프로필 12개는 시작용이라 추천 품질을 더 높이려면 후보 국가 수와 속성을 더 늘려야 한다. 또한 답변 저장을 언제 DB 엔티티로 올릴지, LLM 프롬프트 입력용 구조를 어디서 고정할지도 다음 단계에서 더 분명히 해야 한다.

## 2026-03-24 - 공통 박스/버튼 모서리 각지게 통일

- 단계: 공통 UI 폴리시
- 목적: 현재 사이트 전반의 버튼, 패널, 카드, 입력창, 모달이 둥근 모서리를 써서 “차갑고 각진 우주 HUD”라는 시각 방향과 어긋나 있었다. 이번 조각에서는 공통 CSS를 한 번에 정리해 모든 박스형 컴포넌트의 모서리를 각지게 통일한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청이나 서버 데이터 흐름은 바뀌지 않았다. 바뀐 것은 공통 표현 계층이다. 모든 화면은 같은 `site.css`를 공유하므로, 헤더 네비게이션, 버튼, 카드, 입력창, 글로브 스테이지, 게임오버 모달, 결과 배너까지 공통 스타일 토큰을 한 번에 수정하는 방식으로 반영된다.
- 데이터 / 상태 변화: DB, Redis, API, 세션 상태 변화는 없다. 변경은 전부 프론트 공통 테마 레벨에 머문다.
- 핵심 도메인 개념: 이 프로젝트는 기능만 아니라 “게임 플랫폼처럼 보이는 일관된 화면 톤”도 중요하다. 각 화면에서 개별적으로 radius를 줄이는 대신 공통 CSS의 반경 선언을 전부 제거해, 모든 박스형 UI가 같은 시각 규칙을 따르도록 맞췄다.
- 예외 상황 또는 엣지 케이스: 이번 검증 범위는 프로젝트가 직접 관리하는 공통 CSS와 템플릿/정적 스크립트이다. 외부 vendor 파일은 수정하지 않았다. 검색 기준으로는 `src/main/resources/static/css/site.css`, `src/main/resources/templates`, `src/main/resources/static/js`, `src/main/resources/static`에서 vendor를 제외하고 `border-radius|radius`를 다시 확인했다.
- 테스트 내용: 자동 UI 테스트는 없어서 애플리케이션 테스트는 돌리지 않았다. 대신 `git diff --check` 통과, `rg -n "border-radius|radius"` 재검색으로 프로젝트가 직접 관리하는 스타일 영역의 반경 선언을 다시 확인했다.
- 면접에서 30초 안에 설명하는 요약: 디자인 톤을 맞추기 위해 버튼과 카드만 일부 고친 게 아니라, 공통 CSS에서 반경 선언을 한 번에 정리했습니다. 그래서 헤더, 버튼, 입력창, 모달, 게임 스테이지 같은 박스형 UI가 모두 같은 각진 HUD 규칙을 따르게 됐습니다.
- 아직 내가 이해가 부족한 부분: 지금은 전역적으로 반경을 0으로 통일했지만, 이후 브랜드 아이덴티티를 더 다듬는 과정에서 “완전한 직각”과 “아주 작은 모따기” 중 어느 쪽이 더 좋은지 시각적으로 한 번 더 비교해볼 필요가 있다.
- blog 작성 여부: 생략. 이번 변경은 요청 흐름이나 도메인 모델이 아니라 공통 테마 조정이라 별도 기술 글보다 WORKLOG 기록이 더 적합하다고 판단했다.

## 2026-03-24 - 공통 박스/버튼 컷 코너 HUD로 재조정

- 단계: 공통 UI 폴리시
- 목적: `border-radius: 0`만으로는 박스가 네모가 되더라도 전체 인상이 여전히 부드럽게 느껴질 수 있었다. 그래서 이번에는 공통 표면 자체를 컷 코너(chamfer) 형태로 바꿔, 버튼과 패널이 더 기계적이고 각진 HUD처럼 보이도록 재조정한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청이나 서버 데이터 흐름은 바뀌지 않았다. 모든 페이지가 공통 `site.css`를 공유하므로, 헤더 네비게이션, 버튼, 입력창, 카드, 모달, 글로브 스테이지 같은 박스형 UI는 같은 컷 코너 규칙을 바로 공유하게 된다.
- 데이터 / 상태 변화: 상태 변화는 없다. 전부 공통 표현 계층 수정이다.
- 핵심 도메인 개념: 이번 수정의 핵심은 “직각”과 “각진 인상”이 다르다는 점이다. 단순히 radius를 0으로 두는 것만으로는 충분하지 않아서, 공통 표면에 동일한 clip-path 컷 코너 규칙을 적용해 디자인 언어 자체를 더 날카롭게 통일했다.
- 예외 상황 또는 엣지 케이스: 이번 범위는 공통 CSS가 직접 관리하는 표면 요소들이다. 외부 vendor 자산은 건드리지 않았다. 테이블 셸처럼 overflow가 있는 요소도 같은 규칙을 적용했기 때문에, 실제 체감은 브라우저 강력 새로고침 후 확인하는 것이 안전하다.
- 테스트 내용: `git diff --check` 통과, `rg -n "border-radius|border-top-left-radius|border-top-right-radius|border-bottom-left-radius|border-bottom-right-radius|radius"` 재검색으로 프로젝트가 직접 관리하는 스타일 범위를 다시 점검했다. `curl -I http://localhost:8080/css/site.css`로 새 CSS가 정상 서빙되는 것도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 처음에는 모서리 반경만 제거했지만, 그 정도로는 여전히 화면이 부드럽게 느껴졌습니다. 그래서 공통 CSS에서 버튼, 패널, 입력창, 모달 같은 박스형 UI 전체에 같은 컷 코너 규칙을 넣어, 사이트 전반이 더 각진 게임 HUD처럼 보이도록 한 번 더 정리했습니다.
- 아직 내가 이해가 부족한 부분: clip-path 기반 컷 코너가 브라우저별 렌더링이나 모바일 성능에 어떤 차이를 주는지는 실제 기기에서 추가 확인이 필요하다.
- blog 작성 여부: 생략. 이번 변경도 요청 흐름이나 도메인 모델이 아니라 공통 테마 조정이라 WORKLOG 기록으로 충분하다고 판단했다.

## 2026-03-24 - 공통 CSS 반영 경로 정리와 각진 HUD 2차 강화

- 단계: 공통 UI 폴리시
- 목적: 사용자 스크린샷상 둥근 디자인이 그대로 남아 있었고, 확인 결과 8080에서 실제로 내려가던 `site.css`가 수정 전 구버전이었다. 그래서 이번에는 컷 코너를 더 크게 강화하는 동시에, 모든 템플릿의 스타일 링크에 버전 쿼리를 붙여 새 공통 CSS가 확실히 반영되도록 정리한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 비즈니스 요청 흐름은 바뀌지 않았다. 바뀐 것은 렌더링 자산 전달 경로다. 각 페이지 템플릿은 이제 `/css/site.css?v=20260324-angular-hud-2` 형태로 공통 스타일을 참조해, 브라우저와 서버가 이전 CSS를 잡고 있더라도 새 버전을 강제로 읽게 된다.
- 데이터 / 상태 변화: DB, Redis, API, 게임 세션 상태는 바뀌지 않는다. 이번 변경은 전부 공통 UI 자산 전달과 표현 계층에 있다.
- 핵심 도메인 개념: “반경을 0으로 둔다”와 “사용자가 실제로 각진 화면을 본다”는 다른 문제다. 이번에는 컷 코너 크기를 더 키워 디자인 언어를 더 날카롭게 만들고, 동시에 전역 스타일 링크에 버전 쿼리를 붙여 수정본이 실제로 화면에 반영되도록 했다.
- 예외 상황 또는 엣지 케이스: 이 수정은 템플릿이 직접 참조하는 공통 CSS에만 적용된다. 만약 브라우저 탭이 아주 오래된 HTML 자체를 들고 있으면 한 번 새로고침이 여전히 필요할 수 있다. 또한 현재 버전 쿼리는 수동 문자열이므로, 나중에 정적 자산 빌드 파이프라인이 생기면 해시 기반으로 바꾸는 편이 더 좋다.
- 테스트 내용: `/css/site.css` 응답 본문을 직접 확인해 이전 `border-radius: 18px`, `999px` 구버전이 내려오고 있음을 재현했고, 이후 템플릿 링크와 CSS를 수정했다. `git diff --check` 통과, `rg -n "site.css"`로 모든 템플릿의 스타일 링크를 다시 점검했다.
- 면접에서 30초 안에 설명하는 요약: 처음에는 공통 CSS만 수정했지만, 실제 화면은 오래된 정적 자산을 계속 보고 있었습니다. 그래서 버튼과 패널의 컷 코너를 더 크게 강화하는 동시에, 모든 페이지의 `site.css` 링크에 버전 쿼리를 붙여 새 스타일이 확실히 반영되도록 정리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 수동 버전 문자열로 캐시를 깨고 있는데, 이후 빌드/배포 환경이 생기면 정적 자산 fingerprinting으로 어떻게 자동화할지 더 정리해야 한다.
- blog 작성 여부: 생략. 이번 변경은 공통 스타일 반영 경로와 시각 조정이라 별도 기술 글보다 WORKLOG 기록이 적합하다고 판단했다.

## 2026-03-24 - 컷 코너 제거와 완전한 사각형 UI로 재정렬

- 단계: 공통 UI 폴리시
- 목적: 사용자 피드백 기준으로 컷 코너 자체가 촌스럽고 불필요했다. 이번에는 “각진 느낌”이 아니라 진짜 직사각형을 목표로, 공통 UI에서 잘린 모서리 규칙을 제거하고 모든 박스형 요소를 완전한 사각형으로 정리한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 바뀌지 않았다. 렌더링 레이어에서만 바뀌었다. 모든 템플릿이 이제 `/css/site.css?v=20260324-square-ui-1`을 참조하고, 공통 CSS에서는 컷 코너용 `clip-path`와 관련 변수들을 제거해 단순한 사각형 표면만 남겼다.
- 데이터 / 상태 변화: 없다. 표현 계층만 수정했다.
- 핵심 도메인 개념: 사용자가 원하는 것은 “기계적인 컷 코너”가 아니라 “깔끔한 직사각형”이었다. 그래서 반경을 없앤 뒤 남아 있던 clip-path까지 제거해, 박스형 UI가 해석 여지 없이 직사각형으로 보이도록 정리했다.
- 예외 상황 또는 엣지 케이스: 버전 쿼리를 다시 바꿨기 때문에 새 HTML을 받은 뒤에는 오래된 CSS를 잡을 가능성이 낮다. 다만 브라우저 탭이 아주 오래된 문서를 유지 중이면 한 번 새로고침은 여전히 필요할 수 있다.
- 테스트 내용: `rg -n "clip-path|surface-cut|cut-sm|cut-md|cut-lg"`로 컷 코너 관련 선언과 참조를 다시 확인했고, 스타일 링크가 모든 템플릿에서 `20260324-square-ui-1`로 바뀌었는지 검색으로 점검했다.
- 면접에서 30초 안에 설명하는 요약: 처음에는 radius 제거, 그다음엔 컷 코너까지 시도했지만 사용자 취향과 안 맞았습니다. 그래서 공통 CSS에서 컷 코너 자체를 걷어내고, 모든 박스형 UI를 `border-radius: 0`인 완전한 직사각형으로 다시 정리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 완전한 직사각형으로 통일했지만, 이후 타이포/간격/보더 두께까지 함께 다듬어야 더 세련된 결과가 나올 수 있다.
- blog 작성 여부: 생략. 이번 변경은 공통 테마 정리라 WORKLOG 기록으로 충분하다고 판단했다.

## 2026-03-24 - 위치 게임 선택 HUD 단순화와 핑크 네온 하이라이트

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 플레이 화면에서 `선택 상태`, `진행 가이드`, `선택 취소`가 오히려 게임 템포를 끊고 있었다. 이번에는 선택 정보 텍스트를 걷어내고, 지구본 위 하이라이트와 제출 버튼만 남겨 위치 게임 입력 흐름을 더 단순하게 만든다.
- 변경 파일:
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 여전히 `GET /api/games/location/sessions/{sessionId}/state`로 현재 Stage를 받고, 사용자가 국가를 클릭하면 클라이언트 로컬 상태의 `selectedCountryIso3Code`만 바뀐다. 달라진 점은 이 선택 상태를 더 이상 하단 텍스트 HUD로 풀어주지 않는다는 것이다. 사용자는 지구본 위 선택 하이라이트를 보고 바로 `POST /api/games/location/sessions/{sessionId}/answer` 제출만 한다.
- 데이터 / 상태 변화: 서버 API나 엔티티는 바뀌지 않았다. 바뀐 것은 프론트 표현과 입력 흐름이다. `selectedCountryIso3Code`는 여전히 클라이언트 로컬 상태로 유지되고, 사용자가 다른 국가를 다시 클릭하면 곧바로 교체된다.
- 핵심 도메인 개념: 위치 게임의 선택 상태는 서버가 저장할 필요가 없는 일시적 UI 상태다. 그래서 텍스트 박스로 상태를 길게 설명하기보다, 지구본 위 하이라이트 자체를 선택 신호로 쓰는 편이 게임답고 단순하다. 제출 전 국가명 비노출 규칙도 그대로 유지된다.
- 예외 상황 또는 엣지 케이스: 선택 취소 버튼을 제거했기 때문에 “선택 해제” 대신 “다른 나라 재선택”이 유일한 변경 방식이 됐다. 아무 것도 선택하지 않은 상태에서 제출하면 기존처럼 메시지 박스로 안내한다. 핑크 네온 하이라이트는 선택된 국가의 cap/stroke/altitude만 조정하므로 서버 판정에는 영향을 주지 않는다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과, `git diff --check` 통과. 추가로 플레이 페이지 HTML을 확인해 `선택 상태`, `진행 가이드`, `선택 취소` 요소가 제거되고, 제출 버튼만 남았는지 점검했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임은 선택 상태를 굳이 텍스트 박스로 길게 보여줄 필요가 없다고 판단했습니다. 그래서 하단에서는 제출 버튼만 남기고, 선택 여부는 지구본 위 핑크 네온 하이라이트로만 보여주도록 단순화했습니다. 선택을 바꾸고 싶으면 취소 대신 다른 국가를 다시 클릭하면 됩니다.
- 아직 내가 이해가 부족한 부분: 핑크 네온 하이라이트 강도가 실제 플레이에서 너무 강하거나 약한지, 그리고 모바일 화면에서 제출 버튼 하나만 있는 구성이 충분히 직관적인지는 한 번 더 체감 확인이 필요하다.
- blog 작성 여부: 생략. 이번 변경은 위치 게임 입력 HUD를 단순화하는 UX polish라 WORKLOG 기록으로 충분하다고 판단했다.

## 2026-03-24 - 추천 후보 국가 풀 30개로 확장

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 계산 구조는 이미 만들어졌지만, 후보 풀이 12개 국가에 머물러 있어 결과가 몇몇 익숙한 국가에 과도하게 몰릴 수 있었다. 이번 조각에서는 점수 계산 로직은 유지하고, 추천 전용 국가 프로필 카탈로그를 30개로 확장해 후보 다양성을 먼저 높인다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalogTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/09-survey-recommendation-engine.md`
  - `blog/10-expand-recommendation-candidate-pool.md`
- 요청 흐름 / 데이터 흐름: 추천 요청 흐름 자체는 그대로다. 사용자가 설문을 제출하면 `RecommendationSurveyForm -> RecommendationSurveyAnswers -> RecommendationSurveyService.recommend()` 순서로 흐른다. 달라진 점은 서비스가 순회하는 `RecommendationCountryProfileCatalog`의 데이터 폭이다. 이제 북미, 유럽, 동아시아, 동남아, 중동, 남미, 아프리카, 오세아니아까지 포함한 30개 프로필을 비교 대상으로 사용한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 이번 단계의 변화는 추천 전용 카탈로그 데이터다. `country` 테이블은 여전히 국가 기본 정보 source of truth로 남고, 추천 속성만 별도 프로필 카탈로그에 더 풍부하게 채워 넣었다.
- 핵심 도메인 개념: 추천 품질은 계산 공식뿐 아니라 “어떤 후보 데이터를 비교하느냐”에 크게 의존한다. 그래서 이번 단계에서는 가중치 수식을 먼저 뒤엎지 않고, 동일한 점수 계산 구조가 더 넓은 후보 풀을 평가하도록 만들어 추천 결과의 다양성을 키웠다.
- 예외 상황 또는 엣지 케이스: 프로필 카탈로그가 커질수록 ISO 코드 오타나 시드 데이터와의 불일치가 숨어들 수 있다. 그래서 이번에는 추천 품질 테스트뿐 아니라 “모든 프로필 ISO 코드가 실제 시드 국가에 존재하는가”를 검증하는 카탈로그 테스트를 추가했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과. 카탈로그 테스트는 30개 / 중복 없음 / 시드 ISO 존재 여부를 검증했고, 서비스 테스트는 확장 후보 풀에서 `말레이시아` 같은 신규 후보가 실제 1위로 올라오는 시나리오를 고정했다.
- 면접에서 30초 안에 설명하는 요약: 추천 엔진은 계산식만큼 후보 데이터가 중요해서, 이번에는 점수 공식을 바꾸지 않고 추천 프로필 카탈로그를 12개에서 30개로 넓혔습니다. 북미, 유럽, 동남아, 남미, 아프리카까지 후보를 분산시킨 뒤, ISO 유효성과 실제 추천 결과 상위권 진입 여부를 테스트로 고정해 추천 다양성을 먼저 높였습니다.
- 아직 내가 이해가 부족한 부분: 지금은 프로필 값을 수작업으로 관리하므로, 이후 후보 국가가 더 늘어나면 어떤 기준으로 값을 보정하고 versioning할지 더 정리할 필요가 있다.

## 2026-03-24 - 추천 결과 비저장 원칙과 만족도 피드백 수집 1차

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 결과 자체를 DB에 저장하지 않겠다는 방향을 명확히 하고, 대신 설문 개선을 위한 최소 신호만 남기는 구조를 만든다. 이번 조각에서는 결과 페이지에서 `1~5점 만족도`, `surveyVersion`, `engineVersion`, 사용자가 고른 6개 답변만 익명으로 저장한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyResultView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackPayloadView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSubmission.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSavedResponse.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/static/js/recommendation-feedback.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/12-collect-recommendation-feedback.md`
- 요청 흐름 / 데이터 흐름: 사용자는 `POST /recommendation/survey`로 추천 결과 페이지를 받는다. 이때 서버는 `RecommendationSurveyResultView` 안에 `surveyVersion`, `engineVersion`, 그리고 선택한 6개 답변 코드로 이루어진 feedback payload를 같이 넣어 준다. 결과 페이지에서 사용자가 만족도 1~5점을 고르면 브라우저가 `POST /api/recommendation/feedback`를 호출하고, 서버는 `RecommendationFeedbackRequest -> RecommendationFeedbackSubmission -> RecommendationFeedbackService` 순서로 익명 피드백 레코드를 저장한다.
- 데이터 / 상태 변화: 추천 결과 top 3 자체는 저장하지 않는다. 저장되는 것은 `surveyVersion`, `engineVersion`, `satisfactionScore`, 그리고 6개 답변 enum 스냅샷이다. 즉 “결과 저장”이 아니라 “설문 개선 신호 저장”으로 범위를 제한했다.
- 핵심 도메인 개념: 이번 설계의 핵심은 “추천 결과를 기록하지 않아도 설문을 개선할 수 있다”는 점이다. 어떤 설문 버전과 엔진 버전에서 만족도가 높거나 낮았는지, 그리고 어떤 답변 조합에서 만족도가 낮은지를 보려면 결과 자체보다 `답변 스냅샷 + 만족도 점수`가 더 중요할 수 있다. 그래서 결과 저장은 생략하고, 개선용 피드백만 최소 구조로 남겼다.
- 예외 상황 또는 엣지 케이스: 만족도 점수는 1~5 범위만 허용한다. 결과 페이지에서 아무 점수도 선택하지 않으면 JS가 제출을 막고, 서버도 `@Min/@Max` 검증으로 한 번 더 막는다. 지금은 중복 제출 방지를 프론트 단의 버튼 잠금으로만 처리하고 있으며, 사용자 식별이나 결과 dedupe는 일부러 넣지 않았다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test` 전체 통과. 피드백 통합 테스트는 `POST /api/recommendation/feedback`가 실제 레코드를 저장하는지와 6점 같은 잘못된 점수를 400으로 거절하는지를 검증했다.
- 면접에서 30초 안에 설명하는 요약: 추천 결과는 저장하지 않기로 했고, 대신 설문을 개선할 수 있는 최소 신호만 남겼습니다. 결과 페이지에서 1~5점 만족도와 설문/엔진 버전, 사용자가 고른 6개 답변만 익명으로 저장하고, 이 데이터를 기준으로 어떤 설문 버전이 더 만족도가 높은지 나중에 비교할 수 있게 했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 피드백을 저장만 하고 집계 조회는 아직 없다. 다음 단계에서 버전별 평균 점수, 응답 수, 특정 답변 조합별 만족도 같은 집계 기준을 어디까지 보여줄지 더 정리해야 한다.

## 2026-03-24 - 추천 만족도 버전 집계 조회 1차

- 단계: 6. 설문 기반 추천 엔진
- 목적: 만족도 피드백을 저장하기만 하면 설문 개선에 바로 쓰기 어렵다. 이번 조각에서는 `surveyVersion + engineVersion` 기준으로 평균 점수, 응답 수, 1~5점 분포를 읽는 최소 집계 화면과 API를 붙인다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackVersionSummaryProjection.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackInsightsView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSummaryView.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
  - `src/main/resources/templates/recommendation/feedback-insights.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/12-collect-recommendation-feedback.md`
  - `blog/13-recommendation-feedback-insights.md`
- 요청 흐름 / 데이터 흐름: 사용자는 만족도 제출 뒤 내부적으로 `GET /api/recommendation/feedback/summary` 또는 `GET /recommendation/feedback-insights`를 통해 버전별 집계를 본다. 컨트롤러는 진입만 처리하고, 실제 집계는 `RecommendationFeedbackRepository.summarizeByVersion() -> RecommendationFeedbackService.summarizeByVersion()` 순서로 수행된다.
- 데이터 / 상태 변화: 이번 단계에서는 새 데이터를 더 저장하지 않는다. 이미 저장하던 `RecommendationFeedback`만 읽고, `surveyVersion + engineVersion` 그룹 기준으로 `responseCount`, `averageSatisfaction`, `score1~5Count`, `lastSubmittedAt`를 계산해 view로 바꾼다.
- 핵심 도메인 개념: 추천 결과를 저장하지 않더라도 설문 품질을 개선할 수 있는 핵심 단위는 `버전 조합`이다. 같은 `surveyVersion`과 `engineVersion`이 얼마나 만족도를 받았는지, 응답 수가 충분한지, 낮은 점수 분포가 몰려 있는지를 보면 “어느 버전을 유지/폐기할지” 판단할 수 있다.
- 예외 상황 또는 엣지 케이스: 응답 수가 1~2개뿐인 버전은 평균 점수만으로 판단하면 위험하다. 그래서 표에는 평균 점수뿐 아니라 응답 수와 1~5점 분포를 같이 보여준다. 아직은 답변 조합별 drill-down은 넣지 않고, 버전 집계까지만 제한한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest` 통과. 집계 통합 테스트는 `GET /api/recommendation/feedback/summary`가 버전별 응답 수와 평균 점수를 계산하는지, `GET /recommendation/feedback-insights`가 SSR 페이지를 렌더링하는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 결과는 저장하지 않지만, 설문과 엔진 버전별 만족도 집계는 볼 수 있게 만들었습니다. `RecommendationFeedback`를 `surveyVersion + engineVersion` 기준으로 그룹핑해서 평균 점수, 응답 수, 1~5점 분포를 계산하고, 이 집계를 API와 SSR 화면으로 노출해 설문 개선 기준으로 삼았습니다.
- 아직 내가 이해가 부족한 부분: 지금은 버전 조합 단위 집계까지만 있다. 실제 설문 개선을 더 정교하게 하려면 “어떤 답변 조합에서 낮은 점수가 많이 나왔는지”까지 내려다볼지, 아니면 버전 평균만으로도 충분한지 더 판단해야 한다.

## 2026-03-24 - 추천 가중치 튜닝과 경계값 조정 1차

- 단계: 6. 설문 기반 추천 엔진
- 목적: 후보 국가 풀을 넓힌 뒤에도 영어 친화도나 최우선 기준 점수가 강하게 작용하면, 저물가·음식 중심 취향에서도 싱가포르 같은 일부 국가가 계속 상단에 남을 수 있었다. 이번 단계에서는 점수식의 경계값과 보조 정렬 기준을 조정해 추천 결과가 설문 의도를 더 직접 반영하도록 튜닝한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCandidateView.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/10-expand-recommendation-candidate-pool.md`
  - `blog/11-recommendation-weight-tuning.md`
- 요청 흐름 / 데이터 흐름: 추천 요청 흐름은 그대로 `RecommendationSurveyForm -> RecommendationSurveyAnswers -> RecommendationSurveyService.recommend()`이다. 바뀐 것은 서비스 내부 점수 계산과 정렬 규칙이다. 서비스는 각 후보에 대해 기후/속도/물가/도시성/영어/우선순위 점수를 계산한 뒤, 이제는 `정확 일치 보너스`, `초과 물가 패널티`, `핵심 생활 조건 coherence bonus`까지 합산하고, 동점 시에는 `강한 신호 개수 -> 정확 일치 개수 -> 국가명` 순으로 정렬한다.
- 데이터 / 상태 변화: DB와 설문 입력 구조는 바뀌지 않았다. 변한 것은 추천 계산식과 view 모델에 실어 두는 보조 비교 정보다. `RecommendationCandidateView`에는 이제 `strongSignalCount`, `exactMatchCount`도 함께 담긴다.
- 핵심 도메인 개념: 추천 품질은 후보 데이터뿐 아니라 경계값 설계에도 크게 좌우된다. 특히 물가처럼 사용자 제약에 가까운 항목은 단순한 거리 점수만으로는 부족하므로, “허용 범위를 초과했는가” 같은 규칙을 별도 패널티로 드러내는 편이 더 설명 가능하고 납득 가능한 결과를 만든다.
- 예외 상황 또는 엣지 케이스: 점수식이 복잡해질수록 왜 한 나라가 위로 갔는지 설명하기 어려워질 수 있다. 그래서 이번 단계에서는 너무 많은 축을 새로 만들지 않고, 기존 축 위에 exact match / over-budget penalty / coherence bonus만 얹는 선에서 멈췄다. 이렇게 해야 테스트와 면접 설명이 함께 유지된다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test` 전체 통과. 서비스 테스트는 `저물가 + 음식 중심` 시나리오에서 `말레이시아`가 `싱가포르`보다 앞서도록 기대값을 고정했다.
- 면접에서 30초 안에 설명하는 요약: 추천 후보 풀을 넓힌 뒤에는 점수식도 한 번 다듬었습니다. 이번에는 정확 일치 보너스, 물가 초과 패널티, 생활 조건 coherence bonus를 추가하고, 동점 비교 기준도 `강한 신호 개수`와 `정확 일치 개수`까지 보게 해서 설문 의도가 결과에 더 직접 반영되도록 조정했습니다.
- 아직 내가 이해가 부족한 부분: 지금 경계값은 서비스 내부 상수로 관리되는데, 이후 추천 품질을 더 높이려면 실제 사용자 피드백이나 저장된 추천 이력을 기준으로 어떤 값을 재조정할지 실험 체계가 더 필요하다.
