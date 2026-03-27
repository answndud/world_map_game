# 국기 게임을 열기 전에 FlagAssetCatalog를 먼저 만들기

## 왜 이 조각이 필요한가

국기 보고 나라 맞히기 게임은 얼핏 보면 간단하다.

- 국기 이미지 1개 보여 주기
- 나라 4지선다 내기

하지만 실제로는 게임 규칙보다
`국기 자산을 어디서 읽고, local/demo에서 어떻게 재현할 것인가`
가 먼저 풀려야 한다.

외부 URL을 그대로 붙이면 이런 문제가 생긴다.

- local 환경에서 네트워크가 없으면 재현이 안 된다.
- 써드파티 링크가 바뀌면 게임이 깨질 수 있다.
- “지금 출제 가능한 국가가 몇 개인가”를 서버 코드로 설명하기 어렵다.

그래서 이번 조각의 목표는
`flag game mode를 바로 여는 것`이 아니라,
`서버가 읽을 수 있는 국기 자산 catalog를 먼저 만드는 것`이었다.

## 이번 조각에서 만든 것

이번에는 `FlagAssetCatalog`를 추가했다.

- `flag-assets.json` manifest 추가
- `static/images/flags/*.svg` sample 자산 12개 추가
- 서버가 manifest를 읽고 entry를 검증하는 catalog 추가
- ISO3 / 경로 / format / 실제 파일 존재 검증 테스트 추가

즉, 이제는 서버가
“현재 출제 가능한 국기 자산이 어떤 것들인가”
를 코드로 설명할 수 있다.

## 어떤 파일이 바뀌는가

### 자산 catalog와 manifest

- [FlagAsset.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAsset.java)
- [FlagAssetCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)
- [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)

### sample SVG 자산

- [jpn.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/jpn.svg)
- [fra.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/fra.svg)
- [deu.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/deu.svg)
- [ita.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/ita.svg)
- [irl.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/irl.svg)
- [bel.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/bel.svg)
- [pol.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/pol.svg)
- [ukr.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/ukr.svg)
- [aut.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/aut.svg)
- [nld.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/nld.svg)
- [est.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/est.svg)
- [ltu.svg](/Users/alex/project/worldmap/src/main/resources/static/images/flags/ltu.svg)

### 테스트

- [FlagAssetCatalogTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagAssetCatalogTest.java)

## 요청 흐름은 아직 없다

이번 조각은 아직 public 요청이 시작되지 않는다.

즉,

- `/games/flag/start`
- `/api/games/flag/sessions`

같은 엔드포인트는 아직 없다.

대신 지금 생긴 흐름은
`애플리케이션이 startup에서 국기 자산 manifest를 읽는 흐름`
이다.

1. [FlagAssetCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)가 `classpath:data/flag-assets.json`을 읽는다.
2. 각 entry의 `iso3Code`, `relativePath`, `format`을 검증한다.
3. `classpath:static/images/flags/...` 실제 파일이 있는지 확인한다.
4. 이후 다른 서비스는 `supportedIso3Codes()`와 `findByIso3Code()`만 사용해 출제 가능 국기 풀을 읽는다.

즉, 이번 조각은 `게임 요청 흐름`보다
`출제 자산 read model`을 먼저 고정하는 조각이다.

## 왜 컨트롤러가 아니라 catalog가 맡아야 하나

국기 게임에서 이 규칙은 HTTP 요청 처리보다
`시스템이 어떤 자산을 신뢰할 수 있느냐`
에 가깝다.

그래서 이 로직을 컨트롤러에 둘 이유가 없다.

오히려 [FlagAssetCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)가 startup 시점에

- manifest가 비어 있지 않은가
- ISO3가 중복되지 않는가
- 경로가 `/images/flags/` 아래인가
- format이 `svg`인가
- 실제 파일이 존재하는가

를 바로 검증하는 편이 훨씬 설명 가능하다.

이렇게 해 두면 나중에 `flag` game mode를 열 때는
문제 생성과 정답 판정에만 집중하면 된다.

## 왜 DB 컬럼보다 manifest를 먼저 택했나

이 프로젝트에서 `country`는 공통 도메인 데이터다.

반면 국기 이미지는

- 정적 파일
- 배포 산출물
- local/demo 재현 자산

에 가깝다.

그래서 1차에서는
`country.flagPath`를 DB에 넣는 것보다
`정적 파일 + manifest`를 source of truth로 두는 편이 맞다.

이 방식의 장점은 명확하다.

- 네트워크 없이 local/demo 재현 가능
- 출제 가능 국가를 교집합 기준으로 설명 가능
- 게임이 열리기 전에도 자산 준비 상태를 테스트로 고정 가능

## 테스트는 무엇으로 고정했나

이번 조각은 아래 테스트로 고정했다.

- [FlagAssetCatalogTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagAssetCatalogTest.java)
  - manifest를 읽어 12개 sample 자산이 로드되는지 확인
  - ISO3 lookup이 동작하는지 확인
  - manifest의 ISO3가 실제 `countries.json` 시드에 모두 존재하는지 확인

실행한 검증은 아래다.

```bash
./gradlew test --tests com.worldmap.game.flag.application.FlagAssetCatalogTest
git diff --check
```

## 이 조각에서 배우는 포인트

국기 게임은 “화면에 이미지를 띄우는 문제”가 아니라
`출제 자산을 어떻게 검증 가능한 상태로 보관할 것인가`
의 문제다.

이걸 먼저 catalog와 테스트로 고정해 두면,
다음 조각에서 game mode를 열 때는

- 출제 가능한 국가 pool
- 4지선다 option generator
- 하트 / 점수 / 랭킹

에만 집중할 수 있다.

즉, 자산 파이프라인을 먼저 분리한 덕분에
게임 규칙 코드는 훨씬 작고 설명 가능해진다.

## 아직 남은 점

지금 sample SVG는 12개뿐이다.

즉, 아직은

- full 194개 국가 전체 운영
- 라이선스 정리 완료
- flag game public vertical slice

까지 간 상태가 아니다.

다음 조각에서는

1. `FlagAssetCatalog`를 쓰는 출제 가능 국가 pool 계산
2. 그 다음 `flag` game mode vertical slice

순으로 열면 된다.
