# 122. demo-lite 셸을 Coinbase-style visual system으로 다시 짜기

## 왜 이 조각이 필요했나

`demo-lite`는 이미 게임 4종과 추천 1종을 끝까지 체험할 수 있었다.

문제는 기능이 아니었다.

- 첫 화면이 제품보다 샘플처럼 보였고
- 카드 위계가 가볍게 흩어져 있었고
- 공개 데모인데도 "이건 임시 페이지인가?"라는 인상을 줄 여지가 있었다

즉 지금 필요한 건 새 기능이 아니라, **공개 체험판의 첫 인상과 신뢰감**이었다.

이번 조각은 그 문제를 닫기 위해, `design-coinbase-style` 기준으로 `blue / white / near-black` visual system을 `demo-lite` 전체 셸에 다시 입힌 작업이다.

## 무엇을 만들었나

핵심 파일은 아래다.

- [index.html](/Users/alex/project/worldmap/demo-lite/index.html)
- [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)
- [_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)
- [cloudflare-pages-config.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/cloudflare-pages-config.test.mjs)

실제로 바뀐 건 세 가지다.

1. 홈 셸 구조를 다시 짰다
2. 공통 visual token을 다시 고정했다
3. 외부 폰트 사용에 맞춰 배포 계약도 같이 수정했다

## 홈은 어디서 시작되고 어디서 읽히게 바꿨나

요청 흐름은 그대로다.

1. 사용자가 `#/`에 들어온다.
2. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 홈 셸을 렌더한다.
3. 각 feature route는 기존 runtime을 그대로 유지한다.

즉 상태 변화는 없다.

- 게임 점수 로직 변화 없음
- 추천 계산 로직 변화 없음
- localStorage 구조 변화 없음

대신 home shell을 아래 구조로 다시 잡았다.

- dark hero
- product snapshot card
- white route card grid
- browser recent summary

이렇게 나누면 홈 첫 화면이 "기능 목록"이 아니라, **제품 소개 -> 현재 공개 범위 -> 바로 체험 -> 최근 기록** 순서로 읽힌다.

## 왜 snapshot card는 브라우저 기록이 아니라 제품 사실이어야 하나

초기에는 hero 오른쪽 카드에 브라우저 recent summary를 그대로 재사용하고 있었다.

그런데 그건 역할이 안 맞았다.

hero 오른쪽 카드가 해야 할 일은:

- 이 공개 데모가 무엇인지
- 어디까지 열려 있는지
- 어떤 제약 위에서 돌아가는지

를 짧게 말하는 것이다.

그래서 이번에는 아래처럼 제품 고정 사실로 바꿨다.

- `5 surfaces`
- `browser only`
- `194 countries`
- `pages.dev`

이 판단이 중요한 이유는, **개인 기록과 제품 범위는 다른 정보 계층**이기 때문이다.

개인 기록은 아래 recent summary에서 읽게 하고, hero에서는 demo-lite 자체를 설명하게 분리했다.

## 왜 이 로직이 shell에 있어야 하나

이 조각은 컨트롤러나 feature runtime이 아니라 shell layer의 책임이다.

[app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 맡아야 하는 이유는 아래와 같다.

- 홈 hero의 정보 구조는 feature별 게임 로직과 무관하다
- route metadata를 카드 단위로 묶어 보여 주는 것도 shell 책임이다
- 같은 visual system을 home과 feature route에 같이 적용하려면 공통 셸에서 기준을 잡아야 한다

반대로 각 feature 파일 안에 이 구조를 넣으면:

- 홈과 feature 위계가 다시 분산되고
- route metadata 관리가 흩어지고
- 나중에 공개 셸만 다시 조정하기 어려워진다

즉 이번 조각의 중심은 "새 기능"이 아니라 **셸 책임 재정리**다.

## 왜 CSS만 바꾸지 않고 `_headers`까지 같이 손봤나

이번 리디자인에서는 `Manrope`, `Space Grotesk`를 Google Fonts로 불러왔다.

그런데 Cloudflare Pages의 현재 CSP는 원래 아래처럼 막혀 있었다.

- `style-src 'self'`
- `font-src` 없음

이 상태에서는 로컬에서 디자인이 괜찮아 보여도, 공개 URL에서는 폰트가 막혀 바로 다른 화면이 된다.

그래서 [public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)를 같이 수정해 아래만 최소 허용으로 열었다.

- `https://fonts.googleapis.com`
- `https://fonts.gstatic.com`

그리고 [cloudflare-pages-config.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/cloudflare-pages-config.test.mjs)에서도 이 계약을 같이 고정했다.

이게 중요한 이유는, **visual contract와 deploy contract는 분리되지 않기 때문**이다.

## 무엇을 테스트했나

이번 조각은 디자인 변경이지만, 아래 검증은 그대로 필요하다.

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
git diff --check
```

특히 이번에는 [cloudflare-pages-config.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/cloudflare-pages-config.test.mjs)가 아래를 같이 본다.

- `_headers`에 CSP가 있는지
- `fonts.googleapis.com`이 `style-src`에 들어 있는지
- `fonts.gstatic.com`이 `font-src`에 들어 있는지

즉 디자인 변경도 "그냥 예쁘게 보였다"가 아니라, **public Pages 계약까지 같이 맞았는지**로 닫는다.

## 이번 조각으로 무엇이 좋아졌나

이제 `demo-lite`는:

- 기능은 그대로 유지하면서
- 첫 화면이 더 제품답게 읽히고
- feature route도 같은 제품 family 안에 묶여 보이며
- 공개 URL과 로컬의 시각 차이도 줄었다

즉 이번 조각은 새 게임을 추가한 작업이 아니라, 이미 있는 retained surfaces를 **공개 가능한 체험판 품질로 끌어올린 작업**이다.

## 다음 조각은 무엇인가

다음 후보는 기능 확장보다 아래가 더 맞다.

- README screenshot 갱신
- route별 미세 motion 보강
- 추천/퀴즈 결과 화면의 spacing 미세 조정

반대로 지금 단계에서 또 큰 기능을 얹는 건 우선순위가 아니다.

공개 데모는 먼저 "믿을 만한 제품처럼 보이는가"를 닫아야 한다.
