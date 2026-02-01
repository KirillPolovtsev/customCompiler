class TernaryExpr extends Expr implements TI {
	Expr condition;
	Expr whenTrue;
	Expr whenFalse;

  public TernaryExpr(Expr a, Expr b, Expr c)
  {
    condition = a;
    whenTrue = b;
    whenFalse = c;
  }

  public String typeCheck() throws LangException {
  	String condType = condition.typeCheck();
  	String trueType = whenTrue.typeCheck();
  	String falseType = whenFalse.typeCheck();
  	
	if (!condType.equals("bool") && !condType.equals("int")) {
		throw new LangException("Error: Ternary condition expects 'true', 'false', or bool-compatible type, got " + condType);
	}
  	
  	if (!trueType.equals(falseType)) {
  		throw new LangException("Error: Ternary branches must have same type: " + trueType + " vs " + falseType);
  	}
  	
  	return trueType;
  }

  public String toString(int t)
  {
  	return ("(" + condition.toString(t) + " ? "  + whenTrue.toString(t) + " : " + whenFalse.toString(t) + ")");
  }
}

