# prod 설정을 더 안전하게 만들고 startup rollback 범위를 local/test로 제한하기

## 왜 이 글을 쓰는가

ECS 배포 준비를 하면서 다시 보니,
현재 설정에는 운영에서 바로 사고로 이어질 수 있는 지점이 남아 있었다.

1. `application.yml`이 기본으로 `local` 프로필을 잡고 있었다.
2. `application-prod.yml`이 `ddl-auto=update`라서 운영 부팅이 스키마 변경으로 이어질 수 있었다.
3. `GameLevelRollbackInitializer`가 startup 때 실제 DB와 Redis 데이터를 직접 건드리는데, prod에서도 막혀 있지 않았다.
4. readiness probe가 DB만 보고 Redis는 보지 않았다.

이번 조각은 기능 추가가 아니라
운영에서 “실수로 켜지거나, 실수로 바뀌는 것”을 줄이는 쪽에 집중한다.

## 이번 단계의 목표

- base config가 더 이상 `local`을 기본값으로 강제하지 않게 만든다.
- prod는 schema를 바꾸지 않고 검증만 하게 만든다.
- legacy rollback initializer는 local/test에서만 켜지게 만든다.
- prod readiness가 Redis까지 포함하도록 맞춘다.

## 바뀐 파일

- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-test.yml`
- `src/main/resources/application-prod.yml`
- `src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java`
- `src/test/java/com/worldmap/common/config/ApplicationConfigTest.java`
- `src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java`
- `src/test/java/com/worldmap/common/config/ActuatorHealthEndpointIntegrationTest.java`
- `src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java`

## 설계 핵심 1. base config는 local을 기본으로 강제하지 않는다

원래 `application.yml`에는 이런 설정이 있었다.

```yaml
spring:
  profiles:
    default: local
```

이 구조는 개발할 때는 편하지만,
프로필을 명시하지 않은 실행이 전부 local처럼 동작한다는 뜻이기도 하다.

운영에서는 “명시하지 않으면 local”보다
“반드시 어떤 프로필로 띄우는지 분명해야 한다”가 더 중요하다.

그래서 base config에서 default local을 제거했다.

이제 local은 명시적으로 `local` 프로필을 켰을 때만 동작한다.

## 설계 핵심 2. prod는 schema를 바꾸지 않고 validate만 한다

운영에서 가장 위험한 설정 중 하나가 `ddl-auto=update`다.

이번에는 `application-prod.yml`을 이렇게 바꿨다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

이제 prod는 부팅할 때

- 엔티티와 스키마가 맞는지 검증은 하고
- 스키마를 직접 수정하지는 않는다

라는 성격을 갖는다.

즉, 스키마 변경은 애플리케이션 startup side effect가 아니라
별도 migration 책임으로 분리된다.

## 설계 핵심 3. legacy rollback initializer는 항상 켜두면 안 된다

`GameLevelRollbackInitializer`는 Level 2 rollback 호환성을 위해 남겨 둔 코드다.

문제는 이 코드가 startup 시점에

- legacy 컬럼 존재 여부를 확인하고
- constraint를 내리고
- 예전 row를 삭제하고
- Redis key도 정리한다

는 점이다.

이건 local 호환성 조각으로는 괜찮지만,
prod에서 항상 돌아도 되는 성격은 아니다.

그래서 이번에는 property gate를 붙였다.

```java
@ConditionalOnProperty(name = "worldmap.legacy.rollback.enabled", havingValue = "true")
```

프로필별 설정은 이렇게 나뉜다.

- `application.yml`: `false`
- `application-local.yml`: `true`
- `application-test.yml`: `true`
- `application-prod.yml`: `false`

즉, local/test에서는 예전 DB를 재현하고 정리하는 데 계속 쓸 수 있고,
prod는 startup 때 이런 mutation을 하지 않는다.

## 설계 핵심 4. readiness는 Redis까지 봐야 한다

현재 prod는 session과 ranking에서 Redis 비중이 크다.

그런데 readiness가 DB만 보고 있으면
애플리케이션은 요청을 받을 준비가 안 됐는데도
ALB나 오케스트레이터는 “ready”로 볼 수 있다.

그래서 readiness group을 이렇게 바꿨다.

```yaml
management:
  endpoint:
    health:
      group:
        readiness:
          include: readinessState,db,redis,ping
```

이제 prod readiness는

- Spring readiness state
- DB
- Redis
- ping

까지 같이 본다.

## 요청 흐름보다 중요한 것: startup 책임을 줄이는 것

이번 조각은 API 요청 흐름을 바꾸지 않는다.

대신 “앱이 뜨는 순간 어떤 side effect를 허용할 것인가”를 좁힌다.

즉, 이번 단계의 핵심은 request-time 비즈니스 로직이 아니라
startup-time 운영 안전성이다.

## 테스트

이번 조각에서 직접 확인한 테스트는 다음이다.

- `ApplicationConfigTest`
- `ProdProfileConfigTest`
- `ActuatorHealthEndpointIntegrationTest`
- `GameLevelRollbackInitializerIntegrationTest`

이 테스트로 아래를 고정했다.

- base config가 더 이상 local default를 강제하지 않는가
- prod가 `ddl-auto=validate`와 Redis readiness를 쓰는가
- readiness endpoint가 실제로 열리는가
- rollback initializer가 property gate 아래에서도 test profile에서 계속 동작하는가

## 면접에서 어떻게 설명할까

이렇게 설명하면 된다.

> ECS 배포 준비를 하면서 운영 설정을 다시 보니, 기본 프로필이 local로 강제되고 prod가 `ddl-auto=update`를 쓰는 상태였습니다. 그래서 base config에서는 local default를 제거하고, prod는 schema를 바꾸지 않고 validate만 하게 바꿨습니다. 또 legacy Level 2 rollback initializer는 startup 때 DB와 Redis를 직접 수정하는 코드라 local/test에서만 켜지도록 property gate를 붙였고, readiness probe에는 Redis도 포함시켜 실제로 요청을 받을 준비가 된 뒤에만 트래픽을 받게 정리했습니다.
