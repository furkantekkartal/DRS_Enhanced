// File: ClientHandler.java

package Server;

import java.io.*;
import java.net.*;
import Util.DatabaseConnection;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DatabaseConnection dbManager;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.dbManager = new DatabaseConnection();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String response = processRequest(inputLine);
                out.println(response);
                if (inputLine.equals("EXIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String processRequest(String request) {
        String[] parts = request.split("\\|");
        String command = parts[0];

        switch (command) {
            case "LOGIN":
                return dbManager.validateLogin(parts[1], parts[2], parts[3]);
            case "ADD_REPORT":
                return dbManager.addReport(parts[1], parts[2], parts[3], parts[4], parts[5]);
            case "GET_REPORTS":
                return dbManager.getReports();
            case "UPDATE_REPORT":
                return dbManager.updateReport(parts[1], parts[2], parts[3]);
            case "EXIT":
                return "Goodbye!";
            default:
                return "Unknown command";
        }
    }
}