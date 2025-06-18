package com.dawciobiel.pacman.utils;

import javafx.scene.text.Font;

import java.util.Random;

/**
 * Klasa zawierająca wszystkie stałe używane w grze Pacman.
 * Centralizacja stałych ułatwia zarządzanie i modyfikację parametrów gry.
 */
public final class GameConstants {

    public static final Random SHARED_RANDOM = new Random();

    // === WYMIARY PLANSZY ===
    public static final int TILE_SIZE = 50;
    public static final int BOARD_WIDTH = 1200;
    public static final int BOARD_HEIGHT = 800;
    public static final int BOARD_COLS = BOARD_WIDTH / TILE_SIZE;
    public static final int GHOST_HOME_X = BOARD_COLS / 2;
    public static final int BOARD_ROWS = BOARD_HEIGHT / TILE_SIZE;
    public static final int GHOST_HOME_Y = BOARD_ROWS / 2;
    // === POZYCJE SCATTER MODE DLA DUCHÓW ===
    public static final int[][] SCATTER_TARGETS = {{BOARD_COLS - 1, 0},           // czerwony — prawy górny
            {0, 0},                        // różowy — lewy górny
            {BOARD_COLS - 1, BOARD_ROWS - 1}, // cyan — prawy dolny
            {0, BOARD_ROWS - 1}            // pomarańczowy — lewy dolny
    };
    public static final int TUNNEL_ROW = BOARD_ROWS / 2;
    // === POZYCJE POWER PELLETS ===
    public static final int[][] POWER_PELLET_CORNERS = {{3, 3}, {BOARD_COLS - 4, 3}, {3, BOARD_ROWS - 4}, {BOARD_COLS - 4, BOARD_ROWS - 4}};
    // === RUCH I ANIMACJE ===
    public static final double MOVEMENT_SPEED = 0.2;
    public static final double PLAYER_ANIMATION_SPEED = 0.15;
    /* Time in ms
        Calculation
        1 sec = 1000000000 nanoseconds
        90 FPS = 1 000 000 000 ÷ 90 = 11_111_111 nanoseconds
        60 FPS = 1 000 000 000 / 60 = 16_666_666,6667

    Best choice is usually 60FPS
        30 FPS: 33_333_333 (33.33ms)
        60 FPS: 16_666_667 (16.67ms)
        90 FPS: 11_111_111 (11.11ms)
        120 FPS: 8_333_333 (8.33ms)
        144 FPS: 6_944_444 (6.94ms)
 */
    public static final long UPDATE_INTERVAL = 16_666_667;
    // === PUNKTACJA ===
    public static final int FOOD_POINTS = 10;
    public static final int POWER_PELLET_POINTS = 50;
    public static final int GHOST_POINTS = 200;
    // === POWER PELLET ===
    public static final int POWER_PELLET_DURATION = 10000; // 10 sekund w milisekundach
    public static final long POWER_PELLET_DURATION_NANOS = POWER_PELLET_DURATION * 1_000_000L;
    // === DUCHY ===
    public static final int GHOST_SCATTER_DURATION = 420; // 7 sekund * 60 FPS
    public static final int GHOST_CHASE_DURATION = 1200; // 20 sekund * 60 FPS
    // === POZYCJE STARTOWE ===
    public static final int PLAYER_START_X = 1;
    public static final int PLAYER_START_Y = 1;
    // === GENEROWANIE LABIRYNTU ===
    public static final int[][] MAZE_DIRECTIONS = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
    public static final int MIN_OPEN_AREAS = 3;
    public static final int MAX_ADDITIONAL_AREAS = 3;
    // === CZCIONKI ===
    public static final Font UI_FONT = new Font("Arial", 20);
    public static final Font GAME_OVER_FONT = new Font("Arial", 40);
    // === WYMIARY OBIEKTÓW (jako ułamki TILE_SIZE) ===
    public static final double FOOD_SIZE_RATIO = 0.5;
    public static final double FOOD_OFFSET_RATIO = 0.25;
    public static final double POWER_PELLET_SIZE_RATIO = 0.7;
    public static final double POWER_PELLET_OFFSET_RATIO = 0.15;
    public static final double ENTITY_SIZE_RATIO = 0.8; // dla kolizji
    public static final double DRAW_OFFSET = 2; // offset dla rysowania
    public static final int POWER_PELLET_SEARCH_RADIUS = 5;

    // === GRA ===
    public static final int INITIAL_LIVES = 3;
    public static final int INITIAL_SCORE = 0;
    public static final int INITIAL_GHOST_MULTIPLIER = 1;
    public static final int GHOST_MULTIPLIER_INCREMENT = 2;

    // === STRATEGIA DUCHÓW ===
    public static final int PINK_GHOST_LOOKAHEAD = 4;  // Pinky patrzy 4 pola do przodu
    public static final int CYAN_GHOST_LOOKAHEAD = 2;  // Inky patrzy 2 pola do przodu
    public static final double ORANGE_GHOST_DISTANCE_THRESHOLD = 8.0; // Clyde zmienia strategię

    // === PRAWDOPODOBIEŃSTWA AI ===
    public static final int GHOST_CONTINUE_DIRECTION_PROBABILITY = 7; // z 10
    public static final int GHOST_RANDOM_CHOICE_MAX = 10;

    private GameConstants() {
        throw new UnsupportedOperationException("GameConstants is a utility class and cannot be instantiated");
    }
}