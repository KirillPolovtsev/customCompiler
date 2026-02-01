class TypeCastExpr extends ActionExpr implements TI {
	Type type;
  Expr expression;
  public TypeCastExpr(Type t, Expr e)
  {
    type = t;
    expression = e;
  }

  public String typeCheck() throws LangException {
  	expression.typeCheck();
  	return type.toString(0);
  }

  public String toString(int t)
  {
  	return("((" + type.toString(t) + ")" + expression.toString(t) + ")");
  }
}

