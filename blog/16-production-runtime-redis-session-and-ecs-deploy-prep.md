# [Spring Boot 게임 플랫폼 포트폴리오] 16. production runtime, Redis session, ECS deploy prep를 어떻게 코드로 남겼는가

## 1. 이번 글에서 풀 문제

local에서 `./gradlew bootRun`이 된다고 해서 production-ready라고 부를 수는 없습니다.  
운영 런타임에서는 기능 코드보다 먼저 아래를 설명할 수 있어야 합니다.

- 어떤 이미지로 실행하는가
- 어떤 profile이 production의 source of truth인가
- session을 어디에 두고, local/test와 왜 다르게 두는가
- health check는 무엇을 살아 있다고 보나
- ECS task definition은 어떤 값이 환경변수이고 어떤 값이 secret인가
- GitHub Actions 배포 전, 빠진 입력을 어떻게 미리 찾는가

WorldMap에서는 이 질문들을 아래 파일로 닫았습니다.

- [Dockerfile](../Dockerfile)
- [application-prod.yml](../src/main/resources/application-prod.yml)
- [RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)
- [deploy-prod-ecs.yml](../.github/workflows/deploy-prod-ecs.yml)
- [task-definition.prod.sample.json](../deploy/ecs/task-definition.prod.sample.json)
- [render_ecs_task_definition.py](../scripts/render_ecs_task_definition.py)
- [check_prod_deploy_preflight.py](../scripts/check_prod_deploy_preflight.py)

즉, 이 글은 "배포도 해 볼 수 있다"가 아니라 **운영 계약을 코드와 테스트로 고정했다**는 이야기를 합니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 production runtime 파트는 아래 상태여야 합니다.

- Java 25 기반 multi-stage Docker image를 실제로 빌드할 수 있다
- runtime image는 `bootJar` 결과물 1개와 JRE만 담고, 테스트/문서/스크립트는 포함하지 않는다
- base/local/test/prod profile 책임이 서로 다르고, prod는 `application-prod.yml`이 source of truth다
- prod는 `ddl-auto=validate`, `demo bootstrap off`, `legacy rollback off`, `WMSESSION`, `graceful shutdown`, `forwarded headers`를 기본 계약으로 가진다
- prod에서만 Redis-backed session이 켜지고, local/test는 `SessionAutoConfiguration`을 제외해 servlet session을 유지한다
- `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`가 운영 probe endpoint로 열린다
- prod 설정은 readiness group을 현재 `readinessState,db,redis,ping`로 선언하고, 현재 테스트는 이 설정값과 endpoint 노출까지만 고정한다
- ECS task definition sample은 environment와 secrets를 분리해서 보관한다
- render script는 sample JSON을 실제 AWS 리소스 값으로 치환해 unresolved placeholder 없이 rendered JSON을 만든다
- GitHub Actions deploy workflow는 `workflow_dispatch -> gradle test -> ECR push -> render task definition -> ECS deploy` 순서를 강제한다
- preflight script는 workflow가 요구하는 GitHub repository variables를 읽어 `Ready: YES/NO` 보고서를 만든다
- 현재 workflow 기준 required GitHub repository variable 수는 13개이며, preflight는 live repo 변수나 offline fixture에 대해 같은 규칙을 적용한다

즉, 최종 상태는 "배포 방법을 알고 있다"가 아니라 **운영 입력이 빠지면 어디서 멈추는지까지 저장소가 스스로 말해 준다**는 것입니다.

## 3. 먼저 알아둘 개념

### 3-1. runtime contract

production 환경에서 앱이 인프라와 맺는 약속입니다.

예:

- datasource는 어디서 읽는가
- session은 어디에 저장하는가
- health check가 무엇을 포함하는가
- shutdown은 어떤 방식으로 끝나는가

### 3-2. self-contained image

빌드와 실행에 필요한 Java runtime과 app jar를 함께 담은 이미지입니다.  
WorldMap에서는 runtime container 안에서 다시 Gradle을 돌리지 않습니다.

### 3-3. prod-only Redis session

운영 환경에서만 `Spring Session + Redis`를 켜고, local/test는 기존 servlet session 흐름을 유지하는 전략입니다.

### 3-4. task definition template

실제 ECS task definition을 바로 저장소에 박아 넣는 대신, 샘플 JSON에 placeholder를 두고 render script가 실제 값으로 치환하는 방식입니다.

### 3-5. preflight

배포를 누르기 전에 "missing input"을 미리 찾는 단계입니다.  
WorldMap에서는 이걸 문서 체크리스트가 아니라 Python script로 남겼습니다.

## 4. 이번 글에서 다룰 파일

- [Dockerfile](../Dockerfile)
- [.dockerignore](../.dockerignore)
- [application.yml](../src/main/resources/application.yml)
- [application-local.yml](../src/main/resources/application-local.yml)
- [application-test.yml](../src/main/resources/application-test.yml)
- [application-prod.yml](../src/main/resources/application-prod.yml)
- [RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)
- [deploy-prod-ecs.yml](../.github/workflows/deploy-prod-ecs.yml)
- [task-definition.prod.sample.json](../deploy/ecs/task-definition.prod.sample.json)
- [render_ecs_task_definition.py](../scripts/render_ecs_task_definition.py)
- [check_prod_deploy_preflight.py](../scripts/check_prod_deploy_preflight.py)
- [DEPLOYMENT_RUNBOOK_AWS_ECS.md](../docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md)
- [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)
- [ActuatorHealthEndpointIntegrationTest.java](../src/test/java/com/worldmap/common/config/ActuatorHealthEndpointIntegrationTest.java)
- [RedisSessionConfigurationIntegrationTest.java](../src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)
- [RenderEcsTaskDefinitionScriptTest.java](../src/test/java/com/worldmap/common/config/RenderEcsTaskDefinitionScriptTest.java)
- [GitHubActionsDeployWorkflowTemplateTest.java](../src/test/java/com/worldmap/common/config/GitHubActionsDeployWorkflowTemplateTest.java)
- [ProdDeployPreflightScriptTest.java](../src/test/java/com/worldmap/common/config/ProdDeployPreflightScriptTest.java)
- [prod-deploy-preflight.md](../build/reports/deploy-preflight/prod-deploy-preflight.md)

