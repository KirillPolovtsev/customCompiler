class MethodStart extends Token implements TI
{
	ReturnType returnType;
	String id;

	public MethodStart(ReturnType r, String i)
	{
		returnType = r;
		id = i;
	}

	public String typeCheck() throws LangException {
		String retType = returnType.toString(0);
		InOutList params = new InOutList();
		InOut returnTypeParam = new ConcreteInOut(retType);
		params.prepend(returnTypeParam);
		symbolTable.addRoutine(id, params);
		return retType;
	}

	public String toString(int t)
	{
    	return( returnType.toString(t) + " " + id);
	}
}
