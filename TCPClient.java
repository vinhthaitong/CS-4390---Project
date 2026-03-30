import java.io.*;
import java.net.*;

class TCPClient {

  public static void main(String argv[]) throws Exception {
    System.out.println("Client is running");

    Socket clientSocket = new Socket("127.0.0.1", 6789);
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    BufferedReader inFromServer = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

    System.out.print("Enter your name: ");
    String clientName = inFromUser.readLine();
    if (clientName == null || clientName.trim().isEmpty()) {
      System.out.println("Name cannot be empty.");
      clientSocket.close();
      return;
    }

    outToServer.writeBytes("JOIN " + clientName.trim() + '\n');
    outToServer.flush();
    String serverResponse = inFromServer.readLine();
    System.out.println("FROM SERVER: " + serverResponse);

    if (serverResponse == null || !serverResponse.startsWith("ACK ")) {
      System.out.println("Handshake failed. Closing client.");
      clientSocket.close();
      return;
    }

    while (true) {
      System.out.print("Enter expression (e.g., 2 + 3 * 4) or CLOSE: ");
      String userInput = inFromUser.readLine();
      if (userInput == null) {
        break;
      }

      String trimmed = userInput.trim();
      if (trimmed.isEmpty()) {
        continue;
      }

      if (trimmed.equalsIgnoreCase("CLOSE")) {
        outToServer.writeBytes("CLOSE\n");
        outToServer.flush();
        String byeResponse = inFromServer.readLine();
        System.out.println("FROM SERVER: " + byeResponse);
        break;
      }

      outToServer.writeBytes("EXPR " + trimmed + '\n');
      outToServer.flush();
      String mathResponse = inFromServer.readLine();
      System.out.println("FROM SERVER: " + mathResponse);
    }

    clientSocket.close();
  }
}
