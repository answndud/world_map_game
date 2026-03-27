# 국기 게임 자산 파이프라인 계획

## 목적

국기 보고 나라 맞히기 게임은 규칙 자체보다
`국기 이미지 자산을 어떤 기준으로 저장하고 검증할 것인가`가 먼저 정리돼야 한다.

현재 위치 / 수도 / 인구수 / 인구 비교 게임은 모두 `country` 데이터만으로 시작할 수 있었지만,
국기 게임은 정답 판정 이전에 아래가 필요하다.

- 국기 자산 source
- 저장 위치
- 어떤 국가가 출제 가능한지 결정하는 기준
- local/demo 환경에서 재현 가능한 패키징 방식

이 문서는 그 기준을 먼저 고정한다.

## 왜 DB보다 정적 자산 파이프라인이 먼저인가

국기 게임은 도메인 규칙보다 에셋 품질과 재현성이 더 큰 리스크다.

- DB에 `flagUrl`만 넣으면 외부 의존성이 생긴다.
- CDN 링크나 써드파티 URL은 나중에 끊기거나 바뀔 수 있다.
- local/demo 환경에서도 같은 문제를 그대로 재현하려면 저장소 안에 자산이 있거나, 재생성 스크립트가 있어야 한다.

그래서 첫 결정은
`국기 자산은 앱 내부 정적 파일 + manifest로 관리한다`
는 것이다.

## 1차 결정

### 1. 자산 저장 위치

- 실제 파일: `src/main/resources/static/images/flags/{iso3}.svg`
- manifest: `src/main/resources/data/flag-assets.json`

이유:

- 정적 파일은 SSR/바닐라 JS 구조와 가장 잘 맞는다.
- 경로가 단순해서 템플릿과 JS에서 바로 쓸 수 있다.
- local/demo 부팅 시 외부 네트워크가 없어도 재현 가능하다.

### 2. 기본 포맷

- 1차 기본 포맷은 `svg`

이유:

- 국기는 벡터가 가장 자연스럽다.
- 194개 독립국 전체를 넣어도 파일 크기를 비교적 작게 유지할 수 있다.
- 브라우저 렌더링이 단순하고, retina 대응도 따로 고민할 일이 적다.

예외:

- 특정 국기가 SVG 확보가 어렵다면 임시로 PNG fallback을 허용할 수 있다.
- 다만 첫 구현 기준은 `svg only`를 목표로 한다.

### 3. 현재 source snapshot

- 현재 저장소는 `flagcdn.com`에서 내려받은 SVG snapshot 36개를 `static/images/flags/` 아래에 포함한다.
- `scripts/fetch_flag_assets.py`가 선택된 ISO3 목록을 기준으로 SVG와 `flag-assets.json`을 함께 재생성한다.

즉, local/demo 부팅은 여전히 네트워크 없이 가능하고,
자산 확대나 갱신이 필요할 때만 스크립트를 다시 실행하는 구조다.

### 4. 출제 가능 국가 기준

국기 게임의 출제 가능 국가는

- `country` 시드에 존재하고
- `flag-assets.json`에도 존재하고
- 실제 정적 파일이 존재하는 국가

의 교집합으로 잡는다.

즉, 194개 country seed와 국기 자산 수가 일치하지 않더라도
게임은 manifest에 있는 국가만으로 먼저 운영할 수 있다.

## manifest 설계 초안

`flag-assets.json`은 아래 shape를 목표로 한다.

```json
[
  {
    "iso3Code": "KOR",
    "relativePath": "/images/flags/kor.svg",
    "format": "svg",
    "source": "local-static",
    "licenseNote": "public-domain-or-compatible"
  }
]
```

1차에서 필요한 최소 필드는 아래다.

- `iso3Code`
- `relativePath`
- `format`

나머지 필드는 설명용이다.

## 왜 country 테이블에 바로 넣지 않는가

지금은 `country`를 게임 공통 도메인 데이터로 유지하는 편이 더 중요하다.

국기 자산은

- 국가 본질 데이터라기보다
- 정적 에셋과 배포 패키징 문제

에 가깝다.

그래서 1차는 DB 컬럼 추가보다
`manifest + file existence validation`
이 더 단순하고 설명 가능하다.

필요하면 나중에 `country.flagAssetPath`를 읽기 모델 캐시로 넣을 수 있지만,
첫 조각은 그렇게까지 가지 않는다.

## local/demo 재현 원칙

국기 게임은 local/demo에서도 바로 확인 가능해야 한다.

그래서 아래 원칙을 둔다.

- 국기 파일은 저장소 안에 둔다.
- local 부팅 시 외부 다운로드를 강제하지 않는다.
- 필요하면 별도 스크립트로 자산 카탈로그를 재생성하되,
  앱 부팅 자체는 스크립트 없이 가능해야 한다.

현재 재생성 명령:

```bash
python3 scripts/fetch_flag_assets.py
```

즉, demo 재현은
`git clone -> local profile bootRun`
만으로 충분해야 한다.

## 다음 구현 순서

국기 게임은 아래 순서로 여는 것이 맞다.

1. `flag-assets.json`과 `static/images/flags/` 기준 고정
2. `FlagAssetCatalog` read model 추가
3. startup 또는 테스트에서 manifest / file existence 검증
4. 출제 가능 flag country pool 계산
5. 그 다음에야 `flag` game mode vertical slice 구현

즉, 다음 작은 코드 조각은
`국기 이미지 하나를 보여 주는 화면`이 아니라
`국기 자산 catalog와 검증기`다.

## 면접에서 이렇게 설명할 수 있다

> 국기 게임은 규칙보다 에셋 재현성이 더 중요한 모드라고 봤습니다. 그래서 처음부터 외부 URL을 DB에 넣지 않고, 저장소 내부 정적 파일과 manifest를 source of truth로 두기로 했습니다. 이렇게 하면 local/demo 환경에서도 네트워크 없이 동일한 문제를 재현할 수 있고, 출제 가능 국가도 `country seed ∩ manifest ∩ 실제 파일 존재`로 명확하게 설명할 수 있습니다.
