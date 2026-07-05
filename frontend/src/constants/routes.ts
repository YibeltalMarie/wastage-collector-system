/**
 * All route paths in one place.
 * Use these constants instead of string literals in <Link> and navigate().
 * A typo here fails at compile time. A typo in a string literal fails at runtime.
 */
export const ROUTES = {
  // Public
  LOGIN:    '/login',
  REGISTER: '/register',

  // Citizen
  CITIZEN_DASHBOARD:      '/dashboard',
  CITIZEN_NEW_REQUEST:    '/requests/new',
  CITIZEN_REQUESTS:       '/requests',
  CITIZEN_REQUEST_DETAIL: (id: string) => `/requests/${id}`,

  // Collector
  COLLECTOR_DASHBOARD:        '/collector',
  COLLECTOR_ASSIGNMENT_DETAIL: (id: string) => `/collector/assignments/${id}`,
  COLLECTOR_HISTORY:           '/collector/history',

  // Admin
  ADMIN_DASHBOARD:   '/admin',
  ADMIN_REQUESTS:    '/admin/requests',
  ADMIN_COLLECTORS:  '/admin/collectors',
  ADMIN_CITIZENS:    '/admin/citizens',

  // Shared
  UNAUTHORIZED: '/unauthorized',
} as const;
