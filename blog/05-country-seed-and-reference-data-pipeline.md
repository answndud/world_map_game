# [Spring Boot 게임 플랫폼 포트폴리오] 05. 국가 시드와 기준 데이터 파이프라인을 어떻게 만들었는가

## 1. 이번 글에서 풀 문제

WorldMap은 게임 플랫폼이지만, 실제로는 **국가 데이터 제품**이기도 합니다.  
나라 이름, 대륙, 수도, 기준 좌표, 인구 연도와 값이 흔들리면 위치 게임, 인구수 게임, 추천, 통계가 전부 흔들립니다.

그래서 이 글은 아래 문제를 닫습니다.

- 왜 국가 정보를 코드 상수나 CSV가 아니라 JSON seed + validator + initializer 파이프라인으로 다뤄야 하는가
- 왜 `Country`를 단순 lookup 테이블이 아니라 모든 게임의 기준 reference entity로 봐야 하는가
- 왜 seed loading이 "처음 insert만"이 아니라 **동기화(sync)** 여야 하는가
- 왜 asset generation script와 runtime seed loading을 분리해야 하는가

## 2. 최종 도착 상태

이 글이 끝났을 때 아래 상태가 있어야 합니다.

- [countries.json](../src/main/resources/data/countries.json)이 194개 주권 국가와 메타데이터를 담는다
- [Country.java](../src/main/java/com/worldmap/country/domain/Country.java)가 `iso2`, `iso3`, 한/영 이름, 대륙, 수도, 기준 좌표, 인구를 가진 기준 엔티티가 된다
- [CountrySeedReader.java](../src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java)가 JSON을 문서 객체로 읽는다
- [CountrySeedValidator.java](../src/main/java/com/worldmap/country/application/CountrySeedValidator.java)가 형식, 범위, 중복, 메타데이터를 검증한다
- [CountrySeedInitializer.java](../src/main/java/com/worldmap/country/application/CountrySeedInitializer.java)가 startup 시 seed를 DB와 동기화한다
- [CountryCatalogService.java](../src/main/java/com/worldmap/country/application/CountryCatalogService.java)와 [CountryApiController.java](../src/main/java/com/worldmap/country/web/CountryApiController.java)가 `/api/countries` 조회를 제공한다
- [generate_country_assets.py](../scripts/generate_country_assets.py)가 외부 데이터 소스를 읽어 seed/geojson 자산을 다시 생성할 수 있다

즉, 이 단계의 목표는 "나라 목록이 있다"가 아니라 **모든 게임과 화면이 공유하는 기준 데이터 체인**을 만드는 것입니다.

## 3. 먼저 알아둘 개념

### 3-1. reference data

`Country`는 플레이어가 바꾸는 데이터가 아니라, 애플리케이션이 참조하는 기준 데이터입니다.  
하지만 기준 데이터라고 해서 아무렇게나 넣으면 안 됩니다. 게임 품질 전체가 이 데이터에 달려 있기 때문입니다.

### 3-2. seed synchronization

seed loading은 단순 최초 insert가 아니라 현재 DB 상태를 **seed 문서와 동기화**해야 합니다.

- 새 국가는 추가
- 기존 국가는 갱신
- seed에서 빠진 국가는 삭제

### 3-3. metadata

인구 데이터는 값만 있으면 끝이 아닙니다.  
`populationYear` 같은 메타데이터가 있어야 "이 숫자가 어느 연도 기준인가"를 설명할 수 있습니다.

### 3-4. asset generation vs runtime loading

외부 API에서 raw 데이터를 모아 가공하는 과정과, 앱 기동 시 검증된 seed를 읽는 과정은 다른 책임입니다.

- script: 외부 세계에서 데이터를 모아 산출물 생성
- runtime: 저장소 안의 산출물을 읽어 DB와 동기화

## 4. 이번 글에서 다룰 파일

