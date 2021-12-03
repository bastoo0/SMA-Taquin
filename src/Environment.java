import java.util.List;
import java.util.Random;

public class Environment {
    private Square[][] grid;
    private Square[][] finalGrid;
    private Agent[] agentList;

    public Environment(int height, int width, int agentCount) {
        this.grid = new Square[height][width];
        agentList = new Agent[agentCount];
        String[] colorList = new String[]{"RED", "BLUE", "PINK", "PURPLE", "YELLOW", "GREEN", "ORANGE"};

        Random r = new Random();
        for(int i = 0; i < agentCount; i++) {
            int x = 0;
            int y = 0;
            int finalX = 0;
            int finalY = 0;
            do {
                x = r.nextInt(width);
                y = r.nextInt(height);
            }
            while(grid[x][y].isTaken());
            do {
                x = r.nextInt(width);
                y = r.nextInt(height);
            }
            while(finalGrid[x][y].isTaken());

            Agent a = new Agent();
            agentList[i] = a;
            a.setColor(colorList[i]);
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
        return null;
    }

    public Agent[] getAgentList() {
        return agentList;
    }

    public Square[][] getGrid() {
        return grid;
    }

    public Square[][] getFinalGrid() {
        return finalGrid;
    }
}
