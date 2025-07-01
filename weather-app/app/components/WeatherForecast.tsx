import type { ForecastData } from '../types/weather';
import { useTranslation } from 'react-i18next';

interface WeatherForecastProps {
  forecast: ForecastData;
}

export const WeatherForecast = ({ forecast }: WeatherForecastProps) => {
  const { t, i18n } = useTranslation();
  
  const formatTemp = (temp: number) => `${Math.round(temp)}°C`;
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString(i18n.language, { 
      weekday: 'short', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  // Group forecast data by day and calculate min/max temperatures
  const dailyForecast = forecast.list.reduce((acc: any[], item: any) => {
    const date = new Date(item.dt_txt).toDateString();
    const existingDay = acc.find(day => new Date(day.dt_txt).toDateString() === date);
    
    if (!existingDay) {
      // First entry for this day
      acc.push({
        ...item,
        main: {
          ...item.main,
          temp_min: item.main.temp_min,
          temp_max: item.main.temp_max
        }
      });
    } else {
      // Update min/max temperatures for this day
      existingDay.main.temp_min = Math.min(existingDay.main.temp_min, item.main.temp_min);
      existingDay.main.temp_max = Math.max(existingDay.main.temp_max, item.main.temp_max);
      
      // Update weather icon to the one with highest temperature (usually afternoon)
      if (item.main.temp > existingDay.main.temp) {
        existingDay.weather = item.weather;
      }
    }
    
    return acc;
  }, []).slice(0, 5);

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 transition-all duration-300 ease-in-out">
      <h3 className="text-2xl font-bold text-gray-800 mb-6 transition-colors duration-300">{t('weather.forecast')}</h3>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {dailyForecast.map((day, index) => (
          <div key={index} className="text-center p-4 bg-gray-50 rounded-lg transition-all duration-300 hover:bg-gray-100">
            <p className="font-semibold text-gray-800 mb-2 transition-colors duration-300">
              {formatDate(day.dt_txt)}
            </p>
            <img
              src={`https://openweathermap.org/img/wn/${day.weather[0].icon}@2x.png`}
              alt={day.weather[0].description}
              className="w-12 h-12 mx-auto mb-2 transition-transform duration-300"
            />
            <p className="text-sm text-gray-600 capitalize mb-2 transition-colors duration-300">
              {day.weather[0].description}
            </p>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500 transition-colors duration-300">
                {formatTemp(day.main.temp_min)}
              </span>
              <span className="text-blue-600 font-semibold transition-colors duration-300">
                {formatTemp(day.main.temp_max)}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}; 