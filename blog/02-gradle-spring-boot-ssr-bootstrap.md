# [Spring Boot 게임 플랫폼 포트폴리오] 02. Gradle, Spring Boot, Thymeleaf SSR로 WorldMap의 baseline을 어떻게 세웠는가

## 1. 이번 글에서 풀 문제

프로젝트를 다시 만든다고 할 때 가장 먼저 필요한 것은 "멋진 기능"이 아니라 **안정적으로 부팅되고, 첫 화면이 나오고, 이후 기능이 같은 패턴 위에 얹히는 baseline**입니다.

WorldMap에서는 이 baseline이 아래 질문을 닫아 줘야 했습니다.

- 왜 처음부터 SPA가 아니라 `Spring Boot + Thymeleaf SSR + JSON API` 혼합 구조를 택했는가
- 왜 `build.gradle`에 게임, 추천, 랭킹, 인증으로 확장될 의존성을 초기에 같이 심어 두는가
- 왜 홈 화면과 공통 API 에러 응답을 baseline 단계에서 같이 잡는가
- 왜 verification task까지 build에 같이 두는가

이 글은 현재 저장소의 bootstrap 계층을 source of truth로 삼아, 나중 단계 글들이 기대는 뼈대를 먼저 설명합니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 저장소는 아래 상태여야 합니다.

- `settings.gradle`이 저장소 이름을 `worldmap`으로 고정한다
- `build.gradle`이 Java 25, Spring Boot 3.5.12, Thymeleaf SSR, JPA, Redis, Validation, Playwright smoke test까지 담는다
- `WorldMapApplication`이 `@SpringBootApplication` + `@ConfigurationPropertiesScan`으로 애플리케이션 시작점을 제공한다
- `GET /`가 [HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)를 통해 [home.html](../src/main/resources/templates/home.html)을 SSR로 렌더링한다
- 이후 게임/추천/랭킹으로 확장될 공통 에러 포맷이 [ApiErrorResponse.java](../src/main/java/com/worldmap/common/response/ApiErrorResponse.java)와 [GlobalApiExceptionHandler.java](../src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java)에 고정된다
- 최소 검증으로 [HomeControllerTest.java](../src/test/java/com/worldmap/web/HomeControllerTest.java)가 홈 셸이 깨지지 않았음을 잡아 준다

즉, baseline 단계의 목표는 "기능을 많이 넣는 것"이 아니라 **SSR shell, API contract, build entry, verification lane을 한 번에 시작하는 것**입니다.

## 3. 먼저 알아둘 개념

### 3-1. server-driven game platform

WorldMap은 브라우저에서 게임을 그리더라도, 게임의 진짜 상태와 정답 판정은 서버가 들고 갑니다.  
그래서 baseline도 처음부터 "정적 사이트"가 아니라 Spring MVC 애플리케이션이어야 합니다.

### 3-2. SSR shell

WorldMap의 첫 화면은 React hydration이 아니라 서버 템플릿 렌더링입니다.  
첫 페이지를 서버가 그려 주면 아래가 쉬워집니다.

- 로그인/게스트 상태에 따라 링크를 다르게 보여 주기
- 검색 엔진이나 심사자가 JS 없이도 구조를 바로 읽기
- 이후 각 게임의 `start/play/result` 페이지를 같은 방식으로 확장하기

### 3-3. JSON API contract

페이지는 SSR이지만, 상태 전이는 대부분 JSON API가 처리합니다.  
그래서 baseline 단계부터 `성공 응답` 못지않게 `에러 응답`의 공통 형식이 필요합니다.

### 3-4. verification task

WorldMap은 후반에 브라우저 smoke test와 public URL smoke까지 도입했습니다.  
현재 저장소를 다시 구현하려면 `build.gradle`이 단순 컴파일 스크립트가 아니라 **verification entrypoint**라는 점도 같이 이해해야 합니다.

## 4. 이번 글에서 다룰 파일

