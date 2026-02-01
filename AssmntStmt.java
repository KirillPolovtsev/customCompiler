class AssmntStmt extends NonWhileStmt implements TI {
	Name name;
  Expr expression;
  public AssmntStmt(Name n, Expr e)
  {
    name = n;
    expression = e;
  }

  public String typeCheck() throws LangException {
  	String lhsType = name.typeCheck();
  	String rhsType = expression.typeCheck();
  	
  	if (symbolTable.isFinal(name.id)) {
  		throw new LangException("Error: Cannot assign to final variable " + name.id);
  	}
  	
  	if (!isCompatibleAssignment(lhsType, rhsType)) {
  		throw new LangException("Error: Tried to assign " + rhsType + " to type " + lhsType + " : " + toString(0));
  	}
  	return "";
  }
  
  private boolean isCompatibleAssignment(String lhs, String rhs) {
  	if (lhs.equals(rhs)) return true;
  	
  	if (lhs.equals("bool") && rhs.equals("int")) return true;
  	if (lhs.equals("float") && rhs.equals("int")) return true;
  	if (lhs.equals("string") && !rhs.contains("[]")) return true;
  	
  	return false;
  }

  public String toString(int t)
  {
  	return(T(t) + name.toString(t) + " = " + expression.toString(t) + ";\n");
  }
}

