package com.dawciobiel.pacman;


import com.dawciobiel.pacman.sound.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.dawciobiel.pacman.utils.Direction;
import com.dawciobiel.pacman.utils.GhostMode;
import com.dawciobiel.pacman.entities.Food;
import com.dawciobiel.pacman.entities.Player;
import com.dawciobiel.pacman.entities.Ghost;
import com.dawciobiel.pacman.entities.PowerPellet;
import com.dawciobiel.pacman.game.CollisionDetector;


public class PacmanGame extends Application {


    private long lastUpdate = 0;
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
    private static final long UPDATE_INTERVAL = 16_666_667;
    public static final double MOVEMENT_SPEED = 0.2;

    public static final int TILE_SIZE = 50;
    // OPTYMALIZACJA: Re-używalne obiekty
    public static final Random SHARED_RANDOM = new Random();
    private static final int BOARD_WIDTH = 1200;
    public static final int BOARD_COLS = BOARD_WIDTH / TILE_SIZE;
    private static final int BOARD_HEIGHT = 800;
    public static final int BOARD_ROWS = BOARD_HEIGHT / TILE_SIZE;
    // Pozycje rogów dla scatter mode
    public static final int[][] SCATTER_TARGETS = {{BOARD_COLS - 1, 0},        // czerwony — prawy górny
            {0, 0},                     // różowy — lewy górny
            {BOARD_COLS - 1, BOARD_ROWS - 1}, // cyan — prawy dolny
            {0, BOARD_ROWS - 1}         // pomarańczowy — lewy dolny
    };
    private static final Font UI_FONT = new Font("Arial", 20);
    private static final Font GAME_OVER_FONT = new Font("Arial", 40);
    // Power pellet settings
    private static final int POWER_PELLET_DURATION = 10000; // 10 sekund w milisekundach
    private static final int GHOST_POINTS = 200; // Punkty za zjedzenie ducha
    // OPTYMALIZACJA: Object pooling dla kierunków
    private static final int[][] MAZE_DIRECTIONS = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
    // OPTYMALIZACJA: Bufory do wielokrotnego użytku
    private final List<int[]> stackBuffer = new ArrayList<>(100);
    private final List<int[]> neighborsBuffer = new ArrayList<>(4);
    public SoundManager soundManager;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    public Player pacman;
    public List<Ghost> ghosts;
    public List<Food> foods;
    public List<PowerPellet> powerPellets;
    private boolean[][] walls;
    public int score = 0;
    public int lives = 3;
    public boolean gameOver = false;
    private boolean gameWon = false;
    public long powerPelletStartTime = 0;
    public boolean isPowerPelletActive = false;
    public int ghostMultiplier = 2; // Mnożnik punktów za duchy

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
                    0,              // startAngle (prawo)
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

