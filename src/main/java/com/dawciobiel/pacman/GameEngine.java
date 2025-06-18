package com.dawciobiel.pacman;


import com.dawciobiel.pacman.entities.Food;
import com.dawciobiel.pacman.entities.Ghost;
import com.dawciobiel.pacman.entities.Player;
import com.dawciobiel.pacman.entities.PowerPellet;
import com.dawciobiel.pacman.game.CollisionDetector;
import com.dawciobiel.pacman.game.GameState;
import com.dawciobiel.pacman.input.InputHandler;
import com.dawciobiel.pacman.level.MazeGenerator;
import com.dawciobiel.pacman.sound.SoundManager;
import com.dawciobiel.pacman.utils.GameConstants;
import com.dawciobiel.pacman.utils.GhostMode;
import com.dawciobiel.pacman.utils.Point;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.dawciobiel.pacman.game.GameState.addScore;
import static com.dawciobiel.pacman.game.GameState.lives;
import static com.dawciobiel.pacman.game.GameState.score;
import static com.dawciobiel.pacman.utils.GameConstants.BOARD_HEIGHT;
import static com.dawciobiel.pacman.utils.GameConstants.BOARD_WIDTH;
import static com.dawciobiel.pacman.utils.GameConstants.GAME_OVER_FONT;
import static com.dawciobiel.pacman.utils.GameConstants.GHOST_POINTS;
import static com.dawciobiel.pacman.utils.GameConstants.POWER_PELLET_DURATION;
import static com.dawciobiel.pacman.utils.GameConstants.TILE_SIZE;
import static com.dawciobiel.pacman.utils.GameConstants.UI_FONT;
import static com.dawciobiel.pacman.utils.GameConstants.UPDATE_INTERVAL;


public class GameEngine extends Application {

    // Bufory do wielokrotnego użytku
    public static SoundManager soundManager;
    public static Player pacman;
    public static List<Ghost> ghosts;
    public static List<Food> foods;
    public static List<PowerPellet> powerPellets;
    public static long powerPelletStartTime = 0;
    public static boolean isPowerPelletActive = false;
    public static int ghostMultiplier = 1; // Mnożnik punktów za duchy. Zmienia po aktywacji power-up-ów
    private static AnimationTimer gameLoop;
    private static long lastUpdate = 0;
    private static boolean[][] walls;

    private GraphicsContext gc;

    public static void main(final String[] args) {
        launch(args);
    }

    /**
     * Rysuje ducha w stylu klasycznego Pac‑Mana.
     *
     * @param gc    aktualny GraphicsContext canvas-a
     * @param x     lewy górny róg obszaru rysowania
     * @param y     lewy górny róg obszaru rysowania
     * @param size  szerokość i wysokość ducha (piksele)
     * @param body  kolor ciała ducha
     * @param pupil kolor źrenic (w zależności od kierunku patrzenia możesz je przesuwać)
     */
    public static void drawGhost(final GraphicsContext gc, final double x, final double y, final double size, final Color body, final Color pupil) {

        double r = size / 2;        // promień górnej półkuli
        int waves = 4;              // ile „ząbków” na dole
        double waveW = size / waves;

        /* ---------- CIAŁO ---------- */
        gc.setFill(body);

        // 1) górna półkula
        gc.fillArc(x, y, size, size, 0, 180, ArcType.ROUND);

        // 2) prostokątny tułów
        gc.fillRect(x, y + r, size, r);

        // 3) falbanka na dole (cztery półkola)
        for (int i = 0; i < waves; i++) {
            gc.fillArc(x + i * waveW, y + size - waveW / 2, waveW,          // szerokość
                    waveW,          // wysokość (półkole)
                    0,              // 'startAngle'` (prawo)
                    180,            // długość łuku
                    ArcType.ROUND);
        }

        /* ---------- OCZY ---------- */
        double eyeR = size * 0.18;  // promień białej części oka
        double pupilR = eyeR * 0.45;  // promień źrenicy
        double eyeY = y + size * 0.35;
        double leftEyeX = x + size * 0.28;
        double rightEyeX = x + size * 0.72;

        // białka
        gc.setFill(Color.WHITE);
        gc.fillOval(leftEyeX - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2);
        gc.fillOval(rightEyeX - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2);

        // źrenice – minimalnie przesunięte w dół, aby „patrzyły”
        gc.setFill(pupil);
        gc.fillOval(leftEyeX - pupilR, eyeY - pupilR + eyeR * 0.3, pupilR * 2, pupilR * 2);
        gc.fillOval(rightEyeX - pupilR, eyeY - pupilR + eyeR * 0.3, pupilR * 2, pupilR * 2);
    }

