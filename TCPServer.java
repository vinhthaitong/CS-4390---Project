import java.io.*;
import java.net.*;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

class TCPServer {

  public static void main(String argv[]) throws Exception {
    ServerSocket welcomeSocket = new ServerSocket(6789);
    System.out.println("Server is UP and running on port 6789");

    while (true) {
      Socket connectionSocket = welcomeSocket.accept();
      String remote =
          connectionSocket.getInetAddress().getHostAddress() + ":" + connectionSocket.getPort();
      System.out.println("New connection from " + remote);

      BufferedReader inFromClient = new BufferedReader(
          new InputStreamReader(connectionSocket.getInputStream()));
      DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

      String firstMessage = inFromClient.readLine();
      if (firstMessage == null) {
        System.out.println("Connection closed before JOIN from " + remote);
        connectionSocket.close();
        continue;
      }

      String clientName = null;
      if (firstMessage.startsWith("JOIN ")) {
        clientName = firstMessage.substring(5).trim();
        if (!clientName.isEmpty()) {
          System.out.println("Client joined: " + clientName + " (" + remote + ")");
          outToClient.writeBytes("ACK " + clientName + '\n');
          outToClient.flush();
        } else {
          outToClient.writeBytes("ERROR Name cannot be empty\n");
          outToClient.flush();
          connectionSocket.close();
          continue;
        }
      } else {
        outToClient.writeBytes("ERROR Expected: JOIN <name>\n");
        outToClient.flush();
        connectionSocket.close();
        continue;
      }

      String message;
      while ((message = inFromClient.readLine()) != null) {
        String trimmed = message.trim();

        if (trimmed.equalsIgnoreCase("CLOSE")) {
          outToClient.writeBytes("BYE\n");
          outToClient.flush();
          System.out.println("Client disconnected: " + clientName + " (" + remote + ")");
          break;
        }

        if (trimmed.startsWith("EXPR ")) {
          String expression = trimmed.substring(5).trim();
          if (expression.isEmpty()) {
            outToClient.writeBytes("ERROR Empty expression\n");
            outToClient.flush();
            continue;
          }

          try {
            double result = evaluateExpression(expression);
            System.out.println(clientName + " requested: " + expression + " => " + formatResult(result));
            outToClient.writeBytes("RESULT " + formatResult(result) + '\n');
            outToClient.flush();
          } catch (IllegalArgumentException ex) {
            outToClient.writeBytes("ERROR " + ex.getMessage() + '\n');
            outToClient.flush();
          }
          continue;
        }

        outToClient.writeBytes("ERROR Expected: EXPR <expression> or CLOSE\n");
        outToClient.flush();
      }

      connectionSocket.close();
    }
  }

  private static String formatResult(double value) {
    if (value == (long) value) {
      return Long.toString((long) value);
    }
    return Double.toString(value);
  }

  private static double evaluateExpression(String expression) {
    try {
      Expression parsedExpression = new ExpressionBuilder(expression).build();
      double result = parsedExpression.evaluate();

      if (Double.isNaN(result) || Double.isInfinite(result)) {
        throw new IllegalArgumentException("DIVIDE_BY_ZERO");
      }

      return result;
    } catch (IllegalArgumentException ex) {
      if ("DIVIDE_BY_ZERO".equals(ex.getMessage())) {
        throw ex;
      }
      throw new IllegalArgumentException("Invalid expression");
    }
  }
}
