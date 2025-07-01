export const getCountryName = (countryCode: string, locale: string = 'en') => {
  try {
    return new Intl.DisplayNames([locale], { type: 'region' }).of(countryCode);
  } catch (error) {
    return countryCode; // fallback to original code
  }
}; 