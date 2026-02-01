class WhileStmt extends NonIfStmt implements TI {
	WhileBase whileBase;
  ScopeStmt body;
  public WhileStmt(WhileBase w, ScopeStmt s)
  {
    whileBase = w;
    body = s;
  }

  public String typeCheck() throws LangException {
  	whileBase.typeCheck();
  	body.typeCheck();
  	return "";
  }

  public String toString(int t)
  {
  	return whileBase.toString(t) + body.toString(t);
  }
}