- [settings.gradle](../settings.gradle)
- [build.gradle](../build.gradle)
- [WorldMapApplication.java](../src/main/java/com/worldmap/WorldMapApplication.java)
- [HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)
- [ModeCardView.java](../src/main/java/com/worldmap/web/view/ModeCardView.java)
- [home.html](../src/main/resources/templates/home.html)
- [GlobalApiExceptionHandler.java](../src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java)
- [ApiErrorResponse.java](../src/main/java/com/worldmap/common/response/ApiErrorResponse.java)
- [HomeControllerTest.java](../src/test/java/com/worldmap/web/HomeControllerTest.java)

## 5. 핵심 도메인 모델 / 상태

baseline 단계에서는 아직 게임 도메인 엔티티가 없습니다.  
대신 나중 단계 전체를 받치는 네 가지 "기초 상태"가 있습니다.

### 5-1. build state

어떤 플러그인과 의존성을 쓰는지가 곧 기술 방향을 결정합니다.

- Spring MVC
- Thymeleaf SSR
- Validation
- JPA
- Redis
- Playwright 기반 browser smoke

즉, baseline 단계의 진짜 모델 하나는 `build.gradle`입니다.

### 5-2. application entry state

`main()`이 어디 있고, `@ConfigurationProperties` 클래스가 어떤 방식으로 읽히는지가 전체 애플리케이션 부팅 경계를 만듭니다.

### 5-3. public home shell state

홈은 단순 정적 페이지가 아니라, 서버가 아래 모델을 넣어 렌더링하는 셸입니다.

- `modeCards`
- `entrySteps`
- `accountNotes`

이 셋이 이후 public IA의 시작점입니다.

### 5-4. API error envelope

API 실패 시 모든 컨트롤러가 아래 공통 JSON 포맷으로 응답합니다.

- `timestamp`
- `status`
- `error`
- `message`
- `path`

이 envelope 덕분에 후반의 게임 API, 추천 API, 인증 API, 랭킹 API가 같은 규약으로 묶입니다.

## 6. 설계 구상

### 왜 Spring Boot MVC부터 시작했는가

WorldMap은 "브라우저에서 그려지는 게임"이지만, 설계 목표는 프런트 showcase가 아니라 **서버 주도 플랫폼**입니다.

처음부터 SPA로 가면 아래 책임이 흐려질 수 있습니다.

- 세션 상태 보관
- 정답 판정
- 권한/ownership 검사
- terminal result 공개 규칙

반대로 Spring Boot MVC로 시작하면, 첫 단계부터 아래 책임을 분리할 수 있습니다.

- 서버: 페이지 셸, 세션, 상태 전이, API
- 프런트: 입력 수집, 인터랙션, 표현

### 왜 Thymeleaf SSR을 baseline에 같이 넣었는가

홈 화면과 각 게임의 `start/play/result`는 모두 "첫 진입은 서버가 그려 주고, 중간 상태는 JS가 API를 치는 구조"입니다.

이 구조는 다음 장점이 있습니다.

- 첫 진입 화면이 빠르게 보인다
- 로그아웃/관리자/랭킹 링크처럼 현재 서버 상태를 SSR로 바로 반영할 수 있다
- 각 페이지를 하나의 서버 제품처럼 설명하기 쉽다

### 왜 JPA, Redis 의존성을 일찍 넣었는가

초기 단계에서 쓰지 않는다고 해서 나중에 갑자기 의존성을 넣으면, baseline 설명이 "중간에 기술이 바뀐 프로젝트"처럼 보입니다.

WorldMap은 처음부터 아래 방향을 고정해 둡니다.

- JPA는 게임/인증/추천의 durable state 저장소
- Redis는 session, ranking, 운영 성능 축

즉, 아직 기능이 없더라도 **프로젝트의 저장 전략 자체는 baseline에서 미리 선언**합니다.

