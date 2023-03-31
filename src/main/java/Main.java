import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");
        startServer();
    }

    private static void startServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(6379);
            serverSocket.setReuseAddress(true);
            System.out.println("Server started on port 6379");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(() -> {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.equalsIgnoreCase("ping")) {
                                out.println("+PONG\r\n");
                            }
                        }
                        System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        System.err.println("Error handling client request: " + e.getMessage());
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            System.err.println("Error closing client socket: " + e.getMessage());
                        }
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
}
