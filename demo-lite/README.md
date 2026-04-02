# demo-lite

`demo-lite`는 풀 기능 Spring Boot 앱을 대체하는 것이 아니라, 무료 공개용으로 줄인 sibling 앱입니다.

현재는 아래까지 실제로 열려 있습니다.

- 전용 header/navigation
- retained route map
- 메인 저장소 정적 데이터/국기 자산을 `public/generated`로 복사해 읽는 shared data adapter
- `#/games/capital` local-state 수도 맞히기 한 판
- `#/games/flag` local-state 국기 퀴즈 한 판
- `#/games/population-battle` local-state 인구 비교 배틀 한 판
- `#/recommendation` 20문항 local-state 추천 결과 loop
- `#/` home에서 browser recent play / cross-mode summary
- `#/` home에서 recent streak / copyable one-line summary

현재 공개 URL:

- [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)

현재 공개 URL은 수동 `wrangler pages deploy` 기준입니다. 다만 가장 최근 production alias는 clean repo commit `5356fde` 기준으로 다시 맞췄습니다. 아직 Git-connected 자동 배포 source of truth는 아니므로, 다음 단계는 이 상태를 `main` 기준 auto deploy 흐름으로 넘기는 것입니다.

아직 없는 것:

- feedback 저장과 ops review
- `engine-v20` combo bonus 100% parity

## 실행

```bash
cd demo-lite
npm install
npm run dev
```

첫 실행 전 `npm run sync:shared`를 따로 칠 필요는 없습니다. `dev`, `build` 스크립트가 공통으로 메인 저장소의 공유 자산을 `public/generated/`로 복사합니다.

`.npmrc`의 `workspaces=false`는 현재 로컬 npm 환경에서 상위 저장소를 workspace처럼 해석하지 않도록 하기 위한 local guard입니다.

## 빌드 확인

```bash
cd demo-lite
npm run build
npm run verify:pages
```

빌드 흐름:

1. `scripts/sync-shared-assets.mjs`가 메인 저장소의 `countries.json`, `flag-assets.json`, `flags/*`를 `public/generated/`로 복사
2. `scripts/check-shared-assets.mjs`가 generated 자산 수와 manifest 일관성 확인
3. Vite가 static 산출물 `dist/` 생성

`npm run verify:pages`는 아래를 확인합니다.

1. `package.json` build 스크립트에 `sync:shared`, `verify:shared`, `vite build`가 모두 있는지
2. `.node-version`이 concrete Node 버전으로 고정돼 있는지
3. `public/_headers`에 기본 보안 헤더와 캐시 규칙이 들어 있는지

## Cloudflare Pages 기준 배포 값

현재 `demo-lite`는 Git-connected Cloudflare Pages 기준 baseline을 같이 갖고 있습니다.

- Root directory: `demo-lite`
- Build command: `npm run build`
- Build output directory: `dist`
- Node version: [.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)
- Static headers: [public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)

중요:

- 현재 route는 `hash route`이므로 `_redirects`를 따로 두지 않습니다.
- Git-connected Pages 정적 배포 기준으로는 `wrangler.toml`도 아직 필요 없습니다.
- 자세한 클릭 순서는 [DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)를 봅니다.

## 현재 route

- `#/`
- `#/games/capital`
- `#/games/flag`
- `#/games/population-battle`
- `#/recommendation`

현재 playable route:

- `#/games/capital`
- `#/games/flag`
- `#/games/population-battle`
- `#/recommendation`

## shared source

현재는 메인 앱 runtime을 재사용하지 않고 아래 정적 source만 build-time에 복사해 사용합니다.

- `../src/main/resources/data/countries.json`
- `../src/main/resources/data/flag-assets.json`
- `../src/main/resources/static/images/flags/*`

브라우저는 이 원본 경로를 직접 읽지 않습니다. [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)는 `public/generated/data/*.json`을 fetch하고, 게임 화면도 같은 generated 자산을 기준으로 확장할 예정입니다.

즉 `demo-lite`는 "메인 앱의 profile 하나"가 아니라, 같은 저장소 안의 별도 공개 앱입니다.
