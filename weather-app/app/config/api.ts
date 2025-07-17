// API Configuration
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  TIMEOUT: 10000, // 10 seconds
  RETRY_ATTEMPTS: 3,
} as const;

// API Endpoints
export const API_ENDPOINTS = {
  // Weather Service
  WEATHER: {
    CURRENT: '/api/v1/weather/current',
    FORECAST: '/api/v1/weather/forecast',
    SUMMARY: '/api/v1/weather/summary',
    STATUS: '/api/v1/weather/status',
    PROVIDERS: '/api/v1/weather/providers',
  },
  // User Service
  AUTH: {
    REGISTER: '/api/v1/auth/register',
    LOGIN: '/api/v1/auth/login',
    LOGOUT: '/api/v1/auth/logout',
    REFRESH: '/api/v1/auth/refresh',
    VALIDATE: '/api/v1/auth/validate',
    ME: '/api/v1/auth/me',
  },
  USERS: {
    BASE: '/api/v1/users',
    BY_ID: (id: number) => `/api/v1/users/${id}`,
    BY_USERNAME: (username: string) => `/api/v1/users/username/${username}`,
    BY_EMAIL: (email: string) => `/api/v1/users/email/${email}`,
    CHECK_USERNAME: (username: string) => `/api/v1/users/check/username/${username}`,
    CHECK_EMAIL: (email: string) => `/api/v1/users/check/email/${email}`,
  },
} as const;

// Helper function to build full API URLs
export const buildApiUrl = (endpoint: string): string => {
  return `${API_CONFIG.BASE_URL}${endpoint}`;
}; 