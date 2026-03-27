# 국기 자산 pool을 36개 snapshot으로 넓히고 재생성 스크립트를 붙이기

## 왜 이 조각이 필요한가

국기 게임 vertical slice는 이미 public 제품에 열려 있었다.

하지만 현재 자산 pool은 너무 작고 편향돼 있었다.

- 출제 가능 국가는 12개
- 대륙 분포는 `EUROPE 11 / ASIA 1`

즉, 게임은 동작하지만
same-continent distractor 품질과 국가 다양성이 모두 제한적이었다.

이번 조각의 목적은 이 제약을 줄이는 것이다.

## 이번에 바뀐 파일

- [scripts/fetch_flag_assets.py](/Users/alex/project/worldmap/scripts/fetch_flag_assets.py)
- [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)
- [static/images/flags](/Users/alex/project/worldmap/src/main/resources/static/images/flags)
- [FlagAssetCatalogTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagAssetCatalogTest.java)
- [FlagQuestionCountryPoolServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolServiceIntegrationTest.java)

같이 [README.md](/Users/alex/project/worldmap/README.md), [FLAG_GAME_ASSET_PIPELINE_PLAN.md](/Users/alex/project/worldmap/docs/FLAG_GAME_ASSET_PIPELINE_PLAN.md), [NEW_GAME_EXPANSION_PLAN.md](/Users/alex/project/worldmap/docs/NEW_GAME_EXPANSION_PLAN.md), [PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md), [WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md), [50-current-state-rebuild-map.md](/Users/alex/project/worldmap/blog/50-current-state-rebuild-map.md)도 현재 기준으로 맞췄다.

## 무엇을 바꿨나

이번에는 국기 파일을 그냥 수작업으로 몇 장 더 넣지 않았다.

대신 [fetch_flag_assets.py](/Users/alex/project/worldmap/scripts/fetch_flag_assets.py)가
선택된 ISO3 목록을 기준으로

- `flagcdn.com`에서 SVG를 내려받고
- `static/images/flags/{iso3}.svg`에 저장하고
- `flag-assets.json` manifest를 같이 다시 쓴다.

즉, 자산 확대 자체를 재생성 가능한 작업으로 바꿨다.

## 현재 pool 규모

현재 출제 가능 국기 국가는 `36개`다.

대륙 분포는 아래처럼 바뀌었다.

- `EUROPE 15`
- `ASIA 8`
- `NORTH_AMERICA 3`
- `SOUTH_AMERICA 4`
- `AFRICA 4`
- `OCEANIA 2`

완전히 균형적이지는 않지만,
기존 `EUROPE 11 / ASIA 1`보다 훨씬 설명 가능한 분포가 됐다.

## 왜 스크립트를 따로 두는가

핵심 이유는 재현성이다.

국기 게임은 다른 게임과 달리 static asset이 본질이다.
그래서 “파일이 저장소에 들어 있다”만으로는 부족하고,
어떤 기준으로 이 파일들이 만들어졌는지도 같이 남아 있어야 한다.

이번 구조에서는

- runtime은 계속 local static file만 읽는다.
- local/demo boot는 네트워크가 필요 없다.
- 자산을 늘리거나 갱신할 때만 스크립트를 다시 돌리면 된다.

즉, 앱 부팅과 자산 재생성을 분리했다.

## 현재 서버 규칙은 그대로다

요청 흐름은 바뀌지 않았다.

여전히 국기 게임은

`country seed ∩ flag manifest ∩ 실제 파일 존재`

교집합만 출제 가능 국가로 본다.

즉,

- [FlagAssetCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)는 manifest와 파일 존재를 검증하고
- [FlagQuestionCountryPoolService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)는 실제 출제 가능 국가 pool을 계산하고
- [FlagGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java)는 그 pool을 읽어 문제를 만든다.

자산 수만 늘렸지, 책임 경계는 그대로 유지했다.

## 테스트로 무엇을 고정했나

[FlagAssetCatalogTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagAssetCatalogTest.java)에서

- asset 수 `36`
- `JPN`, `KOR`, `USA` lookup

을 고정했다.

[FlagQuestionCountryPoolServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolServiceIntegrationTest.java)에서는

- 출제 가능 국가 `36`
- 대륙 분포
- ASIA / OCEANIA lookup

을 현재 기준으로 다시 고정했다.

실행:

```bash
python3 -m py_compile scripts/fetch_flag_assets.py
./gradlew test --tests com.worldmap.game.flag.application.FlagAssetCatalogTest --tests com.worldmap.game.flag.application.FlagQuestionCountryPoolServiceIntegrationTest
./gradlew test
```

## 지금 상태를 어떻게 설명하면 되나

이제 국기 게임은

- static asset이 저장소 안에 있고
- 그 asset이 재생성 가능한 스크립트로 관리되고
- 출제 가능 국가는 36개로 넓어졌고
- 대륙 분포도 이전보다 덜 편향된 상태

라고 설명할 수 있다.

즉, 국기 게임이 더 이상 “12개 sample만 겨우 붙어 있는 demo”가 아니라,
확장 가능한 자산 파이프라인 위에 올라간 모드가 된다.

## 다음 단계

다음 후보는 둘 중 하나다.

- 국기 게임 난이도와 distractor 품질을 더 다듬기
- 홈 / 랭킹 / Stats에서 신규 게임 3종 카드 밀도를 다시 정리하기

## 면접에서 이렇게 설명할 수 있다

> 국기 게임은 자산이 적으면 same-continent distractor 품질과 국가 다양성이 급격히 떨어집니다. 그래서 sample 12개로 vertical slice를 먼저 연 뒤, 다음 조각에서 `fetch_flag_assets.py`를 만들어 선택된 ISO3 목록의 SVG와 manifest를 함께 재생성하도록 바꿨습니다. 그 결과 출제 가능 국가는 36개로 늘었고, 서버는 여전히 `country seed ∩ manifest ∩ 실제 파일 존재` 교집합만 문제 pool로 쓰기 때문에 자산 확대 후에도 설명 가능한 구조를 유지할 수 있습니다.
