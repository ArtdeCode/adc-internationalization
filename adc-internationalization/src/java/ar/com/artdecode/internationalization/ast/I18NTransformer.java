package ar.com.artdecode.internationalization.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

public class I18NTransformer implements  Opcodes {
	
	ClassNode classNode;
	FieldNode fieldNode;
	
	public I18NTransformer(ClassNode classNode, FieldNode fieldNode) {
		super();
		this.classNode = classNode;
		this.fieldNode = fieldNode;
	}
	
	protected MethodNode getOrCreateMethod(String methodName) {
	
		Parameter [] parameters = Parameter.EMPTY_ARRAY;
		
		ClassNode returnClassNode = ClassHelper.OBJECT_TYPE;
		
		if (methodName.startsWith("set")) {
			
			FieldNode fieldNode =  classNode.getField(StringUtils.uncapitalize(methodName.substring(3)));
			
			parameters = new Parameter[1];
			
			parameters[0] = new Parameter(fieldNode.getType(), fieldNode.getName());
			
			returnClassNode = ClassHelper.VOID_TYPE;
			
			//System.out.println("set method " + methodName);
		}
		
		MethodNode mn = classNode.getMethod(methodName, parameters);
		
		if(mn == null){
			
			//System.out.println("create method");
			
			classNode.addMethod(methodName, Modifier.PUBLIC, returnClassNode, parameters, null, new BlockStatement());
			
			mn = classNode.getMethod(methodName, parameters);
			
			assert mn != null;
		}
		
		return mn;
	}
	
	
	
	
	protected void addNullableConstraint(String fieldName) {
		FieldNode closure = classNode.getDeclaredField("constraints");
		
		if (closure != null) {

			ClosureExpression exp = (ClosureExpression) closure.getInitialExpression();
			BlockStatement block = (BlockStatement) exp.getCode();

			if (!hasFieldInClosure(closure, fieldName)) {
		
				NamedArgumentListExpression namedarg = new NamedArgumentListExpression();
				
				namedarg.addMapEntryExpression(new ConstantExpression("nullable"), new ConstantExpression(true));
				
				namedarg.addMapEntryExpression(new ConstantExpression("blank"),	new ConstantExpression(true));
				
				MethodCallExpression constExpr = new MethodCallExpression(VariableExpression.THIS_EXPRESSION,new ConstantExpression(fieldName), namedarg);
				
				block.addStatement(new ExpressionStatement(constExpr));
			}
		}
	}

	protected boolean hasFieldInClosure(FieldNode closure, String fieldName) {
		if (closure != null) {
			ClosureExpression exp = (ClosureExpression) closure
					.getInitialExpression();
			BlockStatement block = (BlockStatement) exp.getCode();
			List<Statement> ments = block.getStatements();
			for (Statement expstat : ments) {
				if (expstat instanceof ExpressionStatement
						&& ((ExpressionStatement) expstat).getExpression() instanceof MethodCallExpression) {
					MethodCallExpression methexp = (MethodCallExpression) ((ExpressionStatement) expstat)
							.getExpression();
					ConstantExpression conexp = (ConstantExpression) methexp
							.getMethod();
					if (conexp.getValue().equals(fieldName)) {
						return true;
					}
				}
			}
		}
		return false;
	}



	protected void addGetter() {
		addGetter(fieldNode, ACC_PUBLIC);
	}

	protected void addGetter(FieldNode fieldNode,
			int modifier) {
		ClassNode type = fieldNode.getType();
		String getterName = "get" + StringUtils.capitalize(fieldNode.getName());
		classNode.addMethod(getterName, modifier, nonGeneric(type),
				Parameter.EMPTY_ARRAY, null, new ReturnStatement(
						new FieldExpression(fieldNode)));
	}

	protected void addSetter() {
		addSetter(ACC_PUBLIC);
	}

	protected void addSetter(int modifier) {
		addSetter(fieldNode, modifier);
	}
		
	
	protected void addSetter(FieldNode fieldNode, int modifier) {
		ClassNode type = fieldNode.getType();
		String name = fieldNode.getName();
		String setterName = "set" + StringUtils.capitalize(name);
		classNode.addMethod(
				setterName,
				modifier,
				ClassHelper.VOID_TYPE,
				new Parameter[] { new Parameter(nonGeneric(type), "value") },
				null,
				new ExpressionStatement(new BinaryExpression(
						new FieldExpression(fieldNode), Token.newSymbol(
								Types.EQUAL, -1, -1), new VariableExpression(
								"value"))));
	}