### 왜 공통 예외 응답을 early stage에 두는가

실제 프로젝트가 커지면 에러 포맷을 뒤늦게 통일하기 어렵습니다.

WorldMap은 baseline에서 먼저 아래를 정합니다.

- validation error는 `400`
- 잘못된 입력도 `400`
- stale submit 같은 잘못된 상태는 `409`
- 리소스 없음은 `404`
- 세션 ownership 위반은 `403`

이 규약을 일찍 박아 두면 후반부 기능 추가가 훨씬 설명 가능해집니다.

## 7. 코드 설명

### 7-1. `settings.gradle`: 저장소 식별자 고정

[settings.gradle](../settings.gradle)은 한 줄뿐이지만 중요합니다.

```gradle
rootProject.name = 'worldmap'
```

이 이름은 단순 표시가 아니라 아래에 계속 연결됩니다.

- Gradle project identity
- IDE import
- CI log와 artifact naming
- 문서와 런북의 제품 식별

재구현 시에도 이름을 먼저 고정해야 이후 문서/스크립트 설명이 흔들리지 않습니다.

### 7-2. `build.gradle`: 기술 방향과 verification lane을 코드로 고정

현재 [build.gradle](../build.gradle)은 다음 세 층으로 읽는 것이 좋습니다.

#### 플러그인

- `java`
- `org.springframework.boot`
- `io.spring.dependency-management`

이 조합으로 Spring Boot dependency BOM과 Java build pipeline을 같이 잡습니다.

#### runtime dependencies

- `spring-boot-starter-web`
- `spring-boot-starter-thymeleaf`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-data-redis`
- `spring-session-data-redis`
- `spring-security-crypto`
- `spring-boot-starter-actuator`
- `postgresql`

즉, 현재 저장소 기준 baseline은 단순 "웹 서버"가 아니라 **SSR + API + persistence + session + observability**까지 포함한 application skeleton입니다.

#### verification tasks

`test` 외에 아래 task가 같이 있습니다.

- `browserSmokeTest`
- `publicUrlSmokeTest`

이건 후반 production-ready 단계에서 추가된 것이지만, 현재 코드를 다시 만들려면 build에 이미 포함된 전제입니다.  
블로그에서 이 사실을 숨기면 독자는 현재 저장소의 `build.gradle`을 다시 쓸 수 없습니다.

### 7-3. Java 25 toolchain

현재 빌드는 Java 25를 요구합니다.

```gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

재구현할 때 이 선언을 빠뜨리면 아래 문제가 생깁니다.

- 로컬 JDK 버전에 따라 빌드가 흔들림
- CI와 로컬 결과 불일치
- later step에서 Java 25 기준 Docker/ECS 설명과 어긋남

### 7-4. `WorldMapApplication`: boot entry와 configuration properties scan

[WorldMapApplication.java](../src/main/java/com/worldmap/WorldMapApplication.java)는 단순해 보이지만, 현재 코드에서는 두 가지를 선언합니다.

- `@SpringBootApplication`
- `@ConfigurationPropertiesScan`

두 번째가 중요한 이유는, 후반 단계에서 아래 같은 설정 객체를 여러 개 쓰기 때문입니다.

- `CountrySeedProperties`
- 추천 엔진 설정
- 운영 bootstrap 관련 properties

즉, 이 클래스는 단순 `main()` 이상으로 **프로젝트 전역 설정 읽기 방식**까지 결정합니다.

### 7-5. `HomeController`: 첫 SSR 셸을 만드는 최소 컨트롤러

[HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)는 복잡한 서비스 호출 없이도 현재 public 제품 방향을 드러냅니다.

핵심은 모델을 문자열 하드코딩으로 흩뿌리지 않고 아래 구조로 정리한다는 점입니다.

- `modeCards()`
- `entrySteps()`
- `accountNotes()`

그리고 카드 하나는 [ModeCardView.java](../src/main/java/com/worldmap/web/view/ModeCardView.java) record로 표현합니다.

