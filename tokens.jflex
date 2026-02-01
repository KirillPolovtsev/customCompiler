/*-***
 *
 * This file defines a stand-alone lexical analyzer for a subset of the Pascal
 * programming language.  This is the same lexer that will later be integrated
 * with a CUP-based parser.  Here the lexer is driven by the simple Java test
 * program in ./PascalLexerTest.java, q.v.  See 330 Lecture Notes 2 and the
 * Assignment 2 writeup for further discussion.
 *
 */


import java_cup.runtime.*;


%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%cup
%line
%column
%unicode
%class Lexer

%{

/**
 * Return a new Symbol with the given token id, and with the current line and
 * column numbers.
 */
Symbol newSym(int tokenId) {
    return new Symbol(tokenId, yyline, yycolumn);
}

/**
 * Return a new Symbol with the given token id, the current line and column
 * numbers, and the given token value.  The value is used for tokens such as
 * identifiers and numbers.
 */
Symbol newSym(int tokenId, Object value) {
    return new Symbol(tokenId, yyline, yycolumn, value);
}

%}


/*-*
 * PATTERN DEFINITIONS:
 */
tab     				= \\t
newline	        = \\n
slash			      = \\ 
escapeapos		  = {slash}'
escapequote		  = {slash}\"
letter      	  = [A-Za-z]
digit       	  = [0-9]
id   			      = {letter}({letter}|{digit})* 
intlit	    	  = {digit}+
floatlit    	  = {intlit}+\.{intlit}+
//                Technically, the rules allow for literal tabs and new line in chars
//                While this is untested, this solution allows it
charchar		    = [[^\\]&&[^']]|{newline}|{tab}|{escapeapos}|{slash}{slash}
charlit     	  = '{charchar}'
//                We allow a string to accept any character outside of what is explicitly
//                banned in the rules.
stringchar		  = [[[^\\]&&[^\"]]&&[[^\n]&&[^\t]]]|{newline}|{tab}|{escapequote}|{slash}{slash}
stringlit		    = \"{stringchar}*\"
blockcommentS   = {slash}\*
blockcommentE   = \*{slash}
//                A comment body character can be anything other than the end to
//                the block comment.
commentbody		  = ([^\*]|(\*+[^\\]))
blockcomment    = {blockcommentS}{commentbody}*?{blockcommentE}
inlinecomment 	= {slash}{slash}.*(\n|\r|\r\n)
whitespace      = [ \n\t\r]


%%
/**
 * LEXICAL RULES:
 */
class           { return newSym(sym.CLASS, "class");}
"&&"            { return newSym(sym.AND, "&&"); }
else            { return newSym(sym.ELSE, "else"); }
if              { return newSym(sym.IF, "if"); }
while		        { return newSym(sym.WHILE, "while"); }
read		        { return newSym(sym.READ, "read"); }
print		        { return newSym(sym.PRINT, "print"); }
printline	      { return newSym(sym.PRINTLN, "printline"); }
return		      { return newSym(sym.RETURN, "return"); }
"||"            { return newSym(sym.OR, "||"); }
"*"             { return newSym(sym.TIMES, "*"); }
"+"             { return newSym(sym.PLUS, "+"); }
"+" 		        { return newSym(sym.PREFIXPLUS, "+"); }
"++"		        { return newSym(sym.PLUSPLUS, "++"); }
"-"             { return newSym(sym.MINUS, "-"); }
"-"		          { return newSym(sym.PREFIXMINUS, "-"); }
"--"		        { return newSym(sym.MINUSMINUS, "--"); }
"/"             { return newSym(sym.DIVIDE, "/"); }
";"             { return newSym(sym.SEMI, ";"); }
"("             { return newSym(sym.LEFT_PAREN, "("); }
")"             { return newSym(sym.RT_PAREN, ")"); }
"{"		          { return newSym(sym.LEFT_BRACE, "{"); }
"}"		          { return newSym(sym.RT_BRACE, "}"); }
"["             { return newSym(sym.LEFT_BRKT, "["); }
"]"             { return newSym(sym.RT_BRKT, "]"); }
"=="            { return newSym(sym.EQ, "=="); }
">"             { return newSym(sym.GTR, ">"); }
"<"             { return newSym(sym.LESS, "<"); }
"<="            { return newSym(sym.LESS_EQ, "<="); }
">="            { return newSym(sym.GTR_EQ, ">="); }
"<>"            { return newSym(sym.NOT_EQ, "<>"); }
"~"		          { return newSym(sym.NOT, "~"); }
"?"		          { return newSym(sym.CONDITION, "?"); }
":"             { return newSym(sym.COLON, ":"); }
"="             { return newSym(sym.ASSMNT, "="); }
","		          { return newSym(sym.COMMA, ","); }
final 		      { return newSym(sym.FINAL, "final"); }
void		        { return newSym(sym.VOID, "void"); }
int		          { return newSym(sym.INT, "int"); }
float		        { return newSym(sym.FLOAT, "float"); }
bool		        { return newSym(sym.BOOL, "bool"); }
char		        { return newSym(sym.CHAR, "char"); }
true		        { return newSym(sym.TRUE, "true"); }
false		        { return newSym(sym.FALSE, "false"); }
{id}    	      { return newSym(sym.ID, yytext()); }
{intlit}        { return newSym(sym.INTLIT, new Integer(yytext())); }
{floatlit}      { return newSym(sym.FLOATLIT, new Double(yytext())); }
{charlit}       { return newSym(sym.CHARLIT, yytext()); }
{stringlit}	    { return newSym(sym.STRINGLIT, yytext()); }
{inlinecomment} { /* For this stand-alone lexer, print out comments. */}
{blockcomment}	{ /* For this stand-alone lexer, print out comments. */}
{whitespace}    { /* Ignore whitespace. */ }
.               { System.out.println("Illegal char, '" + yytext() +
                    "' line: " + yyline + ", column: " + yychar); }