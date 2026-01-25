package org.example.server;

import com.sun.net.httpserver.HttpServer;
import org.example.controller.MediaController;
import org.example.controller.RatingController;
import org.example.controller.UserController;
import org.example.controller.FavoriteController;
import org.example.controller.RecommendationController;
import org.example.controller.LeaderboardController;
import org.example.repository.MediaRepository;
import org.example.repository.UserRepository;
import org.example.repository.RatingRepository;
import org.example.repository.FavoriteRepository;
import org.example.service.MediaService;
import org.example.service.UserService;
import org.example.service.RatingService;
import org.example.service.RecommendationService;
import org.example.service.LeaderboardService;
import org.example.service.FavoriteService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RestServer {
    private final HttpServer server;

    public RestServer(int port) throws IOException {
        // Create repositories
        UserRepository userRepository = new UserRepository();
        MediaRepository mediaRepository = new MediaRepository();
        RatingRepository ratingRepository = new RatingRepository();
        FavoriteRepository favoriteRepository = new FavoriteRepository();

        // Create all tables - order is important due to foreign keys
        userRepository.createTable();
        mediaRepository.createTable();
        ratingRepository.createTable();
        favoriteRepository.createTable();

        // Create services
        UserService userService = new UserService(userRepository);
        MediaService mediaService = new MediaService(mediaRepository);
        RatingService ratingService = new RatingService(ratingRepository, mediaRepository);
        RecommendationService recommendationService = new RecommendationService(userRepository);
        LeaderboardService leaderboardService = new LeaderboardService(userRepository);
        FavoriteService favoriteService = new FavoriteService(favoriteRepository, mediaRepository);

        // Create controllers
        UserController userController = new UserController(userService);
        MediaController mediaController = new MediaController(mediaService, userService);
        RatingController ratingController = new RatingController(ratingService, userService);
        FavoriteController favoriteController = new FavoriteController(favoriteService, userService);
        RecommendationController recommendationController = new RecommendationController(recommendationService, userService);
        LeaderboardController leaderboardController = new LeaderboardController(leaderboardService, userService);

        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Setup contexts (endpoints) - Order important: more specific paths first
        server.createContext("/api/users/register", userController::handleRegister);
        server.createContext("/api/users/login", userController::handleLogin);
        server.createContext("/api/recommendations", recommendationController::handle);
        server.createContext("/api/leaderboard", leaderboardController::handle);
        server.createContext("/api/users/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.contains("/favorites")) {
                favoriteController.handle(exchange);
            } else if (path.contains("/rating-history")) {
                ratingController.handleRatingHistory(exchange);
            } else if ("PUT".equals(method)) {
                userController.handleUpdateProfile(exchange);
            } else {
                userController.handleGetUser(exchange);
            }
        });
        server.createContext("/api/media/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.contains("/ratings")) {
                ratingController.handleMediaRatings(exchange);
            } else {
                mediaController.handleMedia(exchange);
            }
        });
        server.createContext("/api/media", mediaController::handleMedia);
        server.createContext("/api/ratings", ratingController::handleRating);

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("Server started on port " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }
}

