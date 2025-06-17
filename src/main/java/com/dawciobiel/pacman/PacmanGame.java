package com.dawciobiel.pacman;

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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.dawciobiel.pacman.PacmanGame.BOARD_COLS;
import static com.dawciobiel.pacman.PacmanGame.BOARD_ROWS;
import static com.dawciobiel.pacman.PacmanGame.SCATTER_TARGETS;


enum Direction {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

    public final int dx;
    public final int dy;

    Direction(final int dx, final int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}

enum GhostMode {
    NORMAL, FRIGHTENED, SCATTER, CHASE, DEAD
}

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
    static final double MOVEMENT_SPEED = 0.2;

    static final int TILE_SIZE = 50;
    // OPTYMALIZACJA: Re-używalne obiekty
    static final Random SHARED_RANDOM = new Random();
    private static final int BOARD_WIDTH = 1200;
    static final int BOARD_COLS = BOARD_WIDTH / TILE_SIZE;
    private static final int BOARD_HEIGHT = 800;
    static final int BOARD_ROWS = BOARD_HEIGHT / TILE_SIZE;
    // Pozycje rogów dla scatter mode
    protected static final int[][] SCATTER_TARGETS = {{BOARD_COLS - 1, 0},        // czerwony — prawy górny
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
    private SoundManager soundManager;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private Player pacman;
    private List<Ghost> ghosts;
    private List<Food> foods;
    private List<PowerPellet> powerPellets;
    private boolean[][] walls;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private long powerPelletStartTime = 0;
    private boolean isPowerPelletActive = false;
    private int ghostMultiplier = 2; // Mnożnik punktów za duchy

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
        checkCollisionWithFood(currentTime);

        // Check collisions with ghosts
        if (checkCollisionsWithGhosts()) return;

        // Check winning conditions
        checkWinningConditions();
    }

