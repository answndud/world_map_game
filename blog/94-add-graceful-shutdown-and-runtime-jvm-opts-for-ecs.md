# ECS에서 graceful shutdown과 JVM 옵션 기준 먼저 고정하기

배포 준비에서 `Dockerfile`과 `application-prod.yml`이 생겼다고 바로 ECS에 올릴 수 있는 건 아니다.

여전히 두 가지가 비어 있으면 운영 설명이 어색해진다.

1. ALB 뒤에서 Spring Boot가 어떻게 종료되어야 하는가
2. Fargate 메모리 한도 안에서 JVM 옵션을 어디서 조정할 것인가

이번 조각은 이 두 가지를 코드로 먼저 고정한 작업이다.

## 왜 지금 이걸 했나

현재 프로젝트는 아직 `Spring Session + Redis`가 없어서 ECS task를 1개로 시작해야 한다.
즉 지금 중요한 것은 scale-out보다 `한 개 task가 안정적으로 뜨고, 잘 내려가는가`다.

그래서 이번 기준은 아래 두 줄로 요약할 수 있다.

- Spring Boot는 prod에서 graceful shutdown으로 내려간다.
- Docker runtime은 `JAVA_RUNTIME_OPTS`로 JVM 옵션을 override할 수 있다.

## 변경 파일

- [Dockerfile](/Users/alex/project/worldmap/Dockerfile)
- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)
- [ProdProfileConfigTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)

## 1. prod profile에 graceful shutdown 기준 추가

먼저 prod profile에 종료 기준을 넣었다.

```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 20s

server:
  forward-headers-strategy: native
  shutdown: graceful
```

핵심은 이거다.

- `server.forward-headers-strategy=native`
  - ALB가 붙여 주는 forwarded header를 읽는다.
- `server.shutdown=graceful`
  - SIGTERM을 받았을 때 바로 죽지 않고, 진행 중 요청을 마무리할 시간을 가진다.
- `spring.lifecycle.timeout-per-shutdown-phase=20s`
  - graceful shutdown에 사용할 최대 시간을 정한다.

이 설정은 컨트롤러나 서비스가 아니라 `application-prod.yml`에 있어야 한다.
이유는 요청 로직이 아니라 배포 환경의 종료 정책이기 때문이다.

## 2. Docker runtime JVM 옵션을 env로 외부화

기존 Dockerfile은 JVM 옵션을 이렇게 하드코딩하고 있었다.

```dockerfile
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
```

이 방식은 기본값은 괜찮지만, ECS task definition에서 메모리 정책을 바꾸고 싶을 때 설명이 번거롭다.

그래서 이번에는 아래처럼 바꿨다.

```dockerfile
ENV JAVA_RUNTIME_OPTS="-XX:MaxRAMPercentage=75.0"
STOPSIGNAL SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_RUNTIME_OPTS -jar /app/app.jar"]
```

이 구조의 장점은 분명하다.

- 기본 메모리 기준은 Dockerfile에 남는다.
- ECS에서는 필요하면 `JAVA_RUNTIME_OPTS`만 바꿔 override할 수 있다.
- `STOPSIGNAL SIGTERM`과 Spring Boot graceful shutdown이 같은 방향으로 맞물린다.

즉 `Dockerfile`은 “프로세스를 어떻게 띄우는가”의 source of truth가 되고,
`application-prod.yml`은 “애플리케이션이 어떤 종료 정책을 읽는가”의 source of truth가 된다.

## 3. 요청 흐름은 안 바뀌고, 부팅 흐름만 달라진다

이번 작업은 게임 API나 추천 API 요청 흐름을 바꾸지 않는다.

대신 부팅/종료 흐름이 이렇게 정리된다.

1. ECS task definition이 env를 주입한다.
2. Docker entrypoint가 `JAVA_RUNTIME_OPTS`를 읽어 JVM을 띄운다.
3. Spring Boot는 `prod` 프로필에서 forwarded header와 graceful shutdown 설정을 읽는다.
4. ALB 뒤에서 요청을 처리한다.
5. 새 배포나 축소로 SIGTERM이 오면 graceful shutdown 기준으로 종료를 시도한다.

이건 웹 요청 흐름이 아니라 운영 런타임 흐름이다.

## 4. 테스트

이번 조각은 아래 기준으로 닫았다.

- `./gradlew test --tests com.worldmap.common.config.ProdProfileConfigTest`
- `docker build -t worldmap-runtime-check .`
- `docker run --rm --entrypoint sh worldmap-runtime-check -c 'echo "$JAVA_RUNTIME_OPTS"'`
- `git diff --check`

특히 `ProdProfileConfigTest`는 `application-prod.yml`을 직접 읽어서 아래를 고정한다.

- `server.forward-headers-strategy=native`
- `server.shutdown=graceful`
- `spring.lifecycle.timeout-per-shutdown-phase=20s`
- redis TLS 분기
- demo bootstrap off

## 5. 아직 남은 것

이 조각으로 끝난 건 아니다.

아직 남은 핵심은 아래다.

1. `Actuator readiness/liveness`
2. `Secrets Manager` 또는 `SSM Parameter Store`
3. `Spring Session + Redis`

즉 지금 상태는
“컨테이너가 어떻게 뜨고 어떻게 내려갈지”는 정리됐지만,
“ECS가 health check로 어떻게 판단할지”와
“다중 task에서도 세션이 유지되는가”는 아직 다음 단계다.

## 면접에서 30초 설명

> Dockerfile과 prod profile만으로는 아직 운영 기준이 부족했습니다. 그래서 prod에서는 `server.shutdown=graceful`과 종료 timeout을 명시하고, Docker runtime은 `JAVA_RUNTIME_OPTS`로 JVM 옵션을 override 가능하게 바꿨습니다. 핵심은 ALB 뒤에서 앱이 어떤 방식으로 뜨고 SIGTERM 때 어떻게 내려가야 하는지를 코드 파일 수준에서 설명 가능하게 만든 것입니다.
