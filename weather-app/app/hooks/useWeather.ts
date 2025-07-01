import { useState, useCallback, useRef, useEffect, useMemo } from 'react';
import { weatherService } from '../services/weatherService';
import type { WeatherData, ForecastData } from '../types/weather';

interface UseWeatherReturn {
  currentWeather: WeatherData | null;
  forecast: ForecastData | null;
  loading: boolean;
  weatherLoading: boolean;
  forecastLoading: boolean;
  error: string | null;
  fetchWeather: (city: string) => Promise<void>;
  fetchForecast: (city: string) => Promise<void>;
  fetchWeatherByCoords: (lat: number, lon: number) => Promise<void>;
  fetchForecastByCoords: (lat: number, lon: number) => Promise<void>;
  refreshWithCurrentLanguage: () => Promise<void>;
}

export const useWeather = (language: string = 'en'): UseWeatherReturn => {
  const [currentWeather, setCurrentWeather] = useState<WeatherData | null>(null);
  const [forecast, setForecast] = useState<ForecastData | null>(null);
  const [weatherLoading, setWeatherLoading] = useState(false);
  const [forecastLoading, setForecastLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isMounted = useRef(true);
  
  // Store last search parameters for refresh
  const lastSearchRef = useRef<{
    type: 'city' | 'coords';
    city?: string;
    lat?: number;
    lon?: number;
  } | null>(null);

  // Map React i18n languages to OpenWeatherMap language codes
  const getApiLanguage = useMemo(() => {
    const languageMap: Record<string, string> = {
      'en': 'en',
      'pt': 'pt',
      'es': 'es',
      'fr': 'fr',
      'de': 'de',
      'it': 'it',
      'ru': 'ru',
      'ja': 'ja',
      'ko': 'kr',
      'zh': 'zh_cn'
    };
    
    return languageMap[language] || 'en';
  }, [language]);

  // Cleanup on unmount
  const cleanup = useCallback(() => {
    isMounted.current = false;
  }, []);

  const fetchWeather = useCallback(async (city: string) => {
    if (!isMounted.current) return;
    
    lastSearchRef.current = { type: 'city', city };
    setWeatherLoading(true);
    setError(null);
    try {
      const data = await weatherService.getCurrentWeather(city, getApiLanguage);
      if (isMounted.current) {
        setCurrentWeather(data);
      }
    } catch (err) {
      if (isMounted.current) {
        setError(err instanceof Error ? err.message : 'Failed to fetch weather data');
      }
    } finally {
      if (isMounted.current) {
        setWeatherLoading(false);
      }
    }
  }, [getApiLanguage]);

  const fetchForecast = useCallback(async (city: string) => {
    if (!isMounted.current) return;
    
    lastSearchRef.current = { type: 'city', city };
    setForecastLoading(true);
    setError(null);
    try {
      const data = await weatherService.getForecast(city, getApiLanguage);
      if (isMounted.current) {
        setForecast(data);
      }
    } catch (err) {
      if (isMounted.current) {
        setError(err instanceof Error ? err.message : 'Failed to fetch forecast data');
      }
    } finally {
      if (isMounted.current) {
        setForecastLoading(false);
      }
    }
  }, [getApiLanguage]);

  const fetchWeatherByCoords = useCallback(async (lat: number, lon: number) => {
    if (!isMounted.current) return;
    
    lastSearchRef.current = { type: 'coords', lat, lon };
    setWeatherLoading(true);
    setError(null);
    try {
      const data = await weatherService.getCurrentWeatherByCoords(lat, lon, getApiLanguage);
      if (isMounted.current) {
        setCurrentWeather(data);
      }
    } catch (err) {
      if (isMounted.current) {
        setError(err instanceof Error ? err.message : 'Failed to fetch weather data');
      }
    } finally {
      if (isMounted.current) {
        setWeatherLoading(false);
      }
    }
  }, [getApiLanguage]);

  const fetchForecastByCoords = useCallback(async (lat: number, lon: number) => {
    if (!isMounted.current) return;
    
    lastSearchRef.current = { type: 'coords', lat, lon };
    setForecastLoading(true);
    setError(null);
    try {
      const data = await weatherService.getForecastByCoords(lat, lon, getApiLanguage);
      if (isMounted.current) {
        setForecast(data);
      }
    } catch (err) {
      if (isMounted.current) {
        setError(err instanceof Error ? err.message : 'Failed to fetch forecast data');
      }
    } finally {
      if (isMounted.current) {
        setForecastLoading(false);
      }
    }
  }, [getApiLanguage]);

  // Refresh data with current language - defined after all fetch functions
  const refreshWithCurrentLanguage = useCallback(async () => {
    if (!lastSearchRef.current) {
      return;
    }
    
    const { type, city, lat, lon } = lastSearchRef.current;
    
    if (type === 'city' && city) {
      await Promise.all([
        weatherService.getCurrentWeather(city, getApiLanguage),
        weatherService.getForecast(city, getApiLanguage)
      ]).then(([weatherData, forecastData]) => {
        if (isMounted.current) {
          setCurrentWeather(weatherData);
          setForecast(forecastData);
        }
      }).catch((err) => {
        if (isMounted.current) {
          setError(err instanceof Error ? err.message : 'Failed to refresh data');
        }
      });
    } else if (type === 'coords' && lat && lon) {
      await Promise.all([
        weatherService.getCurrentWeatherByCoords(lat, lon, getApiLanguage),
        weatherService.getForecastByCoords(lat, lon, getApiLanguage)
      ]).then(([weatherData, forecastData]) => {
        if (isMounted.current) {
          setCurrentWeather(weatherData);
          setForecast(forecastData);
        }
      }).catch((err) => {
        if (isMounted.current) {
          setError(err instanceof Error ? err.message : 'Failed to refresh data');
        }
      });
    }
  }, [getApiLanguage]);

  return {
    currentWeather,
    forecast,
    loading: weatherLoading || forecastLoading,
    weatherLoading,
    forecastLoading,
    error,
    fetchWeather,
    fetchForecast,
    fetchWeatherByCoords,
    fetchForecastByCoords,
    refreshWithCurrentLanguage,
  };
}; 