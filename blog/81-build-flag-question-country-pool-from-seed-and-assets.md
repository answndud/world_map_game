# 국기 자산과 country seed를 합쳐 출제 가능 국가 pool 만들기

## 왜 이 조각이 필요한가

`FlagAssetCatalog`만 있으면 아직 국기 게임을 열 수는 없다.

이유는 간단하다.

- manifest에 국기 파일 경로가 있다고 해서
- 그 나라가 실제 `country` 시드에 있고
- 게임에서 바로 문제로 낼 수 있다는 뜻은 아니기 때문이다.

국기 게임을 설명 가능하게 만들려면 먼저 서버가 아래 질문에 답할 수 있어야 한다.

- 지금 국기 게임에 실제로 출제 가능한 국가는 몇 개인가?
- 그 국가는 어떤 기준으로 골라졌는가?
- 자산이 있는 나라와 seed에 있는 나라가 어긋나면 어디서 막히는가?

이번 조각은 그 답을 `FlagQuestionCountryPoolService`로 고정하는 작업이다.

## 이번에 바뀐 파일

- [FlagQuestionCountryPoolService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)
- [FlagQuestionCountryPoolView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolView.java)
- [FlagQuestionCountryView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryView.java)
- [FlagQuestionCountryContinentCountView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryContinentCountView.java)
- [FlagQuestionCountryPoolServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolServiceIntegrationTest.java)

기존 자산 catalog는 그대로 재사용한다.

- [FlagAssetCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)
- [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)

## 설계 핵심

### 1. source of truth는 두 개다

국기 게임에서 출제 가능 국가는 두 저장소를 동시에 만족해야 한다.

1. `country` seed에 존재해야 한다.
2. `flag-assets.json`과 실제 SVG 파일이 존재해야 한다.

즉, 출제 가능 국가는

`country seed ∩ flag manifest ∩ 실제 파일 존재`

교집합이다.

이 기준을 그냥 말로 남기지 않고, 서버 코드로 계산하는 read model을 만들었다.

### 2. catalog와 pool은 역할이 다르다

여기서 중요한 분리가 하나 있다.

- `FlagAssetCatalog`
  - manifest와 정적 파일 무결성을 검증한다.
- `FlagQuestionCountryPoolService`
  - 그 catalog를 `country` 시드와 합쳐 실제 출제 가능 국가 목록을 만든다.

둘을 합쳐 버리면 `자산 검증`과 `게임용 문제 pool 계산`이 섞여 버린다.

이번에는 이 둘을 분리해서, 다음 `flag` game mode가 시작될 때는 `FlagQuestionCountryPoolService`만 읽으면 되게 만들었다.

## 현재 데이터 흐름

이번 조각은 아직 public HTTP 요청이 없다.

대신 서버 내부 흐름은 이렇다.

```text
FlagQuestionCountryPoolService.loadPool()
  -> CountryRepository.findAllByOrderByNameKrAsc()
  -> FlagAssetCatalog.findByIso3Code()
  -> 교집합 국가만 FlagQuestionCountryView로 매핑
  -> continentCounts 계산
  -> FlagQuestionCountryPoolView 반환
```

즉, 지금은 `게임 화면`을 여는 작업이 아니라, 게임이 시작되기 전에 신뢰할 수 있는 출제 가능 pool을 만드는 작업이다.

## 읽기 모델에 무엇을 넣었나

`FlagQuestionCountryView`에는 다음 정보를 넣었다.

- `countryId`
- `iso3Code`
- `countryNameKr`
- `countryNameEn`
- `continent`
- `flagRelativePath`
- `flagFormat`

이렇게 해 둔 이유는 다음 단계 skeleton에서

- 문제로 어떤 나라를 고를지
- 보기를 어떤 이름으로 보여 줄지
- 같은 대륙 distractor를 쓸지
- 정적 이미지를 어떤 경로로 노출할지

를 추가 조회 없이 바로 결정할 수 있게 하기 위해서다.

## 예외 처리는 왜 이렇게 했나

두 종류로 나눴다.

### 1. manifest에는 있는데 seed에는 없는 ISO3

이 경우는 조용히 무시하지 않고 예외로 실패한다.

이유는 자산 준비와 country seed가 어긋난 상태이기 때문이다.
이걸 숨기면 나중에 `왜 어떤 국기는 출제 안 되지?`를 찾기 어려워진다.

### 2. seed에는 있는데 국기 자산이 없는 국가

이건 현재 1차 sample 상태에서는 정상이다.

예를 들어 지금은 194개 독립국 전체를 다 넣은 게 아니라 sample SVG 12개만 먼저 저장소에 포함했다.

그래서 이 경우는 실패가 아니라 `아직 출제 가능 pool에 안 들어감`으로 처리한다.

## 테스트로 무엇을 막았나

[FlagQuestionCountryPoolServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolServiceIntegrationTest.java)에서 아래를 고정했다.

- 출제 가능 국가 수가 `12`개인지
- ISO3 목록이 manifest와 맞는지
- 모든 경로가 `/images/flags/*.svg`인지
- 대륙별 count가 `EUROPE 11 / ASIA 1`인지
- `JPN` lookup이 가능한지
- `KOR`처럼 아직 자산이 없는 나라는 pool에서 빠지는지

즉, 다음에 누가 국기 파일을 더 넣거나 빼더라도 이 서비스가 어떤 나라를 출제 가능 대상으로 보는지 테스트로 바로 확인할 수 있다.

## 지금 상태를 어떻게 설명하면 되나

아직 국기 게임은 public으로 열리지 않았다.

현재 상태는 이렇다.

- asset catalog 있음
- sample SVG 12개 있음
- seed와 자산 교집합을 계산하는 pool service 있음
- 다음 단계는 `flag` game mode skeleton

즉, 화면보다 먼저 `문제로 낼 수 있는 나라 목록`이 서버에서 설명 가능한 상태를 만든 것이다.

## 면접에서 이렇게 말할 수 있다

> 국기 게임은 규칙보다 에셋 재현성이 더 중요해서, 바로 컨트롤러부터 만들지 않았습니다. 먼저 `FlagAssetCatalog`로 manifest와 정적 SVG를 검증하고, 그다음 `FlagQuestionCountryPoolService`로 `country seed ∩ flag manifest ∩ 실제 파일 존재` 교집합을 계산해서 실제 출제 가능 국가 pool을 서버 read model로 고정했습니다. 지금은 이 pool이 12개 국가와 대륙 분포를 정확히 설명해 주기 때문에, 다음 단계에서 flag game mode를 열어도 어떤 국가가 왜 문제에 들어가는지 명확하게 설명할 수 있습니다.
