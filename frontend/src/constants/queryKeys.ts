/**
 * Centralised React Query cache keys.
 *
 * Why centralise?
 * React Query caches data by key. If you use string literals everywhere,
 * a typo gives you two separate caches for the same data.
 * Centralising here makes cache invalidation safe and predictable.
 *
 * Usage:
 *   useQuery({ queryKey: QUERY_KEYS.MY_REQUESTS, queryFn: ... })
 *   queryClient.invalidateQueries({ queryKey: QUERY_KEYS.MY_REQUESTS })
 */
export const QUERY_KEYS = {
  MY_REQUESTS:       ['my-requests'] as const,
  MY_REQUEST_DETAIL: (id: string) => ['my-requests', id] as const,

  COLLECTOR_TASKS:   ['collector-tasks'] as const,
  COLLECTOR_HISTORY: ['collector-history'] as const,

  ALL_REQUESTS:      ['admin-requests'] as const,
  ALL_COLLECTORS:    ['admin-collectors'] as const,
  ALL_CITIZENS:      ['admin-citizens'] as const,
  DASHBOARD_STATS:   ['admin-dashboard'] as const,

  NOTIFICATIONS:     ['notifications'] as const,
  UNREAD_COUNT:      ['notifications', 'unread-count'] as const,
} as const;
