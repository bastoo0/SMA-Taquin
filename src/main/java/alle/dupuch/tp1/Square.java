package alle.dupuch.tp1;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class Square {
    private Optional <Agent> agent;
    private ReentrantLock mutexForMove;

    public Square(){
        agent = Optional.empty ();
        mutexForMove = new ReentrantLock ();
    }

    public boolean isTaken() {
        return agent.isPresent();
    }

    public void setAgent (Agent agent) {
        this.agent = Optional.ofNullable (agent);
    }

    public void allowMove () {
        mutexForMove.unlock();
    }

    public boolean isLocked () {
        return mutexForMove.isLocked();
    }

    public Optional<Agent> getAgent() {
        return this.agent;
    }

    // renvoie False si un autre agent essaye de se déplacer sur cette case en même temps
    public boolean freezeOtherMoves() {
        return mutexForMove.tryLock();
    }

    // Pour la comparaison de la grille actuelle avec la grille finale
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Square square = (Square) o;
        return agent.equals(square.agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash (agent);
    }
}
