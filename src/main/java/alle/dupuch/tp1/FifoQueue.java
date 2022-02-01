package alle.dupuch.tp1;

import java.util.LinkedList;

public class FifoQueue <E> extends LinkedList<E> {
    private int limit;

    public FifoQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }
}
