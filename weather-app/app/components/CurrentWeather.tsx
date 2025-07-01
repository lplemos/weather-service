import type { WeatherData } from '../types/weather';
import { Thermometer, Droplets, Wind, Eye } from 'lucide-react';
import { getCountryName } from '../hooks/useCountryName';
import { useTranslation } from 'react-i18next';

interface CurrentWeatherProps {
  weather: WeatherData;
}

export const CurrentWeather = ({ weather }: CurrentWeatherProps) => {
  const { t } = useTranslation();
  const formatTemp = (temp: number) => `${Math.round(temp)}°C`;
  const formatWindSpeed = (speed: number) => `${speed} km/h`;
  
  return (
    <div className="bg-white rounded-lg shadow-lg p-6 mb-6 transition-all duration-300 ease-in-out">
      <div className="text-center mb-6">
        <h2 className="text-2xl sm:text-3xl font-bold text-gray-800 mb-2 transition-colors duration-300">{weather.name}</h2>
        <p className="text-gray-500 text-lg transition-colors duration-300">{getCountryName(weather.sys.country)}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="text-center">
          <div className="text-6xl font-bold text-blue-600 mb-2 transition-all duration-300">
            {formatTemp(weather.main.temp)}
          </div>
          <p className="text-gray-600 text-lg transition-colors duration-300">
            {t('weather.feelsLike')} {formatTemp(weather.main.feels_like)}
          </p>
          <p className="text-gray-800 text-xl font-semibold capitalize transition-colors duration-300">
            {weather.weather[0].description}
          </p>
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg transition-all duration-300">
            <div className="flex items-center gap-3">
              <Thermometer className="w-5 h-5 text-red-500" />
              <span className="text-gray-700">{t('weather.temperature')}</span>
            </div>
            <span className="font-semibold transition-all duration-300">{formatTemp(weather.main.temp)}</span>
          </div>

          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg transition-all duration-300">
            <div className="flex items-center gap-3">
              <Droplets className="w-5 h-5 text-blue-500" />
              <span className="text-gray-700">{t('weather.humidity')}</span>
            </div>
            <span className="font-semibold transition-all duration-300">{weather.main.humidity}%</span>
          </div>

          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg transition-all duration-300">
            <div className="flex items-center gap-3">
              <Wind className="w-5 h-5 text-green-500" />
              <span className="text-gray-700">{t('weather.wind')}</span>
            </div>
            <span className="font-semibold transition-all duration-300">{formatWindSpeed(weather.wind.speed)}</span>
          </div>

          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg transition-all duration-300">
            <div className="flex items-center gap-3">
              <Eye className="w-5 h-5 text-purple-500" />
              <span className="text-gray-700">{t('weather.visibility')}</span>
            </div>
            <span className="font-semibold transition-all duration-300">{(weather.visibility / 1000).toFixed(1)} km</span>
          </div>
        </div>
      </div>
    </div>
  );
}; 