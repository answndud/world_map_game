# demo-lite Git-connected Pages handoff를 위한 verify workflow 추가

지금 `demo-lite`는 이미 [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/) 로 공개되어 있습니다. 하지만 이 URL은 **Direct Upload 프로젝트**로 먼저 연 상태입니다.

즉 다음 단계인 “Git-connected Pages auto deploy”는 단순히 기존 프로젝트 옵션을 바꾸는 문제가 아닙니다. 실제로는 **같은 repo/demo-lite를 source로 쓰는 새 Git-connected Pages 프로젝트**를 따로 만들고 넘겨야 합니다.

이번 조각의 목적은 그 전환 전에,
저장소 쪽에서 `main` branch가 정말 Pages source of truth가 될 준비가 되어 있는지
먼저 CI로 고정하는 것이었습니다.

핵심 파일은 아래입니다.

- [inspect-pages-git-handoff.mjs](/Users/alex/project/worldmap/demo-lite/scripts/inspect-pages-git-handoff.mjs)
- [demo-lite-verify.yml](/Users/alex/project/worldmap/.github/workflows/demo-lite-verify.yml)
- [DemoLiteVerifyWorkflowTemplateTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/DemoLiteVerifyWorkflowTemplateTest.java)
- [DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)

## 왜 이 조각이 필요한가

현재 상태는 이렇게 나뉩니다.

- public URL은 이미 있음
- 공개 URL smoke도 저장소 명령으로 가능함
- 하지만 GitHub commit이 자동으로 Pages production으로 이어지지는 않음

이 상태에서 바로 “이제 auto deploy로 바꾸자”라고 하면,
대시보드 클릭은 남아 있는데
정작 저장소 쪽에서 branch readiness를 자동으로 확인하는 기준이 없습니다.

그래서 이번 조각은 Cloudflare 대시보드 작업보다 먼저,
**repo/main이 static hosting source of truth로 안전한지 보는 CI 레일**
을 만드는 데 초점을 맞췄습니다.

## 이번에 추가한 workflow

[demo-lite-verify.yml](/Users/alex/project/worldmap/.github/workflows/demo-lite-verify.yml)은 아래를 합니다.

### 1. push / pull request 기본 lane

`demo-lite/**`가 바뀌면 아래를 자동 실행합니다.

1. `npm ci`
2. `npm test`
3. `npm run build`
4. `npm run verify:pages`

즉 `demo-lite`가 브라우저 게임 로직, shared asset sync, Cloudflare Pages baseline을 계속 만족하는지 먼저 봅니다.

### 2. workflow_dispatch 수동 lane

수동 실행에서는 `public_base_url`을 넣을 수 있게 했습니다.

그러면 아래가 추가로 실행됩니다.

```bash
npm run smoke:public -- <public-url>
```

즉 배포 후에는 GitHub Actions에서도
실제 production URL에 대해 root HTML, `/assets/*`, generated JSON, 대표 SVG, 핵심 헤더를
다시 확인할 수 있습니다.

### 3. 로컬 handoff inspection

이번 조각에서는 workflow만 추가한 것이 아니라,
현재 Pages 프로젝트 상태를 바로 읽는 로컬 명령도 같이 고정했습니다.

```bash
cd demo-lite
npm run inspect:pages-git
```

이 명령은 아래를 바로 보여 줍니다.

- 현재 Pages 프로젝트의 `Git Provider`
- 현재 로컬 브랜치
- working tree dirty 여부
- 다음 handoff step

즉 “왜 지금 바로 Git auto deploy로 못 넘기는가”를
저장소 안에서 재현 가능한 출력으로 남긴 셈입니다.

## 왜 이 로직이 workflow여야 하나

이 조각은 앱 feature가 아닙니다.

수도 게임, 국기 게임, 추천 로직은 브라우저에서 돌아가야 하고,
Pages build baseline은 정적 호스팅 계약입니다.

이번 verify lane은:

- 어떤 branch를 배포 source로 볼 것인가
- static build가 계속 되는가
- public smoke를 언제/어떻게 다시 돌릴 것인가

를 정하는 **release rule** 입니다.

그래서 controller/service가 아니라
GitHub Actions workflow에 있어야 맞습니다.

## 이번에 같이 문서화한 기준

[DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)에는 아래를 명시했습니다.

- 현재 `worldmap-demo-lite`는 Direct Upload 프로젝트다
- Git integration으로 넘기려면 기존 프로젝트를 바꾸는 것이 아니라 새 Git-connected Pages 프로젝트를 만들어야 한다
- 그 전에 저장소 쪽에서는 `demo-lite-verify` workflow로 branch readiness를 먼저 닫아야 한다

즉 이번 조각은 “Git-connected Pages 전환 완료”가 아니라,
**전환을 위한 repo-side handoff contract를 먼저 닫는 조각**입니다.

## 테스트

이번 조각은 아래로 검증했습니다.

```bash
./gradlew test --tests com.worldmap.common.config.DemoLiteVerifyWorkflowTemplateTest
cd demo-lite && npm test
cd demo-lite && npm run build
cd demo-lite && npm run verify:pages
cd demo-lite && npm run smoke:public -- https://worldmap-demo-lite.pages.dev
```

즉 workflow 템플릿 자체와,
그 workflow가 가리키는 정적 앱 검증 명령 둘 다 같이 확인했습니다.

## 한 줄 요약

이번 조각으로 `demo-lite`는 Direct Upload Pages 상태에서 바로 Git 전환을 시도하는 대신, 먼저 `main` branch가 정적 배포 source of truth로 안전한지 GitHub Actions verify lane으로 증명할 수 있게 됐습니다. 핵심은 대시보드 클릭보다 먼저 **저장소 쪽 release contract를 CI로 고정**한 점입니다.
