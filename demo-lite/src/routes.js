export const RETAINED_ROUTES = [
  {
    path: "/",
    navLabel: "Home",
    title: "무료로 바로 체험하는 WorldMap",
    subtitle: "demo-lite",
    kind: "home"
  },
  {
    path: "/games/capital",
    navLabel: "수도",
    title: "수도 맞히기",
    subtitle: "4지선다",
    kind: "feature",
    status: "지금 플레이 가능",
    summary: "국가를 보고 수도 보기 4개 중 하나를 고르는 retained demo 게임입니다."
  },
  {
    path: "/games/flag",
    navLabel: "국기",
    title: "국기 퀴즈",
    subtitle: "국기 이미지",
    kind: "feature",
    status: "지금 플레이 가능",
    summary: "정적 flag 자산과 국가 데이터만으로도 바로 체험 가능한 retained demo 게임입니다."
  },
  {
    path: "/games/population-battle",
    navLabel: "배틀",
    title: "인구 비교 배틀",
    subtitle: "2-choice",
    kind: "feature",
    status: "지금 플레이 가능",
    summary: "인구 순위 gap으로 만든 두 나라 pair 중 더 인구가 많은 쪽을 빠르게 고르는 retained demo 게임입니다."
  },
  {
    path: "/recommendation",
    navLabel: "추천",
    title: "국가 추천",
    subtitle: "20문항",
    kind: "feature",
    status: "지금 플레이 가능",
    summary: "feedback 저장은 제거하고, 20문항 breadth와 30국가 deterministic top 3만 local-state로 제공하는 retained demo surface입니다."
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
