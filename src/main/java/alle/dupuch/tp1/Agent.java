package alle.dupuch.tp1;

import alle.dupuch.tp1.message.Message;
import alle.dupuch.tp1.message.MessageQueue;
import alle.dupuch.tp1.message.MessageType;
import javafx.scene.effect.Light;

import java.util.*;
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
            if (!isInFinalPosition ()) tryMoveAStar ();
            else {
                Message msg = waitMessage(MessageType.REQUEST_MOVE);
                getRequest(msg);
            }
            try {
                Thread.sleep (500);
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

    private void tryMoveAStar() {
        if (!mutexForMove.tryLock ()) return; // on essaye de verrouiller le mutex pour le déplacement (pour éviter les problèmes d'affichage)

        BoundedPoint2D startingPosition = this.currentPosition;
        LinkedList<BoundedPoint2D> openList = new LinkedList<BoundedPoint2D>();
        openList.add(startingPosition);
        LinkedList<BoundedPoint2D> closedList = new LinkedList<BoundedPoint2D>();

        HashMap<BoundedPoint2D, Integer> gScore = new HashMap<BoundedPoint2D, Integer>();
        gScore.put(startingPosition, 0);
        HashMap<BoundedPoint2D, Integer> fScore = new HashMap<BoundedPoint2D, Integer>();
        fScore.put(startingPosition, startingPosition.manhattanDistance(finalPosition));

        HashMap<BoundedPoint2D, BoundedPoint2D> cameFrom = new HashMap<BoundedPoint2D, BoundedPoint2D>();

        LinkedList<BoundedPoint2D> path = new LinkedList<BoundedPoint2D> ();

        while(!openList.isEmpty()) {

            /*
             On récupère la position avec le fScore le plus faible
             Le fScore est calculé tel que F = G + H
             Avec G = cout d'un déplacement (1 dans notre cas)
             Et H = Heuristique d'évaluation du A*: dans notre cas la distance de Manhattan avec la position finale
             */
            BoundedPoint2D bestPosition = null;
            for (BoundedPoint2D pos : openList) {
                if (bestPosition == null) {
                    bestPosition = pos;
                } else if (fScore.getOrDefault(pos, Integer.MAX_VALUE) < fScore.getOrDefault(bestPosition, Integer.MAX_VALUE)) {
                    bestPosition = pos;
                }
            }
            BoundedPoint2D currentAlgPos = bestPosition;

            // Fin si on a atteint la position finale
            if(currentAlgPos.equals(this.finalPosition)) {
                BoundedPoint2D curr = currentAlgPos;
                path.add(curr);
                while(cameFrom.containsKey(curr)) {
                    curr = cameFrom.get(curr);
                    path.addFirst(curr);
                }
                break;
            }

            // On n'évaluera plus la position actuelle
            openList.remove(currentAlgPos);
            closedList.add(currentAlgPos);

            // On vérifie tous les voisins
            for (BoundedPoint2D neighbor : environment.getNeighbours(currentAlgPos)) {
                Square square = environment.getSquare (neighbor, Grids.CURRENT);

                // Si position libre et jamais explorée
                if(!closedList.contains(neighbor)) {

                    /*
                     On ajoute les voisins avec un gScore plus élevé dans la openList.
                     On met à jour tous les scores avant de passer à la position suivante.
                     */
                    int currGScore = gScore.getOrDefault(currentAlgPos, Integer.MAX_VALUE)
                            + currentAlgPos.manhattanDistance(neighbor);

                    if (currGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {

                        cameFrom.put(neighbor, currentAlgPos); // Pour reconstituer le chemin
                        // Mise à jour des scores
                        gScore.put(neighbor, currGScore);
                        int currFScore = currGScore + neighbor.manhattanDistance(finalPosition);
                        fScore.put(neighbor, currFScore);

                        if (!openList.contains(neighbor)) {
                            openList.add(neighbor);
                        }
                    }
                }
            }
        }
        path.pop();
        for (int i = 0; i < path.size(); i++) {
            BoundedPoint2D move = path.get(i);
            Square square = environment.getSquare (move, Grids.CURRENT);
            if (!square.isTaken () && square.freezeOtherMoves()) {
                makeMove(move);
                environment.getSquare (move, Grids.CURRENT).allowMove();
            } else {
                Optional<Agent> reqAgent = square.getAgent();
                if(reqAgent != null && !reqAgent.isEmpty() && pushAgent(reqAgent.orElseThrow())) {
                    makeMove(move);
                } else i--;
            }
        }
        mutexForMove.unlock ();
    }

    private void makeMove(BoundedPoint2D move) {
        environment.setNewPositionInCurrentGrid (this, move);
        setCurrentPosition (move);
    }

    private boolean pushAgent(Agent agentToPush) {
        Message message = grabMessage(MessageType.REQUEST_MOVE);
        if(message != null) {
            sendMessage(this.getId(), message.getSender(), MessageType.RESPONSE_MOVE, true);
        }
            sendMessage(this.getId(), agentToPush.getId(), MessageType.REQUEST_MOVE, false);
            Message msg = waitMessage(MessageType.RESPONSE_MOVE);
            return getResponse(msg);
    }

    private void sendMessage(int sender, int receiver, MessageType type, boolean isSuccess) {
        MessageQueue.add(new Message(sender, receiver, type, isSuccess));
    }

    private Message grabMessage(MessageType type) {
        return MessageQueue.getNext(this, type);
    }

    private Message waitMessage(MessageType type) {
        Message message = grabMessage(type);
        while(message == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            message = grabMessage(type);
        }
        return message;
    }

    // Lorsque l'agent reçoit une request, il cherche où bouger
    private void getRequest(Message message) {
        List<BoundedPoint2D> emptySquareList = new ArrayList<>();
        List<BoundedPoint2D> takenSquareList = new ArrayList<>();

        List<BoundedPoint2D> moveList = environment.getNeighbours(currentPosition);
        for (BoundedPoint2D move : moveList) {
            Square square = environment.getSquare (move, Grids.CURRENT);
            Optional<Agent> a = square.getAgent();
            if (a == null) emptySquareList.add(move);
            else takenSquareList.add(move);
        }
        // Priorité aux cases vides (statégie de sélection aléatoire)
        // S'il n'y en a pas, on pousse un voisin
        Random r = new Random();
        boolean hasMoved = false;
        if(emptySquareList.isEmpty()) {
            BoundedPoint2D chosenMove = takenSquareList.get(r.nextInt(takenSquareList.size()));
            Square square = environment.getSquare(chosenMove, Grids.CURRENT);
            Agent agentToPush = square.getAgent().orElseThrow();
            hasMoved = pushAgent(agentToPush);
            if(hasMoved) makeMove(chosenMove);
        }
        else {
            BoundedPoint2D chosenMove = emptySquareList.get(r.nextInt(emptySquareList.size()));
            makeMove(chosenMove);
            hasMoved = true;
        }
        sendMessage(this.getId(), message.getSender(), MessageType.RESPONSE_MOVE, hasMoved);
    }

    private boolean getResponse(Message message) {
        return message.isSuccess();
    }

        public int getId () {
        return id;
    }

    public Direction getDirection(BoundedPoint2D origin, BoundedPoint2D dest) {
        int Xoff = dest.getX() - origin.getX();
        int Yoff = dest.getY() - origin.getY();

        if(Xoff == 1 && Yoff == 0) {
            return Direction.RIGHT;
        }
        if(Xoff == -1 && Yoff == 0) {
            return Direction.LEFT;
        }
        if(Xoff == 0 && Yoff == 1) {
            return Direction.UP;
        }
        if(Xoff == 0 && Yoff == -1) {
            return Direction.DOWN;
        }
        return null;
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