import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Environment {
    private Square[][] grid;
    private Square[][] finalGrid;
    private Agent[] agentList;

    public Environment(int height, int width, int agentCount) {
        this.grid = new Square[height][width];
        agentList = new Agent[agentCount];
        String[] colorList = new String[]{"RED", "BLUE", "PINK", "PURPLE", "YELLOW", "GREEN", "ORANGE"};

        Random r = new Random();
        int x = 0;
        int y = 0;
        int finalX = 0;
        int finalY = 0;
        for(int i = 0; i < agentCount; i++) {
            // Permutations
            do {
                x = r.nextInt(width);
                y = r.nextInt(height);
            }
            while(grid[x][y].isTaken());
            do {
                finalX = r.nextInt(width);
                finalY = r.nextInt(height);
            }
            while(finalGrid[x][y].isTaken());

            Agent a = new Agent (i);
            agentList[i] = a;
            a.setCoords(x, y);
            a.setFinalCoords(finalX, finalY);
            a.setEnvironnment(this);
            grid[x][y].setAgent(a);
            finalGrid[finalX][finalY].setAgent(a);
        }
    }

    public boolean isPossibleMove(int x, int y) {
        int height = grid.length;
        int width = grid[0].length;
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public List<int[]> getPossibleMoves(int x, int y) {
        List <int []> allMoves = List.of (
                new int [] {x - 1, y - 1},
                new int [] {x - 1, y + 1},
                new int [] {x + 1, y - 1},
                new int [] {x + 1, y + 1}
        );
        List <int []> possibleMoves = allMoves
                .stream ()
                .filter (move -> isPossibleMove (move [0], move [1]))
                .collect (Collectors.toList ());
        return possibleMoves;
    }

    public int computeManhattanDistance (int case1_x, int case1_y, int case2_x, int case2_y) {
        int diffX = Math.abs (case2_x - case1_x);
        int diffY = Math.abs (case2_y - case1_y);
        return diffX + diffY;
    }

    public Agent[] getAgentList() {
        return agentList;
    }

    public Square getCaseInGrid (int x, int y) {
        return grid [x][y];
    }

    public Square getCaseInFinalGrid (int x, int y) {
        return finalGrid [x][y];
    }

    public String toString () {
        return Arrays.deepToString (grid);
    }
}
