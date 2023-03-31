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

                while (scanner.hasNext()) {
                    String command = scanner.nextLine();
                    System.out.println("Received command: " + command);

                    if (command.startsWith("ping")) {
                        String pong = "+PONG\r\n";
                        outputStream.write(pong.getBytes());
                    } else if (command.startsWith("echo")) {
                        String[] tokens = command.split("\\s+");
                        if (tokens.length > 1) {
                            String echoResponse = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
                            outputStream.write(("+" + echoResponse + "\r\n").getBytes());
                        } else {
                            outputStream.write(("-ERR wrong number of arguments for 'echo' command\r\n").getBytes());
                        }
                    } else if (command.startsWith("DOCS")) {
                        outputStream.write("+\r\n".getBytes());
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
