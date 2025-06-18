package com.dawciobiel.pacman.game;

import com.dawciobiel.pacman.utils.GameConstants;

/**
 * Klasa zarządzająca stanem gry Pacman.
 * Zawiera wszystkie zmienne stanu gry i metody do ich zarządzania.
 */
public class GameState {

    // === STAN GRY ===
    public static int score;
    public static int lives;
    public static boolean gameOver;
    public static boolean gameWon = false;

    // === POWER PELLET ===
    private boolean isPowerPelletActive;
    private long powerPelletStartTime;
    private int ghostMultiplier;

    /**
     * Konstruktor inicjalizujący stan gry z domyślnymi wartościami.
     */
    public GameState() {
        reset();
    }

    /**
     * Dodaje punkty do wyniku.
     *
     * @param points liczba punktów do dodania
     */
    public static void addScore(int points) {
        score += points;
    }

    // === METODY BIZNESOWE ===
    public static void loseLife() {
        lives--;
    }

    /**
     * Resetuje stan gry do wartości początkowych.
     */
    public void reset() {
        score = GameConstants.INITIAL_SCORE;
        lives = GameConstants.INITIAL_LIVES;
        gameOver = false;
        this.gameWon = false;
        this.isPowerPelletActive = false;
        this.powerPelletStartTime = 0;
        this.ghostMultiplier = GameConstants.INITIAL_GHOST_MULTIPLIER;
    }

    /**
     * Dodaje punkty za zjedzenie ducha z aktualnym mnożnikiem.
     *
     * @return liczba punktów które zostały dodane
     */
    public int addGhostScore() {
        int points = GameConstants.GHOST_POINTS * ghostMultiplier;
        addScore(points);
        ghostMultiplier *= GameConstants.GHOST_MULTIPLIER_INCREMENT;
        return points;
    }

    /**
     * Aktywuje power pellet.
     *
     * @param currentTime aktualny czas systemowy w nanosekundach
     */
    public void activatePowerPellet(long currentTime) {
        this.isPowerPelletActive = true;
        this.powerPelletStartTime = currentTime;
        this.ghostMultiplier = GameConstants.INITIAL_GHOST_MULTIPLIER;
    }

    /**
     * Sprawdza, czy power pellet wygasł i go dezaktywuje, jeśli tak.
     *
     * @param currentTime aktualny czas systemowy w nanosekundach
     * @return true jeśli power pellet właśnie wygasł, false w przeciwnym razie
     */
    public boolean checkAndDeactivatePowerPellet(long currentTime) {
        if (isPowerPelletActive && currentTime - powerPelletStartTime > GameConstants.POWER_PELLET_DURATION_NANOS) {

            isPowerPelletActive = false;
            ghostMultiplier = GameConstants.INITIAL_GHOST_MULTIPLIER;
            return true; // power pellet właśnie wygasł
        }
        return false;
    }

    /**
     * Resetuje stan po śmierci gracza (ale nie resetuje całej gry).
     */
    public void resetAfterDeath() {
        this.isPowerPelletActive = false;
        this.ghostMultiplier = GameConstants.INITIAL_GHOST_MULTIPLIER;
    }

    /**
     * Kończy grę jako wygraną.
     */
    public void winGame() {
        this.gameWon = true;
    }

    /**
     * Sprawdza czy gra powinna się skończyć (wygrana lub przegrana).
     *
     * @return true jeśli gra się skończyła
     */
    public boolean isGameFinished() {
        return gameOver || gameWon;
    }

    // === GETTERY ===

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        GameState.score = score;
    }

    public static int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        GameState.lives = lives;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        GameState.gameOver = gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    // === SETTERY (głównie do testów) ===

    public boolean isPowerPelletActive() {
        return isPowerPelletActive;
    }

    public long getPowerPelletStartTime() {
        return powerPelletStartTime;
    }

    public int getGhostMultiplier() {
        return ghostMultiplier;
    }

    /**
     * Zwraca czas pozostały do końca działania power pelleta w milisekundach.
     *
     * @param currentTime aktualny czas systemowy w nanosekundach
     * @return czas pozostały w milisekundach, 0 jeśli power pellet nie jest aktywny
     */
    public long getPowerPelletTimeRemaining(long currentTime) {
        if (!isPowerPelletActive) {
            return 0;
        }

        long elapsedNanos = currentTime - powerPelletStartTime;
        long remainingNanos = GameConstants.POWER_PELLET_DURATION_NANOS - elapsedNanos;

        return Math.max(0, remainingNanos / 1_000_000L); // konwersja na milisekundy
    }

    @Override
    public String toString() {
        return String.format("GameState{score=%d, lives=%d, gameOver=%s, gameWon=%s, powerPelletActive=%s}", score, lives, gameOver, gameWon, isPowerPelletActive);
    }
}