    private void checkCollisionWithFood(long currentTime) {
        if (pacman.isAtTileCenter()) {
            // Check collisions with food
            Iterator<Food> foodIterator = foods.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                eatFood(food, foodIterator);
            }

            // Check collisions with power pellets
            Iterator<PowerPellet> powerPelletIterator = powerPellets.iterator();
            while (powerPelletIterator.hasNext()) {
                PowerPellet powerPellet = powerPelletIterator.next();
                if (powerPellet.x() == pacman.getTileX() && powerPellet.y() == pacman.getTileY()) {
                    score += 50;
                    powerPelletIterator.remove();
                    soundManager.playSound("power_pellet");

                    // Aktywuj power pellet
                    isPowerPelletActive = true;
                    powerPelletStartTime = currentTime;
                    ghostMultiplier = 1;

                    // Zmień tryb duchów na frightened
                    changeGhostsMode(GhostMode.FRIGHTENED);
                }
            }
        }
    }

    private void changeGhostsMode(GhostMode ghostMode) {
        for (Ghost ghost : ghosts) {
            ghost.setMode(ghostMode);
        }
    }

    private void eatFood(Food food, Iterator<Food> foodIterator) {
        if (food.x() == pacman.getTileX() && food.y() == pacman.getTileY()) {
            score += 10;
            foodIterator.remove();
            soundManager.playSound("chomp");
        }
    }

    private boolean checkCollisionsWithGhosts() {
        Iterator<Ghost> ghostIterator = ghosts.iterator();
        while (ghostIterator.hasNext()) {
            Ghost ghost = ghostIterator.next();
            double dx = ghost.getDrawX() - pacman.getDrawX();
            double dy = ghost.getDrawY() - pacman.getDrawY();
            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared < (TILE_SIZE * 0.8) * (TILE_SIZE * 0.8)) {
                if (isPowerPelletActive && ghost.getMode() == GhostMode.FRIGHTENED) {
                    eatGhost(ghost);
                } else if (ghost.getMode() != GhostMode.DEAD) { // Nie zabijaj, jeśli duch jest martwy
                    executePacmanIsDead();
                    if (lives <= 0) {
                        executePacmanIsDeadAndNoMoreLives(gameOver, "game_over");
                        return true;
                    } else {
                        // Restart pozycji
                        resetPacmanPosition();

                        // Przywróć atakujący tryb duchów
                        changeGhostsMode(GhostMode.SCATTER);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Pacman zjada ducha
     *
     * @param ghost
     */
    private void eatGhost(Ghost ghost) {
        score += GHOST_POINTS * ghostMultiplier;
        ghostMultiplier *= 2;
        soundManager.playSound("ghost_eaten");
        ghost.setModeDead();
    }

    private void resetPacmanPosition() {
        pacman.reset(1, 1);
        isPowerPelletActive = false;
        ghostMultiplier = 1;
    }

    private void executePacmanIsDeadAndNoMoreLives(boolean gameOver, String game_over) {
        this.gameOver = true;
        soundManager.playSound(game_over);
        gameLoop.stop();
    }

    private void executePacmanIsDead() {
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

class Player {
    private static final double ANIMATION_SPEED = 0.15;
    private double x;
    private double y;
    private int tileX;
    private int tileY;
    private Direction direction = Direction.RIGHT;
    private Direction queuedDirection = null;
    private boolean isMoving = false;
    private double moveProgress = 0.0;
    private double animationTime = 0.0;

    public Player(int startX, int startY) {
        this.tileX = startX;
        this.tileY = startY;
        this.x = startX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.y = startY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
    }

    public void reset(final int newX, final int newY) {
        this.tileX = newX;
        this.tileY = newY;
        this.x = newX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.y = newY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.direction = Direction.RIGHT;
        this.queuedDirection = null;
        this.isMoving = false;
        this.moveProgress = 0.0;
        this.animationTime = 0.0;
    }

    public void draw(GraphicsContext gc) {
        double startAngle = switch (direction) {
            case RIGHT -> 0;
            case DOWN -> 270;
            case LEFT -> 180;
            case UP -> 90;
        };

        double mouthAngle = 45 * Math.sin(animationTime * Math.PI * 2);
        if (mouthAngle < 0) mouthAngle = 0;

        double drawX = getDrawX() - PacmanGame.TILE_SIZE * 0.5 + 2;
        double drawY = getDrawY() - PacmanGame.TILE_SIZE * 0.5 + 2;
        double size = PacmanGame.TILE_SIZE - 4;

        gc.setFill(Color.YELLOW);

        if (mouthAngle > 0) {
            gc.fillArc(drawX, drawY, size, size, startAngle + mouthAngle * 0.5, 360 - mouthAngle, ArcType.ROUND);
        } else {
            gc.fillOval(drawX, drawY, size, size);
        }
    }

    public void update(boolean[][] walls) {
        if (isMoving) {
            animationTime += ANIMATION_SPEED;
            if (animationTime >= 1.0) {
                animationTime = 0.0;
            }
        }

        if (queuedDirection != null && !isMoving) {
            if (canMove(queuedDirection, walls)) {
                direction = queuedDirection;
                queuedDirection = null;
                startMoving();
            }
        }

        if (!isMoving && canMove(direction, walls)) {
            startMoving();
        }

        if (isMoving) {
            moveProgress += PacmanGame.MOVEMENT_SPEED;

            if (moveProgress >= 1.0) {
                completeMove();
                moveProgress = 0.0;
                isMoving = false;
                handleTunnels();
            } else {
                updateInterpolatedPosition();
            }
        }
    }

    private void startMoving() {
        isMoving = true;
        moveProgress = 0.0;
    }

    private void completeMove() {
        tileX += direction.dx;
        tileY += direction.dy;

        x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        y = tileY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
    }

    private void updateInterpolatedPosition() {
        double startX = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        double startY = tileY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;

        double targetX = startX + direction.dx * PacmanGame.TILE_SIZE;
        double targetY = startY + direction.dy * PacmanGame.TILE_SIZE;

        x = startX + (targetX - startX) * moveProgress;
        y = startY + (targetY - startY) * moveProgress;
    }

    private boolean canMove(Direction dir, boolean[][] walls) {
        int nextX = tileX + dir.dx;
        int nextY = tileY + dir.dy;

        if (nextX < 0 || nextX >= walls.length || nextY < 0 || nextY >= walls[0].length) {
            return (nextX < 0 || nextX >= walls.length) && nextY == BOARD_ROWS / 2;
        }

        return !walls[nextX][nextY];
    }

    private void handleTunnels() {
        if (tileX < 0) {
            tileX = BOARD_COLS - 1;
            x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        } else if (tileX >= BOARD_COLS) {
            tileX = 0;
            x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        }
    }

    public void queueDirection(Direction newDirection) {
        this.queuedDirection = newDirection;
    }

    public boolean isAtTileCenter() {
        return !isMoving;
    }

    public double getDrawX() {
        return x;
    }

    public double getDrawY() {
        return y;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public Direction getDirection() {
        return direction;
    }
}

class Ghost {

    private static final int SCATTER_DURATION = 420; // 7 sekund * 60 FPS
    private static final int CHASE_DURATION = 1200;  // 20 sekund * 60 FPS
    private static final int HOME_X = BOARD_COLS / 2;
    private static final int HOME_Y = BOARD_ROWS / 2;
    private final Color originalColor;
    private final List<Direction> validDirections = new ArrayList<>(4);
    private final int scatterTargetX;
    private final int scatterTargetY;
    private final int ghostIndex; // 0-3 dla różnych strategii
    private double x;
    private double y;
    private int tileX;
    private int tileY;
    private Direction direction;
    private boolean isMoving = false;
    private double moveProgress = 0.0;
    private GhostMode mode = GhostMode.SCATTER;
    private int modeTimer = 0;


    public Ghost(int startX, int startY, Color color) {
        this.tileX = startX;
        this.tileY = startY;
        this.x = startX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.y = startY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.originalColor = color;
        this.direction = Direction.values()[PacmanGame.SHARED_RANDOM.nextInt(4)];
        // Przypisz indeks na podstawie koloru
        if (color.equals(Color.RED)) this.ghostIndex = 0;
        else if (color.equals(Color.PINK)) this.ghostIndex = 1;
        else if (color.equals(Color.CYAN)) this.ghostIndex = 2;
        else this.ghostIndex = 3;

        // Przypisz cel scatter
        this.scatterTargetX = SCATTER_TARGETS[ghostIndex][0];
        this.scatterTargetY = SCATTER_TARGETS[ghostIndex][1];

    }

    // Metoda do zarządzania stanami
    private void updateMode() {
        if (mode == GhostMode.FRIGHTENED || mode == GhostMode.DEAD) {
            return; // Nie zmieniaj automatycznie w tych trybach
        }

        modeTimer++;

        if (mode == GhostMode.SCATTER && modeTimer >= SCATTER_DURATION) {
            mode = GhostMode.CHASE;
            modeTimer = 0;
        } else if (mode == GhostMode.CHASE && modeTimer >= CHASE_DURATION) {
            mode = GhostMode.SCATTER;
            modeTimer = 0;
        }
    }

    // Metoda do wybierania celu
    private int[] getTarget(int pacmanX, int pacmanY, Direction pacmanDirection) {
        switch (mode) {
            case SCATTER:
                return new int[]{scatterTargetX, scatterTargetY};

            case CHASE:
                return getChaseTarget(pacmanX, pacmanY, pacmanDirection);

            case FRIGHTENED:
                return null; // Losowy ruch

            case DEAD:
                return new int[]{HOME_X, HOME_Y};

            default:
                return new int[]{scatterTargetX, scatterTargetY};
        }
    }

    // Metoda do strategii ścigania
    private int[] getChaseTarget(int pacmanX, int pacmanY, Direction pacmanDirection) {
        switch (ghostIndex) {
            case 0: // Czerwony (Blinky) - bezpośrednio do Pacmana
                return new int[]{pacmanX, pacmanY};

            case 1: // Różowy (Pinky) - 4 pola przed Pacmanem
                int targetX = pacmanX;
                int targetY = pacmanY;
                switch (pacmanDirection) {
                    case LEFT:
                        targetX -= 4;
                        break;
                    case RIGHT:
                        targetX += 4;
                        break;
                    case UP:
                        targetY -= 4;
                        break;
                    case DOWN:
                        targetY += 4;
                        break;
                }
                return new int[]{targetX, targetY};

            case 2: // Cyan (Inky) - 2 pola przed Pacmanem
                targetX = pacmanX;
                targetY = pacmanY;
                switch (pacmanDirection) {
                    case LEFT:
                        targetX -= 2;
                        break;
                    case RIGHT:
                        targetX += 2;
                        break;
                    case UP:
                        targetY -= 2;
                        break;
                    case DOWN:
                        targetY += 2;
                        break;
                }
                return new int[]{targetX, targetY};

            case 3: // Pomarańczowy (Clyde) - jeśli daleko to jak Blinky, jeśli blisko to scatter
                double distance = Math.sqrt((tileX - pacmanX) * (tileX - pacmanX) + (tileY - pacmanY) * (tileY - pacmanY));
                if (distance > 8) {
                    return new int[]{pacmanX, pacmanY};
                } else {
                    return new int[]{scatterTargetX, scatterTargetY};
                }

            default:
                return new int[]{pacmanX, pacmanY};
        }
    }

    // Metoda do ruchu w kierunku celu
    private Direction getBestDirectionToTarget(int[] target, boolean[][] walls) {
        if (target == null) return null;

        Direction bestDirection = direction;
        double shortestDistance = Double.MAX_VALUE;

        for (Direction dir : Direction.values()) {
            // Nie cofaj się (chyba że musisz)
            if (getOppositeDirection(dir) == direction && validDirections.size() > 1) {
                continue;
            }

            if (canMove(dir, walls)) {
                int nextX = tileX + dir.dx;
                int nextY = tileY + dir.dy;
                double distance = Math.sqrt((nextX - target[0]) * (nextX - target[0]) + (nextY - target[1]) * (nextY - target[1]));

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestDirection = dir;
                }
            }
        }

        return bestDirection;
    }

    // Metoda pomocnicza dla kierunków przeciwnych
    private Direction getOppositeDirection(Direction dir) {
        switch (dir) {
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
            default:
                return dir;
        }
    }


    public void respawn(int newX, int newY) {
        this.tileX = newX;
        this.tileY = newY;
        this.x = newX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.y = newY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.isMoving = false;
        this.moveProgress = 0.0;
        this.mode = GhostMode.SCATTER;
        this.modeTimer = 0;
    }

    public void update(boolean[][] walls, int pacmanX, int pacmanY, boolean isPowerPelletActive, Direction pacmanDirection) {
        if (mode != GhostMode.FRIGHTENED) {
            updateMode();
        }

        // Sprawdź, czy duch w trybie DEAD dotarł do "domu"
        if (mode == GhostMode.DEAD) {
            if (Math.abs(tileX - HOME_X) <= 1 && Math.abs(tileY - HOME_Y) <= 1) {
                mode = GhostMode.SCATTER;
                modeTimer = 0;
            }
        }

        if (!isMoving) {
            chooseDirection(walls, pacmanX, pacmanY, pacmanDirection);
            if (canMove(direction, walls)) {
                startMoving();
            }
        }

        if (isMoving) {
            moveProgress += PacmanGame.MOVEMENT_SPEED;
            if (moveProgress >= 1.0) {
                completeMove();
                moveProgress = 0.0;
                isMoving = false;
                handleTunnels();
            } else {
                updateInterpolatedPosition();
            }
        }
    }

    public void setModeDead() {
        this.mode = GhostMode.DEAD;
        this.modeTimer = 0;
    }

    private void chooseDirection(boolean[][] walls, int pacmanX, int pacmanY, Direction pacmanDirection) {
        validDirections.clear();
        chooseAllValidDirections(walls);

        if (!validDirections.isEmpty()) {
            if (mode == GhostMode.FRIGHTENED) {
                chooseDirectionInFrightenedMode(pacmanX, pacmanY);
            } else {
                chooseDirectionInScatterChaseDeadModes(walls, pacmanX, pacmanY, pacmanDirection);
            }
        }
    }

    /**
     * Wybierz kierunek ruchu dla trybów [ Scatter, Chase, Dead modes ]
     *
     * @param walls
     * @param pacmanX
     * @param pacmanY
     * @param pacmanDirection
     */
    private void chooseDirectionInScatterChaseDeadModes(boolean[][] walls, int pacmanX, int pacmanY, Direction pacmanDirection) {
        int[] target = getTarget(pacmanX, pacmanY, pacmanDirection);
        Direction bestDirection = getBestDirectionToTarget(target, walls);

        if (bestDirection != null && validDirections.contains(bestDirection)) {
            direction = bestDirection;
        } else {
            // Fallback — kontynuuj obecny kierunek lub wybierz losowy
            if (PacmanGame.SHARED_RANDOM.nextInt(10) < 7 && validDirections.contains(direction)) {
                // Kontynuuj obecny kierunek
            } else {
                direction = validDirections.get(PacmanGame.SHARED_RANDOM.nextInt(validDirections.size()));
            }
        }
    }

    /**
     * Unikaj Pacmana
     *
     * @param pacmanX
     * @param pacmanY
     */
    private void chooseDirectionInFrightenedMode(int pacmanX, int pacmanY) {
        Direction bestDirection = getBestDirection(pacmanX, pacmanY);
        if (bestDirection != null) {
            direction = bestDirection;
        } else {
            direction = validDirections.get(PacmanGame.SHARED_RANDOM.nextInt(validDirections.size()));
        }
    }

    private void chooseAllValidDirections(boolean[][] walls) {
        for (Direction direct : Direction.values()) {
            if (canMove(direct, walls)) {
                validDirections.add(direct);
            }
        }
    }

    @Nullable
    private Direction getBestDirection(int pacmanX, int pacmanY) {
        Direction bestDirection = null;
        double maxDistance = -1;
        for (Direction dir : validDirections) {
            int nextX = tileX + dir.dx;
            int nextY = tileY + dir.dy;
            double distance = Math.sqrt((nextX - pacmanX) * (nextX - pacmanX) + (nextY - pacmanY) * (nextY - pacmanY));
            if (distance > maxDistance) {
                maxDistance = distance;
                bestDirection = dir;
            }
        }
        return bestDirection;
    }

    private void startMoving() {
        isMoving = true;
        moveProgress = 0.0;
    }

    private void completeMove() {
        tileX += direction.dx;
        tileY += direction.dy;

        x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        y = tileY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
    }

    private void updateInterpolatedPosition() {
        double startX = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        double startY = tileY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;

        double targetX = startX + direction.dx * PacmanGame.TILE_SIZE;
        double targetY = startY + direction.dy * PacmanGame.TILE_SIZE;

        x = startX + (targetX - startX) * moveProgress;
        y = startY + (targetY - startY) * moveProgress;
    }

    private boolean canMove(final Direction dir, final boolean[][] walls) {
        int nextX = tileX + dir.dx;
        int nextY = tileY + dir.dy;

        if (nextX < 0 || nextX >= walls.length || nextY < 0 || nextY >= walls[0].length) {
            return (nextX < 0 || nextX >= walls.length) && nextY == BOARD_ROWS / 2;
        }

        return !walls[nextX][nextY];
    }

    private void handleTunnels() {
        if (tileX < 0) {
            tileX = BOARD_COLS - 1;
            x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        } else if (tileX >= BOARD_COLS) {
            tileX = 0;
            x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        }
    }

    public GhostMode getMode() {
        return mode;
    }

    public void setMode(final GhostMode newMode) {
        this.mode = newMode;
    }

    public double getDrawX() {
        return x;
    }

    public double getDrawY() {
        return y;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public Color getColor() {
        return originalColor;
    }
}

record Food(int x, int y) {
}

record PowerPellet(int x, int y) {
}
