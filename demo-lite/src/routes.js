export const RETAINED_ROUTES = [
  {
    path: "/",
    navLabel: "홈",
    title: "바로 즐기는 WorldMap",
    kind: "home"
  },
  {
    path: "/games/capital",
    navLabel: "수도",
    title: "수도 퀴즈",
    kind: "feature",
    summary: "국가의 수도를 맞추는 퀴즈입니다."
  },
  {
    path: "/games/flag",
    navLabel: "국기",
    title: "국기 퀴즈",
    kind: "feature",
    summary: "국기만 보고 어느 나라인지 맞히는 퀴즈입니다."
  },
  {
    path: "/games/population-battle",
    navLabel: "배틀",
    title: "인구 비교 배틀",
    kind: "feature",
    summary: "두 나라 중 인구가 더 많은 쪽을 고르는 퀴즈입니다."
  },
  {
    path: "/recommendation",
    navLabel: "추천",
    title: "나에게 어울리는 국가 찾기",
    kind: "feature",
    summary: "20문항 답변을 바탕으로 지금 어울리는 국가 3곳을 골라 줍니다."
  }
];

export function normalizeRoute(rawHash) {
  const fallback = "/";
  if (!rawHash || rawHash === "#") {
    return fallback;
  }

  const trimmed = rawHash.replace(/^#/, "");
  if (!trimmed) {
    return fallback;
  }

  return trimmed.startsWith("/") ? trimmed : `/${trimmed}`;
}

export function resolveRoute(pathname) {
  return RETAINED_ROUTES.find((route) => route.path === pathname) ?? RETAINED_ROUTES[0];
}
