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
}
