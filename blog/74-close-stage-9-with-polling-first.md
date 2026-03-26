# polling 유지로 9단계를 닫고 실시간 전달 기준 고정하기

## 왜 이 조각이 필요했는가

Level 2 실험을 걷어낸 뒤에도 9단계는 아직 열려 있었다.
남은 질문은 하나였다.

`랭킹 실시간성은 지금 단계에서 SSE/WebSocket까지 갈 것인가?`

결론은 `아직 아니다`였다.

현재 제품 범위에서는 15초 polling이 이미 충분히 살아 있는 체감을 만들고 있고,
SSE/WebSocket까지 열면 설명 비용이 더 커진다.

그래서 이번 조각의 목표는

1. 현재 실시간 전달 방식을 polling으로 확정하고
2. 그 이유와 재검토 기준을 문서로 남겨
3. 9단계를 정말 닫을 수 있게 만드는 것이었다.

## 현재 요청 흐름

현재 `/ranking` 흐름은 단순하다.

1. 브라우저가 `GET /ranking`으로 SSR 첫 화면을 받는다.
2. 브라우저가 15초마다 `GET /api/rankings/{gameMode}`를 호출한다.
3. 서버는 Redis Sorted Set에서 상위 record id를 읽고, 필요하면 RDB fallback으로 복구한다.
4. 브라우저는 표만 다시 그린다.

이 구조의 핵심은 "실시간처럼 보이되, 연결 관리 복잡도는 늘리지 않는다"는 점이다.

## 왜 polling으로 닫는가

### 1. 현재 랭킹은 read-only다

채팅처럼 초저지연 양방향 이벤트를 다루는 것이 아니다.
랭킹은 게임 종료 시점에만 결과가 반영되므로, 15초 주기면 충분하다.

### 2. 설명 비용이 맞지 않는다

SSE/WebSocket을 붙이면 아래를 같이 설명해야 한다.

- 연결 유지
- 재연결
- 인증 상태 전달
- 서버 인스턴스 확장 시 연결 처리
- fallback

지금 프로젝트의 핵심 포인트는 여기가 아니다.

### 3. 이미 Redis read model 설명이 가능하다

이 프로젝트에서 보여주고 싶은 것은

- 게임 종료 후 run 저장
- after-commit Redis 반영
- 상위 N명 빠른 조회
- Redis miss 시 RDB fallback

같은 read model 설계다.

polling은 이 구조를 설명하는 데 충분하다.

## 코드보다 문서를 먼저 닫은 이유

이번 조각은 새로운 코드를 늘리기보다,
현재 선택을 명확히 설명하는 데 집중했다.

그래서 핵심 산출물은 [docs/REALTIME_DELIVERY_DECISION.md](/Users/alex/project/worldmap/docs/REALTIME_DELIVERY_DECISION.md) 이다.

여기서

- 왜 polling인지
- 왜 SSE/WebSocket이 아닌지
- 언제 다시 검토할지

를 한 번에 정리했다.

## 면접에서는 어떻게 설명할 것인가

> 랭킹은 실시간처럼 보여야 했지만, 현재 프로젝트 범위에서는 15초 polling으로도 충분했습니다. 이 프로젝트의 핵심은 연결 기술보다 게임 상태 관리와 Redis leaderboard read model이었기 때문에, SSE/WebSocket은 다음 확장 후보로만 남기고 현재는 polling으로 닫았습니다.
