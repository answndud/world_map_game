# [Spring Boot 포트폴리오] 02. Spring Boot 3, Gradle, Thymeleaf로 WorldMap 프로젝트 뼈대 만들기

## 1. 이번 글에서 풀 문제

프로젝트를 시작할 때 가장 흔한 실수는 두 가지다.

1. IntelliJ나 Initializr가 만들어준 기본 구조를 그대로 두고 왜 그런 구조인지 설명하지 못하는 것
2. 반대로 너무 많은 기술을 한 번에 넣어서, 어떤 설정이 왜 필요한지 스스로도 놓치는 것

WorldMap 프로젝트의 첫 번째 구현 단계에서는 이 문제를 먼저 해결해야 했다.

이번 글의 목표는 아래 세 가지다.

1. Spring Boot 3 기반의 최소 실행 뼈대를 만든 이유를 설명한다.
2. 왜 `SSR + API 혼합 구조`로 시작하는지 정리한다.
3. 이후 국가 데이터, 게임 세션, 랭킹 기능이 들어갈 자리를 초기에 어떻게 고정했는지 보여 준다.

## 2. 먼저 알아둘 개념

### 2-1. Spring Boot 뼈대

프로젝트 뼈대는 단순히 "앱이 실행되는 상태"만 의미하지 않는다.

이 단계에서 같이 고정해야 하는 것이 있다.

- 어떤 버전을 기준으로 시작하는가
- 어떤 의존성을 최소 단위로 가져가는가
- 어떤 패키지 구조로 기능을 쌓을 것인가
- 개발 환경은 어떤 방식으로 붙일 것인가

즉, 뼈대 단계는 나중에 기능을 빨리 붙이기 위한 준비 작업이다.

### 2-2. SSR + API 혼합 구조

이번 프로젝트는 React SPA로 시작하지 않았다.

이유는 지금 단계에서 가장 중요한 것이 프론트 복잡도가 아니라 `서버 주도 상태 관리`를 설명하는 것이기 때문이다.

그래서 화면 렌더링은 Thymeleaf SSR로 두고, 게임 진행 중 필요한 상호작용만 점진적으로 API로 분리하는 방향을 택했다.

### 2-3. 프로파일 분리

프로파일은 실행 환경에 따라 설정을 분리하는 방법이다.

이번 단계에서는 최소한 아래 세 가지를 나눠 두는 것이 좋다.

- `application.yml`
  - 공통 설정
- `application-local.yml`
  - 로컬 개발 실행
- `application-test.yml`
  - 테스트 실행

이걸 초기에 나누지 않으면 나중에 DB, Redis, LLM 설정이 섞이기 쉽다.

## 3. 이번 글에서 다룰 파일

```text
- build.gradle
- compose.yaml
- src/main/java/com/worldmap/WorldMapApplication.java
- src/main/java/com/worldmap/web/HomeController.java
- src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java
- src/main/resources/application.yml
- src/main/resources/application-local.yml
- src/main/resources/application-test.yml
- src/main/resources/templates/home.html
- src/main/resources/static/css/site.css
- src/test/java/com/worldmap/WorldMapApplicationTests.java
- src/test/java/com/worldmap/web/HomeControllerTest.java
```

## 4. 설계 구상

이번 단계에서 가장 중요한 판단은 `Spring Boot 3 기반으로 시작한다`는 점이었다.

왜냐하면 사용자가 직접 Spring Boot 3 기반을 원했고, 취업용 포트폴리오에서도 현재 실무 친화적인 기준으로 설명하기 좋기 때문이다.

### 왜 Spring Boot 3.5.12로 시작했는가

2026-03-22 기준 `start.spring.io` 메타데이터에는 Spring Boot `3.5.12.RELEASE`가 안정 버전으로 제공되고 있었다.
이번 프로젝트는 이 버전을 기준으로 build 파일을 정리했다.

현재는 로컬 실행 환경 정렬을 위해 `Java 25` toolchain으로 올려 두었다.

### 왜 의존성을 이 정도만 넣었는가

