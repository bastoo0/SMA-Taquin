import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Agent implements Runnable {
    private int id;
    private int currentX;
    private int currentY;
    private int destX;
    private int destY;

    private Environment environment;

    public Agent (int id) {
        this.id = id;
    }

    @Override
    public void run() {
        // n'afficher que les 10 itérations par exemple
        while (!)
    }

    private void tryMove() {
        List<int []> possibleMoves = environment.getPossibleMoves (currentX, currentY);
        Stream<int []> movesByDistance = possibleMoves
                .stream ()
                .sorted (Comparator.comparingInt (move -> environment.computeManhattanDistance (move [0], move [1], destX, destY)));
        Optional<int []> availableMove = movesByDistance
                .filter (move -> environment.getCaseInGrid (move [0], move [1]).tryLock ())
                .findFirst ();
        if (availableMove.isPresent ()) {
            int [] move = availableMove.get ();
            setCoords (move [0], move [1]);
            environment.getCaseInGrid (move [0], move [1]).tryUnlock ();
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
        for (Square [] row: grid) {
            // ajouter un getter sur grid
            for (Square square: row) {

            }
        }
        return true;
    }

    public String toString () {
        return "" + id;
    }
}
