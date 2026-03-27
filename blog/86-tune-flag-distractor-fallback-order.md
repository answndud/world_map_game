# 국기 게임 distractor fallback 순서를 지역 기준으로 다듬기

## 왜 이 조각이 필요한가

국기 자산 pool을 36개로 넓힌 뒤에도
보기 생성 규칙은 여전히 단순했다.

- 같은 대륙 국가를 먼저 모으고
- 부족하면 전세계 출제 가능 pool에서 보충한다

이 규칙은 유럽이나 아시아처럼 후보가 많은 대륙에서는 크게 문제 없었다.

하지만 북미와 오세아니아처럼 같은 대륙 후보가 적은 경우,
fallback이 너무 멀리 튀어 보기가 들쭉날쭉해질 수 있었다.

이번 조각의 목적은 이 부분을
`설명 가능한 서버 규칙`으로 한 단계 더 정리하는 것이다.

## 이번에 바뀐 파일

- [FlagGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameOptionGenerator.java)
- [FlagGameOptionGeneratorTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagGameOptionGeneratorTest.java)

같이 아래 문서도 현재 기준으로 맞췄다.

- [README.md](/Users/alex/project/worldmap/README.md)
- [PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)
- [50-current-state-rebuild-map.md](/Users/alex/project/worldmap/blog/50-current-state-rebuild-map.md)

## 무엇을 바꿨나

핵심 변경은 [FlagGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameOptionGenerator.java) 하나다.

이제 distractor 생성 순서는 이렇게 된다.

1. 같은 대륙 국가 먼저
2. 그래도 부족하면 target 대륙별 fallback 순서 적용
3. 그래도 부족할 때만 마지막에 전체 출제 가능 pool fallback

즉, 예전의

`same continent -> global fallback`

에서

`same continent -> regional fallback -> global fallback`

으로 바뀌었다.

## fallback 순서는 왜 이렇게 잡았나

이번 조각은 복잡한 지역 메타데이터를 새로 추가하지 않았다.

대신 현재 `Country.continent`만으로 설명 가능한 수준에서
가장 무난한 지역 fallback만 넣었다.

예를 들면:

- `OCEANIA -> ASIA -> NORTH_AMERICA -> EUROPE -> SOUTH_AMERICA -> AFRICA`
- `NORTH_AMERICA -> SOUTH_AMERICA -> EUROPE -> ASIA -> OCEANIA -> AFRICA`

이렇게 하면

- 오세아니아 target은 아시아 국기를 먼저 비교군으로 가져오고
- 북미 target은 남미 국기를 먼저 비교군으로 가져온다.

즉, 대륙 후보가 부족할 때도 전세계 랜덤보다 더 그럴듯한 비교군을 만들 수 있다.

## 왜 이 로직이 컨트롤러가 아니라 generator에 있어야 하나

이건 화면 연출이 아니라
`국기 게임 문제를 어떻게 생성할 것인가`에 대한 규칙이다.

따라서 이 규칙은
[FlagGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/web/FlagGameApiController.java)가 아니라
[FlagGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameOptionGenerator.java)에 있어야 한다.

컨트롤러는 요청을 서비스에 넘기기만 하고,

- 어떤 나라를 문제로 쓰는지
- 어떤 distractor 3개를 만들지
- 어떤 fallback 순서를 타는지

는 서버 게임 규칙으로 고정해야 테스트와 설명이 가능하다.

## 무엇을 테스트로 고정했나

[FlagGameOptionGeneratorTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagGameOptionGeneratorTest.java)에서 세 가지를 고정했다.

1. 유럽처럼 후보가 충분한 대륙은 same-continent distractor만 쓴다.
2. 오세아니아는 아시아를 유럽보다 먼저 fallback으로 쓴다.
3. 북미는 유럽보다 남미를 먼저 fallback으로 쓴다.

즉, 이번 조각은 “좋아졌을 것 같다” 수준이 아니라
fallback 품질 규칙을 테스트로 묶어 둔 것이다.

실행:

```bash
./gradlew test --tests com.worldmap.game.flag.application.FlagGameOptionGeneratorTest --tests com.worldmap.game.flag.application.FlagQuestionCountryPoolServiceIntegrationTest --tests com.worldmap.game.flag.FlagGameFlowIntegrationTest
./gradlew test
```

## 지금 상태를 어떻게 설명하면 되나

이제 국기 게임은 단순히
“36개 자산이 있다”에서 끝나지 않는다.

현재는

- 출제 가능 국기 국가 pool을 서버가 계산하고
- 같은 대륙을 먼저 쓰고
- 부족하면 지역적으로 가까운 fallback 순서를 타고
- 그래도 부족할 때만 전체 pool으로 내려가는

보기 생성 규칙을 가진다.

즉, asset 개수만 늘린 게 아니라
그 asset을 문제에 쓰는 방식도 더 설명 가능해졌다.

## 다음 단계

다음 후보는 둘 중 하나다.

- 국기 게임 난이도 단계와 결과 카피를 더 다듬기
- 신규 게임 3종의 홈 / 랭킹 / Stats 밀도를 다시 정리하기

## 면접에서 이렇게 설명할 수 있다

> 국기 자산 pool을 36개로 넓힌 뒤에도 same-continent 후보가 부족한 대륙은 여전히 보기 품질이 흔들릴 수 있었습니다. 그래서 `FlagGameOptionGenerator`를 `same continent -> 인접 대륙 -> 전체 pool` fallback 순서로 바꾸고, 오세아니아는 아시아를, 북미는 남미를 먼저 끌어오도록 테스트로 고정했습니다. 덕분에 국기 게임은 자산 수뿐 아니라 distractor 품질도 서버 규칙으로 설명할 수 있게 됐습니다.
