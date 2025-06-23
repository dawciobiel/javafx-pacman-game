package com.dawciobiel.pacman.level;

import com.dawciobiel.pacman.entities.Field;
import com.dawciobiel.pacman.entities.FieldType;
import com.dawciobiel.pacman.utils.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGenerator {
    private static final int[][] MAZE_DIRECTIONS = {{0, -2}, {2, 0}, {0, 2}, {-2, 0}};
    private final Field[][] fields;
    private final int columns;
    private final int rows;
    private final List<Point> pathStack;
    private final List<Point> unvisitedNeighbors;
    private final Random random;

    public MazeGenerator(Field[][] fields, int columns, int rows, Random random) {
        this.fields = fields;
        this.columns = columns;
        this.rows = rows;
        this.pathStack = new ArrayList<>();
        this.unvisitedNeighbors = new ArrayList<>();
        this.random = random;
    }

    public void generateMaze(int startX, int startY) {
        boolean[][] visited = new boolean[columns][rows];
        initializeStartPoint(startX, startY, visited);

        while (!pathStack.isEmpty()) {
            Point current = getCurrentPosition();
            List<Point> neighbors = findUnvisitedNeighbors(current, visited);

            if (!neighbors.isEmpty()) {
                processNextStep(current, neighbors, visited);
            } else {
                pathStack.removeLast();
            }
        }
    }

    private void initializeStartPoint(int startX, int startY, boolean[][] visited) {
        fields[startX][startY] = new Field(FieldType.EMPTY);
        visited[startX][startY] = true;
        pathStack.add(new Point(startX, startY));
    }

    private Point getCurrentPosition() {
        return pathStack.getLast();
    }

    private List<Point> findUnvisitedNeighbors(Point current, boolean[][] visited) {
        unvisitedNeighbors.clear();
        for (int[] dir : MAZE_DIRECTIONS) {
            int newX = current.x() + dir[0];
            int newY = current.y() + dir[1];
            if (isValidPosition(newX, newY) && !visited[newX][newY]) {
                unvisitedNeighbors.add(new Point(newX, newY));
            }
        }
        return unvisitedNeighbors;
    }

    private boolean isValidPosition(int x, int y) {
        return x > 0 && x < columns - 1 && y > 0 && y < rows - 1;
    }

    private void processNextStep(Point current, List<Point> neighbors, boolean[][] visited) {
        Point chosen = neighbors.get(random.nextInt(neighbors.size()));
        int wallX = current.x() + (chosen.x() - current.x()) / 2;
        int wallY = current.y() + (chosen.y() - current.y()) / 2;

        fields[chosen.x()][chosen.y()] = new Field(FieldType.EMPTY);
        fields[wallX][wallY] = new Field(FieldType.EMPTY);
        visited[chosen.x()][chosen.y()] = true;
        pathStack.add(chosen);
    }

}
