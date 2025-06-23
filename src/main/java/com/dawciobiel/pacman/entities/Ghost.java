package com.dawciobiel.pacman.entities;

import com.dawciobiel.pacman.utils.Direction;
import com.dawciobiel.pacman.utils.GhostMode;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.dawciobiel.pacman.utils.GameConstants.BOARD_COLS;
import static com.dawciobiel.pacman.utils.GameConstants.BOARD_ROWS;
import static com.dawciobiel.pacman.utils.GameConstants.MOVEMENT_SPEED;
import static com.dawciobiel.pacman.utils.GameConstants.SCATTER_TARGETS;
import static com.dawciobiel.pacman.utils.GameConstants.SHARED_RANDOM;
import static com.dawciobiel.pacman.utils.GameConstants.TILE_SIZE;


public class Ghost extends GameEntity {

    private static final int SCATTER_DURATION = 420; // 7 sekund * 60 FPS
    private static final int CHASE_DURATION = 1200;  // 20 sekund * 60 FPS

    private static final int HOME_X = BOARD_COLS / 2;
    private static final int HOME_Y = BOARD_ROWS / 2;

    private final Color originalColor;

    private final List<Direction> validDirections = new ArrayList<>(4);

    private final int scatterTargetX;
    private final int scatterTargetY;

    private final int ghostIndex; // 0-3 dla różnych strategii

    private double moveProgress = 0.0;
    private GhostMode mode = GhostMode.SCATTER;
    private int modeTimer = 0;


    public Ghost(int startX, int startY, Color color) {
        this.tileX = startX;
        this.tileY = startY;
        this.x = startX * TILE_SIZE + TILE_SIZE * 0.5;
        this.y = startY * TILE_SIZE + TILE_SIZE * 0.5;
        this.originalColor = color;
        this.direction = Direction.values()[SHARED_RANDOM.nextInt(4)];
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
    private Direction getBestDirectionToTarget(int[] target, Field[][] fields) {
        if (target == null) return null;

        Direction bestDirection = direction;
        double shortestDistance = Double.MAX_VALUE;

        for (Direction dir : Direction.values()) {
            // Nie cofaj się (chyba że musisz)
            if (getOppositeDirection(dir) == direction && validDirections.size() > 1) {
                continue;
            }

            if (canMove(dir, fields)) {
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
        this.x = newX * TILE_SIZE + TILE_SIZE * 0.5;
        this.y = newY * TILE_SIZE + TILE_SIZE * 0.5;
        this.isMoving = false;
        this.moveProgress = 0.0;
        this.mode = GhostMode.SCATTER;
        this.modeTimer = 0;
    }

    public void update(Field[][] fields, int pacmanX, int pacmanY, boolean isPowerPelletActive, Direction pacmanDirection) {
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
            chooseDirection(fields, pacmanX, pacmanY, pacmanDirection);
            if (canMove(direction, fields)) {
                startMoving();
            }
        }

        if (isMoving) {
            moveProgress += MOVEMENT_SPEED;
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

    private void chooseDirection(Field[][] fields, int pacmanX, int pacmanY, Direction pacmanDirection) {
        validDirections.clear();
        chooseAllValidDirections(fields);

        if (!validDirections.isEmpty()) {
            if (mode == GhostMode.FRIGHTENED) {
                chooseDirectionInFrightenedMode(pacmanX, pacmanY);
            } else {
                chooseDirectionInScatterChaseDeadModes(fields, pacmanX, pacmanY, pacmanDirection);
            }
        }
    }

    /**
     * Wybierz kierunek ruchu dla trybów [ Scatter, Chase, Dead modes ]
     *
     * @param fields
     * @param pacmanX
     * @param pacmanY
     * @param pacmanDirection
     */
    private void chooseDirectionInScatterChaseDeadModes(Field[][] fields, int pacmanX, int pacmanY, Direction pacmanDirection) {
        int[] target = getTarget(pacmanX, pacmanY, pacmanDirection);
        Direction bestDirection = getBestDirectionToTarget(target, fields);

        if (bestDirection != null && validDirections.contains(bestDirection)) {
            direction = bestDirection;
        } else {
            // Fallback — kontynuuj obecny kierunek lub wybierz losowy
            if (SHARED_RANDOM.nextInt(10) < 7 && validDirections.contains(direction)) {
                // Kontynuuj obecny kierunek
            } else {
                direction = validDirections.get(SHARED_RANDOM.nextInt(validDirections.size()));
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
            direction = validDirections.get(SHARED_RANDOM.nextInt(validDirections.size()));
        }
    }

    private void chooseAllValidDirections(Field[][] fields) {
        for (Direction direct : Direction.values()) {
            if (canMove(direct, fields)) {
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

        x = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        y = tileY * TILE_SIZE + TILE_SIZE * 0.5;
    }

    private void updateInterpolatedPosition() {
        double startX = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        double startY = tileY * TILE_SIZE + TILE_SIZE * 0.5;

        double targetX = startX + direction.dx * TILE_SIZE;
        double targetY = startY + direction.dy * TILE_SIZE;

        x = startX + (targetX - startX) * moveProgress;
        y = startY + (targetY - startY) * moveProgress;
    }

    private boolean canMove(final Direction dir, final Field[][] fields) {
        int nextX = tileX + dir.dx;
        int nextY = tileY + dir.dy;

        if (nextX < 0 || nextX >= fields.length || nextY < 0 || nextY >= fields[0].length) {
            return (nextX < 0 || nextX >= fields.length) && nextY == BOARD_ROWS / 2;
        }

        Field field = fields[nextX][nextY];
        FieldType fieldType = field.getType();

        return !(fieldType == FieldType.WALL);
    }

    private void handleTunnels() {
        if (tileX < 0) {
            tileX = BOARD_COLS - 1;
            x = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        } else if (tileX >= BOARD_COLS) {
            tileX = 0;
            x = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        }
    }

    public GhostMode getMode() {
        return mode;
    }

    public void setMode(final GhostMode newMode) {
        this.mode = newMode;
    }

    public Color getColor() {
        return originalColor;
    }
}