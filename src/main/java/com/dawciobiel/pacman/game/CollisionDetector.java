package com.dawciobiel.pacman.game;

import com.dawciobiel.pacman.PacmanGame;
import com.dawciobiel.pacman.entities.Food;
import com.dawciobiel.pacman.entities.Ghost;
import com.dawciobiel.pacman.entities.Player;
import com.dawciobiel.pacman.entities.PowerPellet;
import com.dawciobiel.pacman.utils.GhostMode;

import java.util.Iterator;

import static com.dawciobiel.pacman.utils.GameConstants.TILE_SIZE;

public class CollisionDetector {


    public static boolean checkCollisionWithOneGhost(PacmanGame pacmanGame, double distanceSquared, Ghost ghost) {
        if (distanceSquared < (TILE_SIZE * 0.8) * (TILE_SIZE * 0.8)) {
            if (pacmanGame.isPowerPelletActive && ghost.getMode() == GhostMode.FRIGHTENED) {
                pacmanGame.eatGhost(ghost);
            } else if (ghost.getMode() != GhostMode.DEAD) { // Nie zabijaj, jeśli duch jest martwy
                pacmanGame.executePacmanIsDead();
                if (pacmanGame.lives <= 0) {
                    pacmanGame.executePacmanIsDeadAndNoMoreLives(pacmanGame.gameOver, "game_over");
                    return true;
                } else {
                    // Restart pozycji
                    pacmanGame.resetPacmanPosition();

                    // Przywróć atakujący tryb duchów
                    pacmanGame.changeGhostsMode(GhostMode.SCATTER);
                }
            }
        }
        return false;
    }

    public static boolean checkCollisionsWithGhosts(PacmanGame pacmanGame) {
        Iterator<Ghost> ghostIterator = pacmanGame.ghosts.iterator();
        while (ghostIterator.hasNext()) {
            Ghost ghost = ghostIterator.next();
            double distanceSquared = calculateDistanceSquared(pacmanGame.pacman, ghost);
            if (CollisionDetector.checkCollisionWithOneGhost(pacmanGame, distanceSquared, ghost)) {

                return true;
            }
        }

        return false;
    }

    private static double calculateDistanceSquared(Player pacman, Ghost ghost) {
        double dx = ghost.getDrawX() - pacman.getDrawX();
        double dy = ghost.getDrawY() - pacman.getDrawY();

        return dx * dx + dy * dy;
    }

    public static void checkCollisionWithFood(PacmanGame pacmanGame, long currentTime) {
        if (pacmanGame.pacman.isAtTileCenter()) {
            // Check collisions with food
            Iterator<Food> foodIterator = pacmanGame.foods.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                pacmanGame.eatFood(food, foodIterator);
            }

            // Check collisions with power pellets
            Iterator<PowerPellet> powerPelletIterator = pacmanGame.powerPellets.iterator();
            while (powerPelletIterator.hasNext()) {
                PowerPellet powerPellet = powerPelletIterator.next();
                if (powerPellet.x() == pacmanGame.pacman.getTileX() && powerPellet.y() == pacmanGame.pacman.getTileY()) {
                    pacmanGame.score += 50;
                    powerPelletIterator.remove();
                    pacmanGame.soundManager.playSound("power_pellet");

                    // Aktywuj power pellet
                    pacmanGame.isPowerPelletActive = true;
                    pacmanGame.powerPelletStartTime = currentTime;
                    pacmanGame.ghostMultiplier = 1;

                    // Zmień tryb duchów na frightened
                    pacmanGame.changeGhostsMode(GhostMode.FRIGHTENED);
                }
            }
        }
    }
}