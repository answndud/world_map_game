# Simple Account Progress Plan

## 목적

이 문서는 WorldMap의 8단계에서 붙일 `가벼운 계정 / 전적 유지 구조`를 정리한다.

핵심 방향은 두 가지다.

1. 비회원은 지금처럼 바로 플레이한다.
2. 기록을 오래 남기고 싶을 때만 로그인하게 만든다.

즉, 커뮤니티나 SNS처럼 개인정보가 핵심인 서비스가 아니라, `닉네임으로 점수를 쌓고 랭킹과 전적을 보는 게임 서비스`에 맞는 단순한 계정 구조를 택한다.

## 제품 방향

### 비회원

- 별도 가입 없이 바로 플레이
- 현재처럼 세션 기반으로 게임 진행
- 같은 브라우저 세션 안에서는 결과와 흐름 유지
- 브라우저 세션이 끝나면 기록 보장은 하지 않음

### 회원

- `닉네임 + 비밀번호`만으로 가입
- 로그인 후에는 플레이 기록이 계정에 누적
- 마이페이지에서 내 기록, 최고 점수, 최근 플레이 확인
- 랭킹에서도 같은 닉네임으로 계속 기록 축적

### 유도 문구

- 게임 종료 화면
  - `기록을 계속 남기려면 로그인하세요`
- 마이페이지 placeholder
  - `로그인하면 내 기록과 랭킹 변화를 계속 볼 수 있습니다`

## 왜 이메일 없는 단순 계정으로 가는가

이 프로젝트의 계정 목적은 아래뿐이다.

- 내 점수와 전적 누적
- 내 랭킹 확인
- 닉네임 유지

즉, 중요한 개인정보나 복구 프로세스가 핵심이 아니다.

그래서 MVP는 아래처럼 단순하게 간다.

- 로그인 ID = 닉네임
- 비밀번호만 저장
- 이메일 / 휴대폰 / 프로필 이미지 / 친구 기능 없음

## 계정 모델 초안

### `member`

- `id`
- `nickname`
- `passwordHash`
- `role`
  - `USER`, `ADMIN`
- `createdAt`
- `lastLoginAt`

MVP에서는 `nickname`이 곧 로그인 ID이자 공개 닉네임이다.

## 게스트와 회원을 같이 다루는 기준

게임/랭킹 레코드에는 아래 둘 중 하나만 가진다.

- `memberId`
- `guestSessionKey`

그리고 항상 `nicknameSnapshot`은 그대로 남긴다.

즉:

- 회원 플레이: `memberId` 채움
- 비회원 플레이: `guestSessionKey` 채움
- 랭킹 표시: 항상 당시의 `nicknameSnapshot` 사용

## 게스트 → 회원 전환 흐름

비회원은 지금처럼 세션 기반으로 플레이한다.

그래서 현재 브라우저 세션에만 연결된 `guestSessionKey`를 둔다.

예:

- HttpSession 생성 시 `guestSessionKey`
- 게임 시작 시 `guestSessionKey`를 게임 세션과 랭킹 레코드에 같이 저장

회원가입 또는 로그인에 성공하면, 현재 브라우저 세션의 `guestSessionKey`로 생성된 미귀속 기록을 그 계정에 귀속시킨다.

예:

1. 비회원으로 몇 판 플레이
2. `guestSessionKey = GUEST-ABC`
3. 회원가입 또는 로그인
4. 서버가 `memberId = 12` 확보
5. `guestSessionKey = GUEST-ABC`이고 `memberId IS NULL`인 게임 세션 / 랭킹 레코드를 `memberId = 12`로 업데이트

즉, `같은 브라우저 세션 안에서 한 비회원 기록만` 자연스럽게 계정으로 이어진다.

## 기존 도메인에 추가될 필드

### 게임 세션 계열

`LocationGameSession`, `PopulationGameSession` 같은 세션 엔티티에 아래 필드를 추가한다.

- `memberId` nullable
- `guestSessionKey` nullable
- `nicknameSnapshot`

### 랭킹 레코드

`LeaderboardRecord`에도 동일한 관점을 유지한다.

- `memberId` nullable
- `guestSessionKey` nullable
- `playerNickname`

## 인증 흐름 초안

### public

- `GET /signup`
- `POST /signup`
- `GET /login`
- `POST /login`
- `POST /logout`
- `GET /mypage`

### 처리 순서

#### 회원가입

1. 닉네임/비밀번호 입력
2. 닉네임 중복 확인
3. 비밀번호 해시 저장
4. 로그인 처리
5. 현재 `guestSessionKey` 기록을 새 계정으로 귀속
6. `/mypage` 이동

#### 로그인

1. 닉네임/비밀번호 확인
2. 로그인 세션 생성
3. 현재 `guestSessionKey` 기록을 로그인한 계정으로 귀속
4. `/mypage` 이동

#### 로그아웃

1. 로그인 세션만 해제
2. 새 비회원 플레이를 위해 새 `guestSessionKey` 생성

## 마이페이지 MVP 범위

### `/mypage`

보여 줄 것:

- 내 닉네임
- 누적 플레이 수
- 위치 게임 최고 점수
- 인구수 게임 최고 점수
- 최근 플레이 10개
- 내 최고 랭킹

당장 안 넣을 것:

- 친구
- 팔로우
- 댓글
- 프로필 커스터마이징
- 알림

## 추천 기능과의 관계

추천 결과 자체는 계속 저장하지 않는다.

즉, 계정이 생겨도 추천은 여전히 아래만 수집한다.

- 만족도 1~5점
- surveyVersion
- engineVersion
- 답변 스냅샷

추천 이력 저장은 MVP 8단계에서는 보류한다.

## 보안 기준

지금 단계에서는 단순하지만 최소한 아래는 지킨다.

- 비밀번호는 BCrypt 해시 저장
- admin route는 `ADMIN` role만 허용
- `/mypage`는 로그인 사용자만 접근
- public 게임은 로그인 없이 접근 가능

## 구현 순서 제안

1. `member` 엔티티와 repository 추가
2. `guestSessionKey` 발급/유지 유틸 추가
3. 게임 세션 / 랭킹 레코드에 `memberId`, `guestSessionKey` 연결
4. 회원가입 / 로그인 / 로그아웃 추가
5. 로그인 직후 guest 기록 귀속 서비스 추가
6. `/mypage`에 내 점수 / 최근 플레이 조회 추가
7. admin route 접근 제어 추가

## 면접에서 설명할 핵심

- 왜 비회원 플레이를 유지했는가
- 왜 이메일 없는 단순 계정으로 시작했는가
- 왜 `memberId`와 `guestSessionKey`를 같이 두는가
- 왜 비회원 기록 귀속 범위를 “현재 브라우저 세션”으로 제한했는가
- 왜 추천 이력 저장은 계정 단계에서도 보류했는가
