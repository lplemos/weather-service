import axios from 'axios';
import type { WeatherData, ForecastData, WeatherSummary } from '../types/weather';
import { API_CONFIG, API_ENDPOINTS, buildApiUrl } from '../config/api';

const api = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
});

// Add request interceptor for authentication
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await axios.post(buildApiUrl(API_ENDPOINTS.AUTH.REFRESH), {
            refreshToken
          });
          
          const { token } = response.data;
          localStorage.setItem('token', token);
          
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

export const getCurrentWeather = async (
  city: string,
  lang: string = 'en'
): Promise<WeatherData> => {
  try {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.WEATHER.CURRENT), {
      params: { city, lang },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching current weather:', error);
    throw error;
  }
};

export const getForecast = async (
  city: string,
  lang: string = 'en'
): Promise<ForecastData> => {
  try {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.WEATHER.FORECAST), {
      params: { city, lang },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching weather forecast:', error);
    throw error;
  }
};

export const getCurrentWeatherByCoords = async (
  lat: number,
  lon: number,
  lang: string = 'en'
): Promise<WeatherData> => {
  try {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.WEATHER.CURRENT), {
      params: { lat, lon, lang },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching current weather:', error);
    throw error;
  }
};

export const getForecastByCoords = async (
  lat: number,
  lon: number,
  lang: string = 'en'
): Promise<ForecastData> => {
  try {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.WEATHER.FORECAST), {
      params: { lat, lon, lang },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching weather forecast:', error);
    throw error;
  }
};

export const getWeatherSummary = async (
  lat: number,
  lon: number,
  lang: string = 'en'
): Promise<WeatherSummary> => {
  try {
    const response = await api.get(buildApiUrl(API_ENDPOINTS.WEATHER.SUMMARY), {
      params: { lat, lon, lang },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching weather summary:', error);
    throw error;
  }
};

// Export the weatherService object with all methods
export const weatherService = {
  getCurrentWeather,
  getForecast,
  getCurrentWeatherByCoords,
  getForecastByCoords,
  getWeatherSummary,
}; 