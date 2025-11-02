package org.example;

import org.example.server.RestServer;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class  Main {
    public static void main(String[] args) {
        try {
            // Database configuration
            String dbUrl = "jdbc:postgresql://localhost:5432/mrp_db";
            String dbUser = "postgres";
            String dbPassword = "postgres";

            // Create and start server
            RestServer server = new RestServer(8080, dbUrl, dbUser, dbPassword);
            server.start();

            System.out.println("Media Ratings Platform Server is running on http://localhost:8080");
            System.out.println("Press Ctrl+C to stop the server");

            // Keep the application running
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}