/* SERVER SIDE:
  - Accept multiple clients at the same time (one thread per client).
  - Server receives JOIN, EXPR, and CLOSE messages from each client.
  - Send ACK/RESULT/ERROR/BYE responses back to the client.
  - Keeping track of connection time, disconnection time, and duration in seconds.
*/

import java.util.*;
import java.io.*;
import java.time.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.format.DateTimeFormatter;

class TCPServer {
  // Handle one client session from JOIN until CLOSE or disconnect.
  private static void handleClient(Socket clientSocket) {
    String clientEndpoint = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
    Instant connectionAcceptedAt = Instant.now();
    System.out.println("New connection from " + clientEndpoint + " at " + formatTimestamp(connectionAcceptedAt));

    String clientName = null;
    Instant attachedAt = null;
    String disconnectReason = "connection closed";

    try (Socket activeSocket = clientSocket;
         BufferedReader clientInputReader = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
         DataOutputStream clientOutputStream = new DataOutputStream(activeSocket.getOutputStream())) {

      String joinRequestLine = clientInputReader.readLine();
      if (joinRequestLine == null) {
        disconnectReason = "closed before JOIN";
        return;
      }

      if (joinRequestLine.startsWith("JOIN: ")) {
        clientName = joinRequestLine.substring(6).trim();
        if (!clientName.isEmpty()) {
          attachedAt = Instant.now();
          System.out.println("Client connected: " + clientName + " (" + clientEndpoint + ") at " + formatTimestamp(attachedAt));
          clientOutputStream.writeBytes("ACK: " + clientName + '\n');
          clientOutputStream.flush();
        } else {
          clientOutputStream.writeBytes("ERROR: Name cannot be empty\n");
          clientOutputStream.flush();
          disconnectReason = "empty JOIN name";
          return;
        }
      } else {
        clientOutputStream.writeBytes("ERROR: Expected JOIN: <name>\n");
        clientOutputStream.flush();
        disconnectReason = "invalid JOIN message";
        return;
      }

      String requestLine;
      while ((requestLine = clientInputReader.readLine()) != null) {
        String normalizedRequest = requestLine.trim();

        if (normalizedRequest.equalsIgnoreCase("CLOSE")) {
          clientOutputStream.writeBytes("BYE\n");
          clientOutputStream.flush();
          disconnectReason = "client requested CLOSE";
          break;
        }

        if (normalizedRequest.startsWith("EXPR: ")) {
          String expressionText = normalizedRequest.substring(6).trim();
          if (expressionText.isEmpty()) {
            clientOutputStream.writeBytes("ERROR: Empty expression\n");
            clientOutputStream.flush();
            continue;
          }

          try {
            double evaluatedResult = evaluateExpression(expressionText);
            String resultText = Double.toString(evaluatedResult);
            System.out.println(clientName + " requested: " + expressionText + " => " + resultText);
            clientOutputStream.writeBytes("RESULT = " + resultText + '\n');
            clientOutputStream.flush();
          } catch (IllegalArgumentException parseException) {
            clientOutputStream.writeBytes("ERROR: " + parseException.getMessage() + '\n');
            clientOutputStream.flush();
          }
          continue;
        }

        clientOutputStream.writeBytes("ERROR: Expected EXPR: <expression> or CLOSE\n");
        clientOutputStream.flush();
      }

      if (requestLine == null) {
        disconnectReason = "client stream closed";
      }
    } catch (IOException ioException) {
      disconnectReason = "I/O error: " + ioException.getMessage();
      System.out.println("Connection error with " + clientEndpoint + ": " + ioException.getMessage());
    } finally {
      Instant disconnectedAt = Instant.now();
      if (attachedAt != null && clientName != null) {
        Duration attachedDuration = Duration.between(attachedAt, disconnectedAt);
        System.out.println(
            "Client connected: " + clientName + " (" + clientEndpoint + ") | connected at "
                + formatTimestamp(attachedAt) + " | disconnected at "
                + formatTimestamp(disconnectedAt) + " | duration: "
                + formatDuration(attachedDuration) + " | reason: " + disconnectReason);
      } else {
        System.out.println("Connection closed without successful attachment from " + clientEndpoint + " at "
                            + formatTimestamp(disconnectedAt) + " | reason: " + disconnectReason);
      }
    }
  }

  // Parse and evaluate one expression string using the parser helper.
  private static double evaluateExpression(String expressionText) {
    try {
      return Parser.parse(expressionText);
    } catch (IllegalArgumentException parseException) {
      if ("DIVIDE_BY_ZERO".equals(parseException.getMessage())) {
        throw parseException;
      }
      throw new IllegalArgumentException("Invalid expression");
    }
  }

  // Format an Instant into a readable local date-time string.
  private static String formatTimestamp(Instant timestamp) {
    DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.US).withZone(ZoneId.systemDefault());
    return timestampFormatter.format(timestamp);
  }

  // Convert attached duration into total seconds text.
  private static String formatDuration(Duration attachedDuration) {
    long totalSeconds = Math.max(0, attachedDuration.getSeconds());
    return totalSeconds + "s";
  }

  // Start the server socket and dispatch each new client to a thread.
  public static void main(String argv[]) throws Exception {
    // Server is listening on port 6789
    ServerSocket listeningSocket = new ServerSocket(6789);
    System.out.println("Server is UP and running on port 6789");

    while (true) {
      Socket acceptedClientSocket = listeningSocket.accept();
      // Each client connected will create a new thread to handle multiple clients.
      Thread clientHandlerThread = new Thread(() -> handleClient(acceptedClientSocket));
      clientHandlerThread.start();
    }
  }
}
