package org.example.server;

import com.sun.net.httpserver.HttpServer;
import org.example.controller.MediaController;
import org.example.controller.UserController;
import org.example.repository.MediaRepository;
import org.example.repository.UserRepository;
import org.example.service.MediaService;
import org.example.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RestServer {
    private final HttpServer server;
    private final Connection connection;

    public RestServer(int port, String dbUrl, String dbUser, String dbPassword) throws IOException, SQLException {
        // Initialize database connection
        this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

        UserRepository userRepository = new UserRepository(connection);
        MediaRepository mediaRepository = new MediaRepository(connection);

        userRepository.createTable();
        mediaRepository.createTable();

        UserService userService = new UserService(userRepository);
        MediaService mediaService = new MediaService(mediaRepository);

        UserController userController = new UserController(userService);
        MediaController mediaController = new MediaController(mediaService, userService);

        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Setup contexts (endpoints)
        server.createContext("/api/users/register", userController::handleRegister);
        server.createContext("/api/users/login", userController::handleLogin);
        server.createContext("/api/users", userController::handleGetUser);
        server.createContext("/api/media", mediaController::handleMedia);

        server.setExecutor(null); //default executor
    }

    public void start() {
        server.start();
        System.out.println("Server started on port " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

