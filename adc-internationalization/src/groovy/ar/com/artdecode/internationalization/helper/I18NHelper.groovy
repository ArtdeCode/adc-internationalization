package ar.com.artdecode.internationalization.helper

import grails.util.Holders;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder

import ar.com.artdecode.internationalization.ast.I18NField;

class I18NHelper {
	
	
	def static methodMissingDefinition(domainClass) {
	  
	  def mc = domainClass.clazz.metaClass

	  def originalMapConstructor = mc.retrieveConstructor(Map)
 	  
	  mc.constructor = { Map m ->
	 	
		m =  i18nMap(domainClass.clazz,m)
		  
	    def instance = originalMapConstructor.newInstance(m)
		
		def fieldNames = domainClass.clazz.i18nFields
		
		fieldNames.each {
			
			if (!instance."${it}Default") {
				instance."${it}Default" = m[i18n(it)]
			}
			
		}
	 
	   	instance
	  }
	  
	  def oldSetProperties = mc.methods.find { it.name == 'setProperties' }
	
	  mc.setProperties = { Object  bindingSource ->
		
		  if (bindingSource instanceof Map) {
			 bindingSource =  i18nMap(domainClass.clazz,bindingSource)
			 	  
		  }
		   
		  oldSetProperties.doMethodInvoke(delegate, bindingSource)
		
	   }
	  
	  		
	  def oldMethodMissing = mc.methods.find { it.name == '$static_methodMissing' }
	  
	  def newMethodMissing
	  
	  newMethodMissing = mc.static.methodMissing = { String name, args ->
		  
		def match = name =~ /(findI18NBy|findI18NAllBy)(\w+)/
		
		if (match) {
		  // Get the column name
		  def column = match[0][2]
		  def method = match[0][1]
		  
		  if (method == 'findI18NBy')  {
			     
			    def findI1BNBy = { value ->
				
				  def i18nColumn = createI18NMethod(domainClass.clazz, column)
				  		   
				  return domainClass.clazz."findBy${i18nColumn}"(value)
				  
				}
	  
				mc.static."findI18NBy${column}" = findI1BNBy
				
				return findI1BNBy(args)
		  }
		  else {
			  	  def findI1BNAllBy = { value ->
				  
					def i18nColumn = createI18NMethod(domainClass.clazz, column)
		
					return domainClass.clazz."findAllBy${i18nColumn}"(value)
					
				  }
		
				  mc.static."findI18NAllBy${column}" = findI1BNAllBy
				  
				  return findI1BNAllBy(args)
  
		  }

		} 
		else {
		  if (oldMethodMissing) {
			
			def result = oldMethodMissing.doMethodInvoke(delegate, name, args)

			mc.static.methodMissing = newMethodMissing

			return result
		  } 
		  else {
			throw new MissingMethodException(name, delegate, args)
		  }
		}
	  }
		
		
	}
	
	static private createI18NMethod(Class clazz, String method) {
		
		def fieldNames = clazz.i18nFields
		
		fieldNames.each {
			 method = method.replaceAll(StringUtils.capitalize(it), StringUtils.capitalize(i18n(it)))
		}
		
		method
	}

	def static findI18N(Class clazz, String query, Map parameters=[:]) {
		
		
		def fieldNames = clazz.i18nFields
		
		fieldNames.each {
			 query = query.replaceAll(it, i18n(it))
			 if (parameters && parameters.containsKey(it)) {
				 def value = parameters.remove(it)
				 parameters.put(i18n(it), value)
			 }
			 
		}
		
		
		clazz."find"(query,parameters);
	}
		
	def static findI18NAll(Class clazz, String query, Map parameters=[:]) {
		
		def fieldNames = clazz.i18nFields
		
		fieldNames.each {
		 	query = query.replaceAll(it, i18n(it))
			 
			if (parameters && parameters.containsKey(it)) {
				 def value = parameters.remove(it)
				 parameters.put(i18n(it), value)
			}
		
		}
			  
		clazz."findAll"(query,parameters);
	}
	
	def static Map i18nMap(Class clazz, Map parameters) {
		
		def fieldNames = clazz.i18nFields
		
		fieldNames.each {
			 
			if (parameters && parameters.containsKey(it)) {
				 def value = parameters.remove(it)
				 parameters.put(i18n(it), value)
			}
		}
		
		parameters
		
	}
	
	def static createI18N(Class clazz, Map parameters) {

		parameters = i18nMap(clazz, parameters);	
		
		clazz.newInstance(parameters)
		
	}
	
	def static findWhereI18N(Class clazz, Map queryMap, Map parameters=[:]) {
		
		def fieldNames = clazz.i18nFields
		
		fieldNames.each {
			 
			if (queryMap && queryMap.containsKey(it)) {
				 def value = queryMap.remove(it)
				 queryMap.put(i18n(it), value)
			}
		
		}
			  
		clazz."findWhere"(queryMap,parameters);
	}
	
	def static findAllWhereI18N(Class clazz, Map queryMap, Map parameters=[:]) {
		
		def fieldNames = clazz.i18nFields
		
		fieldNames.each {
			 
			if (queryMap && queryMap.containsKey(it)) {
				 def value = queryMap.remove(it)
				 queryMap.put(i18n(it), value)
			}
		
		}
			  
		clazz."findAllWhere"(queryMap,parameters);
	}
	
	def static i18n(String property) {
		
		Locale locale = LocaleContextHolder.getLocale()
	    
		String language = locale?.getLanguage()
		
		if (!Holders.config.grails.i18n.languages.contains(language)) {
			return;
		}
		
		String propertyLanguage = property + StringUtils.capitalize(language)
		
		return propertyLanguage;
	}
	
	
	
	def static withLocale = { Locale newLocale, Closure closure ->
		
		def previousLocale = 	LocaleContextHolder.getLocale()
		
		LocaleContextHolder.setLocale(newLocale)
		
		def result = closure.call()
		
		LocaleContextHolder.setLocale(previousLocale)
	
		return result
	}
	
	
	def static setValue(Object bean, String fieldName, String value) {
		
		Locale locale = LocaleContextHolder.getLocale()
	    
		String language = locale?.getLanguage()
		
		if (!Holders.config.grails.i18n.languages.contains(language)) {
			return;
		}
		
		String propertyLanguage = fieldName + StringUtils.capitalize(language)
		
		bean."$propertyLanguage" = value
		
		if (!bean."${fieldName}Default") {
			bean."${fieldName}Default" = value
		}
	}

	def static getValue(Object bean, String fieldName) {
		
		Locale locale = LocaleContextHolder.getLocale()
		
		String language = locale?.getLanguage()
		
		if (!Holders.config.grails.i18n.languages.contains(language)) {
			return bean."${fieldName}Default"
		}
		
		String propertyLanguage = fieldName + StringUtils.capitalize(language)
	
		String value = bean."$propertyLanguage"
		
		if (!value) {
			value = bean."${fieldName}Default"
		}
		
		value 
	
	}
	
}
