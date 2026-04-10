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
    summary: "국가와 수도를 빠르게 연결하는 가장 가벼운 퀴즈입니다.",
    cardMeta: "5문제 러닝"
  },
  {
    path: "/games/flag",
    navLabel: "국기",
    title: "국기 퀴즈",
    kind: "feature",
    summary: "국기만 보고 나라를 골라내는 시각형 퀴즈입니다.",
    cardMeta: "same-continent 보기"
  },
  {
    path: "/games/population",
    navLabel: "인구수",
    title: "인구수 퀴즈",
    kind: "feature",
    summary: "숫자 암기보다 규모 감각으로 푸는 인구 구간 퀴즈입니다.",
    cardMeta: "4지선다"
  },
  {
    path: "/games/population-battle",
    navLabel: "배틀",
    title: "인구 비교 배틀",
    kind: "feature",
    summary: "두 나라 중 더 큰 인구를 즉시 고르는 반응형 배틀입니다.",
    cardMeta: "2지선다"
  },
  {
    path: "/recommendation",
    navLabel: "추천",
    title: "나에게 어울리는 국가 찾기",
    kind: "feature",
    summary: "20문항 생활 취향 설문으로 지금 맞는 국가 3곳을 비교해 줍니다.",
    cardMeta: "20문항 설문"
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
