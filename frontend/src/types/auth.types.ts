export type Role = 'CITIZEN' | 'COLLECTOR' | 'ADMIN';

export interface AuthUser {
  id:          string;
  fullName:    string;
  phoneNumber: string;
  role:        Role;
}

export interface AuthResponse {
  accessToken:  string;
  refreshToken: string;
  userId:       string;
  role:         Role;
  fullName:     string;
  phoneNumber:  string;
}

export interface AuthContextType {
  user:      AuthUser | null;
  isLoading: boolean;
  login:     (userData: AuthUser, accessToken: string, refreshToken: string) => void;
  logout:    () => Promise<void>;
}
