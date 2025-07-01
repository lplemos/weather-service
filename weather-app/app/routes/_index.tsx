import { useEffect, useCallback, useState } from 'react';
import { SearchBar } from '../components/SearchBar';
import { CurrentWeather } from '../components/CurrentWeather';
import { WeatherForecast } from '../components/WeatherForecast';
import { useWeather } from '../hooks/useWeather';
import { LanguageSelector } from '../components/LanguageSelector';
import '../i18n'; // Initialize i18n

export default function Index() {
  const [hasInitialized, setHasInitialized] = useState(false);
  const [currentLanguage, setCurrentLanguage] = useState('en');
  

  
  const { 
    currentWeather, 
    forecast, 
    loading, 
    weatherLoading, 
    forecastLoading, 
    error, 
    fetchWeather, 
    fetchForecast,
    fetchWeatherByCoords,
    fetchForecastByCoords,
    refreshWithCurrentLanguage
  } = useWeather(currentLanguage);

  const handleSearch = useCallback((city: string) => {
    fetchWeather(city);
    fetchForecast(city);
  }, [fetchWeather, fetchForecast]);

  // Refresh data when language changes
  useEffect(() => {
    if (hasInitialized) {
      refreshWithCurrentLanguage();
    }
  }, [currentLanguage, hasInitialized, refreshWithCurrentLanguage]);

  // Initial load effect
  useEffect(() => {
    if (hasInitialized) return; // Prevent multiple calls
    
    setHasInitialized(true);
    
    // Get user's location on component mount
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          fetchWeatherByCoords(latitude, longitude);
          fetchForecastByCoords(latitude, longitude);
        },
        (error) => {
          // Fallback to a default city if geolocation fails
          fetchWeather('London');
          fetchForecast('London');
        }
      );
    } else {
      // Fallback for browsers that don't support geolocation
      fetchWeather('London');
      fetchForecast('London');
    }
  }, [hasInitialized, fetchWeather, fetchForecast, fetchWeatherByCoords, fetchForecastByCoords]);

  // Cleanup effect
  useEffect(() => {
    return () => {
      // Component cleanup
    };
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="max-w-4xl mx-auto">
        {/* Header with language selector */}
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Weather App</h1>
          <LanguageSelector onLanguageChange={setCurrentLanguage} />
        </div>

        <SearchBar onSearch={handleSearch} loading={loading} />
        
        {loading && !currentWeather && !forecast && (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading weather data...</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        {currentWeather && (
          <div className="relative">
            {weatherLoading && (
              <div className="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center rounded-lg z-10">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              </div>
            )}
            <CurrentWeather weather={currentWeather} />
          </div>
        )}
        
        {forecast && (
          <div className="relative">
            {forecastLoading && (
              <div className="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center rounded-lg z-10">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              </div>
            )}
            <WeatherForecast forecast={forecast} />
          </div>
        )}
      </div>
    </div>
  );
} 