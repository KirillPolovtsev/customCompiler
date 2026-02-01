abstract class Expr extends Token implements TI {
	public abstract String typeCheck() throws LangException;
}
