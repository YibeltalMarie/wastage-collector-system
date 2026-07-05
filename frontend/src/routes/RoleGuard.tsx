import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types/auth.types';

interface RoleGuardProps {
  allowedRoles: Role[];
}

/**
 * Wraps routes that require a specific user role.
 * If wrong role → redirects to /unauthorized.
 *
 * Usage in App.tsx:
 *   <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
 *     <Route path="/admin" element={<AdminDashboard />} />
 *   </Route>
 */
export default function RoleGuard({ allowedRoles }: RoleGuardProps) {
  const { user } = useAuth();

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
