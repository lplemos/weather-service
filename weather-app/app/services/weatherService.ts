import axios from 'axios';
import type { WeatherData, ForecastData } from '../types/weather';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const weatherApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

export const weatherService = {
  async getCurrentWeather(city: string, language: string = 'en'): Promise<WeatherData> {
    const response = await weatherApi.get(`/api/v1/weather/current`, {
      params: { city, lang: language },
    });
    return response.data;
  },

  async getForecast(city: string, language: string = 'en'): Promise<ForecastData> {
    const response = await weatherApi.get(`/api/v1/weather/forecast`, {
      params: { city, lang: language },
    });
    return response.data;
  },

  async getCurrentWeatherByCoords(lat: number, lon: number, language: string = 'en'): Promise<WeatherData> {
    const response = await weatherApi.get(`/api/v1/weather/current`, {
      params: { lat, lon, lang: language },
    });
    return response.data;
  },

  async getForecastByCoords(lat: number, lon: number, language: string = 'en'): Promise<ForecastData> {
    const response = await weatherApi.get(`/api/v1/weather/forecast`, {
      params: { lat, lon, lang: language },
    });
    return response.data;
  },

  async getWeatherSummary(city: string): Promise<WeatherData> {
    const response = await weatherApi.get(`/api/v1/weather/summary`, {
      params: { city },
    });
    return response.data;
  },
}; 