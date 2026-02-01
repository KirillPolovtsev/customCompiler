class ParenExpr extends Expr implements TI {
	Expr expression;
  public ParenExpr(Expr e)
  {
    expression = e;
  }

  public String typeCheck() throws LangException {
  	return expression.typeCheck();
  }

  public String toString(int t)
  {
  	return("(" + expression.toString(t) + ")");
  }
}