- [Country.java](../src/main/java/com/worldmap/country/domain/Country.java)
- [Continent.java](../src/main/java/com/worldmap/country/domain/Continent.java)
- [CountryReferenceType.java](../src/main/java/com/worldmap/country/domain/CountryReferenceType.java)
- [CountryRepository.java](../src/main/java/com/worldmap/country/domain/CountryRepository.java)
- [CountryCatalogService.java](../src/main/java/com/worldmap/country/application/CountryCatalogService.java)
- [CountrySeedInitializer.java](../src/main/java/com/worldmap/country/application/CountrySeedInitializer.java)
- [CountrySeedProperties.java](../src/main/java/com/worldmap/country/application/CountrySeedProperties.java)
- [CountrySeedValidator.java](../src/main/java/com/worldmap/country/application/CountrySeedValidator.java)
- [CountrySeedReader.java](../src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java)
- [CountryApiController.java](../src/main/java/com/worldmap/country/web/CountryApiController.java)
- [CountrySummaryView.java](../src/main/java/com/worldmap/country/application/CountrySummaryView.java)
- [CountryDetailView.java](../src/main/java/com/worldmap/country/application/CountryDetailView.java)
- [countries.json](../src/main/resources/data/countries.json)
- [generate_country_assets.py](../scripts/generate_country_assets.py)
- [CountrySeedIntegrationTest.java](../src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java)

## 5. 핵심 도메인 모델 / 상태

### 5-1. `Country`

[Country.java](../src/main/java/com/worldmap/country/domain/Country.java)는 later stage 전체가 기대는 기준 엔티티입니다.

필드:

- `iso2Code`
- `iso3Code`
- `nameKr`
- `nameEn`
- `continent`
- `capitalCity`
- `capitalCityKr`
- `referenceLatitude`
- `referenceLongitude`
- `referenceType`
- `population`
- `populationYear`

중요한 점은, 이 엔티티가 단순 국명 사전이 아니라 **위치 게임 좌표 기준점 + 수도 퀴즈 정답 + 인구 게임 출제 기준 + 추천/통계 reference**를 동시에 담당한다는 것입니다.

### 5-2. `Continent`, `CountryReferenceType`

[Continent.java](../src/main/java/com/worldmap/country/domain/Continent.java)는 6개 대륙 enum입니다.

- `AFRICA`
- `ASIA`
- `EUROPE`
- `NORTH_AMERICA`
- `OCEANIA`
- `SOUTH_AMERICA`

[CountryReferenceType.java](../src/main/java/com/worldmap/country/domain/CountryReferenceType.java)는 현재 `CAPITAL_CITY`만 갖습니다.  
즉, 위치 게임 Level 1의 기준 좌표는 "수도 기준점"이라는 뜻입니다.

### 5-3. `CountrySeedDocument`

[CountrySeedReader.java](../src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java)는 JSON을 아래 문서 구조로 읽습니다.

- `SeedMetadata`
- `CountrySeedItem`

여기서 `metadata.populationYear`가 매우 중요합니다.  
인구 게임과 stats 문구는 이 메타데이터를 근거로 연도를 표시합니다.

### 5-4. seed synchronization state

`CountrySeedInitializer`는 DB의 현재 `country` 테이블과 seed 문서를 비교해 아래를 계산합니다.

- `insertedCount`
- `updatedCount`
- `countriesToDelete`

즉, 이 단계의 진짜 상태 변화는 "seed를 읽었다"가 아니라 **DB 기준 데이터 집합을 현재 문서와 일치시키는 것**입니다.

## 6. 설계 구상

### 왜 `Country`를 먼저 강하게 만들어야 하는가

게임부터 만들고 국가 데이터는 나중에 정리하는 방식은 처음에는 빨라 보이지만, later stage에서 아래가 꼬입니다.

- 위치 게임은 좌표를 어디서 읽나?
- 수도 게임은 어떤 국가가 출제 대상인가?
- 인구 게임은 어느 연도 인구를 쓰나?
- 추천 결과 카드에는 어느 이름과 어느 수도를 표기하나?

그래서 WorldMap은 게임보다 앞서 **기준 데이터의 품질**을 먼저 세웁니다.

### 왜 seed를 JSON 문서로 뒀는가

JSON을 택한 이유는 단순합니다.

- Git diff가 읽기 쉽다
- metadata와 item list를 같이 담기 쉽다
- Python script와 Spring ObjectMapper가 모두 다루기 쉽다

CSV로는 metadata와 nested structure 표현이 불편했고, SQL seed로는 편집성과 설명성이 떨어졌습니다.

### 왜 runtime에서 검증을 다시 하는가

script가 seed를 생성했더라도, runtime에서 신뢰만 하면 안 됩니다.

