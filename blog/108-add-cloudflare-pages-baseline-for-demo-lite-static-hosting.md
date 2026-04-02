# [Spring Boot 게임 플랫폼 포트폴리오] 108. demo-lite를 Cloudflare Pages에 올리기 전에 저장소에 먼저 고정한 것들

## 1. 이번 글에서 닫을 문제

이전 조각까지 오면 `demo-lite`는 이미 playable합니다.

- 수도 맞히기
- 국기 퀴즈
- 인구 비교 배틀
- 20문항 추천
- 홈 recent summary / streak / copyable summary

그런데 playable하다고 바로 무료 공개가 되는 것은 아닙니다.

정적 hosting에서는 오히려 이런 질문이 더 중요합니다.

- Cloudflare Pages에서 어디를 root로 볼 것인가
- 빌드는 어떤 명령으로 할 것인가
- output 디렉터리는 무엇인가
- Node 버전은 무엇으로 고정할 것인가
- 캐시와 보안 헤더는 어디에 둘 것인가

즉 이번 조각의 목적은
**Cloudflare 대시보드에 들어가기 전에, 배포 계약 자체를 저장소에 먼저 고정하는 것**이었습니다.

## 2. 이번에 바뀐 파일

- [demo-lite/.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)
- [demo-lite/public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)
- [demo-lite/scripts/check-cloudflare-pages-baseline.mjs](/Users/alex/project/worldmap/demo-lite/scripts/check-cloudflare-pages-baseline.mjs)
- [demo-lite/tests/cloudflare-pages-config.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/cloudflare-pages-config.test.mjs)
- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)

같이 정리한 문서는 아래입니다.

- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 `.node-version`을 넣었나

Cloudflare Pages는 build image에서 Node를 잡아 빌드합니다.

`demo-lite`는 지금처럼 작은 앱일수록 “그냥 기본값 쓰면 되지”라고 생각하기 쉽습니다.
하지만 이럴수록 나중에 build image 기본 버전이 바뀌면 원인 추적이 더 어려워집니다.

그래서 이번에는 [demo-lite/.node-version](/Users/alex/project/worldmap/demo-lite/.node-version)으로
`demo-lite`가 어떤 Node 기준으로 빌드되는지 repo 안에 먼저 남겼습니다.

중요한 점은 이 값을
Cloudflare dashboard 수기 입력보다 먼저 **저장소 source of truth**로 둔 것입니다.

즉 지금 기준은:

- Pages env override보다 `.node-version` 우선
- dashboard는 최대한 비워 둔다

입니다.

그리고 이번에는 [demo-lite/package.json](/Users/alex/project/worldmap/demo-lite/package.json)의 `engines.node`도 `20.x`로 맞춰,
repo 안의 사람과 Pages build image가 서로 다른 전제를 읽지 않게 정리했습니다.

## 4. 왜 `_headers`를 저장소에 넣었나

static hosting에서 자주 놓치는 것이 있습니다.

> “배포가 된다”와 “어떤 응답 정책으로 서비스된다”는 다른 문제다

이번 조각의 핵심은 그래서 [demo-lite/public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)입니다.

여기서 고정한 것은 두 축입니다.

### 4-1. 캐시 정책

- `/*` 문서: `must-revalidate`
- `/assets/*`: immutable 장기 캐시
- `/generated/flags/*`: immutable 장기 캐시
- `/generated/data/*`: 짧은 재검증 캐시

즉 빌드 산출물 중 무엇은 오래 캐시하고,
무엇은 더 자주 다시 보게 할지를 저장소에 남긴 것입니다.

### 4-2. 기본 보안 헤더

- `X-Content-Type-Options`
- `X-Frame-Options`
- `Referrer-Policy`
- `Permissions-Policy`
- `Cross-Origin-Opener-Policy`
- `Content-Security-Policy`

특히 CSP는 현재 `demo-lite`가 same-origin script/style/img/connect만 쓰는 정적 앱이라는 점에 맞춰
아주 보수적으로 잡았습니다.

즉 이 파일은 단순 부가 설정이 아니라,
**정적 앱의 응답 계약 그 자체**입니다.

## 5. 왜 `_redirects`는 넣지 않았나

여기서 한 번 더 중요한 판단이 있습니다.

`demo-lite`는 path routing이 아니라 **hash routing**입니다.

실제 path는 항상 `/`이고,
라우팅은 `/#/games/capital` 같은 fragment로만 처리됩니다.

그래서 이번 baseline에서는 `_redirects`를 일부러 넣지 않았습니다.

이 판단의 장점은 명확합니다.

- direct path rewrite 고민이 줄어든다
- Pages 설정이 단순해진다
- routing 문제를 hosting이 아니라 app shell 안에 가둘 수 있다

즉 이번 조각은 Pages 기능을 더 많이 쓰는 방향이 아니라,
**현재 hash route 전략과 가장 잘 맞는 최소 설정만 남기는 방향**이었습니다.

## 6. 왜 `wrangler.toml`도 아직 안 넣었나

