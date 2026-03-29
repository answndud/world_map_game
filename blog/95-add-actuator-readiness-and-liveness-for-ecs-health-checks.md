# ECS와 ALB가 볼 actuator health probe를 실제로 열기

배포 준비에서 Dockerfile, prod profile, graceful shutdown까지 정리해도 아직 비어 있는 질문이 하나 남는다.

> ECS와 ALB는 이 앱이 살아 있는지, 준비됐는지를 어떤 URL로 판단할까?

이번 조각은 그 질문에 답하기 위한 `Actuator health probe` 추가 작업이다.

## 왜 지금 필요한가

현재 프로젝트는 첫 배포를 `ECS task 1개`로 시작할 계획이다.
그렇다면 다음 배포 준비의 핵심은 scale-out이 아니라 `안전하게 health check를 통과하는가`다.

즉 지금 필요한 것은

- 새 컨트롤러 작성
- 임시 `/ping` API 추가

가 아니라, Spring Boot가 이미 제공하는 운영 endpoint를 실제 prod 기준으로 열어 두는 일이다.

## 변경 파일

- [build.gradle](/Users/alex/project/worldmap/build.gradle)
- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)
- [ProdProfileConfigTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)
- [ActuatorHealthEndpointIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ActuatorHealthEndpointIntegrationTest.java)

## 1. actuator 의존성 추가

먼저 의존성부터 붙였다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

이 조각의 핵심은 health endpoint이므로, 별도 라이브러리를 끌어다 쓰기보다 Spring Boot 기본 actuator를 사용하는 편이 맞다.

이유는 단순하다.

- ECS/ALB health check 설명이 쉬워진다.
- Spring Boot 표준 방식이라 면접에서도 납득이 쉽다.
- 커스텀 `/ping` API보다 책임이 분명하다.

## 2. prod profile에 probe group 고정

이번에는 prod profile에서 아래 기준을 고정했다.

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState,ping
        readiness:
          include: readinessState,db,ping
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

핵심은 `liveness`와 `readiness`를 나눠 본다는 점이다.

### liveness

`liveness`는 프로세스 자체가 살아 있는지를 보는 용도다.

- `livenessState`
- `ping`

즉 프로세스 생존 기준만 본다.

### readiness

`readiness`는 이 인스턴스가 지금 요청을 받을 준비가 됐는지를 보는 용도다.

- `readinessState`
- `db`
- `ping`

이번 단계에서는 `db`까지만 readiness에 넣었다.

왜냐하면 아직 `Spring Session + Redis`를 넣지 않았고, 첫 공개 배포는 `task 1개`로 갈 계획이기 때문이다.
즉 readiness는 먼저 “앱과 DB가 준비됐는가”를 기준으로 닫고, Redis/session externalization 단계에서 다시 넓히는 편이 더 현실적이다.

## 3. 어떤 URL이 생기나

이제 prod 기준으로 아래 URL을 사용할 수 있다.

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

이 중에서 실제 ECS/ALB health check는 보통 아래처럼 붙이면 된다.

- ECS task health check: `/actuator/health/liveness`
- ALB target group health check: `/actuator/health/readiness`

즉 “프로세스는 살아 있지만 아직 DB 준비가 안 된 상태”와
“요청을 받아도 되는 상태”를 분리해서 설명할 수 있게 된다.

## 4. 왜 컨트롤러가 아니라 actuator와 config인가

이건 게임 로직이 아니라 운영 관찰 규칙이다.

그래서 별도 `HealthController`를 만드는 것보다,

- actuator가 endpoint를 제공하고
- `application-prod.yml`이 probe group을 정의하고
- ECS/ALB가 그 URL을 읽는다

라는 구조가 훨씬 낫다.

이렇게 해야 면접에서도

> 요청을 처리하는 API와, 운영 환경이 앱을 감시하는 endpoint를 분리했습니다.

라고 자연스럽게 설명할 수 있다.

## 5. 테스트

이번 조각은 아래로 닫았다.

- `./gradlew test --tests com.worldmap.common.config.ProdProfileConfigTest --tests com.worldmap.common.config.ActuatorHealthEndpointIntegrationTest`
- `./gradlew test`
- `git diff --check`

특히 [ActuatorHealthEndpointIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ActuatorHealthEndpointIntegrationTest.java)는 `prod,test` 조합으로 컨텍스트를 띄운 뒤 아래 endpoint가 실제로 열리는지 확인한다.

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

## 6. 아직 남은 것

이제 배포 준비에서 남은 핵심은 크게 두 가지다.

1. `Secrets Manager` 또는 `SSM Parameter Store`
2. `Spring Session + Redis`

즉 “앱이 healthy인지 판단하는 기준”은 생겼고,
다음에는 “비밀 값을 어떻게 안전하게 넣을지”와
“task를 2개로 늘려도 세션이 안 끊기게 할지”를 해결해야 한다.

## 면접에서 30초 설명

> 이번에는 ECS와 ALB가 볼 actuator health probe를 실제로 열었습니다. `spring-boot-starter-actuator`를 추가하고, prod profile에서 `liveness`는 `livenessState + ping`, `readiness`는 `readinessState + db + ping`으로 그룹을 나눠 `/actuator/health/liveness`, `/actuator/health/readiness`를 노출했습니다. 핵심은 게임 API와 별도로, 운영 환경이 앱을 판단할 수 있는 endpoint를 표준 방식으로 분리한 것입니다.
