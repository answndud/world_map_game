# [Spring Boot 게임 플랫폼 포트폴리오] 100. free-tier 공개용 demo-lite sibling app shell을 왜 먼저 여는가

## 1. 이번 글에서 닫을 문제

현재 WorldMap 풀앱은 backend 포트폴리오로는 좋지만, 무료 공개용으로는 너무 무겁습니다.

문제는 단순히 "화면이 많다"가 아닙니다.

- Spring Boot always-on runtime
- PostgreSQL
- Redis
- auth / ownership
- ranking / stats / mypage / dashboard
- recommendation feedback persistence

이 약속들을 다 유지한 채 free-tier에 올리려 하면, 제품 설명도 흐려지고 코드도 과하게 흔들립니다.

그래서 이번 조각의 목표는 demo-lite 게임을 완성하는 것이 아니라 아래를 먼저 고정하는 것입니다.

1. `main`은 그대로 유지
2. free 공개는 sibling `demo-lite` 앱으로 분리
3. 별도 header/navigation과 retained route contract를 실제 코드로 먼저 만든다

## 2. 최종 도착 상태

현재 저장소에는 아래가 추가되어 있습니다.

- [demo-lite](/Users/alex/project/worldmap/demo-lite)
- [package.json](/Users/alex/project/worldmap/demo-lite/package.json)
- [vite.config.mjs](/Users/alex/project/worldmap/demo-lite/vite.config.mjs)
- [index.html](/Users/alex/project/worldmap/demo-lite/index.html)
- [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)
- [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)
- [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)
- [sync-shared-assets.mjs](/Users/alex/project/worldmap/demo-lite/scripts/sync-shared-assets.mjs)
- [README.md](/Users/alex/project/worldmap/demo-lite/README.md)

그리고 route map은 아래로 고정했습니다.

- `#/`
- `#/games/capital`
- `#/games/flag`
- `#/games/population-battle`
- `#/recommendation`

즉 이 조각은 `실제 game loop 이식`이 아니라, **free-tier용 별도 app entrypoint가 실제로 build 가능한가**를 먼저 닫는 단계입니다.

## 3. 왜 sibling app인가

문서에서 정리한 대로, 현재 main Spring Boot 앱은 아래가 너무 깊게 얽혀 있습니다.

