class UnaryStmt extends NonWhileStmt implements TI {
	Name name;
  String operator;
  public UnaryStmt(Name n, String o)
  {
    name = n;
    operator = o;
  }

  public String typeCheck() throws LangException {
  	String nameType = name.typeCheck();
  	
  	if (operator.equals("++") || operator.equals("--")) {
  		if (!nameType.equals("int") && !nameType.equals("float")) {
  			throw new LangException("Cannot increment/decrement variable of type: " + nameType, name.toString(0) + operator + ";");
  		}
  		if (symbolTable.isFinal(name.id)) {
  			throw new LangException("Cannot " + operator + " final variable " + name.id, name.toString(0) + operator + ";");
  		}
  	}
  	
  	return "";
  }

  public String toString(int t)
  {
  	return(T(t) + name.toString(t) + operator + ";\n");
  }
}