Cloudflare에 올린다고 해서 곧바로 `wrangler.toml`을 넣을 필요는 없습니다.

이번 목표는 CLI deploy가 아니라
Git-connected Pages baseline을 repo 안에 먼저 남기는 것이기 때문입니다.

즉 지금 필요한 건:

- root directory
- build command
- output directory
- node version
- headers

정도입니다.

`wrangler.toml`까지 넣으면 초보자 기준으로 아래 혼선이 생깁니다.

- Pages와 Workers 중 무엇이 source of truth인가
- 대시보드 배포와 CLI 배포 중 무엇을 먼저 따라야 하나

그래서 이번 단계에서는 의도적으로 넣지 않았습니다.

## 7. 실제 배포 값은 무엇으로 고정했나

[docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)에는 아래 값을 기준으로 적었습니다.

- Root directory: `demo-lite`
- Build command: `npm run build`
- Build output directory: `dist`

이때 `npm run build`는 실제로 아래를 모두 포함합니다.

1. shared asset sync
2. shared asset verify
3. Vite build

즉 Cloudflare Pages는 정적 앱만 올리는 것이지만,
`demo-lite`는 메인 저장소 자산을 복사해 와야 하므로
**단순 Vite 기본값보다 조금 더 구체적인 build 계약**이 필요합니다.

## 8. 어디에서 상태가 바뀌나

이번 조각은 사용자 기능 상태를 바꾸지 않습니다.

바뀌는 것은 `운영 계약`입니다.

흐름은 아래처럼 설명하면 됩니다.

1. Cloudflare Pages가 `demo-lite`를 root directory로 읽는다
2. `npm run build`를 실행한다
3. shared asset sync / verify가 먼저 돈다
4. Vite가 `dist/`를 만든다
5. `public/_headers`가 같이 산출물에 포함된다
6. Pages는 이 `dist/`를 정적으로 서비스한다

즉 이번 조각의 상태 변화는 DB/Redis가 아니라,
**배포 산출물의 구성과 응답 정책**입니다.

## 9. 어떤 테스트로 닫았나

이번에는 설정 자체를 테스트로 고정했습니다.

```bash
cd demo-lite
npm test
```

[cloudflare-pages-config.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/cloudflare-pages-config.test.mjs)는 아래를 확인합니다.

1. `.node-version`이 실제로 존재하는지
2. `_headers`에 보안 헤더가 들어 있는지
3. `_headers`에 assets / generated flags / generated data 캐시 규칙이 들어 있는지

추가로 아래 명령도 넣었습니다.

```bash
cd demo-lite
npm run verify:pages
```

[check-cloudflare-pages-baseline.mjs](/Users/alex/project/worldmap/demo-lite/scripts/check-cloudflare-pages-baseline.mjs)는
테스트보다 한 단계 더 배포 계약에 가까운 확인을 합니다.

1. build 스크립트가 `sync:shared -> verify:shared -> vite build` 순서를 유지하는지
2. `engines.node`와 `.node-version` 기준이 어긋나지 않는지
3. `_headers`에 필수 규칙이 빠지지 않았는지

그리고 전체 build는 다시 아래로 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심은
“Cloudflare에서 눌러 보면 되겠지”가 아니라
**repo 안의 배포 baseline이 실제 파일과 테스트로 닫혀 있는가**입니다.

## 10. 이 조각이 왜 중요한가

무료 배포는 종종 “빨리 링크를 만든다”로 끝나기 쉽습니다.

하지만 포트폴리오에서는 오히려 이런 질문이 나옵니다.

- 왜 이 플랫폼을 골랐나
- 왜 이런 route 전략을 썼나
- 캐시는 어떻게 나눴나
- 보안 헤더는 어디서 관리하나
- 이 값이 대시보드에만 있는가, repo에도 남아 있는가

이번 조각은 바로 그 설명력을 위한 작업입니다.

즉 `demo-lite`는 이제 단순히 static host에 올릴 수 있는 앱이 아니라,
**정적 배포 계약까지 repo 안에서 설명 가능한 앱**이 됐습니다.

## 11. 다음으로 볼 것

이제 자연스러운 다음 조각은 하나입니다.

1. 실제 Cloudflare Pages 프로젝트 생성
2. 공개 URL 발급
3. `/#/games/capital`, `/#/games/flag`, `/#/recommendation` smoke test
4. copyable summary가 공개 URL에서도 자연스럽게 동작하는지 확인

즉 다음 단계는 문서가 아니라
**실제 공개 URL 검증**입니다.

## 마무리

이번 조각으로 `demo-lite`를 Cloudflare Pages에 올리기 전에 필요한 배포 baseline을 저장소에 고정했습니다. `.node-version`으로 build Node를 pin하고, `_headers`로 캐시/보안 정책을 남기고, `cloudflare-pages-config.test.mjs`로 이 계약이 실제 파일로 존재하는지 검증했습니다. 핵심은 Cloudflare 대시보드 수기 설정에 기대기보다, root/build/output/headers 같은 운영 규칙을 repo 안에서 먼저 설명 가능하게 만든 점입니다.
