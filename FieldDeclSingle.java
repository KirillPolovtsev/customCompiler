class FieldDeclSingle extends FieldDecl implements TI {

	Boolean isFinal;
	OptionalExpr expression;
	public FieldDeclSingle(OptionalFinal f, FieldStart s, OptionalExpr e)
	{
		super(s);
		isFinal = true;
		expression = e;
	}

	public FieldDeclSingle(FieldStart f, OptionalExpr e)
	{
		super(f);
		isFinal = false;
		expression = e;
	}

	public String typeCheck() throws LangException {
		String type = fieldStart.type.toString(0);
		if(isFinal) {
			symbolTable.addFinalVar(fieldStart.id, type);
		} else {
			symbolTable.addVar(fieldStart.id, type);
		}
		if(expression != null) {
			String exprType = expression.typeCheck();
			if (!isCompatibleAssignment(type, exprType)) {
				throw new LangException("Error: Cannot initialize " + type + " with " + exprType);
			}
		}
		return type;
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
		return( T(t) + (isFinal ? "final " : "") + super.toString(t) + 
			(expression != null ? expression.toString(t) : "") + ";\n");
	}

}