실수 가능성:

- 중복 ISO 코드
- 잘못된 위경도
- population year 누락
- 빈 문자열/누락 필드

그래서 seed를 읽은 뒤 validator를 한 번 더 태워, 애플리케이션 기동 시점에 잘못된 reference data를 실패시키는 편이 낫습니다.

### 왜 "insert only"가 아니라 sync인가

기준 데이터는 시간이 지나면 바뀝니다.

- 인구 수치 갱신
- 한글 표기 수정
- 잘못된 행 제거

만약 초기 insert만 하고 이후는 수동으로 관리하면, DB와 seed 문서가 점점 벌어집니다.  
WorldMap은 seed 문서가 source of truth이고, startup 시 DB는 그 상태로 맞춰집니다.

## 7. 코드 설명

### 7-1. `Country`: unique constraint와 synchronize 메서드

[Country.java](../src/main/java/com/worldmap/country/domain/Country.java)는 `iso2_code`, `iso3_code`에 unique constraint를 둡니다.  
그리고 중요한 메서드 두 개가 있습니다.

- `create(...)`
- `synchronize(...)`

`create`는 새 row를 만들고, `synchronize`는 기존 row를 seed 값으로 업데이트합니다.  
이 둘을 분리했기 때문에 initializer가 insert/update를 동일한 흐름 안에서 설명할 수 있습니다.

### 7-2. `CountrySeedReader`: classpath resource를 문서 객체로 읽기

[CountrySeedReader.java](../src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java)는 `ResourceLoader`와 `ObjectMapper`를 이용해 `classpath:data/countries.json`을 읽습니다.

중요 포인트:

- resource가 없으면 `IllegalStateException`
- parse 중 예외가 나도 `IllegalStateException`
- unknown field는 `@JsonIgnoreProperties(ignoreUnknown = true)`로 무시

즉, reader는 파일 입출력과 JSON mapping 책임만 갖습니다.

### 7-3. `CountrySeedValidator`: 품질 게이트

[CountrySeedValidator.java](../src/main/java/com/worldmap/country/application/CountrySeedValidator.java)는 현재 아래를 검증합니다.

- 문서 null 여부
- metadata 존재 여부
- `populationYear >= 1900`
- 국가 목록 비어 있지 않음
- `iso2Code` 2자리 대문자
- `iso3Code` 3자리 대문자
- iso2/iso3 중복 없음
- `continent`, `referenceType` 존재
- 위도 `-90 ~ 90`
- 경도 `-180 ~ 180`
- `population > 0`

이 검증이 없으면 later stage에서 에러가 "게임이 이상하다"로 보이지, 진짜 원인이 reference data라는 사실을 놓치기 쉽습니다.

### 7-4. `CountrySeedInitializer`: startup sync

[CountrySeedInitializer.java](../src/main/java/com/worldmap/country/application/CountrySeedInitializer.java)는 `ApplicationRunner` + `@Order(10)`입니다.

흐름:

1. `worldmap.seed.countries.enabled` 확인
2. reader로 문서 읽기
3. validator 실행
4. DB의 기존 국가를 `iso3 -> Country` 맵으로 준비
5. seed 항목을 돌며 insert/update 대상 계산
6. seed에 없는 기존 row는 `deleteAllInBatch`
7. `saveAll`

즉, 이 클래스는 단순 seed loader가 아니라 **reference data reconciliation service** 역할을 합니다.

### 7-5. `CountryCatalogService`와 `CountryApiController`

runtime에서 읽기 path는 아래처럼 나뉩니다.

- `/api/countries` -> [CountrySummaryView.java](../src/main/java/com/worldmap/country/application/CountrySummaryView.java)
- `/api/countries/{iso3Code}` -> [CountryDetailView.java](../src/main/java/com/worldmap/country/application/CountryDetailView.java)

[CountryCatalogService.java](../src/main/java/com/worldmap/country/application/CountryCatalogService.java)가 repository 결과를 view record로 바꾸고, [CountryApiController.java](../src/main/java/com/worldmap/country/web/CountryApiController.java)는 HTTP 진입점만 담당합니다.

이렇게 service에서 view를 조립하는 이유는 later stage에서 controller를 얇게 유지하기 위해서입니다.

### 7-6. `generate_country_assets.py`: 외부 데이터에서 seed/geojson 산출물 만들기