```java
public record ModeCardView(
    String group,
    String title,
    String subtitle,
    String description,
    String status,
    String href
) {}
```

이 record가 중요한 이유는, 홈이 단순 홍보 페이지가 아니라 **현재 제품 IA를 설명하는 서버 모델**이기 때문입니다.

### 7-6. `home.html`: SSR shell이 왜 중요한지 보여 주는 파일

[home.html](../src/main/resources/templates/home.html)은 현재도 public 진입점입니다.

여기서 봐야 할 포인트는 세 가지입니다.

- 헤더는 fragment를 통해 전역 내비게이션을 재사용한다
- 홈 카드는 `modeCards`를 SSR로 렌더링한다
- 로그인 여부에 따라 `hero-account-callout`이 서버에서 갈린다

즉, 이 페이지는 baseline 단계에서 이미 "서버가 현재 사용자 상태와 제품 라인업을 조립한다"는 철학을 보여 줍니다.

### 7-7. `GlobalApiExceptionHandler`와 `ApiErrorResponse`

[ApiErrorResponse.java](../src/main/java/com/worldmap/common/response/ApiErrorResponse.java)는 단순 record지만, 후반 전체 API 규약의 기준점입니다.

```java
public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {}
```

[GlobalApiExceptionHandler.java](../src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java)는 현재 아래 예외를 HTTP status와 매핑합니다.

- `MethodArgumentNotValidException` -> `400`
- `IllegalArgumentException` -> `400`
- `IllegalStateException` -> `409`
- `NoResourceFoundException` -> `404`
- `ResourceNotFoundException` -> `404`
- `SessionAccessDeniedException` -> `403`

이 설계 덕분에 후반 글에서 각 API가 매번 자기 식의 에러 JSON을 만들지 않습니다.

## 8. 요청 흐름 / 상태 변화

baseline 단계의 요청 흐름은 단순하지만, 이후 확장의 표준이 됩니다.

### 8-1. 홈 SSR 흐름

```text
GET /
-> HomeController.home(Model)
-> modeCards / entrySteps / accountNotes 모델 생성
-> home.html 렌더링
-> 브라우저가 서버가 조립한 첫 화면을 바로 본다
```

여기서 상태 변화는 DB write가 아니라 **서버 모델 조립**입니다.

### 8-2. API 에러 응답 흐름

```text
HTTP 요청
-> controller / service / domain에서 예외 발생
-> GlobalApiExceptionHandler
-> ApiErrorResponse(timestamp, status, error, message, path)
-> JSON 반환
```

이 흐름이 먼저 고정돼 있어야 이후 게임 API에서도 프런트가 안정적으로 에러 메시지를 표시할 수 있습니다.

### 8-3. build verification 흐름

```text
./gradlew test
-> JUnit platform
-> browser-smoke / public-url-smoke tag는 제외

./gradlew browserSmokeTest
-> browser-smoke tag만 실행

./gradlew publicUrlSmokeTest
-> public-url-smoke tag만 실행
```

즉, build layer 자체도 "기능 테스트와 브라우저 검증을 분리하는 구조"를 이미 갖고 있습니다.

## 9. 실패 케이스 / 예외 처리

- 홈 컨트롤러가 모델 없이 템플릿만 반환하면: later IA 변경이 템플릿 하드코딩으로 흩어진다
- API마다 에러 포맷이 다르면: 프런트가 모듈별로 다른 실패 처리 코드를 가져야 한다
- `IllegalStateException`을 무조건 `500`으로 흘려 보내면: stale submit 같은 충돌 상태를 설명하기 어렵다
- baseline에서 verification task를 빼면: 후반 production-ready 검증이 build 밖의 ad-hoc 스크립트가 된다
- Java toolchain을 고정하지 않으면: 로컬/CI/JDK 버전 차이로 실행 경험이 흔들린다

