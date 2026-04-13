/* CLIENT SIDE: 
  - Send the server request, wait until ACK. 
  - User input their name.
  - Send basic math calculation requests (example: 1+2*2/4 *ENTER*).
  - Type "CLOSE" or "close" to close connection. 
*/

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClient {

  // Start the client, join the server, and interactively send expressions.
  public static void main(String argv[]) throws Exception {
    System.out.println("Client is running");

    // Send request to server at IP: 127.0.0.1, PORT: 6789
    Socket serverConnectionSocket = new Socket("127.0.0.1", 6789);
    BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
    BufferedReader serverInputReader = new BufferedReader(new InputStreamReader(serverConnectionSocket.getInputStream()));
    DataOutputStream serverOutputStream = new DataOutputStream(serverConnectionSocket.getOutputStream());

    System.out.print("Enter your name: ");
    String clientDisplayName = userInputReader.readLine();
    if (clientDisplayName == null || clientDisplayName.trim().isEmpty()) {
      System.out.println("Name cannot be empty.");
      serverConnectionSocket.close();
      return;
    }

    serverOutputStream.writeBytes("JOIN: " + clientDisplayName.trim() + '\n');
    serverOutputStream.flush();
    String joinResponseLine = serverInputReader.readLine();
    System.out.println("FROM SERVER: " + joinResponseLine);

    if (joinResponseLine == null || !joinResponseLine.startsWith("ACK: ")) {
      System.out.println("Handshake failed. Closing client.");
      serverConnectionSocket.close();
      return;
    }

    while (true) {
      System.out.print("Enter expression (or CLOSE): ");
      String expressionInputLine = userInputReader.readLine();
      if (expressionInputLine == null) {
        break;
      }

      String normalizedInput = expressionInputLine.trim();
      if (normalizedInput.isEmpty()) {
        continue;
      }

      if (normalizedInput.equalsIgnoreCase("CLOSE")) {
        serverOutputStream.writeBytes("CLOSE\n");
        serverOutputStream.flush();
        String closeAckLine = serverInputReader.readLine();
        System.out.println("FROM SERVER: " + closeAckLine);
        break;
      }

      serverOutputStream.writeBytes("EXPR: " + normalizedInput + '\n');
      serverOutputStream.flush();
      String expressionResultLine = serverInputReader.readLine();
      System.out.println("FROM SERVER: " + expressionResultLine);
    }

    serverConnectionSocket.close();
  }
}
