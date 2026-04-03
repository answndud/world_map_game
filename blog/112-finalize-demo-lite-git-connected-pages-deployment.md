# demo-lite Git-connected Pages 배포 완료 상태를 문서와 도구 기본값에 반영하기

`demo-lite`는 이제 handoff 준비 단계가 아닙니다. 실제로 **Git-connected Cloudflare Pages 프로젝트**에서 운영 중입니다.

이번 조각의 목적은 단순히 “배포됐다”를 한 줄 적는 것이 아니라,
저장소 안의 기본값과 문서를 모두 **새 운영 source of truth** 에 맞추는 것이었습니다.

핵심 파일은 아래입니다.

- [demo-lite/scripts/smoke-public-url.mjs](/Users/alex/project/worldmap/demo-lite/scripts/smoke-public-url.mjs)
- [demo-lite/scripts/inspect-pages-git-handoff.mjs](/Users/alex/project/worldmap/demo-lite/scripts/inspect-pages-git-handoff.mjs)
- [README.md](/Users/alex/project/worldmap/README.md)
- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)

## 지금 무엇이 바뀌었나

현재 운영 기준은 아래입니다.

- 운영 Pages 프로젝트: `world-map-game-demo-lite-git`
- 운영 URL: [https://world-map-game-demo-lite-git.pages.dev/](https://world-map-game-demo-lite-git.pages.dev/)
- production branch: `main`
- custom domain: 사용 안 함

반대로 이전 URL인
[https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)
는 이제 **legacy direct-upload backup** 으로만 봅니다.

즉 이번 조각의 본질은
“Git-connected Pages로 배포할 수 있다”가 아니라
**“이제 무엇을 운영 기준으로 볼 것인가를 저장소 기본값까지 포함해 바꿨다”**
는 것입니다.

## 왜 기본값까지 바꿔야 하나

운영 완료 후에도 아래가 예전 값을 가리키면 문제가 남습니다.

- README 예시 URL
- 공개 smoke 기본 URL
- Pages project inspection 기본 대상
- 런북의 현재 운영 상태 설명

이런 값들이 아직 예전 direct-upload 프로젝트를 가리키면,
사용자도 AI도 계속 오래된 운영 기준을 따라가게 됩니다.

그래서 이번엔 단순 문서 수정이 아니라,
**실제 운영 기본값 자체를 새 Git-connected 프로젝트로 전환** 했습니다.

## 이번에 바꾼 기본값

### 1. public smoke 기본 URL

[smoke-public-url.mjs](/Users/alex/project/worldmap/demo-lite/scripts/smoke-public-url.mjs)의 기본 URL을

- 기존: `worldmap-demo-lite.pages.dev`
- 변경: `world-map-game-demo-lite-git.pages.dev`

로 바꿨습니다.

이제 별도 인자를 주지 않아도 현재 운영 URL 기준으로 smoke를 돌립니다.

### 2. Pages inspection 기본 프로젝트

[inspect-pages-git-handoff.mjs](/Users/alex/project/worldmap/demo-lite/scripts/inspect-pages-git-handoff.mjs)의 기본 프로젝트명도

- 기존: `worldmap-demo-lite`
- 변경: `world-map-game-demo-lite-git`

으로 바꿨습니다.

즉 `npm run inspect:pages-git`는 이제 기본적으로 **운영 기준 프로젝트**를 읽습니다.

legacy direct-upload 프로젝트를 다시 보고 싶을 때만 environment variable로 override하면 됩니다.

예:

```bash
cd demo-lite
DEMO_LITE_PAGES_PROJECT_NAME=worldmap-demo-lite npm run inspect:pages-git
```

## 지금 요청 흐름 대신 운영 흐름으로 설명하면

이번 조각은 앱 요청 흐름이 아니라 운영 흐름입니다.

1. `main`에 `demo-lite` 변경이 반영됩니다.
2. Git-connected Cloudflare Pages 프로젝트가 자동으로 build/deploy 합니다.
3. 필요하면 아래로 운영 상태를 다시 확인합니다.

```bash
cd demo-lite
npm run inspect:pages-git
npm run smoke:public -- https://world-map-game-demo-lite-git.pages.dev
```

즉 지금부터는 `wrangler pages deploy`가 기본 경로가 아니라,
**GitHub -> main -> Pages auto deploy** 가 기본 경로입니다.

## 검증

이번 조각에서는 아래를 확인했습니다.

```bash
cd demo-lite
DEMO_LITE_PAGES_PROJECT_NAME=world-map-game-demo-lite-git npm run inspect:pages-git
npm run smoke:public -- https://world-map-game-demo-lite-git.pages.dev
```

여기서 확인한 핵심은 아래입니다.

- `Git Provider: Yes`
- 운영 URL에서 root HTML, `/assets/*`, generated data, flag SVG 정상 응답

## 한 줄 요약

이번 조각으로 `demo-lite`는 handoff 준비가 아니라 **Git-connected Pages 운영 완료 상태**가 되었고, 저장소 안의 기본 URL/기본 프로젝트 기준도 모두 그 상태에 맞게 바꿨습니다. 핵심은 “배포됐다”를 넘어서 **무엇이 실제 운영 source of truth인지 코드와 문서의 기본값까지 일치시킨 것**입니다.
