# 핵심 ERD

현재 제품 범위를 설명할 때 필요한 핵심 테이블만 정리한다.

```mermaid
erDiagram
    COUNTRY {
        bigint id PK
        string iso3_code
        string name
        string continent
        decimal latitude
        decimal longitude
        bigint population
    }

    MEMBER_ACCOUNT {
        bigint id PK
        string nickname
        string password_hash
        string role
        datetime created_at
    }

    LOCATION_GAME_SESSION {
        bigint id PK
        bigint member_id FK
        string guest_session_key
        string player_nickname
        string status
        int current_stage_number
        int lives_remaining
        int total_score
        datetime started_at
        datetime finished_at
    }

    LOCATION_GAME_STAGE {
        bigint id PK
        bigint session_id FK
        bigint country_id FK
        int stage_number
        string status
        int attempt_count
        int awarded_score
    }

    LOCATION_GAME_ATTEMPT {
        bigint id PK
        bigint stage_id FK
        int attempt_number
        string selected_country_iso3_code
        string selected_country_name
        boolean correct
        int lives_remaining_after
        datetime attempted_at
    }

    POPULATION_GAME_SESSION {
        bigint id PK
        bigint member_id FK
        string guest_session_key
        string player_nickname
        string status
        int current_stage_number
        int lives_remaining
        int total_score
        datetime started_at
        datetime finished_at
    }

    POPULATION_GAME_STAGE {
        bigint id PK
        bigint session_id FK
        bigint country_id FK
        int stage_number
        string status
        int attempt_count
        bigint target_population
        int awarded_score
    }

    POPULATION_GAME_ATTEMPT {
        bigint id PK
        bigint stage_id FK
        int attempt_number
        string selected_option_label
        boolean correct
        int lives_remaining_after
        datetime attempted_at
    }

    LEADERBOARD_RECORD {
        bigint id PK
        bigint member_id FK
        string guest_session_key
        string game_mode
        string player_nickname
        int total_score
        int cleared_stage_count
        int total_attempt_count
        datetime finished_at
    }

    RECOMMENDATION_FEEDBACK {
        bigint id PK
        string survey_version
        string engine_version
        int satisfaction_score
        string answer_snapshot
        datetime created_at
    }

    MEMBER_ACCOUNT ||--o{ LOCATION_GAME_SESSION : owns
    MEMBER_ACCOUNT ||--o{ POPULATION_GAME_SESSION : owns
    MEMBER_ACCOUNT ||--o{ LEADERBOARD_RECORD : owns

    COUNTRY ||--o{ LOCATION_GAME_STAGE : target
    COUNTRY ||--o{ POPULATION_GAME_STAGE : target

    LOCATION_GAME_SESSION ||--o{ LOCATION_GAME_STAGE : has
    LOCATION_GAME_STAGE ||--o{ LOCATION_GAME_ATTEMPT : has

    POPULATION_GAME_SESSION ||--o{ POPULATION_GAME_STAGE : has
    POPULATION_GAME_STAGE ||--o{ POPULATION_GAME_ATTEMPT : has
```

## 읽는 방법

### 게임 기록

- `...SESSION`은 한 판 전체
- `...STAGE`는 한 문제
- `...ATTEMPT`는 그 문제 안의 개별 시도

### 계정과 게스트

- 회원이면 `member_id`
- 비회원이면 `guest_session_key`

두 값 중 하나로 소유권을 표현한다.

### 랭킹

- `leaderboard_record`는 session 자체가 아니라 `완료된 run 결과`
- 그래서 같은 세션을 재시작해도 예전 결과가 덮어써지지 않는다.

## 면접에서 짚을 포인트

1. 왜 session / stage / attempt를 나눴는가
2. 왜 leaderboard_record를 세션과 별도로 뒀는가
3. 왜 guestSessionKey와 memberId를 같이 들고 가는가
