# 118. demo-lite에 인구수 퀴즈를 추가해 정적 공개 체험판 범위를 넓히기

## 왜 이 기능을 demo-lite에 넣었나

`demo-lite`는 처음부터 `Cloudflare Pages 정적 배포를 유지하는 sibling 앱`으로 설계됐다.

즉 아래는 건드리지 않는 게 전제였다.

- Git-connected Pages
- `hash route`
- `public/generated` 정적 JSON과 국기 자산 fetch
- 브라우저 메모리 상태와 `localStorage`
- 서버 저장 없음

이 기준에서 가장 자연스럽게 늘릴 수 있는 다음 기능이 `인구수 4지선다`였다.

이유는 간단하다.

- `countries.json`만 있으면 된다
- 국기처럼 추가 asset pipeline이 필요 없다
- 위치 게임처럼 WebGL과 hit-test가 필요 없다
- 기존 `capital`, `flag`, `population-battle`의 local-state loop를 그대로 재사용할 수 있다

즉 `배포 설정을 유지한 채` 새 게임을 하나 더 붙이기 가장 쉬운 조각이었다.

## 무엇을 만들었나

핵심 파일은 아래다.

- [population-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-game.js)
- [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)
- [population-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/population-game.test.mjs)

새 route는 `#/games/population`이다.

게임 루프는 다른 demo-lite retained game과 같은 패턴으로 닫았다.

- 5문제 러닝
- 하트 3개
- 오답이면 같은 문제 재시도
- 정답이면 다음 문제
- 종료 후 `localStorage` 최고 점수 저장

즉 main 앱의 DB-backed endless run을 그대로 옮긴 게 아니라, **체험판에 맞는 얇은 local-state loop**로 다시 만들었다.

## 보기 4개는 어떻게 만들었나

main 앱의 인구수 게임은 `PopulationScaleBandCatalog` 기준으로 아래 구간을 쓴다.

- 1천만 미만
- 1천만 ~ 3천만
- 3천만 ~ 7천만
- 7천만 ~ 1억 5천만
- 1억 5천만 ~ 3억
- 3억 ~ 6억
- 6억 ~ 10억
- 10억 이상

demo-lite도 같은 아이디어를 가져왔다.

대상 국가의 실제 인구가 어느 band에 속하는지 찾고, 그 주변의 **연속된 4개 band**만 보기로 보여 준다.

예를 들어 대한민국처럼 `3천만 ~ 7천만` band에 들어가면, 그 주변 4개 구간이 보기가 된다.

이 방식의 장점은:

- 정확 숫자 4개를 만들 필요가 없다
- 정답이 완전히 뜬금없는 보기와 섞이지 않는다
- 작은 정적 데이터만으로도 설명 가능한 규칙이 된다

## 왜 main 서비스를 재사용하지 않았나

이건 demo-lite 설계의 핵심이다.

main의 `PopulationGameService`는 아래를 전제로 한다.

- DB session 저장
- stage/attempt 저장
- leaderboard write
- ownership/access context

즉 service를 직접 재사용하면 `demo-lite`가 아니라 사실상 full app의 조건을 다시 끌어오게 된다.

그래서 이번 조각은:

- main에서 `구간 band 규칙`만 참고하고
- [population-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-game.js) 안에 새 local-state runtime을 별도로 두었다

이게 `copy-and-simplify` 전략이다.

## 홈과 최근 기록은 어떻게 연결했나

`demo-lite` 홈은 이미 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)로 mode snapshot을 읽고 있었다.

이번에는 여기에 `population` mode와 `population-best-score` key만 추가했다.

중요한 점은:

- 새로운 브라우저 전적 기능을 만든 게 아니다
- 기존 summary read model이 새 모드를 읽을 수 있게만 확장했다

즉 홈의 `즐긴 게임`, `최고 점수`, `최근 기록` 카드가 새 모드를 자연스럽게 포함하게 됐다.

## Cloudflare Pages 설정은 왜 안 바뀌었나

이번 조각은 정적 앱 계약 안에서 끝나기 때문이다.

여전히:

- Root directory: `demo-lite`
- Build command: `npm run build`
- Output: `dist`
- Route: `hash route`
- Shared data: `public/generated/data/*.json`

즉 새 서버, 새 function, 새 storage가 필요 없었다.

이게 바로 `demo-lite`가 main과 별도 앱이어야 했던 이유다.

## 테스트는 무엇으로 닫았나

이번 조각은 아래로 닫았다.

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
```

특히 새 [population-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/population-game.test.mjs)로 아래를 고정했다.

- question pool이 인구 데이터가 있는 국가만 남기는지
- 한 문제에 보기 4개가 생기는지
- 오답이면 같은 문제를 다시 푸는지
- 정답이면 다음 문제로 넘어가는지
- 다섯 번째 문제 뒤에 세션이 끝나는지

## 남은 다음 조각은 무엇인가

이번 작업으로 `demo-lite`는:

- 수도
- 국기
- 인구수
- 인구 비교 배틀
- 추천

까지 보여 주게 됐다.

이 다음에 가장 자연스러운 조각은:

- recommendation `engine-v20` parity와 combo bonus 정리

다. 반대로 위치 게임이나 서버 랭킹은 여전히 Pages 정적 배포 기준에서 cost가 훨씬 크다.
