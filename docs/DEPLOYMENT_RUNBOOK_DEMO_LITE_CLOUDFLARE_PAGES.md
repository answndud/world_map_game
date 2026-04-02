# demo-lite Cloudflare Pages 배포 런북

최종 업데이트: 2026-04-02

## 1. 목적

이 문서는 [demo-lite](/Users/alex/project/worldmap/demo-lite) 정적 앱을 **Cloudflare Pages 한 플랫폼만으로** 공개하기 위한 실행 문서다.

중요:

- 이 문서는 full Spring Boot 앱이 아니라 `demo-lite` 전용이다.
- 대상은 `hash route + localStorage + generated static assets` 구조의 정적 앱이다.
- GitHub 저장소 연결형 Pages 배포를 기준으로 한다.

## 2. 왜 Pages baseline을 따로 두나

현재 `demo-lite`는 서버 없이 아래만으로 동작한다.

- Vite build
- `public/generated/*` 정적 자산
- browser localStorage
- hash route

즉 필요한 것은 “백엔드 런타임”이 아니라
**정적 파일을 안정적으로 build하고 전달하는 hosting 계약**이다.

그래서 지금 저장소에 고정한 source of truth는 아래다.

- [demo-lite/package.json](/Users/alex/project/worldmap/demo-lite/package.json)
- [demo-lite/.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)
- [demo-lite/public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)
- [demo-lite/scripts/check-cloudflare-pages-baseline.mjs](/Users/alex/project/worldmap/demo-lite/scripts/check-cloudflare-pages-baseline.mjs)
- [demo-lite/vite.config.mjs](/Users/alex/project/worldmap/demo-lite/vite.config.mjs)

## 3. 현재 저장소 기준 build 계약

Cloudflare Pages에서 넣어야 하는 값은 아래다.

- Root directory: `demo-lite`
- Build command: `npm run build`
- Build output directory: `dist`

`npm run build`는 아래를 한 번에 수행한다.

1. `scripts/sync-shared-assets.mjs`
2. `scripts/check-shared-assets.mjs`
3. `vite build`

즉 Pages는 단순히 `vite build`만 하는 것이 아니라,
메인 저장소의 국가/국기 데이터를 먼저 `public/generated/`로 복사한 뒤 static output을 만든다.

## 4. Node 버전은 어디서 고정하나

Cloudflare Pages build image는 `.node-version` 또는 `NODE_VERSION`을 읽을 수 있다.

현재 저장소는 [demo-lite/.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)으로 Node를 고정한다.

원칙:

- 먼저 `.node-version`을 source of truth로 둔다.
- Pages dashboard에서 별도 `NODE_VERSION` env는 기본적으로 넣지 않는다.
- 나중에 Pages build image가 바뀌거나 팀 합의가 생기면 그때만 env override를 고려한다.

## 5. `_headers`는 왜 필요하나

[demo-lite/public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)는 배포 산출물에 같이 포함돼 아래를 고정한다.

- `/` 포함 일반 문서: `must-revalidate`
- `/assets/*`: immutable 장기 캐시
- `/generated/flags/*`: immutable 장기 캐시
- `/generated/data/*`: 짧은 재검증 캐시
- 기본 보안 헤더:
  - `X-Content-Type-Options`
  - `X-Frame-Options`
  - `Referrer-Policy`
  - `Permissions-Policy`
  - `Cross-Origin-Opener-Policy`
  - `Content-Security-Policy`

즉 이번 baseline의 핵심은 “Pages에 올릴 수 있다”보다
**올린 뒤 어떤 캐시/보안 계약으로 응답할 것인가를 저장소에 남기는 것**이다.

## 6. `_redirects`를 두지 않는 이유

현재 `demo-lite`는 path route가 아니라 `hash route`를 쓴다.

실제 URL path는 항상 `/`이고, 라우팅은 `#/games/capital` 같은 fragment에서만 일어난다.

그래서 현재 baseline에서는 `_redirects`를 추가하지 않는다.

이 판단이 맞는 이유:

- direct path rewrite가 필요 없다
- Pages 설정을 더 단순하게 유지할 수 있다
- route 문제를 hosting보다 app shell 안에서 통제할 수 있다

## 7. `wrangler.toml`을 아직 두지 않는 이유

이번 baseline은 Git-connected Pages 정적 배포다.

즉 지금 필요한 것은:

