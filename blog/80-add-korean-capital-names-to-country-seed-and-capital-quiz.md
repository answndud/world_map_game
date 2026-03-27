# 수도 맞히기 seed에 한국어 수도명을 따로 넣고 게임 UI를 맞추기

수도 맞히기 게임을 열어 보면 UI는 한국어인데 보기와 정답 수도명은 영어였다.  
기능은 동작하지만 제품 완성도는 분명히 떨어지는 상태였다.

이번 조각의 목표는 단순 번역이 아니다.

- 영어 원본 수도명은 유지한다.
- 수도 맞히기 게임은 한국어 수도명만 읽게 만든다.
- 이 값은 런타임 번역이 아니라 seed 단계에서 고정한다.

즉, **표현 언어를 바꾸면서도 기존 도메인 호환성을 깨지 않는 방식**을 선택한 것이다.

## 왜 `capitalCity`를 덮어쓰지 않았나

이미 `country.capitalCity`는 여러 곳에서 읽고 있다.

- 수도 맞히기 게임
- 추천 결과 카드
- 국가 조회 API
- seed / test helper

여기서 영어 값을 한국어로 덮어쓰면, 지금 당장 수도 게임은 좋아져도 다른 read model까지 한꺼번에 흔들린다.

그래서 이번에는 `country`에 새 필드 `capitalCityKr`를 추가했다.

- 영어 reference 값: `capitalCity`
- 한국어 표시용 값: `capitalCityKr`

이렇게 나누면 수도 맞히기 게임만 한국어 표기를 쓰고, 나머지 기능은 기존 동작을 유지할 수 있다.

## 어떤 파일이 바뀌었나

### 1. seed와 도메인

- [Country.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/country/domain/Country.java)
- [CountrySeedReader.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java)
- [CountrySeedValidator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/country/application/CountrySeedValidator.java)
- [CountrySeedInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/country/application/CountrySeedInitializer.java)
- [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)

`Country`에 `capital_city_kr`를 추가했고, seed reader/validator/initializer도 새 필드를 읽도록 확장했다.

이제 startup에서 `countries.json`의 `capitalCityKr`가 DB로 같이 동기화된다.

### 2. 수도 맞히기 게임

- [CapitalGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)
- [CapitalGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameOptionGenerator.java)
- [CapitalGameStage.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/domain/CapitalGameStage.java)

이제 출제 국가 필터, 보기 생성, Stage snapshot 저장 모두 영어 `capitalCity`가 아니라 `capitalCityKr`를 읽는다.

즉:

1. seed에 한국어 수도명이 채워진다.
2. capital game이 그 필드를 읽는다.
3. state/answer/result payload에 한국어 수도명이 내려간다.

## 수도명은 어떻게 채웠나

수작업 194건은 유지보수가 어렵다.  
그래서 [sync_capital_city_kr.py](/Users/alex/project/worldmap/scripts/sync_capital_city_kr.py)를 추가했다.

이 스크립트는:

1. `countries.json`을 읽는다.
2. Wikidata country-capital 한국어 label을 가져온다.
3. `capitalCityKr`를 채운다.
4. 예외 국가는 수동 override로 보정한다.

예외가 필요한 경우는 크게 두 가지다.

- 현재 seed의 영어 수도명이 Wikidata의 “현재 공식 수도”와 다른 경우
  - 예: 부룬디, 적도 기니, 스리랑카, 남아공
- 한국어 label이 `서울특별시`, `도쿄도`, `베이징시`처럼 행정 단위 suffix를 포함하는 경우

그래서 스크립트는 `Wikidata 기본값 + 소수의 manual override` 조합으로 재생성 가능성을 유지한다.

## 요청 흐름은 어떻게 달라졌나

런타임 HTTP 흐름은 그대로다.

```text
POST /api/games/capital/sessions
-> CapitalGameService.startGuestGame()
-> createNextStage()
-> CapitalGameOptionGenerator.generate()
```

중요한 차이는 Stage를 만들 때 읽는 값이다.

이전:

- `Country.getCapitalCity()`

현재:

- `Country.getCapitalCityKr()`

즉, **요청 흐름은 그대로지만 read model의 source field가 바뀌었다**.

## 테스트는 무엇으로 막았나

### seed 검증

- [CountrySeedIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java)

여기서 `KOR` 국가가 `capitalCityKr = 서울`로 적재되는지 확인한다.

### 게임 응답 검증

- [CapitalGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/capital/CapitalGameFlowIntegrationTest.java)

여기서:

- `/state`의 보기 4개가 한글 수도명인지
- `/answer`의 `selectedCapitalCity`, `correctCapitalCity`가 한글인지

를 같이 확인한다.

즉, seed에 값이 있어도 실제 게임 응답이 영어로 남아 있으면 이 테스트에서 잡힌다.

## 이번 조각에서 중요한 설계 포인트

이번 작업은 “번역 문자열 추가”가 아니라,  
**공용 seed에서 원본값과 표시값을 분리한 것**이 핵심이다.

그래서 면접에서는 이렇게 설명하면 된다.

> 수도 맞히기 게임은 한국어 UI인데 수도명이 영어로 보이는 문제가 있었습니다.  
> 영어 `capitalCity`를 바로 덮어쓰면 추천/국가 조회까지 흔들릴 수 있어서, seed에 `capitalCityKr`를 따로 추가하고 capital game만 그 필드를 읽게 했습니다.  
> 한국어 수도명은 runtime 번역이 아니라 seed 재생성 스크립트로 미리 고정해 두어서, 서버는 여전히 정적 seed만 읽고도 안정적으로 한국어 수도 퀴즈를 낼 수 있습니다.
