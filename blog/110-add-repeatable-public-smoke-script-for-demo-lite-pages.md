# demo-lite 공개 URL smoke를 저장소 스크립트로 고정하기

`demo-lite`는 이미 [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/) 로 공개되어 있지만, 배포 직후마다 `curl`, 브라우저, 스크린샷을 손으로 다시 확인하면 재현성이 약합니다. 이번 조각의 목적은 **Cloudflare Pages 공개 URL이 정상인지 저장소 안의 명령 하나로 다시 확인할 수 있게 만드는 것**입니다.

핵심 파일은 아래입니다.

- [demo-lite/scripts/smoke-public-url.mjs](/Users/alex/project/worldmap/demo-lite/scripts/smoke-public-url.mjs)
- [demo-lite/tests/public-smoke-script.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/public-smoke-script.test.mjs)
- [demo-lite/package.json](/Users/alex/project/worldmap/demo-lite/package.json)
- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)

## 왜 이 조각이 필요한가

정적 앱 배포에서는 "URL이 열렸다"와 "운영 계약이 유지되고 있다"가 다릅니다.

이번 `demo-lite`는 아래 계약이 같이 살아 있어야 정상입니다.

- root HTML이 정상 반환될 것
- Vite가 만든 `/assets/*` 파일이 실제로 존재할 것
- `countries.json`, `flag-assets.json` generated data가 실제로 열릴 것
- 대표 flag SVG도 실제 응답할 것
- `_headers`에 적은 보안/캐시 헤더가 production 응답에 붙을 것

즉 이번 작업은 새로운 기능 추가보다, **공개 배포 상태를 반복 검증 가능한 release rail로 바꾸는 조각**에 가깝습니다.

## 요청 흐름

이번 조각은 애플리케이션 런타임이 아니라 release 검증 흐름입니다.

1. 배포 후 아래 명령을 실행합니다.

```bash
cd demo-lite
npm run smoke:public -- https://worldmap-demo-lite.pages.dev
```

2. `package.json`이 [smoke-public-url.mjs](/Users/alex/project/worldmap/demo-lite/scripts/smoke-public-url.mjs)를 실행합니다.
3. 스크립트는 아래 순서로 공개 URL을 다시 읽습니다.
   - `/`
   - root HTML이 참조하는 `/assets/*`
   - `/generated/data/countries.json`
   - `/generated/data/flag-assets.json`
   - representative `/generated/flags/*.svg`
4. 마지막으로 root 응답과 flag 응답에서 핵심 헤더를 다시 확인합니다.

중요한 점은, 이 검증이 Pages 대시보드가 아니라 **저장소 안의 repeatable command**로 남는다는 것입니다.

## 왜 이 로직이 라우터나 feature 코드가 아니라 별도 스크립트여야 하나

`demo-lite`의 feature 코드는 브라우저 안에서 게임/추천을 돌리는 책임만 져야 합니다.

이번 smoke는:

- 배포된 URL을 외부에서 다시 읽고
- asset chain과 header contract를 보고
- release 상태를 판정하는 일

이므로 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 나 게임 feature에 들어가면 책임이 섞입니다.

그래서 이 조각은 **browser runtime이 아니라 release tooling** 으로 분리한 것이 맞습니다.

## 핵심 도메인 개념

### 1. public smoke script

공개 URL이 정상인지 저장소 명령 하나로 반복 검증하는 스크립트입니다.

### 2. asset-chain verification

HTML만 `200`이면 끝내지 않고, 실제로 그 HTML이 가리키는 `/assets/*` 와 generated data/flag까지 따라가서 확인합니다.

### 3. header contract

`public/_headers`에 적은 `Cache-Control`, `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`가 production 응답에 실제로 붙어 있는지 봅니다.

### 4. static release rail

정적 앱에서도 release 이후 확인해야 하는 최소 기준을 코드화한 검증 레일입니다.

## 테스트

이번 조각에서는 아래를 확인합니다.

```bash
cd demo-lite
npm test
npm run build
npm run smoke:public -- https://worldmap-demo-lite.pages.dev
```

추가로 루트 저장소에서는 아래를 확인합니다.

```bash
git diff --check
```

[public-smoke-script.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/public-smoke-script.test.mjs) 는 아래를 고정합니다.

- base URL 정규화
- root HTML에서 hashed asset path 추출
- header fragment 판정

즉 네트워크 smoke 자체와, smoke 스크립트의 작은 parser/helper 규칙을 분리해 검증합니다.

## 한 줄 요약

이번 조각으로 `demo-lite`는 **Cloudflare Pages 공개 URL을 사람 손검사 대신 저장소 명령으로 다시 확인할 수 있는 상태**가 되었습니다. 핵심은 정적 앱 배포를 "한 번 올려봤다"에서 끝내지 않고, asset/data/header 계약까지 포함한 반복 가능한 smoke rail로 바꾼 점입니다.
