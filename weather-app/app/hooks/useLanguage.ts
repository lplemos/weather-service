import { useState, useCallback, useMemo } from 'react';
import { useTranslation } from 'react-i18next';

interface UseLanguageProps {
  onLanguageChange?: (newLanguage: string) => void;
}

export const useLanguage = ({ onLanguageChange }: UseLanguageProps = {}) => {
  const { i18n } = useTranslation();
  const [currentLanguage, setCurrentLanguage] = useState(i18n.language);

  const changeLanguage = useCallback((language: string) => {
    if (language !== currentLanguage) {
      i18n.changeLanguage(language);
      setCurrentLanguage(language);
      
      // Call refresh callback after language change
      if (onLanguageChange) {
        // Small delay to ensure i18n has updated
        setTimeout(() => {
          onLanguageChange(language);
        }, 100);
      }
    }
  }, [i18n, currentLanguage, onLanguageChange]);

  const getApiLanguage = useMemo(() => {
    // Map React i18n languages to OpenWeatherMap language codes
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
    
    return languageMap[currentLanguage] || 'en';
  }, [currentLanguage]);

  return {
    currentLanguage,
    changeLanguage,
    getApiLanguage
  };
}; 