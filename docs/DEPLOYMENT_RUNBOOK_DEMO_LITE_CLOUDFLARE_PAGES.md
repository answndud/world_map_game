# demo-lite Cloudflare Pages 배포 런북

최종 업데이트: 2026-04-02

## 1. 목적

이 문서는 [demo-lite](/Users/alex/project/worldmap/demo-lite) 정적 앱을 **Cloudflare Pages 한 플랫폼만으로** 공개하기 위한 실행 문서다.

중요:

- 이 문서는 full Spring Boot 앱이 아니라 `demo-lite` 전용이다.
- 대상은 `hash route + localStorage + generated static assets` 구조의 정적 앱이다.
- GitHub 저장소 연결형 Pages 배포를 기준으로 한다.

중요:

- 현재 실제 public URL [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/) 는 Direct Upload 프로젝트로 먼저 열려 있다.
- Cloudflare Pages는 이 Direct Upload 프로젝트를 Git integration으로 바꾸는 방식이 아니라, **새 Git-connected Pages 프로젝트를 따로 만들어야 하는 구조**로 본다.
- 따라서 이 런북의 목적은 `기존 worldmap-demo-lite를 수정해서 Git 연결`이 아니라, `같은 repo/demo-lite를 source로 쓰는 새 Git-connected project handoff`를 준비하는 것이다.

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
7. 새 프로젝트 이름 입력

권장:

- 기존 Direct Upload 프로젝트 이름과 충돌을 피하기 위해 `worldmap-demo-lite-git`처럼 새 이름을 쓴다.
- 기존 [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/) 는 비교/백업용으로 잠시 유지한다.

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

### 8.4 Git-connected로 넘기기 전에 저장소에서 먼저 볼 것

새 Git-connected Pages 프로젝트를 만들기 전에는 아래 workflow가 먼저 살아 있어야 한다.

- [demo-lite-verify.yml](/Users/alex/project/worldmap/.github/workflows/demo-lite-verify.yml)
- [demo-lite/scripts/inspect-pages-git-handoff.mjs](/Users/alex/project/worldmap/demo-lite/scripts/inspect-pages-git-handoff.mjs)

이 workflow는:

- `demo-lite` 변경 시 `npm test`, `npm run build`, `npm run verify:pages`
- `workflow_dispatch` 시 public URL 입력 후 `npm run smoke:public`

까지 지원한다.

즉 대시보드에서 Git 연결을 하기 전에, 저장소 쪽 `main` branch가 정적 배포 source of truth로 안전한지 먼저 닫는 역할이다.

로컬에서 현재 handoff 상태를 빠르게 보려면 아래를 실행한다.

```bash
cd demo-lite
npm run inspect:pages-git
```

이 명령은 현재 Pages 프로젝트가 Direct Upload인지, 현재 브랜치가 planned production branch와 다른지, working tree가 dirty한지까지 같이 보여 준다.

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

저장소 기준 반복 smoke 명령:

```bash
cd demo-lite
npm run inspect:pages-git
npm run smoke:public -- https://worldmap-demo-lite.pages.dev
```

이 스크립트는 아래를 한 번에 확인한다.

1. `/` root HTML과 핵심 보안/캐시 헤더
2. root HTML이 참조하는 `/assets/*` 정적 파일
3. `/generated/data/countries.json`의 국가 수와 `capitalCityKr`
4. `/generated/data/flag-assets.json`과 실제 `/generated/flags/*.svg`

`npm run inspect:pages-git`은 아래를 같이 보여 준다.

1. 현재 Pages 프로젝트가 direct-upload 상태인지
2. 현재 작업 브랜치가 planned production branch와 맞는지
3. working tree가 clean한지
4. Git-connected handoff를 위해 다음에 눌러야 하는 수동 단계

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

현재는 아래까지 끝났다.

- 실제 Cloudflare Pages 프로젝트 생성
- 첫 공개 URL 생성
- clean repo commit 기준 production alias 재배포
- 공개 URL 기준 smoke test 기록
- 저장소 안의 `npm run smoke:public` 반복 검증 레일 추가

아직 안 한 것은 아래다.

- Git-connected auto deploy source of truth 연결
- custom domain 연결

즉 현재 상태는 **첫 공개 배포 완료 + 아직 수동 운영**에 가깝다.

### 11-1. 왜 기존 프로젝트를 바로 Git-connected로 바꾸지 않나

현재 `worldmap-demo-lite`는 `wrangler pages deploy`로 만든 direct-upload 프로젝트다.

이번 조각에서 `wrangler pages project list --json` 기준으로 확인한 값:

- Project Name: `worldmap-demo-lite`
- Git Provider: `No`

즉 지금 남아 있는 마지막 handoff는 “기존 프로젝트 설정 변경”이 아니라
**새 Git-connected Pages 프로젝트를 만들고, 그쪽을 source of truth로 삼는 것**이다.

### 11-2. 수동 handoff 체크리스트

1. 현재 브랜치 변경을 `main`에 먼저 반영한다.
2. Cloudflare 대시보드에서 `Create -> Pages -> Connect to Git`로 **새** Pages 프로젝트를 만든다.
3. 저장소는 같은 GitHub repo를 선택한다.
4. Production branch는 `main`으로 둔다.
5. Root directory는 `demo-lite`
6. Build command는 `npm run build`
7. Build output directory는 `dist`
8. 첫 자동 배포가 끝나면 새 URL에 대해 `npm run smoke:public -- <new-url>`를 실행한다.
9. 새 Git-connected 프로젝트가 정상이라면, 이후에만 기존 direct-upload 프로젝트 정리나 도메인 cutover를 검토한다.

핵심은 “현재 프로젝트를 수정”하는 것이 아니라, **새 Git-connected 프로젝트를 만들고 검증 후 넘기는 것**이다.

## 12. 한 줄 결론

현재 `demo-lite`는 Cloudflare Pages에서 `root directory = demo-lite`, `build = npm run build`, `output = dist`로 실제 공개 URL까지 열린 상태이며, Node 버전과 캐시/보안 헤더는 각각 [.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)과 [public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)로 고정돼 있다. 그리고 배포 후 검증은 `npm run smoke:public`으로 저장소 안에서 반복할 수 있다.
