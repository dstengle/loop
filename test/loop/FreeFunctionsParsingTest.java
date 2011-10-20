package loop;

import loop.ast.script.FunctionDecl;
import loop.ast.script.Unit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for functions declared in the top-lexical space of a script.
 */
public class FreeFunctionsParsingTest {

  @Test
  public final void simpleFunctionDeclaration() {
    compareFunction("func", "(func: () -> (comput (. x) (+ (. 1))))", "func () ->\n  x + 1\n");
    compareFunction("func", "(func: () -> (comput (. x) (+ (. 1))))",
        "func () ->\n  x + 1\n\n");
  }

  @Test
  public final void manyFunctions() {
    String twoFunctionScript = "func () ->\n  x + 2\n\nfunc2 () ->\n  y.call()\n";

    compareFunction("func", "(func: () -> (comput (. x) (+ (. 2))))",
        twoFunctionScript);
    compareFunction("func2", "(func2: () -> (comput (. y call())))",
        twoFunctionScript);
  }

  @Test
  public final void manyFunctionsWithGaps() {
    String twoFunctionScript = "\n\n\nfunc () ->\n  x + 2\n  \n \nfunc2 () ->\n  y.call()\n";

    compareFunction("func", "(func: () -> (comput (. x) (+ (. 2))))",
        twoFunctionScript);
    compareFunction("func2", "(func2: () -> (comput (. y call())))",
        twoFunctionScript);
  }

  @Test
  public final void manyFunctionsWithInternalGaps() {
    String twoFunctionScript = "\n\n\nfunc () ->\n  x + 2\n\nignore ->\n  x = x - 2 \n\n\n\nfunc2 () ->\n  y.call()\n";

    compareFunction("func", "(func: () -> (comput (. x) (+ (. 2))))",
        twoFunctionScript);
    compareFunction("func2", "(func2: () -> (comput (. y call())))",
        twoFunctionScript);
  }

  @Test
  public final void functionDeclarationWithArgs() {
    compareFunction("func", "(func: (()= x y z) -> (comput (. x) (+ (. 1))))", "func (x, y, z) ->\n  x + 1\n");
    compareFunction("func", "(func: (()= x:Integer y:String z) -> (comput (. x) (+ (. 1))))",
        "func (x: Integer, y: String, z) ->\n  x + 1\n");
  }

  @Test
  public final void functionWithAnonymousFunctionsInside() {
    compareFunction("main",
        "(main: () -> (comput (. func(()= (comput (<anonymous>: () -> (comput (. 4))))))))",
        "main ->\n  func(@() ->\n    4)\n");
    compareFunction("func",
        "(func: (()= x y z) -> (comput (<anonymous>: () -> (comput (. 1) (+ (. 2 toString()))))))",
        "func (x, y, z) ->\n  @() ->\n    1 + 2.toString()\n");
    compareFunction("func",
        "(func: () -> (comput (<anonymous>: () -> (comput (<anonymous>: () -> (comput (. 1)))))))",
        "func() ->\n  @() ->\n    @() ->\n      1");
    compareFunction("func",
        "(func: () -> (comput (<anonymous>: () -> (comput (<anonymous>: () -> (comput (. func(()= (comput (<anonymous>: () -> (comput (. 4))))))))))))",
        "func() ->\n  @() ->\n    @() ->\n      func(@() ->\n    4\n)");
  }

  static void compareFunction(String functionName, String expected, String input) {
    Parser parser = new Parser(new Tokenizer(input).tokenize());
    Unit unit = parser.script();
    Assert.assertNotNull("Parser returned no output", unit);

    FunctionDecl function = unit.get(functionName);
    Assert.assertNotNull("No such function " + functionName, function);


    String stringified = Parser.stringify(function);

    System.out.println("\n------------------------");
    System.out.println("Parse Tree:\n" + function);
    System.out.println("Parse S-Expr:\n" + stringified);
    Assert.assertEquals(expected, stringified);
    System.out.println("PASS");
  }
}
