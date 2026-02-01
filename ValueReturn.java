class ValueReturn extends NonWhileStmt implements TI {
	Expr expression;
  public ValueReturn(Expr e)
  {
    expression = e;
  }

  public String typeCheck() throws LangException {
  	String returnType = expression.typeCheck();
  	String expectedType = MethodDecl.currentMethodReturnType;
  	
  	if (expectedType == null) {
  		throw new LangException("Error: Return statement outside of method");
  	}
  	
  	if (expectedType.equals("void")) {
  		throw new LangException("Error: Cannot return value from void method");
  	}
  	
  	if (!isCompatibleReturn(expectedType, returnType)) {
  		throw new LangException("Error: Cannot return " + returnType + " from method expecting " + expectedType);
  	}
  	
  	return returnType;
  }
  
  private boolean isCompatibleReturn(String expected, String actual) {
  	if (expected.equals(actual)) return true;
  	
  	// Coercion rules for return
  	if (expected.equals("bool") && actual.equals("int")) return true;
  	if (expected.equals("float") && actual.equals("int")) return true;
  	if (expected.equals("string") && !actual.contains("[]")) return true;
  	
  	return false;
  }

  public String toString(int t)
  {
  	return(T(t) + "return " + expression.toString(t) + ";\n");
  }
}

