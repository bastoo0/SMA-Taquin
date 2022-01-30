package alle.dupuch.tp1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.abs;

public class Environment {
    private Grid currentGrid;
    private Grid finalGrid;
    private List <Agent> agentList;
    int[][] spiral;

    public Environment (int width, int height, int agentCount) {
        agentList = new ArrayList <> ();
        for (int i = 0; i < agentCount; ++i) {
            Agent agent = new Agent ();
            agent.setEnvironnment (this);
            agentList.add (agent);
        }

        currentGrid = new Grid (width, height);
        finalGrid = new Grid (width, height);

        populateGrid (currentGrid);
        populateGrid (finalGrid);

        generateSpiral();
        printSpiral();
    }

    private void populateGrid (Grid grid) {
        boolean isFinalGrid = grid == finalGrid;
        int agentCount = agentList.size ();
        List <Integer> permutation = createPermutation (grid.getWidth () * grid.getHeight (), agentCount);

        for (int x = 0; x < grid.getWidth (); ++x) {
            for (int y = 0; y < grid.getHeight (); ++y) {
                int agentId = permutation.get (y * grid.getWidth () + x);
                // Si la case est occupée
                if (agentId != -1) {
                    BoundedPoint2D position = new BoundedPoint2D (x, y);
                    Agent agent = agentList.get (agentId);

                    // On met à jour
                    grid.getSquare (position).setAgent (agent);
                    if (isFinalGrid) {
                        agent.setFinalPosition (position);
                    } else {
                        agent.setCurrentPosition (position);
                    }
                }
            }
        }
    }

    // Renvoie un tableau de type [-1, 1, 0, -1, 2] où -1 correspond à une case vide (permet de placer les agents dans la grille initiale
    // et la grille finale).
    private List <Integer> createPermutation (int valuesCount, int nonEmptyValuesCount) {
        List <Integer> permutation = Stream
                .iterate (0, i -> i + 1)
                .limit (valuesCount)
                .map (i -> i >= nonEmptyValuesCount? -1: i)
                .collect (Collectors.toList ());
        Collections.shuffle(permutation);
        return permutation;
    }

    public List <BoundedPoint2D> getNeighbours (BoundedPoint2D currentPosition) {
        List <Point2D> translations = List.of (
            new Point2D (- 1, 0),
            new Point2D (+1, 0),
            new Point2D (0, - 1),
            new Point2D (0, + 1)
        );
        List <BoundedPoint2D> possibleMoves = new ArrayList <> ();
        for (var translation: translations) {
            try {
                var newPoint = currentPosition.add (translation);
                possibleMoves.add (newPoint);
            } catch (Exception e) {}
        }
        return possibleMoves;
    }

    // On calcule la priorité de l'agent en fonction de sa distance avec les bords
    // Plus un agent est proche du bord, plus sa priorité est élevée
    // Les angles ont une priorité plus élevée que les bords
    public double getPriorityStrategyEdges(Agent agent) {
        if (agent.isInFinalPosition()) return 0;
        int posX = agent.getCurrentPosition().x;
        int posY = agent.getCurrentPosition().y;
        double halfWidth = currentGrid.getWidth() / 2.0;
        double halfHeight = currentGrid.getHeight() / 2.0;
        double priority = Math.pow(posX - halfWidth, 2) * 0.8 + Math.pow(posY - halfHeight, 2);
        // Plus on est loin + on a de priorité
        priority += (abs(agent.getFinalPosition().x - posX) + abs(agent.getFinalPosition().y - posY)) / 2.0;
        return priority;
    }

    private void generateSpiral() {
        int WIDTH = currentGrid.getWidth();
        int HEIGHT = currentGrid.getHeight();

        // On initialise la spirale à 0
        spiral = new int[WIDTH][HEIGHT];
        for (int[] ints : spiral) {
            Arrays.fill(ints, 0);
        }

        int num = WIDTH * HEIGHT;
        int x = 0, y = 0, dx = 1, dy = 0;
        while (num > 0) {
            spiral[x][y] = num--;
            int nextX = x + dx;
            int nextY = y + dy;
            if (nextX < 0 || nextX == WIDTH || nextY < 0 || nextY == HEIGHT || spiral[nextX][nextY] != 0) {
                int t = dy;
                dy = dx;
                dx = -t;
            }
            x += dx;
            y += dy;
        }
    }

    public int getSpiralPriority(BoundedPoint2D pos) {
        return spiral[pos.getX()][pos.getY()];
    }

    private void printSpiral() {
        for (int i = 0; i < spiral.length; i++)
        {
            for (int j = 0; j < spiral.length; j++)
            {
                System.out.print(spiral[i][j]+ "\t");
            }
            System.out.println();
        }
    }

    public double getPriorityStrategySpiral(Agent agent) {
        int posX = agent.getCurrentPosition().x;
        int posY = agent.getCurrentPosition().y;
        return spiral[posX][posY];
    }

    public List <Agent> getAgentList() {
        return new ArrayList <> (agentList);
    }

    public void setNewPositionInCurrentGrid (Agent agent, BoundedPoint2D newPosition) {
        var previousPosition = agent.getCurrentPosition ();
        currentGrid.getSquare (previousPosition).setAgent (null);
        currentGrid.getSquare (newPosition).setAgent (agent);
    }

    public Grid getCurrentGrid () {
        return currentGrid;
    }

    public Grid getFinalGrid () {
        return finalGrid;
    }

    public int getWidth () {
        return currentGrid.getWidth();
    }

    public int getHeight () {
        return currentGrid.getHeight ();
    }

    public Square getSquare (BoundedPoint2D position, Grids gridName) {
        Grid grid = gridName == Grids.CURRENT? currentGrid: finalGrid;
        return grid.getSquare (position);
    }
}
