class MethodDecl extends Token implements TI {

	static String currentMethodReturnType = null;
	MethodStart methodStart;
	ArgDecls argumentDeclarations;
	FieldDecls fieldDeclarations;
	Stmts statements;
	Boolean hasSemicolon;

	public MethodDecl(MethodStart m, ArgDecls a, FieldDecls f, Stmts s, OptionalSemi o)
	{
		methodStart = m;
		argumentDeclarations = a;
		fieldDeclarations = f;
		statements = s;
		hasSemicolon = o != null;
	}

	public MethodDecl(MethodStart m, FieldDecls f, Stmts s, OptionalSemi o)
	{
		methodStart = m;
		argumentDeclarations = null;
		fieldDeclarations = f;
		statements = s;
		hasSemicolon = o != null;
	}

	public String typeCheck() throws LangException {
		String returnType = methodStart.typeCheck();
		currentMethodReturnType = returnType;
		
		ScopeContext.setMethodName(returnType + " " + methodStart.id);
		symbolTable.StartScope();

		if(argumentDeclarations != null) {
			argumentDeclarations.typeCheck();
		}
		if(fieldDeclarations != null) {
			fieldDeclarations.typeCheck();
		}
			
		boolean hasReturn = false;
		if(statements != null) {
			hasReturn = statements.hasReturnStatement();
			statements.typeCheck();
		}
			
		if (!returnType.equals("void") && !hasReturn) {
			throw new LangException("Error: Method " + methodStart.id + " must return " + returnType);
		}
		symbolTable.EndScope();
		currentMethodReturnType = null;
		ScopeContext.clearMethodName();
		
		return "";
	}

	public String toString(int t)
	{
		return( T(t) + methodStart.toString(t) + "(" + 
			( argumentDeclarations != null ? argumentDeclarations.toString(t) : "") 
			+ ")\n" + T(t) +"{\n" + 
			(fieldDeclarations != null ? fieldDeclarations.toString(t + 1) : "") 
			+ (statements != null ? statements.toString(t + 1) : "")
			+ T(t) + "}" + (hasSemicolon ? ";\n" : "\n") );
	}

}