- GitHub repo 선택
- root directory 지정
- build command 지정
- output directory 지정

정도다.

이 단계에서는 `wrangler.toml`까지 넣으면 오히려 초보자에게 “CLI deploy가 필수인가?”라는 혼선을 줄 수 있다.

따라서 현재 기준:

- Git-connected Pages: `wrangler.toml` 불필요
- 나중에 Workers static assets나 CLI deploy로 전환할 때만 재검토

## 8. Cloudflare Pages에서 실제로 누를 것

### 8.1 프로젝트 생성

1. Cloudflare 로그인
2. `Workers & Pages`
3. `Create`
4. `Pages`
5. `Connect to Git`
6. GitHub 저장소 선택

### 8.2 Build 설정

아래처럼 입력한다.

- Production branch: 배포에 쓸 브랜치
- Root directory: `demo-lite`
- Build command: `npm run build`
- Build output directory: `dist`

나머지는 기본값으로 둔다.

### 8.3 Environment variables

현재 baseline에서는 필수 env가 없다.

이유:

- 데이터는 build 시 메인 저장소 파일을 복사한다
- runtime API 호출이 없다
- Node 버전은 `.node-version`으로 고정한다

즉 dashboard에서 꼭 넣어야 하는 secret이나 runtime env는 현재 없다.

## 9. 첫 배포 후 smoke test

배포가 끝나면 아래만 먼저 확인한다.

1. `/`
2. `/#/games/capital`
3. `/#/games/flag`
4. `/#/games/population-battle`
5. `/#/recommendation`

그리고 실제 기능으로는 아래를 본다.

1. 수도 한 판 종료 후 홈 recent summary 반영
2. 국기 한 판 종료 후 홈 recent streak 반영
3. 추천 결과 계산 후 홈 recent recommendation 반영
4. 홈 `한 줄 요약 복사` 버튼 동작

현재 첫 공개 URL:

- Production URL: [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)
- Preview alias example: `https://codex-security-session-guard.worldmap-demo-lite.pages.dev/`

2026-04-02 첫 수동 배포에서 확인한 것:

- `/` -> `200`
- `/generated/data/countries.json` -> `200`
- `/assets/*` -> `200`
- `Cache-Control`, `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`가 `_headers` 기준으로 실제 응답에 포함됨
- Chrome channel screenshot smoke로 `/`, `/#/games/capital`, `/#/recommendation` 렌더링 확인

중요:

- 현재 production URL은 `wrangler pages deploy dist --project-name worldmap-demo-lite --branch main`으로 먼저 연 상태다.
- 이후 clean repo commit `5356fde` 기준으로 같은 명령을 다시 실행해 production alias를 dirty working tree 상태에서 벗겨 냈다.
- 다만 Git-connected Pages production source가 아직 branch commit과 자동으로 연결된 상태는 아니다.
- 다음 단계는 이 수동 production 상태를 `main` 기준 auto deploy source of truth로 넘기는 것이다.

## 10. 저장소 안에서 먼저 확인할 명령

Pages에 올리기 전 아래를 로컬에서 통과시키면 된다.

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
```

`npm run verify:pages`는 아래를 다시 확인한다.

1. build 스크립트가 `sync:shared -> verify:shared -> vite build` 순서를 유지하는지
2. `.node-version`이 concrete 버전으로 pin되어 있는지
3. `public/_headers`에 security/cache 규칙이 빠지지 않았는지

추가로 저장소 루트에서는 아래를 확인한다.

```bash
git diff --check
```

## 11. 지금 상태에서 아직 안 한 것

이번 baseline은 “Pages에서 바로 빌드할 수 있는가”를 닫은 것이다.

아직 안 한 것은 아래다.

- 실제 Cloudflare Pages 프로젝트 생성
- 첫 공개 URL 생성
- 공개 URL 기준 smoke test 기록
- custom domain 연결

즉 현재 상태는 **배포 완료가 아니라 Pages-ready**에 가깝다.

## 12. 한 줄 결론

현재 `demo-lite`는 Cloudflare Pages에서 `root directory = demo-lite`, `build = npm run build`, `output = dist`로 바로 올릴 수 있는 baseline을 갖췄다. 그리고 Node 버전, 캐시 규칙, 기본 보안 헤더는 각각 [.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)과 [public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)로 저장소 안에 고정돼 있다.
