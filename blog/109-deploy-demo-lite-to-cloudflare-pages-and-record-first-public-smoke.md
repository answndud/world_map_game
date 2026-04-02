# [Spring Boot 게임 플랫폼 포트폴리오] 109. demo-lite를 Cloudflare Pages에 실제로 올리고 첫 공개 smoke를 남기기

## 1. 이번 글에서 닫을 문제

이전 조각까지 오면 `demo-lite`는 이미 아래를 갖고 있었습니다.

- 별도 sibling app 구조
- playable retained surface 4개
- browser recent summary / streak / copyable summary
- Cloudflare Pages baseline 파일

하지만 여전히 하나가 비어 있었습니다.

> 그래서 진짜 공개 URL은 어디 있나?

정적 배포는 문서만 잘 써서는 끝나지 않습니다.
실제로 URL이 열리고,
그 URL에서 홈과 핵심 route가 뜨는지까지 봐야
비로소 “공개 demo-lite”라고 부를 수 있습니다.

이번 조각의 목표는 그래서 간단했습니다.

1. Cloudflare Pages 프로젝트를 실제로 만든다
2. `demo-lite/dist`를 production URL에 올린다
3. 응답 헤더와 핵심 route가 실제로 뜨는지 smoke를 남긴다

## 2. 이번에 바뀐 것

이번 조각은 코드 변경보다 **실제 배포 상태**를 만든 작업입니다.

문서 반영 파일:

- [README.md](/Users/alex/project/worldmap/README.md)
- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_DEMO_LITE_CLOUDFLARE_PAGES.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

이번 조각에서 실제로 생긴 public URL:

- [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)

## 3. 실제로 어떤 순서로 배포했나

이번엔 대시보드 클릭 대신 CLI로 먼저 닫았습니다.

순서는 아래였습니다.

### 3-1. 인증 상태 확인

```bash
cd demo-lite
npx -y wrangler whoami
```

여기서 `pages (write)` 권한이 있는 계정인지 먼저 확인했습니다.

### 3-2. Pages 프로젝트 생성

```bash
npx -y wrangler pages project create worldmap-demo-lite --production-branch main
```

이 단계에서 Pages 프로젝트만 먼저 만들어집니다.
아직 production URL이 바로 살아나는 것은 아닙니다.

### 3-3. 첫 preview deploy

```bash
npx -y wrangler pages deploy dist --project-name worldmap-demo-lite
```

이 명령으로 preview deployment와 branch alias가 먼저 생깁니다.

즉 이 단계는
“업로드가 성공하는가”
를 먼저 보는 단계입니다.

### 3-4. production branch로 재배포

```bash
npx -y wrangler pages deploy dist \
  --project-name worldmap-demo-lite \
  --branch main \
  --commit-dirty=true \
  --commit-message "demo-lite initial production deploy"
```

여기서 production URL인
[https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)
가 실제로 살아났습니다.

즉 이번에 중요한 포인트는
**Pages project를 만든 뒤에도 production alias는 별도 branch deploy가 있어야 열린다**는 점입니다.

## 4. 공개 URL에서 무엇을 확인했나

이번엔 단순히 브라우저로 열어 본 게 아니라,
응답 계약과 렌더링을 둘 다 확인했습니다.

### 4-1. curl로 본 정적 응답

```bash
curl -I -L --max-redirs 3 https://worldmap-demo-lite.pages.dev/
curl -I -L --max-redirs 3 https://worldmap-demo-lite.pages.dev/generated/data/countries.json
curl -I -L --max-redirs 3 https://worldmap-demo-lite.pages.dev/assets/index-BO7e55jL.js
```

여기서 확인한 것은:

- `/` -> `200`
- `/generated/data/countries.json` -> `200`
- `/assets/index-*.js` -> `200`
- `_headers`에 넣은 값이 실제 응답에 포함되는지

즉 이번 smoke는 “URL이 열린다”보다,
**우리가 repo에 적어 둔 cache/security 계약이 진짜 응답으로 내려오는가**
를 보는 것이었습니다.

### 4-2. `_headers` 규칙이 실제로 반영됐는가

루트 응답에서는 실제로 아래가 보였습니다.

- `Cache-Control: public, max-age=0, must-revalidate`
- `Content-Security-Policy`
- `Cross-Origin-Opener-Policy`
- `Permissions-Policy`
- `Referrer-Policy`
- `X-Content-Type-Options`
- `X-Frame-Options`

그리고 정적 자산은:

- `/generated/data/*` -> 재검증 캐시
- `/assets/*` -> `immutable`

규칙이 실제 응답으로 내려왔습니다.

즉 [public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)가
문서용이 아니라 실제 배포 계약이라는 것을 확인한 셈입니다.

## 5. hash route 화면은 어떻게 확인했나

정적 응답만 보면 화면이 실제로 뜨는지는 알 수 없습니다.

그래서 이번엔 macOS에 이미 설치된 Chrome channel을 재사용해
Playwright screenshot smoke를 돌렸습니다.

예시는 아래입니다.

```bash
npm exec --yes --package=playwright -- playwright screenshot \
  --channel chrome \
  --wait-for-timeout 3000 \
  'https://worldmap-demo-lite.pages.dev/#/games/capital' \
  /tmp/worldmap-demo-lite-capital.png
```

