class Program extends Token implements TI {
  String id;
  MemberDecls memberDeclarations;

  public Program(String i, MemberDecls m) {
    id = i;
    memberDeclarations = m;
    symbolTable = new SymbolTable();
  }

  public String typeCheck() throws LangException {
    ScopeContext.setClassName(id);
    return memberDeclarations.typeCheck();
  }

  public String toString(int t) {
    return ("class " + id + "\n{\n" + memberDeclarations.toString(t + 1) + "}\n");
  }
}
