import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

class TCPServer {
  private static void handleClient(Socket connectionSocket) {
    String remote = connectionSocket.getInetAddress().getHostAddress() + ":" + connectionSocket.getPort();
    Instant connectedAt = Instant.now();
    System.out.println("New connection from " + remote + " at " + formatTimestamp(connectedAt));

    String clientName = null;
    Instant attachedAt = null;
    String disconnectReason = "connection closed";

    try (Socket socket = connectionSocket;
         BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream())) {

      String firstMessage = inFromClient.readLine();
      if (firstMessage == null) {
        disconnectReason = "closed before JOIN";
        return;
      }

      if (firstMessage.startsWith("JOIN ")) {
        clientName = firstMessage.substring(5).trim();
        if (!clientName.isEmpty()) {
          attachedAt = Instant.now();
          System.out.println(
              "Client connected: " + clientName + " (" + remote + ") at " + formatTimestamp(attachedAt));
          outToClient.writeBytes("ACK " + clientName + '\n');
          outToClient.flush();
        } else {
          outToClient.writeBytes("ERROR Name cannot be empty\n");
          outToClient.flush();
          disconnectReason = "empty JOIN name";
          return;
        }
      } else {
        outToClient.writeBytes("ERROR Expected: JOIN <name>\n");
        outToClient.flush();
        disconnectReason = "invalid JOIN message";
        return;
      }

      String message;
      while ((message = inFromClient.readLine()) != null) {
        String trimmed = message.trim();

        if (trimmed.equalsIgnoreCase("CLOSE")) {
          outToClient.writeBytes("BYE\n");
          outToClient.flush();
          disconnectReason = "client requested CLOSE";
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
            System.out.println(
                clientName + " requested: " + expression + " => " + formatResult(result));
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

      if (message == null) {
        disconnectReason = "client stream closed";
      }
    } catch (IOException ex) {
      disconnectReason = "I/O error: " + ex.getMessage();
      System.out.println("Connection error with " + remote + ": " + ex.getMessage());
    } finally {
      Instant detachedAt = Instant.now();
      if (attachedAt != null && clientName != null) {
        Duration sessionDuration = Duration.between(attachedAt, detachedAt);
        System.out.println(
            "Client connected: " + clientName + " (" + remote + ") | connected at "
                + formatTimestamp(attachedAt) + " | disconnected at " + formatTimestamp(detachedAt)
                + " | duration " + formatDuration(sessionDuration) + " | reason: "
                + disconnectReason);
      } else {
        System.out.println(
            "Connection closed without successful attachment from " + remote + " at "
                + formatTimestamp(detachedAt) + " | reason: " + disconnectReason);
      }
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
      return Parser.parse(expression);
    } catch (IllegalArgumentException ex) {
      if ("DIVIDE_BY_ZERO".equals(ex.getMessage())) {
        throw ex;
      }
      throw new IllegalArgumentException("Invalid expression");
    }
  }

  private static String formatTimestamp(Instant instant) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.US)
        .withZone(ZoneId.systemDefault());
    return formatter.format(instant);
  }

  private static String formatDuration(Duration duration) {
    long totalSeconds = Math.max(0, duration.getSeconds());
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
  }

  public static void main(String argv[]) throws Exception {
    ServerSocket welcomeSocket = new ServerSocket(6789);
    System.out.println("Server is UP and running on port 6789");

    while (true) {
      Socket connectionSocket = welcomeSocket.accept();
      Thread clientThread = new Thread(() -> handleClient(connectionSocket));
      clientThread.start();
    }
  }
}
