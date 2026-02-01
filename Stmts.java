class Stmts extends Token implements TI {
  Stmt statement;
  Stmts statements;
  public Stmts(Stmt s, Stmts x)
  {
    statement = s;
    statements = x;
  }

  public String typeCheck() throws LangException {
  	if(statement != null) {
  		statement.typeCheck();
  	}
  	if(statements != null) {
  		return statements.typeCheck();
  	}
  	return "";
  }
  
  public boolean hasReturnStatement() {
  	if(statement != null && (statement instanceof ValueReturn || statement instanceof VoidReturn)) {
  		return true;
  	}
  	if(statements != null) {
  		return statements.hasReturnStatement();
  	}
  	return false;
  }

  public String toString(int t)
  {
  	return (statement.toString(t) + (statements != null ? statements.toString(t) : ""));
  }
}

