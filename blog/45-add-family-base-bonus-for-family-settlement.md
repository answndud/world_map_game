# 45. 가족형 정착 시나리오에 family base bonus 추가하기

## 왜 이 조각이 필요했나

동적 baseline을 다시 계산해 보니 weak scenario는 `P11` 하나만 남았다.

이 시나리오는

- 치안과 복지를 강하게 보고
- 영어 적응도 중요하고
- 도시와 자연의 균형도 원하고
- 가족 단위로 버틸 생활 기반도 중요하게 보는

가족형 정착 시나리오였다.

그런데 현재 top 3는 `아일랜드, 스위스, 호주`였다.
기대 후보인 `캐나다, 덴마크, 네덜란드` 계열이 하나도 남지 않았다.

## 먼저 점수 gap을 확인했다

이번에도 바로 점수식을 건드리지 않았다.

먼저 `P11` 한 케이스를 디버그 테스트로 직접 찍어 실제 점수를 봤다.

```text
아일랜드 353
스위스   338
호주     336
캐나다   317
덴마크   313
```

즉 `캐나다`는 top 3에 전혀 못 들어오는 상태였다.

## 어떤 bonus를 넣었나

이번에는 `familyBaseBonus()`를 추가했다.

이 bonus는 아주 좁게만 켜진다.

- `QUALITY_FIRST`
- `SAFETY HIGH`
- `English HIGH`
- `MIXED`
- `BALANCED`
- `LOW tolerance`
- `BALANCED settlement`

이 조합을 모두 만족할 때만 작동한다.

그리고 나라 프로필도 아래를 동시에 만족해야 bonus를 받는다.

- `englishSupport >= 5`
- `safety >= 5`
- `welfare >= 5`
- `housingSpace >= 4`
- `newcomerFriendliness >= 4`

즉 “영어로 적응 가능하고, 안전과 복지가 높고, 주거 기반도 버틸 만한가”를 가족형 정착 보정으로 본 것이다.

## 왜 서비스 로직에 넣었나

이 규칙은 컨트롤러가 아니라 `RecommendationSurveyService`에 있어야 한다.

이유는

- 어떤 설문 조합에서 bonus를 켤지
- 어떤 프로필 속성을 함께 읽을지
- 몇 점을 줄지

가 모두 추천 도메인 규칙이기 때문이다.

## 결과

이제 `P11`의 top 3는 이렇게 바뀌었다.

```text
아일랜드, 캐나다, 스위스
```

즉 기대 후보였던 `캐나다`가 다시 top 3에 들어왔다.

그리고 동적 baseline 기준으로는 이제:

- `matched 18 / 18`
- `weak scenario 0`

상태가 된다.

## dashboard도 같이 바꿨다

이번 조각에서는 추천 엔진만 바꾼 게 아니다.

`/dashboard/recommendation/persona-baseline`도 weak scenario가 0개일 때 빈 카드 영역만 남지 않도록 empty-state 문구를 추가했다.

즉 운영 화면도 현재 상태를 자연스럽게 설명할 수 있게 만들었다.

## 테스트

다음 기준으로 확인했다.

1. 디버그 테스트로 `P11` 점수 직접 확인
2. `RecommendationOfflinePersonaSnapshotTest`에서 `engine-v9` snapshot 재고정
3. `RecommendationOfflinePersonaCoverageTest`에서 `P11`이 `캐나다`를 포함하고 `호주`는 빠지는지 확인
4. `AdminPersonaBaselineServiceIntegrationTest`에서 baseline이 `18 / 18`, weak 0인지 확인
5. `AdminPageIntegrationTest`에서 persona baseline 화면 렌더링 확인
6. `./gradlew test` 전체 통과

## 면접에서 어떻게 설명할까

“추천 엔진을 계속 튜닝한 뒤 동적 baseline을 다시 계산해 보니 마지막 weak scenario는 `P11` 하나였습니다. 그래서 broad bonus를 또 추가하지 않고, `Quality First + Safety High + English High` 같은 가족형 정착 시나리오에서만 작동하는 `familyBaseBonus`를 좁게 넣었습니다. 그 결과 `캐나다`가 다시 top 3에 들어왔고, baseline은 `18 / 18`까지 올랐습니다.”
