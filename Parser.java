class Parser {
  private final String text;
  private int pos;

  private Parser(String text) {
    this.text = text;
    this.pos = 0;
  }

  static double parse(String expression) {
    Parser parser = new Parser(expression);
    double result = parser.parseExpression();
    parser.skipSpaces();
    if (!parser.isEnd()) {
      throw new IllegalArgumentException("Invalid expression");
    }
    return result;
  }

  private double parseExpression() {
    double value = parseTerm();
    while (true) {
      skipSpaces();
      if (match('+')) {
        value += parseTerm();
      } else if (match('-')) {
        value -= parseTerm();
      } else {
        return value;
      }
    }
  }

  private double parseTerm() {
    double value = parseFactor();
    while (true) {
      skipSpaces();
      if (match('*')) {
        value *= parseFactor();
      } else if (match('/')) {
        double divisor = parseFactor();
        if (divisor == 0.0) {
          throw new IllegalArgumentException("DIVIDE_BY_ZERO");
        }
        value /= divisor;
      } else {
        return value;
      }
    }
  }

  private double parseFactor() {
    skipSpaces();

    if (match('+')) {
      return parseFactor();
    }
    if (match('-')) {
      return -parseFactor();
    }
    if (match('(')) {
      double value = parseExpression();
      skipSpaces();
      if (!match(')')) {
        throw new IllegalArgumentException("Invalid expression");
      }
      return value;
    }

    return parseNumber();
  }

  private double parseNumber() {
    skipSpaces();
    int start = pos;
    boolean seenDigit = false;
    boolean seenDot = false;

    while (!isEnd()) {
      char ch = text.charAt(pos);
      if (Character.isDigit(ch)) {
        seenDigit = true;
        pos++;
      } else if (ch == '.') {
        if (seenDot) {
          break;
        }
        seenDot = true;
        pos++;
      } else {
        break;
      }
    }

    if (!seenDigit) {
      throw new IllegalArgumentException("Invalid expression");
    }
    return Double.parseDouble(text.substring(start, pos));
  }

  private boolean match(char expected) {
    if (!isEnd() && text.charAt(pos) == expected) {
      pos++;
      return true;
    }
    return false;
  }

  private void skipSpaces() {
    while (!isEnd() && Character.isWhitespace(text.charAt(pos))) {
      pos++;
    }
  }

  private boolean isEnd() {
    return pos >= text.length();
  }
}