## 5. 핵심 도메인 모델 / 상태

이 글은 비즈니스 엔티티보다 **운영 상태**를 다룹니다.

### 5-1. base profile 상태

[application.yml](../src/main/resources/application.yml)는 모든 환경의 공통 기본값입니다.

현재 핵심:

- `spring.application.name=worldmap`
- `spring.data.redis.repositories.enabled=false`
- `spring.jpa.open-in-view=false`
- `management.endpoints.web.exposure.include=health,info`
- `worldmap.legacy.rollback.enabled=false`
- `worldmap.seed.countries.enabled=true`
- `worldmap.ranking.key-prefix=leaderboard`

즉, base는 "어디서든 공통인 최소 계약"만 담습니다.

### 5-2. local/test 상태

[application-local.yml](../src/main/resources/application-local.yml), [application-test.yml](../src/main/resources/application-test.yml)의 핵심은 **운영 흉내보다 개발 단순성**입니다.

current local:

- `SessionAutoConfiguration` 제외
- `thymeleaf.cache=false`
- `ddl-auto=update`
- legacy rollback on
- admin bootstrap on
- demo bootstrap on
- admin 기본 nickname/password env fallback 존재

current test:

- `SessionAutoConfiguration` 제외
- `compose.enabled=false`
- H2 datasource
- `ddl-auto=create-drop`
- legacy rollback on
- admin/demo bootstrap off
- ranking key prefix는 `test:leaderboard`

즉, local/test는 prod를 축소한 버전이 아니라 **다른 목적의 runtime**입니다.

### 5-3. prod profile 상태

[application-prod.yml](../src/main/resources/application-prod.yml)는 운영 계약을 직접 정의합니다.

핵심 값:

