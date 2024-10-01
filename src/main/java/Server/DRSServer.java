package Server;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.concurrent.*;

public class DRSServer {
    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 10;
   
    private ServerSocket serverSocket;

    public static int getPORT() {
        return PORT;
    }
    private ExecutorService executor;
    private volatile boolean running;
    
    public static void main(String[] args) {
        DRSServer newServer = new DRSServer();
        newServer.start();
    }

    public void start() {
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        running = true;
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("DRS Server is running on port " + PORT);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.execute(new ClientHandler(clientSocket));
                } catch (SocketException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //stop();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executor != null) {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped");
    }

    public boolean isRunning() {
        return running;
    }
}