baseline 단계의 실수는 뒤 단계에서 몇 배로 커지기 때문에, 여기서 규약을 빨리 맞추는 편이 낫습니다.

## 10. 테스트로 검증하기

현재 baseline 레이어에서 직접적으로 중요한 테스트는 [HomeControllerTest.java](../src/test/java/com/worldmap/web/HomeControllerTest.java)입니다.

이 테스트는 단순 `200 OK`만 보지 않습니다.

- 뷰 이름이 `home`인지
- `modeCards`, `entrySteps`, `accountNotes` 모델이 존재하는지
- 현재 public 홈 문구가 기대한 정보 구조를 유지하는지
- 비로그인 상태에서 `Dashboard` 링크가 숨겨지는지
- 관리자 세션이면 `Dashboard`가 노출되는지

즉, 홈을 "예쁘게 렌더링되는가"가 아니라 **public shell contract가 유지되는가**로 검증합니다.

실행 명령은 아래입니다.

```bash
./gradlew test --tests com.worldmap.web.HomeControllerTest
```

그리고 local baseline을 실제로 부팅하려면 아래를 같이 씁니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

즉 이 글에서 자동으로 고정되는 baseline contract는 `HomeControllerTest`가 중심이고,
`bootRun`은 사람이 직접 홈 셸과 부팅 경험을 확인하는 수동 smoke입니다.
현재 [build.gradle](../build.gradle)에 `browserSmokeTest`, `publicUrlSmokeTest`가 이미 존재하더라도,
그것을 baseline 단계의 자동 검증으로 과장해 읽으면 안 됩니다.
두 task는 현재 저장소의 verification entrypoint일 뿐, 이 글이 닫는 baseline 핵심 증명 범위는 아닙니다.

## 11. 회고

baseline 단계는 언제나 과소평가되기 쉽습니다.  
하지만 WorldMap처럼 기능이 많아질 프로젝트에서는, baseline이 곧 나머지 모든 글의 문체와 구조를 결정합니다.

이번 구조의 장점은 아래입니다.

- 홈 SSR이 public shell의 출발점이 된다
- API 에러 응답이 early stage부터 통일된다
- build가 runtime + verification을 함께 품는다
- Java / Spring / properties scan 방향이 빨리 고정된다

### 현재 구현의 한계

- baseline 자체만으로는 DB/Redis profile 차이까지 설명되지 않는다
- actuator, Redis session, browser smoke는 build에 들어 있지만 실제 운영 설명은 뒤 글에서 닫힌다
- 홈 셸은 지금 기준으로 꽤 풍부하지만, baseline 시점의 최소 셸보다 확장된 최종 상태를 담고 있다
- `browserSmokeTest`, `publicUrlSmokeTest`가 build에 존재한다는 사실만으로 baseline 단계의 품질이 모두 자동 보장되는 것은 아니다

즉, 이 글은 "첫 번째 커밋 상태"보다 **현재 저장소에서 baseline 역할을 하는 계층**을 설명한다고 보는 편이 맞습니다.

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 Spring Boot + Thymeleaf SSR + JSON API 혼합 구조로 시작하고, `build.gradle`, 홈 SSR, 공통 API 에러 응답을 baseline 단계에서 먼저 고정해 이후 게임과 추천, 인증을 같은 패턴 위에 올렸습니다.

### 12-2. 30초 답변

이 프로젝트는 처음부터 SPA가 아니라 서버 주도 게임 플랫폼을 목표로 했기 때문에, Spring Boot MVC와 Thymeleaf SSR을 baseline으로 택했습니다. `build.gradle`에는 Web, Thymeleaf, Validation, JPA, Redis, Actuator, Playwright smoke test까지 현재 구조에 필요한 의존성과 verification task를 고정했고, `WorldMapApplication`은 `@ConfigurationPropertiesScan`으로 이후 설정 객체를 읽을 준비를 했습니다. 또 `HomeController`와 `home.html`로 public shell을 먼저 만들고, `GlobalApiExceptionHandler`와 `ApiErrorResponse`로 공통 에러 규약을 정해 이후 기능이 같은 구조를 따르도록 했습니다.

