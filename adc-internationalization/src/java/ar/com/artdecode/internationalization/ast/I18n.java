package ar.com.artdecode.internationalization.ast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface I18n {
	
}
