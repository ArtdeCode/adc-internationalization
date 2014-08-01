package ar.com.artdecode.internationalization

import ar.com.artdecode.internationalization.helper.I18NHelper;

class I18NTagLib {

	static def namespace =  "adc"
	
	/**
	 * @attr locale REQUIRED The locale 
	 */

	def withLocale = {attrs, body ->
	
		def locale = attrs.locale
		
		if (locale instanceof String) {
			locale = new Locale(locale)
		}	
					
		I18NHelper.withLocale(locale) {
			body()
		}
		
	}

	/**
	 * @attr filterCurrentLocale 
	 */

	def iterateLocales = {attrs, body ->
	
		Locale locale = LocaleContextHolder.getLocale()
		
		String language = locale?.getLanguage()
		
		
		Holders.config.grails.i18n.languages.each {
		
			if (filterCurrentLocale && language == it) {
				return;
			}
			
			body(locale:new Locale(it))	
		}
		
	}
}
