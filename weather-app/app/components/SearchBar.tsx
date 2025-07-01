import { useState, useCallback, useEffect } from 'react';
import { Search } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface SearchBarProps {
  onSearch: (city: string) => void;
  loading?: boolean;
}

export const SearchBar = ({ onSearch, loading = false }: SearchBarProps) => {
  const [city, setCity] = useState('');
  const [debouncedCity, setDebouncedCity] = useState('');
  const { t } = useTranslation();

  // Debounce effect
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedCity(city);
    }, 500); // 500ms delay

    return () => clearTimeout(timer);
  }, [city]);

  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    if (city.trim() && !loading) {
      onSearch(city.trim());
    }
  }, [city, onSearch, loading]);

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setCity(e.target.value);
  }, []);

  return (
    <form onSubmit={handleSubmit} className="mb-8">
      <div className="relative max-w-md mx-auto">
        <input
          type="text"
          value={city}
          onChange={handleInputChange}
          placeholder={t('weather.search')}
          disabled={loading}
          className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
        />
        <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
        <button
          type="submit"
          disabled={loading || !city.trim()}
          className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-blue-500 text-white px-4 py-1 rounded-md hover:bg-blue-600 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {loading ? '...' : t('common.search')}
        </button>
      </div>
    </form>
  );
}; 