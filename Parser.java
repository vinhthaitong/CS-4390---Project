/* PARSER SIDE:
  - Parse a math expression string into a numeric result.
  - Support +, -, *, /, unary signs, decimals, and parentheses.
  - Respect operator precedence with expression/term/factor parsing.
  - Throw DIVIDE_BY_ZERO or Invalid expression when parsing fails.
*/

class Parser {
  private final String expressionText;
  private int currentIndex;

  // Create a parser instance with the expression and start index.
  private Parser(String expressionText) {
    this.expressionText = expressionText;
    this.currentIndex = 0;
  }

  // Parse the full expression 
  static double parse(String expressionText) {
    Parser expressionParser = new Parser(expressionText);
    double parsedResult = expressionParser.parseExpression();
    expressionParser.skipSpaces();
    if (!expressionParser.isEnd()) {
      throw new IllegalArgumentException("Invalid expression");
    }
    return parsedResult;
  }

  // Parse addition and subtraction operations.
  private double parseExpression() {
    double expressionValue = parseTerm();
    while (true) {
      skipSpaces();
      if (match('+')) {
        expressionValue += parseTerm();
      } else if (match('-')) {
        expressionValue -= parseTerm();
      } else {
        return expressionValue;
      }
    }
  }

  // Parse multiplication and division operations.
  private double parseTerm() {
    double termValue = parseFactor();
    while (true) {
      skipSpaces();
      if (match('*')) {
        termValue *= parseFactor();
      } else if (match('/')) {
        double divisorValue = parseFactor();
        if (divisorValue == 0.0) {
          throw new IllegalArgumentException("DIVIDE_BY_ZERO");
        }
        termValue /= divisorValue;
      } else {
        return termValue;
      }
    }
  }

  // Parse unary operators, parenthesized groups, or numbers.
  private double parseFactor() {
    skipSpaces();

    if (match('+')) {
      return parseFactor();
    }
    if (match('-')) {
      return -parseFactor();
    }
    if (match('(')) {
      double nestedValue = parseExpression();
      skipSpaces();
      if (!match(')')) {
        throw new IllegalArgumentException("Invalid expression");
      }
      return nestedValue;
    }
    return parseNumber();
  }

  // Parse one numeric literal (integer or decimal).
  private double parseNumber() {
    skipSpaces();
    int numberStartIndex = currentIndex;
    boolean foundDigit = false;
    boolean foundDecimalPoint = false;

    while (!isEnd()) {
      char currentChar = expressionText.charAt(currentIndex);
      if (Character.isDigit(currentChar)) {
        foundDigit = true;
        currentIndex++;
      } else if (currentChar == '.') {
        if (foundDecimalPoint) {
          break;
        }
        foundDecimalPoint = true;
        currentIndex++;
      } else {
        break;
      }
    }

    if (!foundDigit) {
      throw new IllegalArgumentException("Invalid expression");
    }
    return Double.parseDouble(expressionText.substring(numberStartIndex, currentIndex));
  }

  // Match one expected character and advance if it exists.
  private boolean match(char expectedChar) {
    if (!isEnd() && expressionText.charAt(currentIndex) == expectedChar) {
      currentIndex++;
      return true;
    }
    return false;
  }

  // Skip whitespace characters in the expression text.
  private void skipSpaces() {
    while (!isEnd() && Character.isWhitespace(expressionText.charAt(currentIndex))) {
      currentIndex++;
    }
  }

  // Check whether the parser reached the end of the expression.
  private boolean isEnd() {
    return currentIndex >= expressionText.length();
  }
}
