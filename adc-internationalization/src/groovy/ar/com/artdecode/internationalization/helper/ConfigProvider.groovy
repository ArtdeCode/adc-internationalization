package ar.com.artdecode.internationalization.helper

import java.util.List;

import grails.util.Environment;
import grails.util.GrailsUtil
import grails.util.BuildSettingsHolder

class ConfigProvider {
	static final CONFIG_LOCATION = "${BuildSettingsHolder.settings.baseDir}/grails-app/conf/Config.groovy"
	static def config
	def static  getConfig() {
		if (!config) {
			config = new ConfigSlurper(Environment.getCurrent().getName()).parse(new File(CONFIG_LOCATION).toURI().toURL())
		}
		return config
	}
	
	static def List<String> getLanguages() {
		
		getConfig().grails.i18n.languages
	}

}