같은 방식으로 아래를 확인했습니다.

- `/`
- `/#/games/capital`
- `/#/recommendation`

여기서 본 것은:

- 홈 hero가 실제로 뜨는지
- 수도 게임 첫 문제 surface가 실제로 뜨는지
- recommendation 20문항 surface가 실제로 뜨는지

즉 이번 smoke는 단순 HTML 응답이 아니라
**hash route 기반 app shell이 실제 production에서 렌더링되는지**
를 보는 단계였습니다.

## 6. 이번에 중요했던 함정

이번 조각에서 가장 중요한 함정은 이것입니다.

> production URL은 열렸지만, 아직 저장소 commit과 1:1로 맞아 있지는 않다

이번 첫 공개는 local working tree 상태에서
`wrangler pages deploy --branch main`
으로 먼저 production에 올린 것입니다.

즉 지금 production URL은 살아 있지만,
엄밀히 말하면 아직
**Git-connected Pages production source of truth**
는 아닙니다.

이건 숨기면 안 되는 사실입니다.

그래서 다음 조각은 반드시 아래여야 합니다.

1. 현재 `demo-lite` 변경을 커밋/푸시
2. Pages production 기준을 저장소 상태와 다시 맞추기
3. 이후부터는 repo 기준으로 재현 가능하게 만들기

## 7. 요청 흐름 대신 배포 흐름으로 설명하면

이번 조각은 사용자 요청 흐름이 아니라 배포 흐름입니다.

1. `wrangler whoami`로 인증 상태 확인
2. `wrangler pages project create`로 프로젝트 생성
3. `wrangler pages deploy dist`로 preview 배포
4. `wrangler pages deploy dist --branch main`으로 production alias 연결
5. `curl`로 루트와 정적 자산 응답 확인
6. Chrome channel screenshot smoke로 hash route 렌더링 확인

즉 이번 단계의 상태 변화는 DB/Redis가 아니라,
**Cloudflare 계정 안의 실제 Pages 프로젝트와 공개 URL**입니다.

## 8. 어떤 검증으로 닫았나

이번 조각은 아래까지 실제로 수행했습니다.

```bash
cd demo-lite
npm run build
npm run verify:pages
npx -y wrangler whoami
npx -y wrangler pages project create worldmap-demo-lite --production-branch main
npx -y wrangler pages deploy dist --project-name worldmap-demo-lite
npx -y wrangler pages deploy dist --project-name worldmap-demo-lite --branch main --commit-dirty=true
```

그리고 공개 URL smoke는 아래로 확인했습니다.

```bash
curl -I -L --max-redirs 3 https://worldmap-demo-lite.pages.dev/
curl -I -L --max-redirs 3 https://worldmap-demo-lite.pages.dev/generated/data/countries.json
curl -I -L --max-redirs 3 https://worldmap-demo-lite.pages.dev/assets/index-BO7e55jL.js
```

추가로 Chrome channel screenshot smoke로
홈 / 수도 / 추천 route도 확인했습니다.

## 9. 이 조각이 왜 중요한가

포트폴리오에서는 “무료 static hosting을 고려했다”보다
“실제로 무료 static hosting에 올려 봤다”가 훨씬 강합니다.

이번 조각으로 `demo-lite`는 이제:

- 로컬에서만 도는 별도 앱

이 아니라

- 실제 public URL이 있는 demo surface

가 되었습니다.

그리고 더 중요한 점은,
그 public URL이 어떤 계약으로 서비스되는지도 같이 설명할 수 있게 됐다는 것입니다.

## 10. 다음으로 볼 것

이제 다음은 하나입니다.

1. 현재 `demo-lite` 전체 변경을 커밋/푸시
2. Pages production source of truth를 저장소 기준으로 다시 맞추기

즉 다음 단계는 “더 예쁘게 만들기”가 아니라
**public URL과 Git source를 일치시키는 정리 작업**입니다.

## 마무리

이번 조각으로 `demo-lite`를 Cloudflare Pages에 실제로 배포해 [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)를 열었습니다. `wrangler pages project create`로 프로젝트를 만들고, `dist/`를 production branch로 배포한 뒤 `curl`로 응답 헤더와 자산 경로를 확인하고, Chrome channel screenshot smoke로 홈·수도·추천 화면이 실제로 렌더링되는지 봤습니다. 핵심은 static hosting 계획을 문서에서 끝내지 않고, 실제 public URL과 smoke 결과까지 남겼다는 점입니다.

---

추가 정리:

이 글을 쓴 직후, 실제로 현재 `demo-lite` 전체 변경을 커밋/푸시하고
production alias를 clean repo commit `5356fde` 기준으로 다시 배포했습니다.

즉 현재 [https://worldmap-demo-lite.pages.dev/](https://worldmap-demo-lite.pages.dev/)는
dirty working tree snapshot이 아니라
**clean repo commit 기준으로 다시 맞춘 상태**입니다.

다만 아직도 이것은 `wrangler pages deploy` 기반 수동 production이고,
Git-connected auto deploy source of truth는 아닙니다.
