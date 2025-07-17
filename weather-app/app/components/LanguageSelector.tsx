import { useTranslation } from 'react-i18next';
import { useLanguage } from '../hooks/useLanguage';
import { Globe, ChevronDown } from 'lucide-react';
import { useEffect, useState, useRef } from 'react';

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

  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [flagComponents, setFlagComponents] = useState<{ US: any; PT: any } | null>(null);

  useEffect(() => {
    import('country-flag-icons/react/3x2')
      .then((flagModule) => {
        setFlagComponents({
          US: flagModule.US,
          PT: flagModule.PT
        });
      })
      .catch(() => {
        setFlagComponents({
          US: () => <span>🇺🇸</span>,
          PT: () => <span>🇵🇹</span>
        });
      });
  }, []);

  const languages = [
    { code: 'en', name: 'English', flag: flagComponents?.US ? <flagComponents.US className="w-4 h-3" /> : <span>🇺🇸</span> },
    { code: 'pt', name: 'Português', flag: flagComponents?.PT ? <flagComponents.PT className="w-4 h-3" /> : <span>🇵🇹</span> },
  ];

  const currentLanguageData = languages.find(lang => lang.code === currentLanguage);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleLanguageSelect = (languageCode: string) => {
    changeLanguage(languageCode);
    setIsOpen(false);
  };

  const handleToggle = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div className="flex items-center gap-2">
      <Globe className="w-4 h-4 text-gray-600" />
      <div className="relative" ref={dropdownRef}>
        <button
          onClick={handleToggle}
          className="flex items-center gap-2 bg-white border border-gray-300 rounded-md px-5 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 hover:bg-gray-50 w-[160px]"
        >
          {currentLanguageData?.flag}
          <span>{currentLanguageData?.name}</span>
          <ChevronDown className={`w-3 h-3 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
        </button>
        
        {isOpen && (
          <div className="absolute top-full left-0 mt-1 bg-white border border-gray-300 rounded-md shadow-lg z-10 w-[160px]">
            {languages.map((lang) => (
              <button
                key={lang.code}
                onClick={() => handleLanguageSelect(lang.code)}
                className={`w-full flex items-center gap-2 px-5 py-2 text-sm text-left hover:bg-gray-100 first:rounded-t-md last:rounded-b-md ${
                  lang.code === currentLanguage ? 'bg-blue-50 text-blue-600' : ''
                }`}
              >
                {lang.flag}
                <span>{lang.name}</span>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}; 