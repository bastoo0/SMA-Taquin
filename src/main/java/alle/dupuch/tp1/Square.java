package alle.dupuch.tp1;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class Square {
    private Optional<Agent> agent;

    private ReentrantLock mutex;

    public Square(){
        agent = Optional.empty();
        mutex = new ReentrantLock();
    }

    public void setAgent(Agent agent) {
        this.agent = Optional.ofNullable(agent);
    }

    public boolean isTaken() {
        return agent.isPresent();
    }

    public boolean tryLock() {
        return mutex.tryLock();
    }

    public boolean tryUnlock() {
        try {
            mutex.unlock();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String toString () {
        return agent.isPresent()? agent.get ().toString (): "#";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Square square = (Square) o;
        return agent.equals(square.agent);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
