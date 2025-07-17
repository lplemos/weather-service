import axios from 'axios';
import { API_CONFIG, API_ENDPOINTS, buildApiUrl } from '../config/api';

const api = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
});

// Add request interceptor to include auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Add response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await api.post(buildApiUrl(API_ENDPOINTS.AUTH.REFRESH), null, {
            params: { refreshToken },
          });
          
          const { accessToken, refreshToken: newRefreshToken } = response.data;
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', newRefreshToken);
          
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// Auth functions
export const authService = {
  async register(userData: {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }) {
    const response = await api.post(buildApiUrl(API_ENDPOINTS.AUTH.REGISTER), userData);
    return response.data;
  },

  async login(credentials: { username: string; password: string }) {
    const response = await api.post(buildApiUrl(API_ENDPOINTS.AUTH.LOGIN), credentials);
    const { accessToken, refreshToken } = response.data;
    
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    
    return response.data;
  },

  async logout() {
    try {
      await api.post(buildApiUrl(API_ENDPOINTS.AUTH.LOGOUT));
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  },

  async getCurrentUser() {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.AUTH.ME));
    return response.data;
  },

  async validateToken() {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.AUTH.VALIDATE));
    return response.data;
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('accessToken');
  },

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  },
};

// User management functions
export const userService = {
  async getAllUsers(page: number = 0, size: number = 20) {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.USERS.BASE), {
      params: { page, size },
    });
    return response.data;
  },

  async getUserById(id: number) {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.USERS.BY_ID(id)));
    return response.data;
  },

  async getUserByUsername(username: string) {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.USERS.BY_USERNAME(username)));
    return response.data;
  },

  async updateUser(id: number, userData: any) {
    const response = await api.put(buildApiUrl(API_ENDPOINTS.USERS.BY_ID(id)), userData);
    return response.data;
  },

  async deleteUser(id: number) {
    await api.delete(buildApiUrl(API_ENDPOINTS.USERS.BY_ID(id)));
  },

  async checkUsernameExists(username: string): Promise<boolean> {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.USERS.CHECK_USERNAME(username)));
    return response.data;
  },

  async checkEmailExists(email: string): Promise<boolean> {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.USERS.CHECK_EMAIL(email)));
    return response.data;
  },
}; 