[generate_country_assets.py](../scripts/generate_country_assets.py)는 runtime 코드가 아니라 **소스 데이터 생성기**입니다.

현재 script는 아래 외부 소스를 조합합니다.

- Rest Countries core fields
- Rest Countries coordinate fields
- World Bank country metadata
- World Bank population indicator

그리고 산출물을 만듭니다.

- `src/main/resources/data/countries.json`
- `src/main/resources/static/data/world-countries-level1.geojson`
- `src/main/resources/static/data/active-countries.geojson`

특히 `active-countries.geojson`은 위치 게임 Level 1의 주요 국가 pool과 직접 연결됩니다.

## 8. 요청 흐름 / 상태 변화

이 글은 두 개의 흐름을 같이 봐야 합니다.

### 8-1. asset generation flow

```text
generate_country_assets.py 실행
-> 외부 공공 API 호출
-> 국가 이름 / 좌표 / 인구 데이터 병합
-> countries.json 생성
-> level1 / active geojson 생성
```

이 단계는 저장소 바깥 세계에서 **산출물을 만드는 흐름**입니다.

### 8-2. runtime seed sync flow

```text
Spring Boot startup
-> CountrySeedInitializer.run()
-> CountrySeedReader.read(location)
-> CountrySeedValidator.validate(document)
-> DB existing countries 조회
-> insert / update / delete 대상 계산
-> country 테이블 동기화
```

### 8-3. read API flow

```text
GET /api/countries
-> CountryCatalogService.getCountries()
-> CountrySummaryView 목록 반환

GET /api/countries/{iso3Code}
-> CountryCatalogService.getCountry(iso3Code)
-> CountryDetailView 반환
```

즉, seed pipeline은 "기동 시 동기화"와 "기동 후 조회" 두 흐름으로 나뉩니다.

## 9. 실패 케이스 / 예외 처리

- seed 파일이 없으면: reader가 `IllegalStateException`
- 메타데이터가 비어 있으면: validator가 기동 실패
- ISO 코드가 중복이면: validator가 기동 실패
- 위경도 범위가 틀리면: validator가 기동 실패
- seed에서 빠진 예전 국가 row가 남아 있으면: initializer가 delete로 정리
- 없는 ISO3로 `/api/countries/{iso3}`를 치면: `ResourceNotFoundException` -> `404`

기준 데이터는 "나중에 발견되면 큰일 나는 문제"라서, 가능한 한 기동 시점에 빨리 실패시키는 편이 낫습니다.

## 10. 테스트로 검증하기

이 단계의 핵심 테스트는 [CountrySeedIntegrationTest.java](../src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java)입니다.

무엇을 검증하나:

- startup seed 로딩 후 `country` row 수가 `194`인지
- `KOR`, `FRA`, `NOR` 같은 대표 국가가 기대한 값으로 들어갔는지
- 기존 잘못된 row가 seed 동기화 후 정리되는지
- `/api/countries/KOR`가 상세 정보를 반환하는지
- 모르는 ISO3는 `404`와 적절한 메시지를 주는지

즉, 이 테스트 하나가 아래를 동시에 막아 줍니다.

- seed 품질 문제
- sync 불일치 문제
- read API 오동작

반대로 이 테스트 하나만으로 아래까지 자동 증명되는 것은 아닙니다.

- `generate_country_assets.py`가 외부 API 응답 변화까지 포함해 항상 같은 산출물을 만드는지
- geojson 자산이 모든 브라우저 셸에서 시각적으로 문제없는지
- 실제 운영에서 seed 갱신 주기와 수동 검수 절차가 충분한지

실행 명령:

```bash
./gradlew test --tests com.worldmap.country.CountrySeedIntegrationTest
```

## 11. 회고

국가 데이터는 초반에 만들면 boring해 보이지만, 실제로는 WorldMap 전체를 지탱하는 기반입니다.

이 구조의 장점은 다음입니다.

- 게임과 추천이 같은 기준 데이터를 본다
- seed 문서가 source of truth가 된다
- runtime에서 데이터 품질을 다시 검증한다
- 조회 API까지 함께 마련돼 디버깅이 쉽다

### 현재 구현의 한계

