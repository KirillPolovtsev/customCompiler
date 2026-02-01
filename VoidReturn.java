class VoidReturn extends NonWhileStmt implements TI {
  public VoidReturn()
  {
  }

  public String typeCheck() throws LangException {
  	String expectedType = MethodDecl.currentMethodReturnType;
  	
  	if (expectedType == null) {
  		throw new LangException("Error: Return statement outside of method");
  	}
  	
  	if (!expectedType.equals("void")) {
  		throw new LangException("Error: Method expecting " + expectedType + " cannot return void");
  	}
  	
  	return "void";
  }

  public String toString(int t)
  {
  	return(T(t) + "return;\n");
  }
}

