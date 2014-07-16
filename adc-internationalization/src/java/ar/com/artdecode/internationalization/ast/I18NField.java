package ar.com.artdecode.internationalization.ast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass({"ar.com.artdecode.internationalization.ast.I18NASTTransformation"})
public @interface I18NField {
	
}
