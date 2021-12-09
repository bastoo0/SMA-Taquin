import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Agent implements Runnable {
    private int id;
    public int currentX;
    public int currentY;
    private int destX;
    private int destY;

    private Environment environment;

    public Agent (int id) {
        this.id = id;
    }

    @Override
    public void run() {
        while (!isSolvedPuzzle()) {
            tryMove ();
            try {
                Thread.sleep (100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryMove() {
        List<int []> possibleMoves = environment.getPossibleMoves (currentX, currentY);
        Stream<int []> movesByDistance = possibleMoves
                .stream ()
                .sorted (Comparator.comparingInt (move -> environment.computeManhattanDistance (move [0], move [1], destX, destY)));
        Optional<int []> availableMove = movesByDistance
                .filter (move -> !environment.getSquareInGrid (move [0], move [1]).isTaken () && environment.getSquareInGrid (move [0], move [1]).tryLock ())
                .findFirst ();
        if (availableMove.isPresent ()) {
            int [] move = availableMove.get ();
            environment.setSquareInGrid (this, move [0], move [1]);
            setCoords (move [0], move [1]);
            environment.getSquareInGrid (move [0], move [1]).tryUnlock ();
        }
    }

    public void setFinalCoords(int finalX, int finalY) {
        this.destX = finalX;
        this.destY = finalY;
    }

    public void setCoords(int x, int y) {
        this.currentX = x;
        this.currentY = y;
    }

    public void setEnvironnment(Environment environment) {
        this.environment = environment;
    }

    private boolean isInRightPosition () {
        return currentX == destX && currentY == destY;
    }

    private boolean isSolvedPuzzle () {
        if (!isInRightPosition()) {
            return false;
        }
        int height = environment.getHeight();
        int width = environment.getWidth();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (!environment.getSquareInGrid(i, j).equals(environment.getSquareInFinalGrid(i, j)))
                    return false;
            }
        }
        return true;
    }

    public String toString () {
        return "" + id;
    }
}
