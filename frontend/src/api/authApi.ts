import axiosInstance from './axiosInstance';
import { AuthResponse } from '@/types/auth.types';

export interface RegisterPayload {
  fullName:    string;
  phoneNumber: string;
  password:    string;
  subCity:     string;
  kebele?:     string;
  address:     string;
}

export interface LoginPayload {
  phoneNumber: string;
  password:    string;
}

export const registerApi = async (payload: RegisterPayload): Promise<AuthResponse> => {
  const { data } = await axiosInstance.post<AuthResponse>('/api/auth/register', payload);
  return data;
};

export const loginApi = async (payload: LoginPayload): Promise<AuthResponse> => {
  const { data } = await axiosInstance.post<AuthResponse>('/api/auth/login', payload);
  return data;
};

export const logoutApi = async (): Promise<void> => {
  await axiosInstance.post('/api/auth/logout');
};

export const refreshTokenApi = async (
  refreshToken: string
): Promise<{ accessToken: string }> => {
  const { data } = await axiosInstance.post<{ accessToken: string }>(
    '/api/auth/refresh',
    { refreshToken }
  );
  return data;
};
