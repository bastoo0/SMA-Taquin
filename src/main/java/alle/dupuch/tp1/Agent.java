package alle.dupuch.tp1;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Agent implements Runnable {
    private int id;
    public int currentX;
    public int currentY;
    private int destX;
    private int destY;
    private ReentrantLock mutex;

    private Environment environment;

    public Agent (int id) {
        this.id = id;
        this.mutex = new ReentrantLock ();
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

    public void allowMove () {
        mutex.unlock ();
    }

    public boolean finishCurrentMove () {
        return mutex.tryLock ();
    }

    private void tryMove() {
        if (!mutex.tryLock ()) return;
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
        mutex.unlock ();
    }

    public void setFinalCoords(int finalX, int finalY) {
        this.destX = finalX;
        this.destY = finalY;
    }

    public int getFinalX () {
        return destX;
    }

    public int getFinalY () {
        return destY;
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
