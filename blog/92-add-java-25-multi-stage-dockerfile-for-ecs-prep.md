# Java 25 기준 multi-stage Dockerfile로 ECS 배포 준비 시작하기

## 왜 이 조각이 필요한가

배포 계획 문서가 있다고 바로 AWS에 올릴 수 있는 것은 아니다.

실제로 필요한 첫 증거는 이것이다.

- 현재 프로젝트가 컨테이너 이미지로 빌드되는가
- 로컬 `build/libs` 산출물에 기대지 않는가
- Java 버전과 runtime image가 실제로 맞는가

이번 조각의 목적은 “production-ready 설정을 한 번에 다 끝내기”가 아니라,
`ECS에 실을 수 있는 이미지가 지금 코드 기준으로 실제로 만들어지는가`를 먼저 증명하는 것이다.

## 이번에 바뀐 파일

- [Dockerfile](/Users/alex/project/worldmap/Dockerfile)
- [.dockerignore](/Users/alex/project/worldmap/.dockerignore)

같이 아래 문서도 현재 기준으로 맞췄다.

- [README.md](/Users/alex/project/worldmap/README.md)
- [DEPLOYMENT_RUNBOOK_AWS_ECS.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md)
- [PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 어떤 Dockerfile을 만들었나

이번 Dockerfile은 multi-stage build다.

### builder stage

- base image: `eclipse-temurin:25-jdk`
- 역할: Gradle wrapper로 `bootJar` 생성

핵심은 builder stage 안에서 직접

```bash
./gradlew --no-daemon bootJar -x test
```

를 실행한다는 점이다.

즉, 로컬에서 미리 jar를 빌드해 둘 필요가 없다.

### runtime stage

- base image: `eclipse-temurin:25-jre`
- 역할: builder가 만든 jar만 들고 실제 앱 실행

runtime image에는

- 소스 코드
- Gradle wrapper
- `.gradle` 캐시
- 테스트 파일

이런 것들이 들어가지 않는다.

그래서 이미지가 더 단순하고,
“실행에 필요한 것만 남긴다”는 설명이 가능해진다.

## 왜 multi-stage가 중요한가

단일 stage Dockerfile로도 동작은 시킬 수 있다.

하지만 그러면 runtime image 안에

- JDK
- Gradle
- 소스 코드
- 빌드 캐시

가 같이 들어가기 쉽다.

이번 프로젝트는 취업 포트폴리오이기 때문에
“컨테이너 이미지를 작게 유지하려고 builder와 runtime을 분리했다”
라고 설명할 수 있어야 한다.

즉, 이번 Dockerfile은 단순히 돌아가는 파일이 아니라
**배포 단위를 어떻게 나눴는가**를 보여 주는 설계 근거다.

## 왜 Java 25를 그대로 썼나

현재 [build.gradle](/Users/alex/project/worldmap/build.gradle) 기준 toolchain은 `Java 25`다.

이번 조각에서는 그 사실을 먼저 존중했다.

즉,

1. Java 25를 유지한 채
2. 실제 Docker runtime image가 있는지 확인하고
3. build가 실제로 되는지 검증했다.

결과적으로

- `eclipse-temurin:25-jdk`
- `eclipse-temurin:25-jre`

기준으로 `docker build`가 통과했다.

이건 의미가 있다.

런북에서 “Java 25는 먼저 확인이 필요하다”고 적어 두기만 하면 계획에 머무르지만,
이번에는 실제로 빌드까지 성공해서 현재 기준 배포 런타임 후보가 검증된 셈이다.

물론 이후에 실무 표준성과 안정성을 더 우선한다면
Java 21 LTS로 내릴 수도 있다.

하지만 적어도 지금은
“현재 코드 기준 Java 25도 컨테이너 빌드가 된다”
는 사실을 확보했다.

## 왜 비root 사용자로 실행했나

runtime stage에서는 `spring` 사용자로 앱을 실행하게 했다.

이유는 단순하다.

- 컨테이너 안에서 굳이 root로 실행할 이유가 없다.
- 포트폴리오에서도 기본적인 보안 위생을 설명할 수 있다.

이건 거대한 보안 아키텍처는 아니지만,
“기본값을 그대로 두지 않았다”는 점에서 설명 가치가 있다.

## .dockerignore는 왜 같이 넣었나

이번 조각은 사실 `Dockerfile`만으로 끝내면 반쪽이다.

`.dockerignore`가 없으면 build context에 불필요한 파일이 다 들어간다.

이번에 제외한 대표 항목은 아래다.

- `build`
- `.gradle`
- `docs`
- `blog`
- `scripts`
- `src/test`
- `.env*`

즉, 이미지 자체가 아니라
**빌드 컨텍스트도 최소화**한 것이다.

## 요청 흐름은 어떻게 바뀌었나

런타임 요청 흐름은 바뀌지 않았다.

여전히 앱은

- `/`
- `/stats`
- `/ranking`
- `/games/*`

같은 HTTP 요청을 Spring Boot가 처리한다.

바뀐 것은 배포 전 build 흐름이다.

이전:

1. 로컬 JVM에서 실행
2. local profile 의존

이제:

1. `docker build`
2. builder stage에서 `bootJar`
3. runtime stage에 `app.jar` 복사
4. ECS 같은 컨테이너 환경으로 이동 가능

즉, 사용자 요청 흐름이 아니라
**배포 artifact 생성 흐름**이 새로 생겼다.

## 테스트는 무엇을 했나

이번 조각에서 가장 중요한 검증은 코드 테스트가 아니라 실제 이미지 빌드다.

실행:

```bash
docker build -t worldmap-dockerfile-check .
```

확인한 것:

- `eclipse-temurin:25-jdk` / `25-jre` pull 성공
- builder stage에서 `./gradlew bootJar -x test` 통과
- runtime stage로 `app.jar` 복사 성공
- 최종 이미지 생성 성공

그리고 문서 형식은 아래로 마감했다.

```bash
git diff --check
```

## 아직 남은 것

이번 조각은 컨테이너 빌드만 연 것이다.

아직 아래는 남아 있다.

- `application-prod.yml`
- forwarded headers
- JVM 메모리 옵션 운영 기준 확정
- graceful shutdown
- Actuator readiness/liveness
- Secrets Manager / SSM 연동
- Spring Session + Redis

즉, 이미지가 만들어진다고 바로 production-ready는 아니다.

하지만 이번 조각이 먼저 있어야
그 다음 ECS task definition과 prod 환경변수를 이야기할 수 있다.

## 다음 단계

다음 작은 조각은 `application-prod.yml`이다.

이제 Dockerfile이 있으니,
그 다음엔 컨테이너 안에서 실제로 어떤 설정으로 앱이 떠야 하는지 분리해야 한다.

## 면접에서 이렇게 설명할 수 있다

> 배포 준비 첫 조각으로 Java 25 기준 multi-stage Dockerfile을 추가했습니다. builder stage는 Gradle wrapper로 `bootJar`를 만들고, runtime stage는 JRE 이미지에 jar만 복사해서 비root 사용자로 실행합니다. 핵심은 로컬 빌드 산출물에 기대지 않고, Docker build만으로 ECS에 올릴 수 있는 이미지가 실제로 만들어진다는 점을 먼저 검증했다는 것입니다.
