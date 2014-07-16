package ar.com.artdecode.internationalization.ast;


import groovy.lang.Closure;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import ar.com.artdecode.internationalization.helper.ConfigProvider;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class I18NASTTransformation implements ASTTransformation, Opcodes {

	
	@Override
	public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
		
		for (ASTNode aSTNode : astNodes) {
			if (!(aSTNode instanceof FieldNode)) {
				continue;
			}

			try {
 				
				FieldNode fieldNode = (FieldNode) aSTNode;
				
				I18NTransformer transformer =  new I18NTransformer(fieldNode.getOwner(), fieldNode);
				
				transformer.addProperty(getDefaultFieldName(fieldNode.getName()), fieldNode.getType().getTypeClass());
				
				transformer.addNullableConstraint(getDefaultFieldName(fieldNode.getName()));
				
				List<String> languages = ConfigProvider.getLanguages();
				
				for (String lanaguage : languages) {

					log("Add property" + fieldNode.getName() + " " + lanaguage);
					
					String languageProperty = fieldNode.getName() + StringUtils.capitalize(lanaguage); 

					transformer.addProperty(languageProperty, fieldNode.getType().getTypeClass());
					
					transformer.addNullableConstraint(languageProperty);
					
				}
				 
				MethodNode getter = transformer.getOrCreateMethod(GrailsClassUtils.getGetterName(fieldNode.getName()));
				
				changeGet(getter, fieldNode.getName());
				
				MethodNode setter = transformer.getOrCreateMethod(GrailsClassUtils.getSetterName(fieldNode.getName()));
		
				changeSet(setter, fieldNode.getName());
				
				transformer.removeField(fieldNode.getName());
				
				transformer.removeConstraintsFor(fieldNode.getName());
				
				transformer.addI18NPropertyList(fieldNode.getName());
				
				if (transformer.classNode.getMethods("withLocale").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(Locale.class), "locale");
					
					parameters[1] = new Parameter(new ClassNode(Closure.class), "caller");

					StringBuilder stringBuilder = new StringBuilder();
					
					stringBuilder.append("ar.com.artdecode.internationalization.helper.I18NHelper.withLocale(locale, caller);\n");
					
					stringBuilder.append("return;");
					
					String configStr = stringBuilder.toString();
					
					BlockStatement statement = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);
					
					MethodNode methodNode = new MethodNode("withLocale", ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE, parameters, null, statement);
					
					transformer.classNode.addMethod(methodNode);
				}

				if (transformer.classNode.getMethods("findI18NAll").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(String.class), "query");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					StringBuilder stringBuilder = new StringBuilder();
					
					stringBuilder.append("return ar.com.artdecode.internationalization.helper.I18NHelper.findI18NAll(" + transformer.classNode.getName() +".class, query, parameters);");
					
					String configStr = stringBuilder.toString();
					
					BlockStatement statement = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);
					
					MethodNode methodNode = new MethodNode("findI18NAll", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, parameters, null, statement);
					
					transformer.classNode.addMethod(methodNode);
				}

				if (transformer.classNode.getMethods("findI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(String.class), "query");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					StringBuilder stringBuilder = new StringBuilder();
					
					stringBuilder.append("return ar.com.artdecode.internationalization.helper.I18NHelper.findI18N(" + transformer.classNode.getName() +".class, query, parameters);");
					
					String configStr = stringBuilder.toString();
					
					BlockStatement statement = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);
					
					MethodNode methodNode = new MethodNode("findI18N", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, parameters, null, statement);
					
					transformer.classNode.addMethod(methodNode);
				}
				if (transformer.classNode.getMethods("findWhereI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(Map.class), "queryMap");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					StringBuilder stringBuilder = new StringBuilder();
					
					stringBuilder.append("return ar.com.artdecode.internationalization.helper.I18NHelper.findWhereI18N(" + transformer.classNode.getName() +".class, queryMap, parameters);");
					
					String configStr = stringBuilder.toString();
					
					BlockStatement statement = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);
					
					MethodNode methodNode = new MethodNode("findWhereI18N", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, parameters, null, statement);
					
					transformer.classNode.addMethod(methodNode);
				}
				if (transformer.classNode.getMethods("findAllWhereI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(Map.class), "queryMap");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					StringBuilder stringBuilder = new StringBuilder();
					
					stringBuilder.append("return ar.com.artdecode.internationalization.helper.I18NHelper.findAllWhereI18N(" + transformer.classNode.getName() +".class, queryMap, parameters);");
					
					String configStr = stringBuilder.toString();
					
					BlockStatement statement = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);
					
					MethodNode methodNode = new MethodNode("findAllWhereI18N", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, parameters, null, statement);
					
					transformer.classNode.addMethod(methodNode);
				}				
			} 
			catch (Exception exception) {
				
				StringWriter stringWriter = new StringWriter();
				
				exception.printStackTrace(new PrintWriter(stringWriter));
				
				log(stringWriter.toString());
			}

			break;
		}

	}
	


	private String getDefaultFieldName(String fieldName) {
		return fieldName + "Default";
	}
	


	private void changeGet(MethodNode mn, String property) {
		
		BlockStatement newConfig = createGetBlock(property);
		
		mn.setCode(newConfig);

//		BlockStatement block = (BlockStatement) mn.getCode();
//		
//		block.addStatement(newConfig.getStatements().get(0));
	}

	private BlockStatement createGetBlock(String property) {
		
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("return ar.com.artdecode.internationalization.helper.I18NHelper.getValue(this, \"").append(property).append("\");");
		
		String configStr = stringBuilder.toString();
		
		BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);

		return newConfig;
	}

	private void changeSet(MethodNode mn, String property) {
		
		BlockStatement newConfig = createSetBlock(property, mn.getParameters()[0].getName());

		mn.setCode(newConfig);
		
		//BlockStatement block = (BlockStatement) mn.getCode();
		
		//block.addStatement(newConfig.getStatements().get(0));
	}

	private BlockStatement createSetBlock(String property, String parameter) {
		
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("ar.com.artdecode.internationalization.helper.I18NHelper.setValue(this, \"").append(property).append("\", ").append(parameter).append(");");
		
		stringBuilder.append(";\nreturn;");
		
		String configStr = stringBuilder.toString();
		
		System.out.println(configStr);
		
		BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);

		return newConfig;
	}	
	


	protected void log(String message) {
		System.out.println("[I18n] " + message);
		
		
	}
	
	

	
	
}