- `spring.thymeleaf.cache=true`
- `spring.docker.compose.enabled=false`
- `spring.lifecycle.timeout-per-shutdown-phase=20s`
- datasource/redis host는 environment variable로만 주입
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.sql.init.mode=never`
- `server.forward-headers-strategy=native`
- `server.shutdown=graceful`
- `server.servlet.session.timeout=14d`
- cookie name `WMSESSION`
- cookie `http-only=true`, `same-site=lax`, `secure=true`
- readiness group `readinessState,db,redis,ping`
- liveness group `livenessState,ping`
- `worldmap.legacy.rollback.enabled=false`
- `worldmap.demo.bootstrap.enabled=false`
- admin bootstrap은 env-driven

즉, prod는 "잘 돌아가면 됨"이 아니라 **운영에서 더 느슨해지면 안 되는 값들**을 모아 둔 파일입니다.

### 5-4. 현재 prod runtime contract를 표로 고정하면

현재 저장소 기준의 prod runtime contract를 그대로 적으면 아래와 같습니다.

| 항목 | 현재 값 | source of truth |
| --- | --- | --- |
| Java runtime | `eclipse-temurin:25-jre` | [Dockerfile](../Dockerfile) |
| build image | `eclipse-temurin:25-jdk` | [Dockerfile](../Dockerfile) |
| app user | `spring` / `uid=10001` | [Dockerfile](../Dockerfile) |
| container port | `8080` | [Dockerfile](../Dockerfile), [task-definition.prod.sample.json](../deploy/ecs/task-definition.prod.sample.json) |
| stop signal | `SIGTERM` | [Dockerfile](../Dockerfile) |
| JVM opt default | `-XX:MaxRAMPercentage=75.0` | [Dockerfile](../Dockerfile), [task-definition.prod.sample.json](../deploy/ecs/task-definition.prod.sample.json) |
| thymeleaf cache | `true` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| compose | `false` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| schema policy | `ddl-auto=validate` | [application-prod.yml](../src/main/resources/application-prod.yml), [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java) |
| sql init | `never` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| forwarded headers | `native` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| shutdown | `graceful` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| shutdown phase timeout | `20s` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| actuator exposure | `health,info` | [application.yml](../src/main/resources/application.yml) |
| readiness group | `readinessState,db,redis,ping` | [application-prod.yml](../src/main/resources/application-prod.yml), [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java) |
| liveness group | `livenessState,ping` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| redis ssl toggle | `${SPRING_DATA_REDIS_SSL_ENABLED:false}` | [application-prod.yml](../src/main/resources/application-prod.yml) |
| ranking key prefix default | `${WORLDMAP_RANKING_KEY_PREFIX:leaderboard}` | [application-prod.yml](../src/main/resources/application-prod.yml), [application.yml](../src/main/resources/application.yml) |

즉, 운영 런타임의 핵심 값들은 이미 문서가 아니라 **Dockerfile + YAML + config test**에 흩어져 있고, 이 표는 그 계약을 다시 읽는 용도입니다.

### 5-5. session backend 상태

WorldMap session 전략은 environment에 따라 갈립니다.

- local/test: servlet session
- prod: Redis-backed session

source of truth:

- local/test는 [application-local.yml](../src/main/resources/application-local.yml), [application-test.yml](../src/main/resources/application-test.yml)의 `SessionAutoConfiguration` exclude
- prod는 [RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)

즉, session backend 전략도 "설정 암묵값"이 아니라 **파일 두 군데에서 명시적으로 갈라집니다**.

### 5-6. deploy artifact 상태

현재 저장소가 가진 deploy artifact는 네 가지입니다.

- container image recipe
- prod runtime config
- ECS task definition template/render
- deploy preflight and workflow

이 네 가지가 함께 있어야 "배포 준비가 됐다"는 말을 코드로 뒷받침할 수 있습니다.

## 6. 설계 구상

### 왜 운영 런타임을 별도 글로 묶는가

이 영역은 점수 계산이나 게임 도메인이 아니라 **runtime contract**입니다.  
기능 설명과 섞이면, 기능도 약해지고 운영 설명도 약해집니다.

예를 들어,

- `LocationGameService`의 stale submit 방어
- `application-prod.yml`의 readiness group

은 둘 다 중요하지만 같은 종류의 설명이 아닙니다.

즉, production runtime은 "도메인 고도화"가 아니라 **환경 계약을 설명하는 별도 계층**으로 묶는 편이 낫습니다.

### 왜 prod에서만 Redis session을 쓰는가

멀티 인스턴스 환경에서는 세션을 서버 메모리에 둘 수 없습니다.  
하지만 local/test까지 같은 전략을 강제하면 개발 복잡도가 커집니다.

WorldMap은 다음 trade-off를 택했습니다.

- local/test: 빠른 개발, 단순 디버깅, H2/servlet session 유지
- prod: scale-out과 task 교체를 견디기 위해 Redis session 사용

즉, "모든 환경 동일"보다 **목적에 맞는 환경 분리**를 택했습니다.

### 왜 Dockerfile과 workflow만으로 충분하지 않은가

Dockerfile만 있으면 이미지 recipe는 있지만, 실제 AWS 입력 경계가 없습니다.  
workflow만 있으면 automation은 있지만, 어떤 값이 secret이고 어떤 값이 environment인지 설명이 흐립니다.

그래서 저장소에는 아래를 같이 둡니다.

- Dockerfile
- task-definition sample
- render script
- workflow
- preflight script

즉, deploy 준비도 하나의 파일이 아니라 **입력 경계가 드러나는 여러 산출물의 조합**입니다.

### 왜 preflight가 꼭 필요한가

배포는 보통 "앱 코드"보다 "입력 누락" 때문에 실패합니다.

현재 workflow가 요구하는 GitHub variables는 13개입니다.

- `AWS_REGION`
- `AWS_ACCOUNT_ID`
- `AWS_GITHUB_ACTIONS_ROLE_ARN`
- `ECR_REPOSITORY`
- `ECS_CLUSTER`
- `ECS_SERVICE`
- `ECS_EXECUTION_ROLE_ARN`
- `ECS_TASK_ROLE_ARN`
- `RDS_ENDPOINT`
- `ELASTICACHE_ENDPOINT`
- `CLOUDWATCH_LOG_GROUP`
- `SPRING_DATASOURCE_PASSWORD_SECRET_ARN`
- `ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN`

이걸 사람 기억에만 맡기면 첫 배포는 거의 입력 누락으로 실패합니다.  
그래서 [check_prod_deploy_preflight.py](../scripts/check_prod_deploy_preflight.py)가 `vars.*` 참조를 workflow에서 직접 읽고 report를 만듭니다.

## 7. 코드 설명

### 7-1. `Dockerfile`: build stage와 runtime stage를 분리한다

[Dockerfile](../Dockerfile) 구조:

1. `eclipse-temurin:25-jdk AS builder`
2. `gradlew`, `gradle/`, `build.gradle`, `settings.gradle`, `src/` 복사
3. `./gradlew --no-daemon bootJar -x test`
4. `eclipse-temurin:25-jre` runtime image
5. `spring` system user 생성
6. `/workspace/build/libs/*.jar`를 `/app/app.jar`로 복사
7. `JAVA_RUNTIME_OPTS` 기본값 지정
8. `exec java $JAVA_RUNTIME_OPTS -jar /app/app.jar`

중요한 점:

- runtime image는 JDK가 아니라 JRE입니다
- 이미지 안에서 테스트를 다시 돌리지 않습니다
- runtime user는 root가 아닙니다
- `STOPSIGNAL SIGTERM`으로 graceful shutdown path를 열어 둡니다

### 7-2. `.dockerignore`: runtime image에 들어가면 안 되는 것을 먼저 뺀다

[.dockerignore](../.dockerignore)는 아래를 제외합니다.

- `.git`, `.github`
- `build`
- `blog`, `docs`
- `scripts`
- `src/test`
- `compose.yaml`
- `.env*`

의미:

- image build context를 줄입니다
- runtime image가 문서/테스트/로컬 compose에 의존하지 않게 합니다
- 테스트는 image build 전에 CI에서 돌린다는 계약이 더 선명해집니다

즉, `.dockerignore`도 runtime contract의 일부입니다.

### 7-3. `application-prod.yml`: 운영에서 느슨해지면 안 되는 값을 모은다

[application-prod.yml](../src/main/resources/application-prod.yml)의 핵심 계약을 그대로 적으면 아래와 같습니다.

- compose off
- thymeleaf cache on
- datasource는 `SPRING_DATASOURCE_*`
- redis는 `SPRING_DATA_REDIS_*`
- `ddl-auto=validate`
- `sql.init.mode=never`
- forwarded headers native
- graceful shutdown
- cookie secure
- demo bootstrap off
- legacy rollback off

특히 중요한 세 가지:

1. `ddl-auto=validate`  
   운영에서 schema를 runtime이 바꾸지 않게 막습니다.
2. `worldmap.legacy.rollback.enabled=false`  
   startup rollback initializer가 production data를 건드리지 않게 막습니다.
3. `readinessState,db,redis,ping`  
   prod 설정 단계에서 앱 프로세스만 살아 있다고 ready로 보지 않겠다는 의도를 고정합니다. 다만 현재 테스트는 이 group 값과 endpoint 노출만 검증하고, readiness payload 안의 각 contributor 평가까지 end-to-end로 확인하지는 않습니다.

### 7-4. `RedisSessionProdConfiguration`: prod에서만 Spring Session Redis를 켠다

[RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)는 매우 짧지만 중요합니다.

```java
@Profile("prod")
@EnableRedisIndexedHttpSession(
    maxInactiveIntervalInSeconds = 60 * 60 * 24 * 14,
    redisNamespace = "worldmap:session"
)
```

의미:

- prod profile일 때만 켜짐
- session TTL은 14일
- Redis namespace는 `worldmap:session`

[RedisSessionConfigurationIntegrationTest.java](../src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)는 이 구성이 실제 테스트 컨텍스트에서 Redis-backed repository bean으로 풀리는지 확인합니다.

중요한 점은 이 테스트가 `test,prod`를 함께 켠다는 것입니다.

- datasource는 H2로 대체
- Redis host는 localhost 테스트 값으로 대체
- 목적은 ECS smoke가 아니라 `prod profile을 얹었을 때 wiring이 실제로 redis session repo를 만들 수 있는가`를 확인하는 것

local/test는 같은 설정이 없습니다.  
대신 `SessionAutoConfiguration` 자체를 꺼 둬서 기존 servlet session을 유지합니다.

즉, prod session externalization은 "대충 Redis host만 넣으면 된다"가 아니라 **prod profile에 묶인 코드 설정**입니다.

### 7-5. health contract는 `/actuator/health/*`로 고정한다

현재 prod 설정이 선언하는 probe contract:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

[ActuatorHealthEndpointIntegrationTest.java](../src/test/java/com/worldmap/common/config/ActuatorHealthEndpointIntegrationTest.java)는 prod profile을 얹은 상태에서도 세 endpoint가 실제로 열리는지 검증합니다.

[ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)는 readiness/liveness group 설정값이 현재 아래인지까지 고정합니다.

- `livenessState,ping`
- `readinessState,db,redis,ping`

중요한 한계도 정직하게 적어야 합니다.

- 현재 테스트는 "endpoint가 열린다"와 "YAML에 어떤 group 값이 적혀 있다"까지만 보장합니다
- 아직 `/actuator/health/readiness` 응답 본문 안에서 `db`, `redis`, `ping` contributor가 실제로 포함·평가되는지까지는 검증하지 않습니다

즉, probe contract는 현재 **설정값 + endpoint 노출 수준**으로 고정돼 있고, full readiness semantics까지 증명한 것은 아닙니다.

### 7-6. `task-definition.prod.sample.json`: environment와 secrets를 분리한다

[task-definition.prod.sample.json](../deploy/ecs/task-definition.prod.sample.json)은 현재 Fargate sample입니다.

현재 resource baseline:

- `networkMode=awsvpc`
- `requiresCompatibilities=["FARGATE"]`
- `cpu=512`
- `memory=1024`
- container name `worldmap-app`
- container port `8080`

중요한 environment:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT=6379`
- `SPRING_DATA_REDIS_SSL_ENABLED=true`
- `WORLDMAP_DEMO_BOOTSTRAP_ENABLED=false`
- `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED=true`
- `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME=worldmap_admin`
- `WORLDMAP_RANKING_KEY_PREFIX=leaderboard`
- `JAVA_RUNTIME_OPTS=-XX:MaxRAMPercentage=75.0`

중요한 secrets:

- `SPRING_DATASOURCE_PASSWORD`
- `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD`

current env/secrets/source matrix를 그대로 적으면 아래와 같습니다.

| 이름 | 값의 출처 | sample 기본값/형태 | 비고 |
| --- | --- | --- | --- |
| `AWS_REGION` | GitHub repo var | 없음 | workflow env, render script 입력 |
| `AWS_ACCOUNT_ID` | GitHub repo var | 없음 | workflow env, render script 입력 |
| `AWS_GITHUB_ACTIONS_ROLE_ARN` | GitHub repo var | 없음 | OIDC assume role |
| `ECR_REPOSITORY` | GitHub repo var | 없음 | `IMAGE_URI` fallback 조립에 사용 |
| `ECS_CLUSTER` | GitHub repo var | 없음 | deploy target |
| `ECS_SERVICE` | GitHub repo var | 없음 | deploy target |
| `ECS_EXECUTION_ROLE_ARN` | GitHub repo var | 없음 | rendered task field |
| `ECS_TASK_ROLE_ARN` | GitHub repo var | 없음 | rendered task field |
| `RDS_ENDPOINT` | GitHub repo var | 없음 | datasource URL render |
| `ELASTICACHE_ENDPOINT` | GitHub repo var | 없음 | redis host render |
| `CLOUDWATCH_LOG_GROUP` | GitHub repo var | 없음 | awslogs group render |
| `SPRING_DATASOURCE_PASSWORD_SECRET_ARN` | GitHub repo var | 없음 | task `secrets` render |
| `ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN` | GitHub repo var | 없음 | task `secrets` render |
| `SPRING_PROFILES_ACTIVE` | template fixed env | `prod` | workflow input 아님 |
| `SPRING_DATASOURCE_USERNAME` | template fixed env | `worldmap_app` | workflow input 아님 |
| `SPRING_DATA_REDIS_PORT` | template fixed env | `6379` | workflow input 아님 |
| `SPRING_DATA_REDIS_SSL_ENABLED` | template fixed env | `true` | sample은 TLS-on 기준 |
| `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED` | template fixed env | `true` | 첫 prod bootstrap 전제 |
| `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME` | template fixed env | `worldmap_admin` | 첫 admin nickname |
| `WORLDMAP_RANKING_KEY_PREFIX` | template fixed env | `leaderboard` | app default와 같음 |
| `JAVA_RUNTIME_OPTS` | template fixed env | `-XX:MaxRAMPercentage=75.0` | runtime override 가능 |
| `IMAGE_URI` | workflow/runtime env | 선택 | 있으면 renderer가 그대로 사용 |
| `IMAGE_TAG` | workflow/runtime env | 보통 `${GITHUB_SHA}` | `IMAGE_URI`가 없을 때만 fallback 조립에 필요 |
| `SPRING_DATASOURCE_PASSWORD` | ECS secret | Secrets Manager ARN | 평문 env 금지 |
| `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD` | ECS secret | SSM parameter ARN | 평문 env 금지 |

참고로 `WORLDMAP_DEMO_BOOTSTRAP_ENABLED=false`는 sample task definition에 있긴 하지만, prod [application-prod.yml](../src/main/resources/application-prod.yml) 자체가 이미 demo bootstrap을 false로 고정하므로 **설명용 중복 안전장치**에 가깝습니다.

또 하나 중요한 점은 sample task definition에 container-level `healthCheck`가 없다는 사실입니다.

즉 현재 저장소는 앱이 어떤 actuator path를 노출하는지는 고정하지만,
ECS/ALB가 그중 어떤 path를 실제 probe로 쓸지는 첫 배포 시점에 수동으로 맞춰야 합니다.

즉, 평문으로 넣어도 되는 값과 secret manager/parameter store에서 가져와야 하는 값을 **JSON template 단계에서 나눠 둔 것**이 핵심입니다.

### 7-7. `render_ecs_task_definition.py`: 샘플을 실제 값으로 치환한다

[render_ecs_task_definition.py](../scripts/render_ecs_task_definition.py)의 실제 책임:

1. input JSON 로드
2. `worldmap-app` container 찾기
3. 아래 env를 항상 강제 요구
   - `AWS_REGION`
   - `AWS_ACCOUNT_ID`
   - `ECS_EXECUTION_ROLE_ARN`
   - `ECS_TASK_ROLE_ARN`
   - `RDS_ENDPOINT`
   - `ELASTICACHE_ENDPOINT`
   - `CLOUDWATCH_LOG_GROUP`
   - `SPRING_DATASOURCE_PASSWORD_SECRET_ARN`
   - `ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN`
4. image는 `IMAGE_URI`가 있으면 그대로 쓰고, 없으면 `ECR_REPOSITORY + IMAGE_TAG`를 추가로 요구해 조립
5. sample placeholder(`<ACCOUNT_ID>`, `<REGION>`, `<IMAGE_TAG>`)를 치환
6. unresolved placeholder가 남아 있으면 실패

즉 `IMAGE_URI`가 이미 build step에서 정해졌으면 그 값을 우선하고,
그렇지 않은 경우에만 `AWS_ACCOUNT_ID + AWS_REGION + ECR_REPOSITORY + IMAGE_TAG` 조합으로 fallback 이미지를 만듭니다.

정리하면 renderer 입력은 두 층입니다.

- 항상 필요한 값: `AWS_REGION`, `AWS_ACCOUNT_ID`, `ECS_* role`, `RDS_ENDPOINT`, `ELASTICACHE_ENDPOINT`, `CLOUDWATCH_LOG_GROUP`, secret ARN 2개
- 조건부로 필요한 값: `IMAGE_URI`가 비어 있을 때의 `ECR_REPOSITORY`, `IMAGE_TAG`

또 이 스크립트는 `worldmap-app`이라는 container name을 전제로 합니다.
즉 sample task definition과 renderer는 느슨한 문자열 치환기가 아니라 **서로 맞물린 한 세트**입니다.

즉, 이 스크립트는 단순 문자열 치환기가 아니라 **배포 입력 검증기 + task definition renderer**입니다.

[RenderEcsTaskDefinitionScriptTest.java](../src/test/java/com/worldmap/common/config/RenderEcsTaskDefinitionScriptTest.java)는 이 스크립트가 실제 env로 concrete JSON을 만들어 placeholder를 모두 제거하는지 확인합니다.

### 7-8. `deploy-prod-ecs.yml`: 첫 배포 순서를 workflow로 고정한다

[deploy-prod-ecs.yml](../.github/workflows/deploy-prod-ecs.yml)의 현재 순서:

1. `workflow_dispatch`
2. Java 25 setup
3. 필수 GitHub variable 비어 있으면 즉시 실패
4. `./gradlew test`
5. OIDC로 AWS credential 획득
6. ECR login
7. Docker build and push
8. task definition render
9. ECS deploy + wait-for-service-stability

현재 workflow input model은 단순합니다.

- trigger는 `workflow_dispatch` 하나뿐
- workflow input field는 **0개**
- deploy 값은 모두 GitHub repository variables에서 읽음
- image tag는 항상 `${GITHUB_SHA}`
- render script는 `IMAGE_URI`가 있으면 그 값을 쓰고, 없으면 `ECR_REPOSITORY + IMAGE_TAG`로 조립
- render script는 container name이 `worldmap-app`이라고 가정

중요한 점:

- 권한은 `contents: read`, `id-token: write`만 엽니다
- concurrency group이 `worldmap-prod-deploy`라 겹치는 prod deploy를 막습니다
- build 전에 반드시 전체 test를 돌립니다
- ECS deploy step은 `wait-for-service-stability: true`로 service 안정화까지 기다립니다
- workflow input field는 0개이고, 배포 값은 전부 GitHub repository variables에서 읽습니다

즉, deploy workflow도 "이미지가 있으면 배포"가 아니라 **검증 -> 인증 -> build -> render -> deploy** 순서를 강제하는 문서이자 코드입니다.

### 7-9. `check_prod_deploy_preflight.py`: workflow가 요구하는 입력을 먼저 읽는다

[check_prod_deploy_preflight.py](../scripts/check_prod_deploy_preflight.py)의 핵심:

- workflow 파일을 직접 읽는다
- `${{ vars.NAME }}` 패턴을 regex로 추출한다
- 현재 GitHub repo variable 목록과 비교한다
- 필요한 파일 존재 여부도 같이 검사한다
- Markdown report를 `build/reports/deploy-preflight/prod-deploy-preflight.md`에 쓴다
- `gh`를 쓰거나 `--variables-json`로 오프라인 입력을 받을 수 있다
- missing input이 있으면 non-zero exit code로 종료한다

현재 preflight가 필수로 보는 파일은 세 개입니다.

- `.github/workflows/deploy-prod-ecs.yml`
- `deploy/ecs/task-definition.prod.sample.json`
- `scripts/render_ecs_task_definition.py`

즉, preflight는 hardcoded checklist가 아니라 **현재 workflow의 요구사항을 그대로 읽는 검사기**입니다.

스크립트와 테스트가 안정적으로 말해주는 사실은 아래입니다.

- 현재 workflow는 `workflow_dispatch`를 가진다
- 현재 workflow가 요구하는 GitHub repository variable 수는 13개다
- repo variables가 비어 있는 fixture를 넣으면 report는 `Ready: NO`가 된다
- required variables가 모두 present인 fixture를 넣으면 report는 `Ready: YES`가 된다

반대로, "오늘 이 저장소의 live GitHub variables가 실제로 몇 개 비어 있는가"는
스크립트를 실행한 시점의 외부 상태에 따라 달라집니다.

중요한 한계도 같이 적어야 합니다. 현재 preflight는 아래만 검사합니다.

- workflow의 `vars.*` 참조
- 필수 파일 3개 존재

즉 `Ready: YES`는 "배포 입력 파일과 GitHub variables는 채워졌다"는 뜻이지,
"AWS 인프라가 올바르게 떠 있다"는 뜻은 아닙니다.

아직 **검사하지 않는 것**:

- AWS 리소스가 실제로 존재하는지
- secret/parameter 내부 값이 맞는지
- RDS/ElastiCache 네트워크 도달성이 되는지
- ECS/ALB wiring이 맞는지
- target group health check path가 맞는지

## 8. 요청 흐름 / 상태 변화

### 8-1. local/test와 prod의 session backend 분기

```text
local/test boot
-> application-local.yml / application-test.yml
-> SessionAutoConfiguration exclude
-> servlet session 유지

prod boot
-> application-prod.yml + RedisSessionProdConfiguration
-> EnableRedisIndexedHttpSession
-> Redis namespace worldmap:session
-> WMSESSION cookie 발급
```

### 8-2. CI/CD 배포 흐름

```text
workflow_dispatch
-> deploy-prod-ecs.yml
-> GitHub variables 검증
-> ./gradlew test
-> OIDC AWS 인증
-> ECR image push
-> render_ecs_task_definition.py
-> amazon-ecs-deploy-task-definition
-> ECS service stability wait
```

### 8-3. preflight 흐름

```text
배포 전
-> check_prod_deploy_preflight.py
-> deploy-prod-ecs.yml에서 vars.* 추출
-> 현재 repo variables와 비교
-> 필수 파일 존재 여부 확인
-> prod-deploy-preflight.md 생성
-> Ready: YES / NO
```

### 8-4. 런타임 health 흐름

```text
ALB / ECS health check
-> /actuator/health/liveness
-> /actuator/health/readiness
-> prod YAML은 readiness group을 readinessState + db + redis + ping으로 선언
-> 실제 target group probe path는 첫 배포 시 운영자가 지정
```

여기서 주의할 점이 있습니다.

- 앱은 `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`를 노출합니다
- workflow는 ECS service stability만 기다립니다
- sample task definition에는 container-level `healthCheck`가 없습니다

즉, 현재 저장소만으로는 ALB target group이 어떤 path를 probe로 쓰는지까지 자동 고정되지 않습니다.  
첫 배포 시에는 **ALB/target group health check path를 `/actuator/health/readiness`로 수동 지정**해야 합니다.

또 현재 테스트 범위는 여기까지입니다.

- prod YAML이 readiness group을 `readinessState,db,redis,ping`로 선언하는가
- `/actuator/health/*` endpoint가 열리는가

아직 여기까지는 아닙니다.

- 실제 readiness 응답이 `db`, `redis`, `ping` contributor를 어떤 payload 형태로 노출하는가
- ALB target group이 그 path를 실제로 사용해 traffic gating을 하는가

## 9. 실패 케이스 / 예외 처리

- local 편의 설정이 prod로 새면: `ddl-auto=update`, demo bootstrap, insecure cookie가 운영에 남습니다
- prod에서 Redis session을 안 쓰면: task 재기동/scale-out에서 세션이 끊깁니다
- local/test까지 Redis session을 강제하면: 개발/테스트 복잡도가 과도해집니다
- readiness가 redis를 안 보면: 세션/랭킹 의존성이 죽어도 service가 ready처럼 보입니다
- task definition sample에 secret를 environment로 넣으면: 민감 정보가 노출됩니다
- render script가 unresolved placeholder를 허용하면: 잘못된 task definition이 배포될 수 있습니다
- preflight가 없으면: AWS 연결값 누락을 deploy 도중에야 알게 됩니다
- workflow가 build 전에 test를 안 돌리면: broken image를 prod로 올릴 수 있습니다

즉, runtime 파트의 핵심 리스크는 "앱이 뜨는가"보다 **잘못된 환경 계약으로 떠 버리는가**에 가깝습니다.

## 10. 테스트로 검증하기

### 10-1. [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)

- prod YAML이 compose off / thymeleaf cache on / `ddl-auto=validate`인지
- WMSESSION cookie와 secure/same-site/http-only가 맞는지
- readiness/liveness group 구성과 `worldmap.legacy.rollback.enabled=false`가 맞는지
- datasource/redis/admin bootstrap env placeholder가 실제로 선언돼 있는지

### 10-2. [ActuatorHealthEndpointIntegrationTest.java](../src/test/java/com/worldmap/common/config/ActuatorHealthEndpointIntegrationTest.java)

- prod profile을 얹은 상태에서도 `/actuator/health`, `/liveness`, `/readiness`가 열리는지
- readiness payload의 세부 contributor 목록까지는 아직 검증하지 않음

### 10-3. [RedisSessionConfigurationIntegrationTest.java](../src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)

- `prod` profile에서 session repository가 실제 Redis-backed implementation인지

이 테스트가 보장하는 범위도 정직하게 적어야 합니다.

- 현재는 bean wiring과 profile activation만 증명합니다
- 아직 `task 2개에서 session이 실제로 유지되는가`까지 검증하는 smoke는 없습니다
- 즉 `prod에서 redis session을 쓰는 설계`는 고정하지만, `실제 scale-out smoke`는 다음 단계입니다

### 10-4. [RenderEcsTaskDefinitionScriptTest.java](../src/test/java/com/worldmap/common/config/RenderEcsTaskDefinitionScriptTest.java)

- sample task definition이 실제 AWS 값으로 render되는지
- datasource URL / redis host / secrets ARN / log group이 올바르게 치환되는지
- unresolved placeholder가 남지 않는지

### 10-5. [GitHubActionsDeployWorkflowTemplateTest.java](../src/test/java/com/worldmap/common/config/GitHubActionsDeployWorkflowTemplateTest.java)

- workflow가 `workflow_dispatch`, OIDC, Gradle test, ECR login, render script, ECS deploy step을 모두 갖는지

### 10-6. [ProdDeployPreflightScriptTest.java](../src/test/java/com/worldmap/common/config/ProdDeployPreflightScriptTest.java)

- repo variables가 비어 있으면 `Ready: NO`와 missing var 목록이 report에 남는지
- 모든 required var가 있으면 `Ready: YES`와 next action이 맞게 출력되는지
- 현재 기준 required variable count가 `13`인지
- report가 `workflow_dispatch: enabled`와 `ALB DNS` 후속 액션까지 남기는지
- 이 테스트는 live GitHub repo 상태가 아니라 fixture 기반 분기만 고정함

실행 명령:

```bash
./gradlew test \
  --tests com.worldmap.common.config.ProdProfileConfigTest \
  --tests com.worldmap.common.config.ActuatorHealthEndpointIntegrationTest \
  --tests com.worldmap.common.config.RedisSessionConfigurationIntegrationTest \
  --tests com.worldmap.common.config.RenderEcsTaskDefinitionScriptTest \
  --tests com.worldmap.common.config.GitHubActionsDeployWorkflowTemplateTest \
  --tests com.worldmap.common.config.ProdDeployPreflightScriptTest

python3 scripts/check_prod_deploy_preflight.py --repo answndud/world_map_game
```

## 11. 회고

production runtime에서 가장 중요한 것은 "멋진 AWS 서비스 이름을 많이 나열하는 것"이 아니라 **실패를 빨리 드러내는 구조**입니다.

현재 구조의 장점:

- prod-only session 전략이 명확합니다
- health contract가 설정과 테스트에 같이 남아 있습니다
- deploy input 경계가 task template / render script / workflow / preflight로 분리돼 있습니다
- preflight가 workflow-required variable 13개와 필수 파일 3개를 기준으로 `Ready: YES/NO`를 계산합니다

### 현재 구현의 한계

- 실제 AWS 리소스는 아직 연결되지 않았습니다
- preflight는 GitHub variables와 파일 존재만 검사하지, AWS 리소스 실존 여부까지 확인하지는 않습니다
- readiness는 현재 설정값과 endpoint 노출까지 고정돼 있고, contributor payload/evaluation의 end-to-end smoke는 아직 없습니다
- Terraform/CloudFormation 같은 IaC는 아직 없습니다
- 실제 public URL smoke 결과는 배포 후 채워야 합니다

즉, 지금 상태는 "production deployment completed"가 아니라 **production deployment preparation is reproducible**에 가깝습니다.

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 Docker image, prod profile, prod-only Redis session, ECS task template/render, GitHub Actions deploy, preflight report를 함께 고정해 운영 런타임 계약을 코드로 설명할 수 있게 만들었습니다.

### 12-2. 30초 답변

운영 런타임에서는 기능보다 환경 계약을 먼저 고정했습니다. `application-prod.yml`이 `ddl-auto=validate`, `WMSESSION`, readiness `db+redis+ping`, demo bootstrap off를 정의하고, `RedisSessionProdConfiguration`이 prod에서만 Redis session을 켭니다. 배포는 `Dockerfile`, ECS task definition sample, render script, GitHub Actions workflow, preflight script로 나눠 저장해 어떤 값이 빠지면 어디서 실패하는지까지 버전 관리합니다.

### 12-3. 예상 꼬리 질문

- 왜 local/test와 prod의 session backend를 다르게 두나요?
- 왜 prod profile에서 `ddl-auto=validate`를 쓰나요?
- 왜 task definition을 완성 JSON이 아니라 sample + render script로 두나요?
- 왜 preflight가 workflow 자체를 파싱하게 했나요?
- 현재 배포 준비는 어디까지 끝났고, 무엇이 아직 비어 있나요?

## 13. 시작 상태

- local 실행은 가능하지만 운영 기준선이 코드로 정리돼 있지 않은 상태
- Docker image와 prod profile이 느슨한 상태
- session externalization과 ECS deploy input 경계가 흐린 상태
- 첫 배포 전 missing variable를 자동으로 찾는 도구가 없는 상태

## 14. 이번 글에서 바뀌는 파일

- `Dockerfile`
- `.dockerignore`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-test.yml`
- `src/main/resources/application-prod.yml`
- `src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java`
- `.github/workflows/deploy-prod-ecs.yml`
- `deploy/ecs/task-definition.prod.sample.json`
- `scripts/render_ecs_task_definition.py`
- `scripts/check_prod_deploy_preflight.py`
- `src/test/java/com/worldmap/common/config/*.java`
- `docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md`

## 15. 구현 체크리스트

1. Java 25 multi-stage Docker image를 만든다
2. runtime image에서 테스트/문서/스크립트를 제외한다
3. prod profile에 datasource/redis/health/cookie/shutdown 계약을 모은다
4. local/test는 session auto-config를 제외해 servlet session을 유지한다
5. prod-only Redis session configuration을 추가한다
6. ECS task definition sample에 environment와 secrets를 나눈다
7. render script가 sample을 concrete JSON으로 치환하게 한다
8. GitHub Actions workflow에 `test -> build -> push -> render -> deploy` 순서를 고정한다
9. preflight script가 workflow vars와 필수 파일을 검사하게 한다
10. preflight 결과를 보고 실제 GitHub repo variable 13개를 채운다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.common.config.ProdProfileConfigTest \
  --tests com.worldmap.common.config.ActuatorHealthEndpointIntegrationTest \
  --tests com.worldmap.common.config.RedisSessionConfigurationIntegrationTest \
  --tests com.worldmap.common.config.RenderEcsTaskDefinitionScriptTest \
  --tests com.worldmap.common.config.GitHubActionsDeployWorkflowTemplateTest \
  --tests com.worldmap.common.config.ProdDeployPreflightScriptTest

python3 scripts/check_prod_deploy_preflight.py --repo answndud/world_map_game
```

재현을 시작하기 전 필요한 것:

- `Java 25`
- `Docker`
- `Python 3`
- `gh` 로그인 상태 또는 `--variables-json`
- AWS 리소스
  - ECR repository
  - ECS cluster / service
  - RDS PostgreSQL
  - ElastiCache Valkey/Redis
  - CloudWatch log group
  - Secrets Manager secret
  - SSM parameter
  - GitHub OIDC role

local 재현 명령 예시:

```bash
docker build -t worldmap-prod-local .
python3 scripts/check_prod_deploy_preflight.py --repo answndud/world_map_game
python3 scripts/render_ecs_task_definition.py \
  --input deploy/ecs/task-definition.prod.sample.json \
  --output deploy/ecs/task-definition.rendered.json
```

이후 실제 첫 배포 순서:

1. GitHub repository variables 13개 채우기
2. preflight에서 `Ready: YES` 만들기
3. `deploy-prod-ecs` workflow 수동 실행
4. ECS/ALB가 stable이 되면 ALB DNS를 첫 public URL로 사용
5. `WORLDMAP_PUBLIC_BASE_URL=... ./gradlew publicUrlSmokeTest` 실행

## 17. 산출물 체크리스트

- prod runtime 기준이 local/test와 분리돼 있다
- prod에서만 Redis-backed session이 켜진다
- readiness/liveness contract가 actuator endpoint로 노출된다
- ECS task definition sample과 render script가 concrete JSON을 만들 수 있다
- deploy workflow가 test부터 deploy까지 순서를 강제한다
- preflight report가 workflow-required variable과 missing 입력을 먼저 알려 준다

## 18. 글 종료 체크포인트

- 왜 운영 런타임은 기능 코드와 다른 층의 문제인가
- 왜 local/test와 prod는 session 전략이 달라야 하는가
- 왜 prod profile은 `validate`, secure cookie, graceful shutdown, readiness group을 함께 가져야 하는가
- 왜 task definition sample과 render script를 분리했는가
- 왜 preflight는 현재 workflow를 파싱해야 하는가
- 지금 저장소 기준으로 첫 배포가 아직 막히는 이유는 무엇인가

## 19. 자주 막히는 지점

- local 편의 설정을 prod에도 그대로 가져가는 것
- prod session externalization을 "나중에"로 미루는 것
- health endpoint를 열어도 readiness가 redis/db를 안 보게 두는 것
- task definition에 secret를 평문 env로 넣는 것
- deploy workflow 입력을 사람 기억에만 의존하는 것
- preflight가 missing input을 보여 주는데도 배포부터 눌러 보는 것
