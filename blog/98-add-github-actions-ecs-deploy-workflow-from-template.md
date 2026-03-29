# GitHub Actions에서 sample task definition을 렌더링해 ECS에 배포하기

## 왜 이 조각이 필요한가

지금까지 저장소에는 아래 준비물이 있었다.

- `Dockerfile`
- `application-prod.yml`
- actuator health probe
- `task-definition.prod.sample.json`
- prod 전용 Spring Session Redis

하지만 이 상태만으로는 초보자가 “그래서 main 브랜치 코드를 어떻게 ECR에 올리고 ECS에 배포하지?”를 바로 따라가기 어렵다.

이번 조각의 목적은 배포 자동화를 “문서 설명”이 아니라 `저장소 안의 실제 workflow 파일`로 고정하는 것이다.

핵심은 두 가지다.

1. GitHub Actions가 `OIDC -> ECR push -> ECS deploy`를 실제로 수행한다.
2. sample task definition을 그대로 쓰지 않고, CI에서 `실제 AWS 값으로 렌더링`한다.

## 이번에 바뀐 파일

- `.github/workflows/deploy-prod-ecs.yml`
- `scripts/render_ecs_task_definition.py`
- `src/test/java/com/worldmap/common/config/GitHubActionsDeployWorkflowTemplateTest.java`
- `src/test/java/com/worldmap/common/config/RenderEcsTaskDefinitionScriptTest.java`

## 요청 흐름이 아니라 배포 흐름을 고정했다

이 조각은 게임 요청을 바꾸는 기능이 아니다.

새로 생긴 흐름은 아래다.

1. GitHub Actions 수동 실행 (`workflow_dispatch`)
2. `./gradlew test`
3. Docker image build
4. ECR push
5. `task-definition.prod.sample.json` 렌더링
6. ECS service deploy
7. ECS service stabilization wait

즉 이번 조각의 source of truth는 컨트롤러가 아니라 `workflow yaml + 렌더링 스크립트`다.

## 왜 sample task definition을 바로 배포하지 않았나

`task-definition.prod.sample.json`은 어디까지나 설명용 template다.

여기에는 아래 값들이 아직 placeholder 성격으로 남아 있다.

- RDS endpoint
- ElastiCache endpoint
- execution role ARN
- task role ARN
- secret ARN
- image URI

이걸 사람이 매번 수동 수정하면 초보자 입장에서 실수하기 쉽다.

그래서 이번에는 `scripts/render_ecs_task_definition.py`가 아래 책임을 맡는다.

- sample JSON 읽기
- GitHub Actions env 값으로 필요한 필드 치환
- placeholder가 남아 있으면 실패
- `task-definition.rendered.json` 생성

즉 “설명 가능한 sample”과 “실제로 deploy 가능한 JSON”을 분리한 셈이다.

## 왜 GitHub Actions를 수동 실행으로 시작했나

처음부터 `main` push마다 production deploy를 열 수도 있다.

하지만 지금 단계에서는 그보다 아래가 더 중요하다.

- AWS account / role / variable이 실제로 맞는지 확인
- ECS 수동 첫 배포와 smoke test를 먼저 성공
- session / cookie / readiness 동작 검증

그래서 workflow는 일단 `workflow_dispatch`만 열었다.

이렇게 하면 면접에서도 설명이 쉽다.

> production 자동 배포는 가능한 상태까지 만들었지만, 실제 운영 안정화 전에는 수동 trigger로 제한해 두었습니다.

## 테스트는 무엇으로 닫았나

두 가지를 고정했다.

### 1. workflow template test

`GitHubActionsDeployWorkflowTemplateTest`에서 아래가 실제로 들어 있는지 확인했다.

- `workflow_dispatch`
- `id-token: write`
- `./gradlew test`
- `aws-actions/configure-aws-credentials`
- `amazon-ecr-login`
- `render_ecs_task_definition.py`
- `amazon-ecs-deploy-task-definition`

즉 “배포 workflow가 있어야 하는 핵심 단계”가 빠지지 않게 막았다.

### 2. render script test

`RenderEcsTaskDefinitionScriptTest`는 Python 스크립트를 실제로 실행해서 아래를 검증한다.

- sample task definition이 실제 값으로 렌더링되는지
- image URI가 들어가는지
- RDS / Redis host가 치환되는지
- secrets ARN이 `valueFrom`에 들어가는지
- `<ACCOUNT_ID>`, `<REGION>` 같은 placeholder가 결과 파일에 남지 않는지

즉 단순 텍스트 치환이 아니라, deploy 가능한 JSON이 나오는지까지 확인한 셈이다.

## 지금 남아 있는 것

아직 실제 AWS에 workflow를 붙인 것은 아니다.

남은 일은 아래다.

1. GitHub repository variables 채우기
2. GitHub OIDC role 만들기
3. ECS 수동 첫 배포 성공
4. 그 다음 workflow로 실제 deploy 실행

즉 이번 조각은 “배포 자동화의 코드 뼈대”를 닫은 단계다.

## 면접에서 30초로 설명하면

이번에는 ECS 배포 자동화를 저장소에 실제 workflow로 추가했습니다. 핵심은 GitHub Actions가 `OIDC 인증 -> 테스트 -> Docker build/push -> sample task definition 렌더링 -> ECS deploy`까지 한 번에 수행하도록 만든 점입니다. 특히 sample JSON을 바로 배포하지 않고, `render_ecs_task_definition.py`로 실제 AWS 값과 image URI를 주입해 rendered task definition을 만드는 구조로 나눠서 초보자도 설명 가능하고 재현 가능하게 정리했습니다.
