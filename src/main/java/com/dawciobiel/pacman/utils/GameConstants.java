package com.dawciobiel.pacman.utils;

import javafx.scene.text.Font;

/**
 * Klasa zawierająca wszystkie stałe używane w grze Pacman.
 * Centralizacja stałych ułatwia zarządzanie i modyfikację parametrów gry.
 */
public final class GameConstants {

    // === WYMIARY PLANSZY ===
    public static final int TILE_SIZE = 50;
    public static final int BOARD_WIDTH = 1200;
    public static final int BOARD_HEIGHT = 800;
    public static final int BOARD_COLS = BOARD_WIDTH / TILE_SIZE;
    public static final int BOARD_ROWS = BOARD_HEIGHT / TILE_SIZE;

    // === RUCH I ANIMACJE ===
    public static final double MOVEMENT_SPEED = 0.1;
    public static final double PLAYER_ANIMATION_SPEED = 0.15;

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
    public static final int GHOST_HOME_X = BOARD_COLS / 2;
    public static final int GHOST_HOME_Y = BOARD_ROWS / 2;

    // === POZYCJE STARTOWE ===
    public static final int PLAYER_START_X = 1;
    public static final int PLAYER_START_Y = 1;

    // === POZYCJE SCATTER MODE DLA DUCHÓW ===
    public static final int[][] SCATTER_TARGETS = {
            {BOARD_COLS - 1, 0},           // czerwony — prawy górny
            {0, 0},                        // różowy — lewy górny
            {BOARD_COLS - 1, BOARD_ROWS - 1}, // cyan — prawy dolny
            {0, BOARD_ROWS - 1}            // pomarańczowy — lewy dolny
    };

    // === GENEROWANIE LABIRYNTU ===
    public static final int[][] MAZE_DIRECTIONS = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
    public static final int TUNNEL_ROW = BOARD_ROWS / 2;
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

    // === POZYCJE POWER PELLETS ===
    public static final int[][] POWER_PELLET_CORNERS = {
            {3, 3},
            {BOARD_COLS - 4, 3},
            {3, BOARD_ROWS - 4},
            {BOARD_COLS - 4, BOARD_ROWS - 4}
    };
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