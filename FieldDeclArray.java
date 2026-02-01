class FieldDeclArray extends FieldDecl implements TI {

	int intlit;
	public FieldDeclArray(FieldStart f, int n)
	{
		super(f);
		intlit = n;
	}

	public String typeCheck() throws LangException {
		String baseType = fieldStart.type.toString(0);
		String arrayType = baseType + "[]";
		symbolTable.addVar(fieldStart.id, arrayType);
		return arrayType;
	}

	public String toString(int t)
	{
		return( T(t) + super.toString(t) + "[" + intlit + "];\n");
	}

}
