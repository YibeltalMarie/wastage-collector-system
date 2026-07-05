import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/context/AuthContext';
import ProtectedRoute from '@/routes/ProtectedRoute';
import RoleGuard from '@/routes/RoleGuard';
import { ROUTES } from '@/constants/routes';

import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import CitizenDashboard from '@/pages/citizen/CitizenDashboard';
import SubmitRequestPage from '@/pages/citizen/SubmitRequestPage';
import RequestHistoryPage from '@/pages/citizen/RequestHistoryPage';
import RequestDetailPage from '@/pages/citizen/RequestDetailPage';
import CollectorDashboard from '@/pages/collector/CollectorDashboard';
import AssignmentDetailPage from '@/pages/collector/AssignmentDetailPage';
import CollectorHistoryPage from '@/pages/collector/CollectorHistoryPage';
import AdminDashboard from '@/pages/admin/AdminDashboard';
import RequestsListPage from '@/pages/admin/RequestsListPage';
import CollectorsListPage from '@/pages/admin/CollectorsListPage';
import CitizensListPage from '@/pages/admin/CitizensListPage';
import NotFoundPage from '@/pages/shared/NotFoundPage';
import UnauthorizedPage from '@/pages/shared/UnauthorizedPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry:     1,
      staleTime: 60_000,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>

            {/* ── Public routes ──────────────────────────────── */}
            <Route path={ROUTES.LOGIN}        element={<LoginPage />} />
            <Route path={ROUTES.REGISTER}     element={<RegisterPage />} />
            <Route path={ROUTES.UNAUTHORIZED} element={<UnauthorizedPage />} />

            {/* ── Protected routes (must be logged in) ──────── */}
            <Route element={<ProtectedRoute />}>

              {/* Citizen only */}
              <Route element={<RoleGuard allowedRoles={['CITIZEN']} />}>
                <Route path={ROUTES.CITIZEN_DASHBOARD}   element={<CitizenDashboard />} />
                <Route path={ROUTES.CITIZEN_NEW_REQUEST} element={<SubmitRequestPage />} />
                <Route path={ROUTES.CITIZEN_REQUESTS}    element={<RequestHistoryPage />} />
                <Route path="/requests/:id"              element={<RequestDetailPage />} />
              </Route>

              {/* Collector only */}
              <Route element={<RoleGuard allowedRoles={['COLLECTOR']} />}>
                <Route path={ROUTES.COLLECTOR_DASHBOARD} element={<CollectorDashboard />} />
                <Route path="/collector/assignments/:id" element={<AssignmentDetailPage />} />
                <Route path={ROUTES.COLLECTOR_HISTORY}   element={<CollectorHistoryPage />} />
              </Route>

              {/* Admin only */}
              <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                <Route path={ROUTES.ADMIN_DASHBOARD}  element={<AdminDashboard />} />
                <Route path={ROUTES.ADMIN_REQUESTS}   element={<RequestsListPage />} />
                <Route path={ROUTES.ADMIN_COLLECTORS} element={<CollectorsListPage />} />
                <Route path={ROUTES.ADMIN_CITIZENS}   element={<CitizensListPage />} />
              </Route>

            </Route>

            {/* Default and catch-all */}
            <Route path="/" element={<Navigate to={ROUTES.LOGIN} replace />} />
            <Route path="*" element={<NotFoundPage />} />

          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
