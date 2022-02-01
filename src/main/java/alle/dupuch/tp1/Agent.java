package alle.dupuch.tp1;

import alle.dupuch.tp1.message.Message;
import alle.dupuch.tp1.message.MessageQueue;
import alle.dupuch.tp1.message.MessageType;

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
    private double agentPower;
    private FifoQueue<BoundedPoint2D> memory;
    private BoundedPoint2D moveToDo;
    private int attemptsToDoMove;

    public Agent () {
        this.id = nextId;
        ++nextId;
        this.mutexForMove = new ReentrantLock ();
        this.memory = new FifoQueue<>(3);
        this.attemptsToDoMove = 0;
    }

    @Override
    public void run() {
        while (!isSolvedPuzzle()) {
            setAgentPower();
            Message msg = grabMessage(MessageType.REQUEST_MOVE);
            if(msg != null)
                handleWithPriority(msg);
            if (!isInFinalPosition ()) {
                tryMoveAStar ();
            }
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

    public void setAgentPower() {
        agentPower = environment.getPriorityStrategySpiral(this);
    }

    public double getAgentPower() {
        return agentPower;
    }

    private LinkedList<BoundedPoint2D> getAStarPath() {

        // Si on a un mouvement à faire (après un push), le mouvement est prioritaire
        if(attemptsToDoMove > 0 && moveToDo != null) {
            attemptsToDoMove--;
            LinkedList<BoundedPoint2D> path = new LinkedList<> ();
            path.add(moveToDo);
            return path;
        }

        BoundedPoint2D startingPosition = this.currentPosition;
        LinkedList<BoundedPoint2D> openList = new LinkedList<>();
        openList.add(startingPosition);
        LinkedList<BoundedPoint2D> closedList = new LinkedList<>();

        HashMap<BoundedPoint2D, Integer> gScore = new HashMap<>();
        gScore.put(startingPosition, 0);
        HashMap<BoundedPoint2D, Integer> fScore = new HashMap<>();
        fScore.put(startingPosition, startingPosition.manhattanDistance(finalPosition));

        HashMap<BoundedPoint2D, BoundedPoint2D> cameFrom = new HashMap<>();

        LinkedList<BoundedPoint2D> path = new LinkedList<> ();

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
                        if(square.freezeOtherMoves()) {
                            if (square.isTaken()) // On évite les cases avec des agents
                                currFScore += 5;
                            if (square.getAgent().isPresent() && square.getAgent().orElseThrow().isInFinalPosition())
                                currFScore += 5;
                            square.allowMove();
                        }
                        else {
                            currFScore += 5;
                        }
                        fScore.put(neighbor, currFScore);

                        if (!openList.contains(neighbor)) {
                            openList.add(neighbor);
                        }
                    }
                }
            }
        }
        path.pop();
        return path;
    }

    private void tryMoveAStar() {
        if (!mutexForMove.tryLock ()) return; // on essaye de verrouiller le mutex pour le déplacement (pour éviter les problèmes d'affichage)
        BoundedPoint2D move = null;
        if(Math.random() > 0.1) {
            LinkedList<BoundedPoint2D> path = getAStarPath();
            move = path.get(0);
            if(this.memory.contains(move)) {
                memory.add(null);
                mutexForMove.unlock();
                return;
            }
        }
        else { // 10% de chance de choisir un mouvement aléatoire (pour débloquer)
            Random r = new Random();
            List<BoundedPoint2D> moveList = environment.getNeighbours(currentPosition);
            // On vérifie qu'un autre agent ne veuille pas aller sur la case
            // Pour éviter les problèmes de thread
            while(move == null && !moveList.isEmpty()) {
                BoundedPoint2D moveToTry = moveList.get(r.nextInt(moveList.size()));
                Square square = environment.getSquare(moveToTry, Grids.CURRENT);
                if(square.freezeOtherMoves()) {
                    move = moveToTry;
                    square.allowMove();
                }
                else moveList.remove(moveToTry);
            }
            if(move == null) {
                mutexForMove.unlock();
                return;
            }
        }

        Square square = environment.getSquare (move, Grids.CURRENT);
        if(square.freezeOtherMoves()) { // On bloque la case, si on ne peut pas bloquer, on passe son tour
            if (!square.isTaken()) {
                makeMove(move);
            } else {
                Optional<Agent> reqAgent = square.getAgent();
                if (reqAgent.isPresent()) {
                    pushAgent(reqAgent.orElseThrow());
                }
            }
            square.allowMove();
        }
        mutexForMove.unlock ();
    }

    private void makeMove(BoundedPoint2D move) {
        this.moveToDo = null;
        environment.setNewPositionInCurrentGrid (this, move);
        setCurrentPosition (move);
    }

    private void pushAgent(Agent agentToPush) {
        Message message = grabMessage(MessageType.REQUEST_MOVE);
        if(message == null) {
            sendMessage(this.getId(), agentToPush.getId(), MessageType.REQUEST_MOVE, false);
        }
        else {
            handleWithPriority(message);
        }
    }

    private void sendMessage(int sender, int receiver, MessageType type, boolean isSuccess) {
        MessageQueue.add(new Message(sender, receiver, type, isSuccess));
    }

    private Message grabMessage(MessageType type) {
        return MessageQueue.getNext(this, type);
    }

    public void handleWithPriority(Message message) {
        if (!mutexForMove.tryLock ()) return; // on essaye de verrouiller le mutex pour le déplacement (pour éviter les problèmes d'affichage)
        System.out.println(id + " is handling message from " + message.getSender());
        // On crée une liste de priorité en spirale, avec une priorité absolue sur les cases vides par rapport aux cases occupées
        // On évitera également de pousser un agent qui a une priorité plus élevée que la notre et on priorise les cases à plus faibles priorité
        Queue<BoundedPoint2D> prioQueue = new PriorityQueue<>(
                4,
                Comparator.comparingInt(pos -> (environment.getSquare((BoundedPoint2D)pos, Grids.CURRENT).getAgent().isEmpty() ? 26 : 0)
                        + (environment.getSpiralPriority((BoundedPoint2D) pos) > this.agentPower ? 0 : environment.getSpiralPriority((BoundedPoint2D) pos)))
                        .reversed()
        );
        List<Square> lockedSquareList = new ArrayList<>();

        // On regarde les mouvements que l'on peut faire ou non
        List<BoundedPoint2D> moveList = environment.getNeighbours(currentPosition);
        for (BoundedPoint2D move : moveList) {
            Square square = environment.getSquare (move, Grids.CURRENT);
            Optional<Agent> a = square.getAgent();
            if(square.freezeOtherMoves()) lockedSquareList.add(square);
            // Si la case est vide ou a un agent qui n'est pas l'agent qui push, on ajoute à la file
            if (a.isEmpty() || a.orElseThrow().getId() != message.getSender()) prioQueue.add(move);
        }
        BoundedPoint2D move = prioQueue.poll();
        Square square = environment.getSquare (move, Grids.CURRENT);
        Optional<Agent> agent = square.getAgent();
        if (square.isTaken() || agent.isPresent()) { // On pousse l'agent si présent
            System.out.println(id + " pushes " + agent.orElseThrow().getId());
            pushAgent(agent.orElseThrow());
            this.attemptsToDoMove = 7; // On va essayer 7 fois d'aller sur la case après le push
            this.moveToDo = move;
        } else {
            System.out.println(id + " is moving");
            this.memory.add(currentPosition); // On ne reviendra pas sur la case pendant un certain temps
            makeMove(move);
        }

        for (Square lockedSquare : lockedSquareList) {
            lockedSquare.allowMove();
        }
        mutexForMove.unlock();
    }

    private boolean getResponse(Message message) {
        return message.isSuccess();
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