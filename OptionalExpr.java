class OptionalExpr extends Token implements TI {

	Expr expression;
	public OptionalExpr(Expr e)
	{
		expression = e;
	}

	public String typeCheck() throws LangException {
		return expression.typeCheck();
	}

	public String toString(int t)
	{
		return( " = " + expression.toString(t));
	}

}
