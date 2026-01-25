package org.example;

import org.example.server.RestServer;

// Entry Point der Applikation
// Startet den HTTP-Server auf Port 8080
public class Main {
    public static void main(String[] args) {
        try {
            // Erstellt und startet Server (DB-Config wird aus application.properties geladen)
            RestServer server = new RestServer(8080);
            server.start();

            System.out.println("Media Ratings Platform Server is running on http://localhost:8080");
            System.out.println("Press Ctrl+C to stop the server");

            // Hält die Applikation am Laufen
            // join() blockiert den Haupt-Thread unbegrenzt
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}