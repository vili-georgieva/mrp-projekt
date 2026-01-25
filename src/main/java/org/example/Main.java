package org.example;

import org.example.server.RestServer;

// Entry Point der Applikation
// Startet den HTTP-Server auf Port 8080
public class Main {
    public static void main(String[] args) {
        try {
            // Create and start server (DB config is loaded from application.properties)
            RestServer server = new RestServer(8080);
            server.start();

            System.out.println("Media Ratings Platform Server is running on http://localhost:8080");
            System.out.println("Press Ctrl+C to stop the server");

            // Keep the application running
            // join() blockiert Haupt-Thread damit Server weiterläuft
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}