import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible
        // when running tests.
        System.out.println("Logs from your program will appear here!");
        final int port = 6379;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            while (true) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                executorService.execute(new RedisConnection(clientSocket));
            }
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    public static class RedisConnection extends Thread {
        private Socket clientSocket;

        public RedisConnection(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream());
                OutputStream outputStream = clientSocket.getOutputStream();
                StringBuilder input = new StringBuilder();

                while (scanner.hasNext()) {
                    String next = scanner.nextLine();
                    System.out.println("next = " + next);
                    input.append(next);

                    // Parse the incoming command
                    String[] tokens = next.split("\\r\\n");
                    String command = tokens[0];
                    String argument = tokens.length > 1 ? tokens[1] : null;

                    // Check if the command is an ECHO command
                    if (command.startsWith("*2") && argument != null && argument.startsWith("$3") && "ECHO".equals(tokens[2])) {
                        String echoValue = argument.substring(5);
                        String response = "+" + echoValue + "\r\n";
                        outputStream.write(response.getBytes());
                    }
                    // Check if the command is a PING command
                    else if (command.equalsIgnoreCase("ping")) {
                        String pong = "+PONG\r\n";
                        outputStream.write(pong.getBytes());
                    }
                    // Otherwise, respond with an error
                    else {
                        String error = "-ERR unsupported command\r\n";
                        outputStream.write(error.getBytes());
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } finally {
                try {
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        }
    }


}
