package alle.dupuch.tp1;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Agent implements Runnable {
    private static int nextId = 0;

    private int id;
    private BoundedPoint2D currentPosition;
    private BoundedPoint2D finalPosition;
    private Environment environment;
    private ReentrantLock mutexForMove;

    public Agent () {
        this.id = nextId;
        ++nextId;
        this.mutexForMove = new ReentrantLock ();
    }

    @Override
    public void run() {
        while (!isSolvedPuzzle()) {
            if (!isInFinalPosition ()) tryMove ();
            try {
                Thread.sleep (200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void allowMove () {
        mutexForMove.unlock ();
    }

    // renvoie true si le mutex a pu être verrouillé (i.e. l'agent n'était pas en train de se déplacer)
    public boolean tryFreeze () {
        return mutexForMove.tryLock ();
    }

    private void tryMove() {
        if (!mutexForMove.tryLock ()) return; // on essaye de verrouiller le mutex pour le déplacement (pour éviter les problèmes d'affichage)
        List <BoundedPoint2D> possibleMoves = environment.getNeighbours (currentPosition);
        List <BoundedPoint2D> movesByDistance = possibleMoves
                .stream ()
                .sorted (Comparator.comparingInt (move -> move.manhattanDistance (finalPosition)))
                .collect (Collectors.toList ());
        for (BoundedPoint2D move: movesByDistance) {
            Square square = environment.getSquare (move, Grids.CURRENT);
            if (!square.isTaken () && square.freezeOtherMoves()) {
                environment.setNewPositionInCurrentGrid (this, move);
                setCurrentPosition (move);
                environment.getSquare (move, Grids.CURRENT).allowMove();
                break;
            }
        }
        mutexForMove.unlock ();
    }

    public int getId () {
        return id;
    }

    public BoundedPoint2D getCurrentPosition () {
        return currentPosition;
    }

    public BoundedPoint2D getFinalPosition () {
        return finalPosition;
    }

    public void setCurrentPosition (BoundedPoint2D position) {
        this.currentPosition = position;
    }

    public void setFinalPosition (BoundedPoint2D position) {
        this.finalPosition = position;
    }

    public void setEnvironnment(Environment environment) {
        this.environment = environment;
    }

    public boolean isInFinalPosition() {
        return currentPosition.equals (finalPosition);
    }

    private boolean isSolvedPuzzle () {
        if (!isInFinalPosition()) {
            return false;
        }
        // L'agent connaît la grille actuelle et la grille finale. Par conséquent, il compare case par case les 2 grilles pour voir si
        // le taquin a été résolu
        int height = environment.getHeight();
        int width = environment.getWidth();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                var position = new BoundedPoint2D (x, y);
                if (!environment.getSquare (position, Grids.CURRENT).equals(environment.getSquare (position, Grids.FINAL)))
                    return false;
            }
        }
        return true;
    }
}