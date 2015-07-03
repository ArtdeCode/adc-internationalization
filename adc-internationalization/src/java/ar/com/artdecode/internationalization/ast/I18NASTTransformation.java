package ar.com.artdecode.internationalization.ast;


import groovy.lang.Closure;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import ar.com.artdecode.internationalization.helper.ConfigProvider;
import ar.com.artdecode.internationalization.helper.I18NHelper;

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
				
				Class<?> helperClass = I18NHelper.class;
				
				sourceUnit.getAST().addImport(helperClass.getSimpleName(), ClassHelper.make(helperClass));
				
				//sourceUnit.getAST().addStaticStarImport(helperClass.getSimpleName(), ClassHelper.make(helperClass));
				
				transformer.addProperty(getDefaultFieldName(fieldNode.getName()), fieldNode.getType().getTypeClass());
				
				transformer.addNullableConstraint(getDefaultFieldName(fieldNode.getName()));
				
				log("Process field " + fieldNode.getName());
				
				List<String> languages = ConfigProvider.getLanguages();
				
				if (languages == null) {
					
					languages = Arrays.asList("es", "en", "fr","de","pt", "it");
				
					log("Process field languages is null ");
				}
				
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
					
					BlockStatement blockStatement = new BlockStatement();
					
		            Expression staticExpression = new StaticMethodCallExpression(new ClassNode(I18NHelper.class),"withLocale" ,new ArgumentListExpression(parameters));
		            
					blockStatement.addStatement(new ExpressionStatement(staticExpression));
					
					MethodNode methodNode = new MethodNode("withLocale", ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE, parameters, null, blockStatement);
					
					transformer.classNode.addMethod(methodNode);
				}

				if (transformer.classNode.getMethods("findI18NAll").isEmpty()) {
					
					String methodName = "findI18NAll";
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(String.class), "query");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					MethodNode methodNode = createDelegateMethod("findI18NAll", parameters);
					
					transformer.classNode.addMethod(methodNode);
				}

				if (transformer.classNode.getMethods("findI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(String.class), "query");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					MethodNode methodNode = createDelegateMethod("findI18N", parameters);
					
					transformer.classNode.addMethod(methodNode);
				}
				if (transformer.classNode.getMethods("findWhereI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(Map.class), "queryMap");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					MethodNode methodNode = createDelegateMethod("findWhereI18N", parameters);
						
					transformer.classNode.addMethod(methodNode);
				}
				if (transformer.classNode.getMethods("findAllWhereI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[2];
					
					parameters[0] = new Parameter(new ClassNode(Map.class), "queryMap");
					
					parameters[1] = new Parameter(new ClassNode(Map.class), "parameters");

					parameters[1].setInitialExpression(new MapExpression());
					
					MethodNode methodNode = createDelegateMethod("findAllWhereI18N", parameters);
					
					transformer.classNode.addMethod(methodNode);
				}
				
				if (transformer.classNode.getMethods("i18nMap").isEmpty()) {
					
					Parameter [] parameters = new Parameter[1];
					
					parameters[0] = new Parameter(new ClassNode(Map.class), "parameters");
					
					MethodNode methodNode = createDelegateMethod("i18nMap", parameters);
					
					transformer.classNode.addMethod(methodNode);
				}		
				
				if (transformer.classNode.getMethods("createI18N").isEmpty()) {
					
					Parameter [] parameters = new Parameter[1];
					
					parameters[0] = new Parameter(new ClassNode(Map.class), "parameters");
					
					MethodNode methodNode = createDelegateMethod("createI18N", parameters);
					
					transformer.classNode.addMethod(methodNode);
				}				
		
			} 
			catch (Exception exception) {
				
				StringWriter stringWriter = new StringWriter();
				
				exception.printStackTrace(new PrintWriter(stringWriter));
				
				log(stringWriter.toString());
				
				throw new RuntimeException(exception.getMessage(), exception);
				
			}

			break;
		}

	}



	private MethodNode createDelegateMethod(String methodName,
			Parameter[] parameters) {
		BlockStatement blockStatement = new BlockStatement();

		List<Expression> expressions = new ArrayList<Expression>();
		
		expressions.add(new VariableExpression("this"));

		for (Parameter parameter : parameters) {

			expressions.add(new VariableExpression(parameter.getName()));
			
		}
		
		Expression staticExpression = new StaticMethodCallExpression(new ClassNode(I18NHelper.class),methodName ,new ArgumentListExpression(expressions));
		
		blockStatement.addStatement(new ReturnStatement(staticExpression));
		
		MethodNode methodNode = new MethodNode(methodName, ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, parameters, null, blockStatement);
		return methodNode;
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
		

		BlockStatement blockStatement = new BlockStatement();
		
		List<Expression> expressions = new ArrayList<Expression>();
		
		expressions.add(new VariableExpression("this"));
		
		expressions.add(new ConstantExpression(property));
		
        Expression staticExpression = new StaticMethodCallExpression(new ClassNode(I18NHelper.class),"getValue" ,new ArgumentListExpression(expressions));
        
		blockStatement.addStatement(new ReturnStatement(staticExpression));
		
		return blockStatement;
	}

	private void changeSet(MethodNode mn, String property) {
		
		BlockStatement newConfig = createSetBlock(property, mn.getParameters()[0].getName());

		mn.setCode(newConfig);
		
		//BlockStatement block = (BlockStatement) mn.getCode();
		
		//block.addStatement(newConfig.getStatements().get(0));
	}

	private BlockStatement createSetBlock(String property, String parameter) {
		
		BlockStatement blockStatement = new BlockStatement();
		
		List<Expression> expressions = new ArrayList<Expression>();
		
		expressions.add(new VariableExpression("this"));
		
		expressions.add(new ConstantExpression(property));
		
		expressions.add(new VariableExpression(parameter));
		
        Expression staticExpression = new StaticMethodCallExpression(new ClassNode(I18NHelper.class),"setValue" ,new ArgumentListExpression(expressions));
        
		blockStatement.addStatement(new ExpressionStatement(staticExpression));
		
		return blockStatement;
		
		/*StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("I18NHelper.setValue(this, \"").append(property).append("\", ").append(parameter).append(");");
		
		stringBuilder.append("\nreturn;");
		
		String configStr = stringBuilder.toString();
		
		//System.out.println(configStr);
		
		BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);

		return newConfig;*/
	}	
	


	protected  void log(String message) {
		System.out.println("[I18n] " + message);
		
		
	}
	
	

	
	
}