- [SiteHeaderModelAdvice.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/SiteHeaderModelAdvice.java)
- [CurrentMemberAccessService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- 각 게임의 `*GameService`

같은 앱 안에 `demo-lite` profile을 넣으면,

- auth
- ranking
- stats
- dashboard
- game service
- recommendation feedback

전부 조건 분기로 흔들리게 됩니다.

그래서 이번에는 `same app + feature flag`보다 **별도 sibling app**이 더 안전하다는 문서 판단을 실제 코드로 옮겼습니다.

## 4. 왜 Vite + vanilla JS인가

첫 조각에서 필요한 것은 이것뿐입니다.

- 가벼운 static build
- 별도 navigation shell
- retained route map
- shared JSON import

그래서 React나 더 무거운 프레임워크보다 `Vite + vanilla JS`가 맞았습니다.

장점은 단순합니다.

1. build가 얇다
2. free static hosting으로 바로 나가기 쉽다
3. 첫 조각에서 과한 상태 관리 라이브러리가 필요 없다
4. retained game을 local state로 옮길 때도 충분하다

## 5. 왜 hash route를 먼저 썼는가

free-tier static hosting은 초기에 rewrite 설정이 가장 먼저 꼬입니다.

그래서 이번에는 history API routing보다 아래를 먼저 택했습니다.

- `#/`
- `#/games/capital`
- `#/games/flag`
- `#/games/population-battle`
- `#/recommendation`

즉 배포 플랫폼별 rewrite보다 **route contract 자체를 먼저 고정**한 것입니다.

## 6. shared source는 어디까지 재사용하는가

이번 조각은 Spring Boot service를 재사용하지 않습니다.

대신 아래 정적 source만 build-time에 `public/generated/`로 복사합니다.

- [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)
- [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)
- [flags](/Users/alex/project/worldmap/src/main/resources/static/images/flags)

[sync-shared-assets.mjs](/Users/alex/project/worldmap/demo-lite/scripts/sync-shared-assets.mjs)가 이 파일들을 `public/generated/`로 복사하고, [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)는 generated JSON을 fetch해,

- 국가 수
- 국기 자산 수
- 대륙 분포
- retained route metadata

를 demo-lite shell에 바로 넣습니다.

즉 shared contract는 backend bean이 아니라 **정적 데이터와 자산**입니다.

## 7. 새 요청 흐름

main Spring Boot 앱의 요청 흐름은 바뀌지 않았습니다.

이번 조각에서 새로 생긴 건 demo-lite의 브라우저 흐름입니다.

1. 브라우저가 [index.html](/Users/alex/project/worldmap/demo-lite/index.html)을 연다.
2. [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)가 현재 hash route를 읽는다.
3. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 [routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)를 이용해 page shell을 렌더링한다.
4. [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)가 `public/generated/data/*.json`을 읽어 summary를 만든다.

즉 현재 demo-lite는 **브라우저 + 정적 JSON**만으로 움직이는 shell입니다.

## 8. 왜 이 로직을 main app 안에 넣지 않았는가

이번 조각은 controller/service/domain 로직을 늘리는 작업이 아닙니다.

만들어야 하는 것은 아래입니다.

- 별도 app entrypoint
- 별도 header/navigation
- 별도 static build

그래서 위치도 아래처럼 나뉩니다.

- main 앱: 기존 Spring Boot runtime 그대로 유지
- demo-lite: [demo-lite](/Users/alex/project/worldmap/demo-lite) 별도 앱
- shared source: 메인 저장소의 정적 data/assets

이게 가장 충돌이 적고 설명도 쉽습니다.

## 9. 테스트와 검증

이번 조각에서 실제로 확인한 것은 아래입니다.

```bash
cd demo-lite
npm install --workspaces=false
npm run build
```

그리고 main 저장소 전체 안정성은 기존대로 아래로 다시 확인했습니다.

```bash
./gradlew test
git diff --check
```

즉 이번 조각의 핵심 검증은 "별도 demo-lite 앱이 실제로 build 가능한가"였습니다.

## 10. 아직 하지 않은 것

이번 조각은 의도적으로 아래를 하지 않았습니다.

- 실제 수도 게임 loop 이식
- 실제 국기 퀴즈 loop 이식
- 실제 인구 배틀 loop 이식
- 추천 결과 계산 이식
- free-tier 호스팅 연결

왜냐하면 지금은 먼저 **어디에, 어떤 방식으로, 어떤 route 계약으로** demo-lite를 둘지 닫는 것이 더 중요하기 때문입니다.

## 11. 다음 조각

다음 조각은 retained route 중 하나를 골라 실제 loop를 붙이는 것입니다.

현재 우선순위는 아래입니다.

1. `capital`
2. `flag`
3. `population-battle`
4. `recommendation`

즉 다음 구현은 `demo-lite shell` 위에 첫 retained mode를 local state 기반으로 옮기는 작업입니다.

## 12. 취업 포인트

### 12-1. 1문장 답변

무료 공개 요구 때문에 main Spring Boot 앱을 억지로 깎지 않고, 정적 데이터만 재사용하는 별도 `demo-lite` sibling app을 먼저 열었습니다.

### 12-2. 30초 답변

현재 WorldMap 풀앱은 DB, Redis, auth, ranking, stats, dashboard가 서로 강하게 얽혀 있어서 free-tier 공개용으로 바로 줄이기 어렵습니다. 그래서 이번에는 같은 앱에 feature flag를 늘리기보다, `demo-lite/`라는 별도 Vite 앱을 만들고 retained route `#/games/capital`, `#/games/flag`, `#/games/population-battle`, `#/recommendation`만 먼저 고정했습니다. 이 앱은 Spring Boot service 대신 메인 저장소의 `countries.json`, `flag-assets.json`, 국기 SVG를 `public/generated/`로 복사해 읽고, `npm run build`로 독립 build가 통과하는 상태까지 먼저 닫았습니다.
