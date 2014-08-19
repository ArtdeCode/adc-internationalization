import org.codehaus.groovy.grails.commons.GrailsClassUtils

import ar.com.artdecode.internationalization.helper.I18NHelper

class AdcInternationalizationGrailsPlugin {
    // the plugin version
    def version = "1.8"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "ArtdeCode Internationalization Plugin" // Headline display name of the plugin
    def author = "Emilio Alberdi"
    def authorEmail = "emilio.alberdi at artdecode dot com dot ar"
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/adc-internationalization"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "ArtdeCode", url: "http://www.artdecode.com.ar/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

	def loadAfter = ['controllers']
	
    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
			application.domainClasses.each { domainClass ->
				if (GrailsClassUtils.getStaticFieldValue(domainClass.clazz, "i18nFields")) {
					I18NHelper.methodMissingDefinition(domainClass)
				}
			}
	}

    def doWithApplicationContext = { applicationContext ->
    }

    def onChange = { event ->
		application.domainClasses.each { domainClass ->
			if (GrailsClassUtils.getStaticFieldValue(domainClass.clazz, "i18nFields")) {
				I18NHelper.methodMissingDefinition(domainClass)
			}
		}
    }

    def onConfigChange = { event ->
    }

    def onShutdown = { event ->
    }
}