    private static void initializeGame() {
        if (ghosts != null) {
            ghosts.clear();
        } else {
            ghosts = new ArrayList<>(4);
        }

        if (foods != null) {
            foods.clear();
        } else {
            foods = new ArrayList<>(200);
        }

        if (powerPellets != null) {
            powerPellets.clear();
        } else {
            powerPellets = new ArrayList<>(4);
        }

        if (walls == null) {
            walls = new boolean[GameConstants.BOARD_COLS][GameConstants.BOARD_ROWS];
        }
        createMaze();

        // Create Pacman
        pacman = new Player(GameConstants.PLAYER_START_X, GameConstants.PLAYER_START_Y);

        // Create ghosts
        ghosts.add(new Ghost(GameConstants.BOARD_COLS - 2, 1, Color.RED));
        ghosts.add(new Ghost(GameConstants.BOARD_COLS - 2, GameConstants.BOARD_ROWS - 2, Color.PINK));
        ghosts.add(new Ghost(1, GameConstants.BOARD_ROWS - 2, Color.CYAN));

        createFood();
        createPowerPellets();

        // Reset power pellet state
        isPowerPelletActive = false;
        powerPelletStartTime = 0;
        ghostMultiplier = 1;
    }

    private static void createMaze() {
        // Wyczyść tablicę, zamiast tworzyć nową
        for (int x = 0; x < GameConstants.BOARD_COLS; x++) {
            for (int y = 0; y < GameConstants.BOARD_ROWS; y++) {
                walls[x][y] = true;
            }
        }

        MazeGenerator generator = new MazeGenerator(walls, GameConstants.BOARD_COLS, GameConstants.BOARD_ROWS, GameConstants.SHARED_RANDOM);
        generator.generateMaze(GameConstants.PLAYER_START_X, GameConstants.PLAYER_START_Y);

        int middleY = GameConstants.BOARD_ROWS / 2;
        walls[0][middleY] = false;
        walls[GameConstants.BOARD_COLS - 1][middleY] = false;

        ensureTunnelAccess(middleY);

        clearArea(1, 1, 2);
        clearArea(GameConstants.BOARD_COLS - 2, 1, 1);
        clearArea(GameConstants.BOARD_COLS - 2, GameConstants.BOARD_ROWS - 2, 1);
        clearArea(1, GameConstants.BOARD_ROWS - 2, 1);

        createOpenAreasAsCross();
    }

    private static void ensureTunnelAccess(final int tunnelRow) {
        walls[1][tunnelRow] = false;
        walls[GameConstants.BOARD_COLS - 2][tunnelRow] = false;
    }