이번 단계에서는 아래 의존성만 넣었다.

- Spring Web
- Thymeleaf
- Validation
- Spring Data JPA
- Spring Data Redis
- PostgreSQL Driver
- Docker Compose Support

이 선택에는 각각 이유가 있다.

- Web
  - SSR 화면과 이후 API를 모두 같은 MVC 기반에서 처리할 수 있다.
- Thymeleaf
  - 홈 화면과 에러 페이지를 빠르게 SSR로 구성할 수 있다.
- Validation
  - 나중에 요청 DTO 검증 흐름을 바로 붙일 수 있다.
- JPA
  - `country`, `game_session`, `game_round` 같은 엔티티 모델링을 대비한다.
- Redis
  - 랭킹 시스템을 위한 기반을 미리 확보한다.
- PostgreSQL
  - 로컬 개발과 이후 운영형 설명에 잘 맞는다.
- Docker Compose Support
  - 로컬 실행 시 DB와 Redis를 일관되게 띄우기 쉽다.

### 왜 패키지 구조를 지금 고정했는가

아직 기능이 거의 없는데도 패키지 구조를 먼저 나눈 이유는, 기능이 늘어날수록 "어디에 무엇이 들어가야 하는가"가 더 중요해지기 때문이다.

이번 단계에서 미리 만든 패키지는 아래다.

- `common`
- `auth`
- `country`
- `game`
- `ranking`
- `recommendation`
- `web`

이 구조는 나중에 기능별로 설명하기가 좋다.

## 5. 코드 설명

### 5-1. `build.gradle`

이 파일은 프로젝트의 기술 선택을 가장 직접적으로 보여 준다.

핵심 포인트는 세 가지다.

1. Spring Boot 버전을 `3.5.12`로 고정했다.
2. Java toolchain을 25로 맞췄다.
3. 테스트는 개별 스타터가 아니라 `spring-boot-starter-test`로 단순화했다.

즉, 지금 단계에서는 "복잡한 빌드"보다 `설명 가능한 기본값`이 더 중요했다.

### 5-2. `WorldMapApplication`

이 클래스는 Spring Boot의 시작점이다.

```java
@SpringBootApplication
public class WorldMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorldMapApplication.class, args);
    }
}
```

여기서 중요한 것은 클래스가 `com.worldmap` 루트에 있다는 점이다.

그래야 이후 아래 패키지가 같이 스캔된다.

- `com.worldmap.web`
- `com.worldmap.common`
- `com.worldmap.game`

### 5-3. `HomeController`

이 컨트롤러는 루트 경로 `/` 요청을 받아 메인 페이지를 렌더링한다.

핵심 메서드:

- `home(Model model)`
  - 입력: 없음
  - 출력: 뷰 이름 `home`
  - 내부 흐름:
    - 모드 카드 목록 생성
    - 핵심 원칙 목록 생성
    - 현재 로드맵 목록 생성
    - 모델에 담아 Thymeleaf 템플릿으로 전달

이 컨트롤러는 아직 게임 로직은 없지만, "이 서비스가 무엇을 만들고 있는가"를 홈 화면에서 설명하는 역할을 한다.

### 5-4. `application.yml` / `application-local.yml` / `application-test.yml`

설정을 세 파일로 나눈 이유는 환경 책임을 섞지 않기 위해서다.

#### `application.yml`

- 애플리케이션 이름
- 기본 프로파일
- Thymeleaf 공통 경로
- Redis repository 비활성화
- `open-in-view` 비활성화
- 에러 페이지 설정

#### `application-local.yml`

- 로컬에서 Thymeleaf 캐시 비활성화
- Docker Compose 연동
- JPA 기본 로컬 설정

#### `application-test.yml`

- 테스트에서 Docker Compose 비활성화
- H2 메모리 DB 사용
- 테스트용 JPA 설정

여기서 중요한 점은 테스트가 로컬 DB/Redis 실행 여부에 흔들리지 않도록 분리했다는 것이다.

