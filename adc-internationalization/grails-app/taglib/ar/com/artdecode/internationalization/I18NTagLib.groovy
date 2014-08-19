package ar.com.artdecode.internationalization

import grails.util.Holders;

import org.springframework.context.i18n.LocaleContextHolder;

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
			out << body(locale:locale)
		}
		
	}

	/**
	 * @attr filterCurrentLocale 
	 */

	def iterateLocales = {attrs, body ->
	
		Locale locale = LocaleContextHolder.getLocale()
		
		String language = locale?.getLanguage()
		
		Holders.config.grails.i18n.languages.each {
			
			if (attrs.filterCurrentLocale && language == it) {
				return;
			}
			
			out << 	body(language:it) 
		}
		
	}
}