- 현재 `CountryReferenceType`은 `CAPITAL_CITY`만 사용한다
- 수도 기준점만 쓰므로, later stage에서 centroid나 polygon centroid 전략이 필요하면 확장해야 한다
- 외부 데이터 script는 네트워크 의존적이므로 주기적 갱신 전략은 별도 운영 결정이 필요하다
- `CountrySeedIntegrationTest`는 runtime sync와 조회 API를 고정하지만, 외부 데이터 생성 스크립트의 live 네트워크 성공 여부까지 보장하지는 않는다

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 국가 데이터를 단순 상수로 두지 않고 JSON seed, validator, startup synchronizer, asset generation script로 나눠 reference data 품질을 먼저 고정했습니다.

### 12-2. 30초 답변

위치 게임, 수도 퀴즈, 인구 게임, 추천이 모두 같은 국가 데이터를 봐야 해서 `Country`를 기준 엔티티로 먼저 만들었습니다. `generate_country_assets.py`가 외부 공공 데이터를 모아 `countries.json`과 geojson 자산을 만들고, 앱 기동 시 `CountrySeedInitializer`가 reader와 validator를 거쳐 DB를 seed 문서와 동기화합니다. 이때 insert만 하는 게 아니라 update와 delete까지 처리해서 seed 문서가 source of truth가 되게 했고, `/api/countries`로 런타임 조회도 가능하게 했습니다.

### 12-3. 예상 꼬리 질문

- 왜 게임부터 만들지 않고 country seed를 먼저 만들었나요?
- 왜 JSON seed를 썼나요?
- 왜 startup 때 검증을 다시 하나요?
- 왜 seed loading을 insert-only가 아니라 sync로 만들었나요?

## 13. 시작 상태

- 국가 정보가 코드 상수나 임시 자료에 흩어져 있다
- later stage 게임들이 어떤 기준 데이터를 공유하는지 정의돼 있지 않다
- 외부 데이터 산출과 앱 runtime loading 책임이 섞여 있다

## 14. 이번 글에서 바뀌는 파일

- `src/main/java/com/worldmap/country/domain/**`
- `src/main/java/com/worldmap/country/application/**`
- `src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java`
- `src/main/java/com/worldmap/country/web/CountryApiController.java`
- `src/main/resources/data/countries.json`
- `src/main/resources/static/data/world-countries-level1.geojson`
- `src/main/resources/static/data/active-countries.geojson`
- `scripts/generate_country_assets.py`
- `src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java`

## 15. 구현 체크리스트

1. `Country` 엔티티와 enum을 만든다
2. seed 문서 구조와 properties를 만든다
3. reader로 JSON parsing을 구현한다
4. validator로 형식/범위/중복 검증을 구현한다
5. initializer로 insert/update/delete sync를 구현한다
6. `CountryCatalogService`와 `/api/countries` 조회를 만든다
7. 외부 데이터 script로 seed와 geojson 산출물을 만든다
8. integration test로 로딩, sync, API까지 고정한다

## 16. 실행 / 검증 명령

```bash
./gradlew test --tests com.worldmap.country.CountrySeedIntegrationTest
```

seed 산출물을 다시 만들고 싶다면:

```bash
python3 scripts/generate_country_assets.py
```

## 17. 산출물 체크리스트

- `country` 테이블이 194개 기준 국가를 가진다
- 한글/영문 이름, 수도, 좌표, 인구, 연도를 함께 저장한다
- startup 시 seed 문서와 DB가 동기화된다
- `/api/countries`와 `/api/countries/{iso3}`로 기준 데이터를 조회할 수 있다
- 위치 게임용 geojson 자산도 같은 source에서 파생된다

## 18. 글 종료 체크포인트

- 왜 `Country`는 단순 lookup table이 아니라 전체 플랫폼의 reference entity인가
- 왜 seed loading에 validator와 synchronizer가 모두 필요한가
- 왜 asset generation과 runtime loading을 분리해야 하는가
- later stage 게임들이 왜 이 데이터 파이프라인 위에 기대는가

## 19. 자주 막히는 지점

- 국가 데이터를 게임별 상수로 나눠 넣는 것
- seed를 최초 insert만 하고 이후 drift를 방치하는 것
- population year 같은 메타데이터를 빼고 숫자만 저장하는 것
- 외부 API 호출 로직을 runtime startup 코드 안으로 넣는 것
