package com.dawciobiel.pacman.input;

import com.dawciobiel.pacman.game.GameState;
import com.dawciobiel.pacman.sound.SoundManager;
import com.dawciobiel.pacman.utils.Direction;
import javafx.scene.input.KeyCode;

public class InputHandler {

    /**
     * Obsługuje wciśnięcie klawisza podczas gry
     *
     * @param keyCode               kod wciśniętego klawisza
     * @param pacmanDirectionSetter funkcja do ustawienia kierunku Pacmana
     * @param gameRestarter         funkcja do restartu gry
     * @param soundManager          menadżer dźwięku
     * @return true jeśli klawisz został obsłużony, false w przeciwnym razie
     */
    public static boolean handleKeyPress(KeyCode keyCode, DirectionSetter pacmanDirectionSetter, Runnable gameRestarter, SoundManager soundManager) {
        // Jeśli gra się skończyła, obsługuj tylko klawisz R do restartu
        if (GameState.gameOver || GameState.gameWon) {
            if (keyCode == KeyCode.R) {
                gameRestarter.run();
                return true;
            }
            return false;
        }

        // Obsługa wyciszenia/włączenia dźwięku
        if (keyCode == KeyCode.M) {
            soundManager.setSoundEnabled(!soundManager.isSoundEnabled());
            return true;
        }

        // Obsługa kierunków ruchu
        Direction newDirection = switch (keyCode) {
            case UP, W -> Direction.UP;
            case DOWN, S -> Direction.DOWN;
            case LEFT, A -> Direction.LEFT;
            case RIGHT, D -> Direction.RIGHT;
            default -> null;
        };

        if (newDirection != null) {
            pacmanDirectionSetter.setDirection(newDirection);
            return true;
        }

        return false;
    }

    @FunctionalInterface
    public interface DirectionSetter {
        void setDirection(Direction direction);
    }
}