### 12-3. 예상 꼬리 질문

- 왜 React SPA 대신 SSR을 먼저 택했나요?
- 왜 기능도 없는데 JPA와 Redis 의존성을 baseline에 넣었나요?
- 왜 홈 화면과 에러 응답을 같은 단계에서 만들었나요?
- `IllegalStateException -> 409` 같은 매핑은 왜 중요한가요?

## 13. 시작 상태

- 저장소만 있고 실행 가능한 Spring Boot 애플리케이션이 없다
- `/` 같은 public 진입점이 없다
- 이후 기능이 공통으로 따를 에러 JSON 포맷도 없다
- Gradle task는 compile/test 수준에 머물러 있고 verification lane이 없다

## 14. 이번 글에서 바뀌는 파일

- `settings.gradle`
- `build.gradle`
- `src/main/java/com/worldmap/WorldMapApplication.java`
- `src/main/java/com/worldmap/web/HomeController.java`
- `src/main/java/com/worldmap/web/view/ModeCardView.java`
- `src/main/resources/templates/home.html`
- `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
- `src/main/java/com/worldmap/common/response/ApiErrorResponse.java`
- `src/test/java/com/worldmap/web/HomeControllerTest.java`

## 15. 구현 체크리스트

1. `settings.gradle`로 프로젝트 이름을 고정한다
2. `build.gradle`에 Spring Boot / Thymeleaf / Validation / JPA / Redis / Test 의존성을 추가한다
3. `WorldMapApplication`으로 부팅 entry를 만든다
4. `HomeController`와 `home.html`로 `GET /` SSR을 띄운다
5. `ApiErrorResponse` record를 만든다
6. `GlobalApiExceptionHandler`에서 공통 예외 매핑을 만든다
7. `HomeControllerTest`로 홈 셸 계약을 고정한다
8. 이후 단계에서 build verification task를 점진적으로 확장한다

## 16. 실행 / 검증 명령

```bash
./gradlew test --tests com.worldmap.web.HomeControllerTest
./gradlew bootRun --args='--spring.profiles.active=local'
```

현재 build에 추가로 존재하는 verification entry는 아래입니다.

```bash
./gradlew browserSmokeTest
./gradlew publicUrlSmokeTest
```

## 17. 산출물 체크리스트

- `worldmap`이라는 이름의 Spring Boot 프로젝트가 부팅된다
- `GET /`가 SSR 홈 화면을 반환한다
- 홈 화면이 서버 모델(`modeCards`, `entrySteps`, `accountNotes`)을 사용한다
- API 실패 시 공통 JSON 에러 포맷을 반환한다
- 최소 controller test가 홈 셸 계약을 잡아 준다

## 18. 글 종료 체크포인트

- 왜 이 프로젝트는 baseline부터 SSR + API 혼합 구조여야 하는가
- 왜 홈 셸과 공통 에러 응답이 같은 단계에서 필요한가
- 왜 build file을 "의존성 목록"이 아니라 verification lane까지 포함한 계약으로 봐야 하는가
- 이후 글들이 어떤 baseline 가정 위에서 출발하는가

## 19. 자주 막히는 지점

- 홈 화면을 순수 정적 HTML로 먼저 만들고 나중에 서버 모델을 붙이려는 것
- API 에러 포맷을 기능별로 제각각 만들고 뒤늦게 통일하려는 것
- Java toolchain을 고정하지 않아 로컬/CI가 다른 JDK로 돌아가게 두는 것
- `build.gradle`을 단순 의존성 파일로만 보고 verification task를 빌드 밖에 두는 것
- SSR을 "낡은 방식"으로 오해하고, 서버 책임을 초기에 약하게 잡는 것
