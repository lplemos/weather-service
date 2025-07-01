import { useTranslation } from 'react-i18next';
import { useLanguage } from '../hooks/useLanguage';
import { Globe } from 'lucide-react';

interface LanguageSelectorProps {
  onLanguageChange?: (language: string) => void;
}

export const LanguageSelector = ({ onLanguageChange }: LanguageSelectorProps) => {
  const { t } = useTranslation();
  const { currentLanguage, changeLanguage } = useLanguage({ 
    onLanguageChange: onLanguageChange ? (newLanguage: string) => {
      onLanguageChange(newLanguage);
    } : undefined 
  });

  const languages = [
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'pt', name: 'Português', flag: '🇵🇹' },
  ];

  return (
    <div className="flex items-center gap-2">
      <Globe className="w-4 h-4 text-gray-600" />
      <select
        value={currentLanguage}
        onChange={(e) => changeLanguage(e.target.value)}
        className="bg-white border border-gray-300 rounded-md px-3 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        {languages.map((lang) => (
          <option key={lang.code} value={lang.code}>
            {lang.flag} {lang.name}
          </option>
        ))}
      </select>
    </div>
  );
}; 