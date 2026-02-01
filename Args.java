class Args extends Token implements TI {
	Expr expression;
  Args arguments;
  public Args(Expr e, Args a)
  {
    expression = e;
    arguments = a;
  }

  public Args(Expr e)
  {
    expression = e;
    arguments = null;
  }

  public String typeCheck() throws LangException {
  	expression.typeCheck();
  	if(arguments != null) {
  		return arguments.typeCheck();
  	}
  	return "";
  }

  public String toString(int t)
  {
  	return(expression.toString(t) +
	       (arguments != null ? ", " + arguments.toString(t) : "") );
  }
}