    private static void clearArea(final int centerX, final int centerY, final int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (x > 0 && x < GameConstants.BOARD_COLS - 1 && y > 0 && y < GameConstants.BOARD_ROWS - 1) {
                    walls[x][y] = false;
                }
            }
        }
    }

    private static void createOpenAreasAsCross() {
        int numAreas = 3 + GameConstants.SHARED_RANDOM.nextInt(3);

        for (int i = 0; i < numAreas; i++) {
            int centerX = 3 + GameConstants.SHARED_RANDOM.nextInt(GameConstants.BOARD_COLS - 6);
            int centerY = 3 + GameConstants.SHARED_RANDOM.nextInt(GameConstants.BOARD_ROWS - 6);

            walls[centerX][centerY] = false;
            walls[centerX - 1][centerY] = false;
            walls[centerX + 1][centerY] = false;
            walls[centerX][centerY - 1] = false;
            walls[centerX][centerY + 1] = false;
        }
    }

    /**
     * Sprawdza, czy na danej pozycji można umieścić jedzenie.
     *
     * @param x współrzędna X na planszy
     * @param y współrzędna Y na planszy
     * @return true jeśli można umieścić jedzenie, false w przeciwnym razie
     */
    private static boolean canPlaceFood(int x, int y) {
        return !walls[x][y] && !(x == GameConstants.PLAYER_START_X && y == GameConstants.PLAYER_START_Y);
    }

    /**
     * Tworzy jedzenie na planszy w dozwolonych pozycjach.
     * Jedzenie jest umieszczane we wszystkich pustych przestrzeniach,
     * z wyjątkiem ścian i pozycji startowej gracza (1,1).
     */
    private static void createFood() {
        for (int x = 1; x < GameConstants.BOARD_COLS - 1; x++) {
            for (int y = 1; y < GameConstants.BOARD_ROWS - 1; y++) {
                if (canPlaceFood(x, y)) {
                    foods.add(new Food(x, y));
                }
            }
        }
    }

    private static void createPowerPellets() {
        List<Point> corners = getCornerPositions();
        for (Point corner : corners) {
            placePowerPelletNearCorner(corner);
        }
    }

    private static List<Point> getCornerPositions() {
        List<Point> corners = new ArrayList<>();
        corners.add(new Point(3, 3));
        corners.add(new Point(GameConstants.BOARD_COLS - 4, 3));
        corners.add(new Point(3, GameConstants.BOARD_ROWS - 4));
        corners.add(new Point(GameConstants.BOARD_COLS - 4, GameConstants.BOARD_ROWS - 4));
        return corners;
    }

    private static void placePowerPelletNearCorner(Point corner) {
        final int MAX_SEARCH_RADIUS = 5;

        for (int radius = 0; radius < MAX_SEARCH_RADIUS; radius++) {
            Point validPosition = findValidPositionInRadius(corner, radius);
            if (validPosition != null) {
                addPowerPelletAt(validPosition);
                break;
            }
        }
    }

    private static Point findValidPositionInRadius(Point center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                Point checkPosition = new Point(center.x() + dx, center.y() + dy);
                if (isValidPowerPelletPosition(checkPosition)) {
                    return checkPosition;
                }
            }
        }
        return null;
    }

    private static boolean isValidPowerPelletPosition(Point position) {
        return position.x() > 0 && position.y() < GameConstants.BOARD_COLS - 1 && position.y() > 0 && position.y() < GameConstants.BOARD_ROWS - 1 && !walls[position.x()][position.y()];
    }

    private static void addPowerPelletAt(Point position) {
        powerPellets.add(new PowerPellet(position.x(), position.y()));
        foods.removeIf(food -> food.x() == position.x() && food.y() == position.y());
    }

    /**
     * Pacman eats the ghost.
     *
     * @param ghost The ghost that was eaten
     */
    public static void eatGhost(Ghost ghost) {
        addScore(GHOST_POINTS * ghostMultiplier);

        ghostMultiplier *= 2;
        soundManager.playSound("ghost_eaten");
        ghost.setModeDead();
    }

    @Override
    public void start(final Stage primaryStage) {
        Canvas canvas;
        BorderPane root = new BorderPane();
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        root.setCenter(canvas);

        Scene scene = new Scene(root, BOARD_WIDTH, BOARD_HEIGHT);
        scene.setOnKeyPressed(e -> InputHandler.handleKeyPress(e.getCode(), pacman::queueDirection, this::restartGame, soundManager));


        primaryStage.setTitle("Pacman Game - by dawciobiel");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        canvas.requestFocus();

        initializeGame();
        soundManager = SoundManager.getInstance();
        startGameLoop();
    }

    private void restartGame() {
        GameState.gameOver = false;
        GameState.gameWon = false;
        initializeGame();
        if (gameLoop != null) {
            gameLoop.stop();
        }
        startGameLoop();
    }

    private void startGameLoop() {
        score = GameConstants.INITIAL_SCORE;
        lives = GameConstants.INITIAL_LIVES;

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= UPDATE_INTERVAL) { // Reduction FPS
                    update(now);
                    render();
                    lastUpdate = now;
                    // System.out.println("[debug] FPS: " + now); // todo Remove it
                }
            }
        };
        gameLoop.start();
    }

    private void update(final long currentTime) {
        if (GameState.gameOver || GameState.gameWon) return;

        // Sprawdź, czy power pellet wygasł
        if (isPowerPelletActive && currentTime - powerPelletStartTime > POWER_PELLET_DURATION * 1_000_000L) {
            isPowerPelletActive = false;
            ghostMultiplier = 1;

            // Przywróć normalny tryb duchów
            for (Ghost ghost : ghosts) {
                ghost.setMode(GhostMode.NORMAL);
            }
        }

        pacman.update(walls);

        for (Ghost ghost : ghosts) {
            ghost.update(walls, pacman.getTileX(), pacman.getTileY(), isPowerPelletActive, pacman.getDirection());
        }

        // As an optimization, check the collisions with food only when Pacman is in the center of the field
        CollisionDetector.checkCollisionWithFood(this, currentTime);

        // Check collisions with ghosts
        if (CollisionDetector.checkCollisionsWithGhosts(this, pacman, ghosts)) {
            return;
        }

        // Check winning conditions
        checkWinningConditions();
    }

    public void changeGhostsMode(GhostMode ghostMode) {
        for (Ghost ghost : ghosts) {
            ghost.setMode(ghostMode);
        }
    }

    public void eatFood(Food food, Iterator<Food> foodIterator) {
        if (food.x() == pacman.getTileX() && food.y() == pacman.getTileY()) {
            addScore(GameConstants.FOOD_POINTS);
            foodIterator.remove();
            soundManager.playSound("chomp");
        }
    }

    public void resetPacmanPosition() {
        pacman.reset(1, 1);
        isPowerPelletActive = false;
        ghostMultiplier = 1;
    }

    public void executePacmanIsDeadAndNoMoreLives(boolean shouldContinueTheGame, String soundForEndOfTheGameType) {
        GameState.gameWon = shouldContinueTheGame; // If level complete
        soundManager.playSound(soundForEndOfTheGameType);
        if (GameState.gameOver) {
            gameLoop.stop();
        }
    }

    public void executePacmanIsDead() {
        GameState.loseLife();
        soundManager.playSound("death");
    }

    private void checkWinningConditions() {
        if (foods.isEmpty() && powerPellets.isEmpty()) {
            executePacmanIsDeadAndNoMoreLives(true, "level_complete");
        }
    }

    private void render() {
        clearMap();
        drawWalls();
        drawFoods();
        drawPowerPellets();
        pacman.draw(gc);
        drawGhosts();
        drawHUD();

        if (isPowerPelletActive) {
            gc.fillText("POWER MODE!", 620, 25);
        }

        if (GameState.gameOver) {
            drawEndScreen(gc, "GAME OVER!", Color.RED, GAME_OVER_FONT);
        } else if (GameState.gameWon) {
            drawEndScreen(gc, "You won!", Color.YELLOW, GAME_OVER_FONT);
        }
    }

    private void drawHUD() {
        gc.setFill(Color.WHITE);
        gc.setFont(UI_FONT);
        gc.fillText("Score: " + score, 100, 25);
        gc.fillText("Lives: " + lives, 350, 25);
    }

    private void drawGhosts() {
        for (Ghost ghost : ghosts) {
            Color ghostColor;
            if (ghost.getMode() == GhostMode.FRIGHTENED) {
                ghostColor = Color.BLUE;
            } else if (ghost.getMode() == GhostMode.DEAD) {
                ghostColor = Color.GRAY; // Inny kolor dla martwych duchów
            } else {
                ghostColor = ghost.getColor();
            }

            gc.setFill(ghostColor);
            double ghostX = ghost.getDrawX() - TILE_SIZE * 0.5 + 2;
            double ghostY = ghost.getDrawY() - TILE_SIZE * 0.5 + 2;
            double ghostSize = (double) TILE_SIZE - 4;
            gc.fillRect(ghostX, ghostY, ghostSize, ghostSize);


//            Color ghostCol  = ghost.getColor();    // np. Color.CYAN
//            ThreadLocalRandom rand = ThreadLocalRandom.current();
////            Color pupilCol  = Color.color(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());          // tak jak w oryginale
//            Color  pupilCol  = Color.DARKRED;
//            drawGhost(gc, ghostX, ghostY, ghostSize, ghost.getColor(), pupilCol);

            //fixme TU SKOŃCZYŁEM
            //todo Martwe duchy (zjedzony) nie są rysowane w kolorze szarym

        }
    }

    private void drawPowerPellets() {
        gc.setFill(Color.TOMATO);
        for (PowerPellet powerPellet : powerPellets) {
            double pelletX = powerPellet.x() * TILE_SIZE + TILE_SIZE * 0.15;
            double pelletY = powerPellet.y() * TILE_SIZE + TILE_SIZE * 0.15;
            double pelletSize = TILE_SIZE * 0.7;
            gc.fillOval(pelletX, pelletY, pelletSize, pelletSize);
        }
    }

    private void drawFoods() {
        gc.setFill(Color.YELLOW);
        for (Food food : foods) {
            double foodX = food.x() * TILE_SIZE + TILE_SIZE * 0.25;
            double foodY = food.y() * TILE_SIZE + TILE_SIZE * 0.25;
            double foodSize = TILE_SIZE * 0.3;
            gc.fillOval(foodX, foodY, foodSize, foodSize);
        }
    }

    private void drawWalls() {
        gc.setFill(Color.BLUE);
        for (int x = 0; x < GameConstants.BOARD_COLS; x++) {
            for (int y = 0; y < GameConstants.BOARD_ROWS; y++) {
                if (walls[x][y]) {
                    double drawX = x * (double) TILE_SIZE;
                    double drawY = y * (double) TILE_SIZE;

                    gc.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void clearMap() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private void drawEndScreen(GraphicsContext gc, String mainText, Color fillColor, Font mainFont) {
        double centerX = BOARD_WIDTH * 0.5;
        double centerY = BOARD_HEIGHT * 0.5;

        gc.setFont(mainFont);
        drawOutlinedText(gc, mainText, centerX - 120, centerY, Color.BLACK, fillColor);

        gc.setFont(UI_FONT);
        drawOutlinedText(gc, "Press R to play again", centerX - 100, centerY + 40, Color.BLACK, Color.WHITE);
    }

    private void drawOutlinedText(GraphicsContext gc, String text, double x, double y, Color strokeColor, Color fillColor) {
        gc.setStroke(strokeColor);
        gc.setLineWidth(20);
        gc.strokeText(text, x, y);

        gc.setFill(fillColor);
        gc.fillText(text, x, y);
    }

    @Override
    public void stop() throws Exception {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (soundManager != null) {
            soundManager.cleanup(); // DODANE
        }
        super.stop();
    }
}