	protected ClassNode nonGeneric(ClassNode type) {
		if (type.isUsingGenerics()) {
			final ClassNode nonGen = ClassHelper.makeWithoutCaching(type
					.getName());
			nonGen.setRedirect(type);
			nonGen.setGenericsTypes(null);
			nonGen.setUsingGenerics(false);
			return nonGen;
		} else {
			return type;
		}
	}
	

	protected FieldNode getOrCreateField(String name, Expression type) {
		if (!fieldExists(name)) addStaticField(name, type);
		
		return classNode.getDeclaredField(name);
	}
	
	protected FieldNode getOrCreateTransientsField() { 
	    return getOrCreateField("transients", new ListExpression()); 
    }
	
	protected FieldNode getOrCreateI18NField() { 
	    return getOrCreateField("i18nFields", new ListExpression()); 
    }
	
	/**
     * Add a static field of a given type to the ClassNode
     */
	protected void addStaticField(String name, Expression initialExpression) {
		FieldNode field = new FieldNode(name, ACC_PUBLIC | ACC_STATIC, new ClassNode(Object.class), classNode, initialExpression);
		field.setDeclaringClass(classNode);
		classNode.addField(field);
	}
	
	protected void addField(String name, Expression initialExpression) {
		FieldNode field = new FieldNode(name, ACC_PUBLIC, new ClassNode(Object.class), classNode, initialExpression);

		field.setDeclaringClass(classNode);

		classNode.addField(field);

	}

	protected boolean fieldExists(String name) {
		return null != classNode.getDeclaredField(name);
	}

	protected void makeFieldTransient(String name) {
		
		fieldNode.setModifiers(Modifier.TRANSIENT);
		
		ListExpression transients = (ListExpression) getOrCreateTransientsField().getInitialExpression();
		
		transients.addExpression(new ConstantExpression(name));;
	}
	
	protected void addI18NPropertyList(String name) {
		
		ListExpression transients = (ListExpression) getOrCreateI18NField().getInitialExpression();
		
		transients.addExpression(new ConstantExpression(name));;
	}


	protected FieldNode addField(String fieldName, Class<?> type) {
		
		FieldNode defaultField = new FieldNode(fieldName, Modifier.PRIVATE,new ClassNode(type), classNode, null);
		
		classNode.addField(defaultField);
		
		return defaultField;
	}
	
	protected FieldNode addProperty(String fieldName, Class<?> type) {
		
		FieldNode fieldNode =  addField(fieldName, type);
		
		addGetter(fieldNode, ACC_PUBLIC);
		
		addSetter(fieldNode, ACC_PUBLIC);
		
		return fieldNode;
	}

	protected void removeField(String name) {
		
		classNode.getProperties().remove(classNode.getProperty(name));
		
		classNode.removeField(name);
	}
	
	private FieldNode getConstraints() {
		return classNode.getDeclaredField("constraints");
	}

    /**
     * Remove all the constraints for a field
     */
	protected void removeConstraintsFor(String field) {
		FieldNode constraints =  getConstraints();
		
		if (constraints == null) {
			return;
		}
				
		ClosureExpression block = (ClosureExpression) constraints.getInitialExpression();
		
		if (block != null) {
			
			BlockStatement blockStatement = (BlockStatement) block.getCode();
			
			List<Statement> statements =  blockStatement.getStatements();
			
			List<Statement> filtered = new ArrayList<Statement>();
			
			for (Statement statement : statements) {
				
				ExpressionStatement expressionStatement = (ExpressionStatement) statement;

				MethodCallExpression methodCallExpression = (MethodCallExpression) expressionStatement.getExpression();
				
				if (!methodCallExpression.getMethodAsString().equals(field)) {
					
					filtered.add(statement);
				}
			}
			
			block.setCode(new BlockStatement(filtered, block.getVariableScope()));
		}
	}

	protected boolean containsAMethodCallExpression(Statement statement) {
		return statement instanceof ExpressionStatement && (((ExpressionStatement) statement).getExpression() instanceof MethodCallExpression); 
	}

}
