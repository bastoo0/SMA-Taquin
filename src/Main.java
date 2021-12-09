import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        var env = new Environment (5, 5, 5);
        var t = new Thread (env);
        t.start ();
        Stream.of (env.getAgentList())
                .forEach (agent -> {
                    (new Thread (agent)).start ();
                });
    }
}