### 5-5. `GlobalApiExceptionHandler`

이번 단계에서 API 예외 응답 구조도 같이 잡았다.

핵심 책임은 아래와 같다.

- 검증 예외를 일관된 JSON 구조로 반환
- 잘못된 인자 예외를 400으로 반환
- 찾을 수 없는 리소스를 404로 반환

이걸 초기에 만들어 두면 나중에 게임 API를 붙일 때 컨트롤러마다 예외 처리를 반복하지 않아도 된다.

### 5-6. `home.html` / `site.css`

홈 화면은 단순한 "Hello, world"로 두지 않았다.

이번 프로젝트가 아래 질문에 바로 답하도록 구성했다.

- 어떤 모드가 있는가?
- 핵심 원칙은 무엇인가?
- 앞으로 어떤 순서로 구현할 것인가?

즉, 홈 화면 자체가 포트폴리오 설명의 첫 페이지 역할을 한다.

## 6. 실제 흐름

현재 단계의 요청 흐름은 아주 단순하다.

1. 사용자가 `/`로 접속한다.
2. `HomeController.home()`가 호출된다.
3. 컨트롤러가 모드 카드, 원칙, 로드맵 데이터를 만든다.
4. `home.html`이 렌더링된다.
5. 정적 스타일 파일 `site.css`가 적용된다.

아직 데이터베이스 상태 변화는 없다.
하지만 패키지 구조와 설정 구조는 이후 상태 변화를 담을 자리를 이미 확보한 상태다.

## 7. 테스트로 검증하기

이번 단계에서는 두 가지 테스트를 넣었다.

### 7-1. `WorldMapApplicationTests`

- 목적:
  - 스프링 컨텍스트가 기본적으로 올라오는지 확인
- 중요성:
  - 뼈대 단계에서 설정 충돌이 없는지 가장 먼저 확인할 수 있다.

### 7-2. `HomeControllerTest`

- 목적:
  - `/` 요청이 정상 응답을 반환하는지 확인
  - 뷰 이름이 `home`인지 확인
  - 모델 속성이 실제로 채워졌는지 확인
- 중요성:
  - SSR 홈 화면이 단순 정적 파일이 아니라 컨트롤러를 통해 렌더링된다는 점을 검증한다.

실제로 이번 단계에서는 `./gradlew test`로 테스트를 통과시켰다.

## 8. 회고

이번 단계에서 중요한 것은 기능 구현이 아니라 `설명 가능한 기반`을 만드는 것이었다.

특히 아래가 의미 있었다.

- Spring Boot 3 기반으로 방향을 명확히 고정했다.
- 로컬/테스트 프로파일을 초기에 나눴다.
- 홈 화면이 프로젝트 설명 화면 역할을 하게 만들었다.
- 패키지 구조와 예외 처리 구조를 미리 정리했다.

다음 단계에서는 실제 게임과 추천 기능이 공통으로 사용할 `country` 데이터와 시드 적재 구조를 만들게 된다.

## 9. 취업 포인트

이 글에서 면접으로 이어질 수 있는 질문은 아래와 같다.

- 왜 React가 아니라 Thymeleaf SSR로 시작했나요?
- 왜 Spring Boot 3 기반을 택했나요?
- 왜 JPA와 Redis 의존성을 처음부터 넣었나요?
- 왜 프로파일을 초기에 나눴나요?
- 왜 예외 처리 구조를 이렇게 먼저 만들었나요?

짧게 답하면 이렇게 정리할 수 있다.

"이 프로젝트는 프론트보다 서버 주도 게임 로직을 설명하는 것이 더 중요해서 Thymeleaf SSR로 시작했고, 이후 엔티티 모델링과 랭킹 확장을 고려해 JPA와 Redis 기반을 먼저 넣었습니다. 또 로컬 실행과 테스트 실행이 서로 흔들리지 않도록 프로파일을 초기에 분리했고, API 예외 응답 구조도 먼저 잡아 이후 게임 API를 일관되게 확장할 수 있게 했습니다."