    @Override
    public void start(final Stage primaryStage) {
        BorderPane root = new BorderPane();
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        root.setCenter(canvas);

        Scene scene = new Scene(root, BOARD_WIDTH, BOARD_HEIGHT);
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));

        primaryStage.setTitle("Pacman Game - by dawciobiel");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        canvas.requestFocus();

        initializeGame();
        soundManager = SoundManager.getInstance();
        startGameLoop();
    }

    private void initializeGame() {
        // OPTYMALIZACJA: Wyczyść istniejące listy, zamiast tworzyć nowe
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

        // Inicjalizacja ścian — re-używanie tablicy, jeśli możliwe
        if (walls == null) {
            walls = new boolean[BOARD_COLS][BOARD_ROWS];
        }
        createMaze();

        pacman = new Player(1, 1);

        // Dodaj duchy z predefiniowanymi kolorami
        ghosts.add(new Ghost(BOARD_COLS - 2, 1, Color.RED));
        ghosts.add(new Ghost(BOARD_COLS - 2, BOARD_ROWS - 2, Color.PINK));
        ghosts.add(new Ghost(1, BOARD_ROWS - 2, Color.CYAN));

        createFood();
        createPowerPellets();

        // Reset power pellet state
        isPowerPelletActive = false;
        powerPelletStartTime = 0;
        ghostMultiplier = 1;
    }

    private void createMaze() {
        // OPTYMALIZACJA: Zresetuj tablicę zamiast tworzyć nową
        for (int x = 0; x < BOARD_COLS; x++) {
            for (int y = 0; y < BOARD_ROWS; y++) {
                walls[x][y] = true;
            }
        }

        generateMaze(1, 1);

        int middleY = BOARD_ROWS / 2;
        walls[0][middleY] = false;
        walls[BOARD_COLS - 1][middleY] = false;

        ensureTunnelAccess(middleY);

        clearArea(1, 1, 2);
        clearArea(BOARD_COLS - 2, 1, 1);
        clearArea(BOARD_COLS - 2, BOARD_ROWS - 2, 1);
        clearArea(1, BOARD_ROWS - 2, 1);

        createOpenAreasAsCross();
    }

    private void ensureTunnelAccess(final int tunnelRow) {
        walls[1][tunnelRow] = false;
        walls[BOARD_COLS - 2][tunnelRow] = false;
    }

    private void generateMaze(final int startX, final int startY) {
        // OPTYMALIZACJA: Re-używanie buforów
        stackBuffer.clear();
        boolean[][] visited = new boolean[BOARD_COLS][BOARD_ROWS];

        walls[startX][startY] = false;
        visited[startX][startY] = true;
        stackBuffer.add(new int[]{startX, startY});

        while (!stackBuffer.isEmpty()) {
            int[] current = stackBuffer.get(stackBuffer.size() - 1);
            int currentX = current[0];
            int currentY = current[1];

            // OPTYMALIZACJA: Re-używanie bufora sąsiadów
            neighborsBuffer.clear();

            for (int[] dir : MAZE_DIRECTIONS) {
                int newX = currentX + dir[0];
                int newY = currentY + dir[1];

                if (newX > 0 && newX < BOARD_COLS - 1 && newY > 0 && newY < BOARD_ROWS - 1 && !visited[newX][newY]) {
                    neighborsBuffer.add(new int[]{newX, newY});
                }
            }

            if (!neighborsBuffer.isEmpty()) {
                int[] chosen = neighborsBuffer.get(SHARED_RANDOM.nextInt(neighborsBuffer.size()));
                int chosenX = chosen[0];
                int chosenY = chosen[1];

                int wallX = currentX + (chosenX - currentX) / 2;
                int wallY = currentY + (chosenY - currentY) / 2;

                walls[chosenX][chosenY] = false;
                walls[wallX][wallY] = false;
                visited[chosenX][chosenY] = true;

                stackBuffer.add(chosen);
            } else {
                stackBuffer.remove(stackBuffer.size() - 1);
            }
        }
    }

    private void clearArea(final int centerX, final int centerY, final int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (x > 0 && x < BOARD_COLS - 1 && y > 0 && y < BOARD_ROWS - 1) {
                    walls[x][y] = false;
                }
            }
        }
    }

    private void createOpenAreasAsCross() {
        int numAreas = 3 + SHARED_RANDOM.nextInt(3);

        for (int i = 0; i < numAreas; i++) {
            int centerX = 3 + SHARED_RANDOM.nextInt(BOARD_COLS - 6);
            int centerY = 3 + SHARED_RANDOM.nextInt(BOARD_ROWS - 6);

            walls[centerX][centerY] = false;
            walls[centerX - 1][centerY] = false;
            walls[centerX + 1][centerY] = false;
            walls[centerX][centerY - 1] = false;
            walls[centerX][centerY + 1] = false;
        }
    }

    private void createFood() {
        for (int x = 1; x < BOARD_COLS - 1; x++) {
            for (int y = 1; y < BOARD_ROWS - 1; y++) {
                if (!walls[x][y] && !(x == 1 && y == 1)) {
                    foods.add(new Food(x, y));
                }
            }
        }
    }

    private void createPowerPellets() {
        // Dodaj power pellets w rogach labiryntu
        List<int[]> corners = new ArrayList<>();
        corners.add(new int[]{3, 3});
        corners.add(new int[]{BOARD_COLS - 4, 3});
        corners.add(new int[]{3, BOARD_ROWS - 4});
        corners.add(new int[]{BOARD_COLS - 4, BOARD_ROWS - 4});

        for (int[] corner : corners) {
            int x = corner[0];
            int y = corner[1];

            // Znajdź najbliższe wolne miejsce
            for (int radius = 0; radius < 5; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        int checkX = x + dx;
                        int checkY = y + dy;

                        if (checkX > 0 && checkX < BOARD_COLS - 1 && checkY > 0 && checkY < BOARD_ROWS - 1 && !walls[checkX][checkY]) {

                            powerPellets.add(new PowerPellet(checkX, checkY));

                            // Usuń zwykłe jedzenie z tego miejsca
                            foods.removeIf(food -> food.x() == checkX && food.y() == checkY);

                            radius = 5; // Przerwij pętle
                            dx = radius + 1;
                            dy = radius + 1;
                        }
                    }
                }
            }
        }
    }

    private void handleKeyPress(final KeyCode keyCode) {
        if (gameOver || gameWon) {
            if (keyCode == KeyCode.R) {
                restartGame();
            }
            return;
        }

        if (keyCode == KeyCode.M) {
            soundManager.setSoundEnabled(!soundManager.isSoundEnabled());
            return;
        }

        Direction newDirection = switch (keyCode) {
            case UP, W -> Direction.UP;
            case DOWN, S -> Direction.DOWN;
            case LEFT, A -> Direction.LEFT;
            case RIGHT, D -> Direction.RIGHT;
            default -> null;
        };

        if (newDirection != null) {
            pacman.queueDirection(newDirection);
        }
    }

    private void restartGame() {
        score = 0;
        lives = 3;
        gameOver = false;
        gameWon = false;
        initializeGame();
        if (gameLoop != null) {
            gameLoop.stop();
        }
        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= UPDATE_INTERVAL) { // Reduction FPS
                    update(now);
                    render();
                    lastUpdate = now;
//                    System.out.println("[debug] FPS: " + now);
                }
            }
        };
        gameLoop.start();
    }

    private void update(final long currentTime) {
        if (gameOver || gameWon) return;

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
        if (CollisionDetector.checkCollisionsWithGhosts(this)) {
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
            score += 10;
            foodIterator.remove();
            soundManager.playSound("chomp");
        }
    }

    /**
     * Pacman zjada ducha
     *
     * @param ghost
     */
    public void eatGhost(Ghost ghost) {
        score += GHOST_POINTS * ghostMultiplier;
        ghostMultiplier *= 2;
        soundManager.playSound("ghost_eaten");
        ghost.setModeDead();
    }

    public void resetPacmanPosition() {
        pacman.reset(1, 1);
        isPowerPelletActive = false;
        ghostMultiplier = 1;
    }

    public void executePacmanIsDeadAndNoMoreLives(boolean gameOver, String game_over) {
        this.gameOver = true;
        soundManager.playSound(game_over);
        gameLoop.stop();
    }

    public void executePacmanIsDead() {
        lives--;
        soundManager.playSound("death");
    }

    private void checkWinningConditions() {
        if (foods.isEmpty() && powerPellets.isEmpty()) {
            executePacmanIsDeadAndNoMoreLives(gameWon, "level_complete");
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

        if (gameOver) {
            drawEndScreen(gc, "GAME OVER!", Color.RED, GAME_OVER_FONT);
        } else if (gameWon) {
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
            double ghostSize = TILE_SIZE - 4;
            gc.fillRect(ghostX, ghostY, ghostSize, ghostSize);


//            Color  ghostCol  = ghost.getColor();    // np. Color.CYAN
//            ThreadLocalRandom rand = ThreadLocalRandom.current();
////            Color  pupilCol  = Color.color(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());          // tak jak w oryginale
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
        for (int x = 0; x < BOARD_COLS; x++) {
            for (int y = 0; y < BOARD_ROWS; y++) {
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
