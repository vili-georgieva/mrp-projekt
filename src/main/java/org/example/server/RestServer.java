package org.example.server;

import com.sun.net.httpserver.HttpServer;
import org.example.controller.MediaController;
import org.example.controller.RatingController;
import org.example.controller.UserController;
import org.example.controller.FavoriteController;
import org.example.repository.MediaRepository;
import org.example.repository.UserRepository;
import org.example.service.MediaService;
import org.example.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RestServer {
    private final HttpServer server;

    public RestServer(int port) throws IOException {
        UserRepository userRepository = new UserRepository();
        MediaRepository mediaRepository = new MediaRepository();

        userRepository.createTable();
        mediaRepository.createTable();

        UserService userService = new UserService(userRepository);
        MediaService mediaService = new MediaService(mediaRepository);

        UserController userController = new UserController(userService);
        MediaController mediaController = new MediaController(mediaService, userService);
        RatingController ratingController = new RatingController();
        FavoriteController favoriteController = new FavoriteController();

        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Setup contexts (endpoints) - Order important: more specific paths first
        server.createContext("/api/users/register", userController::handleRegister);
        server.createContext("/api/users/login", userController::handleLogin);
        server.createContext("/api/users/favorites", favoriteController);
        server.createContext("/api/users", userController::handleGetUser);
        server.createContext("/api/media", mediaController::handleMedia);
        server.createContext("/api/ratings", ratingController::handleRequest);

        server.setExecutor(null); // Default executor
    }

    public void start() {
        server.start();
        System.out.println("Server started on port " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }
}

