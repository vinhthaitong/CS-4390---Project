import java.io.*;
import java.net.*;
import java.util.Random;

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

    String[] sampleExpressions = {
        "2 + 3 * 4",
        "(10 - 2) / 4",
        "8 / 2 + 7",
        "9 - 3 + 1",
        "(3 + 5) * (2 - 1)",
        "18 / (3 * 2)",
        "4 * (2 + 6) / 3",
        "100 / (5 + 5)",
        "(7 - 9) * 3",
        "(2.5 + 1.5) * 2",
        "(8 + 2) / (3 - 1)",
        "42 / 7 + 1",
        "15 - (4 + 6) / 2",
        "6 * 6 - 5"
    };
    Random random = new Random();
    int requestCount = 3;

    for (int i = 1; i <= requestCount; i++) {
      String expression = sampleExpressions[random.nextInt(sampleExpressions.length)];
      int delayMillis = 1000 + random.nextInt(2001);

      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        System.out.println("Client interrupted while waiting to send request.");
        break;
      }

      System.out.println("Sending request " + i + ": EXPR " + expression);
      outToServer.writeBytes("EXPR " + expression + '\n');
      outToServer.flush();

      String mathResponse = inFromServer.readLine();
      System.out.println("FROM SERVER: " + mathResponse);
    }

    outToServer.writeBytes("CLOSE\n");
    outToServer.flush();
    String byeResponse = inFromServer.readLine();
    System.out.println("FROM SERVER: " + byeResponse);

    clientSocket.close();
  }
}
