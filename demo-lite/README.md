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

중요:

- 현재 `worldmap-demo-lite`는 Direct Upload 프로젝트입니다.
- 따라서 Git integration으로 넘기려면 기존 프로젝트를 전환하는 것이 아니라, 같은 저장소를 바라보는 새 Git-connected Pages 프로젝트를 만들어야 합니다.
- 저장소 쪽 준비는 [demo-lite-verify.yml](/Users/alex/project/worldmap/.github/workflows/demo-lite-verify.yml)에서 먼저 닫습니다.

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
npm run inspect:pages-git
npm run smoke:public -- https://worldmap-demo-lite.pages.dev
```

빌드 흐름:

1. `scripts/sync-shared-assets.mjs`가 메인 저장소의 `countries.json`, `flag-assets.json`, `flags/*`를 `public/generated/`로 복사
2. `scripts/check-shared-assets.mjs`가 generated 자산 수와 manifest 일관성 확인
3. Vite가 static 산출물 `dist/` 생성

`npm run verify:pages`는 아래를 확인합니다.

1. `package.json` build 스크립트에 `sync:shared`, `verify:shared`, `vite build`가 모두 있는지
2. `.node-version`이 concrete Node 버전으로 고정돼 있는지
3. `public/_headers`에 기본 보안 헤더와 캐시 규칙이 들어 있는지

`npm run smoke:public`은 실제 공개 URL에 대해 아래를 확인합니다.

1. `/`가 `200`이고 root HTML에 `WorldMap Demo-Lite`가 들어 있는지
2. root HTML이 참조하는 `/assets/*` 정적 파일이 실제로 열리는지
3. `/generated/data/countries.json`이 194개 국가와 `KOR`의 `capitalCityKr`를 포함하는지
4. `/generated/data/flag-assets.json`과 대표 `/generated/flags/*.svg`가 실제로 열리는지
5. `Cache-Control`, `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`가 production 응답에 붙어 있는지

`npm run inspect:pages-git`은 현재 Cloudflare Pages 프로젝트 상태를 읽어 아래를 요약합니다.

1. 현재 프로젝트가 Direct Upload인지 Git-connected인지
2. 현재 로컬 브랜치가 planned production branch와 다른지
3. working tree가 dirty한지
4. 다음 handoff step이 무엇인지

`npm run inspect:pages-git`은 아래를 확인합니다.

1. 현재 Cloudflare Pages 프로젝트가 `Git Provider: No` 인 direct-upload 상태인지
2. 현재 작업 브랜치가 planned production branch와 맞는지
3. working tree가 handoff 가능한 clean 상태인지
4. Git-connected auto deploy로 넘길 때 다음에 눌러야 할 수동 단계가 무엇인지

GitHub Actions 기준으로도 아래가 준비돼 있습니다.

- [demo-lite-verify.yml](/Users/alex/project/worldmap/.github/workflows/demo-lite-verify.yml)
- push / pull request에서 `npm test`, `npm run build`, `npm run verify:pages`
- `workflow_dispatch`에서 public URL을 넣어 `npm run smoke:public` 수동 실행

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
- 현재 `worldmap-demo-lite` 프로젝트는 direct-upload 상태입니다. 기존 프로젝트를 Git integration으로 바꾸는 대신, 새 Git-connected Pages 프로젝트를 만들어 handoff해야 합니다.

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
