package alle.dupuch.tp1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Environment {
    private Grid currentGrid;
    private Grid finalGrid;
    private List <Agent> agentList;

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
