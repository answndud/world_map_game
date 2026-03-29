# ECS task definition sample로 secrets 주입 기준 고정하기

배포 문서를 아무리 길게 써도, 초보자 입장에서는 결국 한 번 막힌다.

> 그래서 AWS 콘솔의 ECS task definition 화면에 정확히 뭘 넣어야 하는데?

이번 조각은 그 질문에 답하기 위해 저장소에 실제 샘플 파일을 추가한 작업이다.

## 왜 지금 필요한가

지금까지 배포 준비에서 아래는 이미 정리됐다.

- Dockerfile
- `application-prod.yml`
- graceful shutdown
- actuator health probe

그런데 아직도 초보자에게 제일 헷갈리는 건

- 어떤 값은 일반 환경변수로 넣고
- 어떤 값은 Secrets Manager / SSM으로 넣어야 하는지

였다.

그래서 이번에는 문장 설명 대신 실제 샘플 파일을 추가했다.

## 추가한 파일

- [task-definition.prod.sample.json](/Users/alex/project/worldmap/deploy/ecs/task-definition.prod.sample.json)
- [EcsTaskDefinitionTemplateTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/EcsTaskDefinitionTemplateTest.java)

## 1. 샘플 파일에서 나눈 기준

이번 샘플의 핵심은 `environment`와 `secrets`를 명확히 나눈 것이다.

### environment

비밀이 아닌 런타임 값은 `environment`에 둔다.

예:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`
- `SPRING_DATA_REDIS_SSL_ENABLED`
- `WORLDMAP_DEMO_BOOTSTRAP_ENABLED=false`
- `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED=true`
- `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME`
- `WORLDMAP_RANKING_KEY_PREFIX`
- `JAVA_RUNTIME_OPTS`

### secrets

비밀번호처럼 평문 env로 두면 안 되는 값은 `secrets`에 둔다.

예:

- `SPRING_DATASOURCE_PASSWORD`
- `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD`

이번 샘플은 두 secret을 일부러 다르게 넣었다.

- DB 비밀번호: `Secrets Manager`
- admin bootstrap 비밀번호: `SSM Parameter Store`

이유는 초보자에게 “둘 다 ECS의 `secrets` 필드로 넣을 수 있다”는 점을 동시에 보여 주기 위해서다.

## 2. 왜 앱 코드를 거의 안 바꿨나

이미 `application-prod.yml`은 아래 이름의 env를 읽도록 정리돼 있다.

- `SPRING_DATASOURCE_*`
- `SPRING_DATA_REDIS_*`
- `WORLDMAP_ADMIN_BOOTSTRAP_*`

즉 이번 조각의 핵심은 앱 코드 변경이 아니라

> 이 이름들을 ECS task definition에서 어떻게 주입할 것인가

를 파일로 남기는 일이었다.

그래서 새로운 컨트롤러나 서비스를 추가하지 않고, 배포 샘플 파일과 검증 테스트를 추가하는 쪽이 더 맞았다.

## 3. 테스트는 왜 필요한가

배포 샘플 파일도 결국 코드처럼 깨질 수 있다.

그래서 [EcsTaskDefinitionTemplateTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/EcsTaskDefinitionTemplateTest.java)로 아래를 고정했다.

- JSON 파일이 실제로 파싱되는지
- `family=worldmap-prod`인지
- `SPRING_PROFILES_ACTIVE=prod`가 들어 있는지
- `WORLDMAP_DEMO_BOOTSTRAP_ENABLED=false`인지
- `SPRING_DATASOURCE_PASSWORD`, `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD`가 `secrets`에만 있는지
- 같은 이름이 `environment`와 `secrets`에 동시에 들어가지 않는지

즉 “배포용 샘플도 저장소의 계약”으로 본 것이다.

## 4. 요청 흐름이 아니라 배포 입력 흐름

이번 조각은 게임 API 요청 흐름을 바꾸지 않는다.

대신 입력 흐름이 이렇게 정리된다.

1. Secrets Manager / SSM에 값 저장
2. ECS task definition `secrets` field에서 ARN 참조
3. 컨테이너 env로 주입
4. Spring Boot가 `application-prod.yml`을 통해 읽음

즉 이 작업의 핵심은 사용자 요청이 아니라 **운영 비밀 값이 앱에 들어오는 경로**를 설명 가능하게 만드는 것이다.

## 5. 아직 남은 것

이제 남은 핵심은 두 가지다.

1. `Spring Session + Redis`
2. ECS 배포 자동화

즉 task definition sample까지 생겼으니,
다음엔 실제로 `task 1개 공개 -> session externalization -> task 2개` 순서를 밟을 준비를 해야 한다.

## 면접에서 30초 설명

> 이번에는 ECS task definition 샘플 파일을 저장소에 추가해서, 어떤 값은 일반 environment로 넣고 어떤 비밀번호는 `secrets` 필드로 넣는지 기준을 고정했습니다. 핵심은 앱 코드보다 배포 입력 계약을 파일로 남긴 점입니다. 그래서 초보자도 ECS 콘솔과 저장소 파일을 1:1로 대응시키며 설명할 수 있습니다.
