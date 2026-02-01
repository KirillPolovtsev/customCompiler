class MemberDecls extends Token implements TI {
  FieldsNMethods fieldsAndMethods;

  public MemberDecls(FieldsNMethods f) {
    fieldsAndMethods = f;
  }

  public String typeCheck() throws LangException {
    return fieldsAndMethods.typeCheck();
  }

  public String toString(int t) {
    return (fieldsAndMethods != null ? fieldsAndMethods.toString(t) : "");
  }
}
