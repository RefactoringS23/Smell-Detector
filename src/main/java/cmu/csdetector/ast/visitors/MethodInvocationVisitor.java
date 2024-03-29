package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Assumes that the root node is a method declaration. This visitor
 * returns bindings for all distinct method calls performed inside the body of the
 * visited method
 * 
 * @author Diego Cedrim
 */
public class MethodInvocationVisitor extends ASTVisitor {
	
	private Set<IMethodBinding> calls;
	
	
	public MethodInvocationVisitor() {
		this.calls = new HashSet<>();
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}

		ITypeBinding typeBinding = methodBinding.getDeclaringClass();
		if (typeBinding == null) { // if we were not able to bind it, just discard.
			return true;
		}
		if (typeBinding.getQualifiedName().startsWith("java")){
			return true;
		}
		
		this.calls.add(methodBinding);
		return true;
	}
	
	public Set<IMethodBinding> getCalls() {
		return calls;
	}
}

























