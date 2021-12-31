package alle.dupuch.tp1;

public class Grid {
    private Square[][] grid;

    public Grid (int width, int height) {
        grid = new Square[width][height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                grid[x][y] = new Square();
            }
        }
    }

    public Square getSquare (BoundedPoint2D position) {
        return grid [position.getX ()][position.getY ()];
    }

    public int getWidth() {
        return grid.length;
    }

    public int getHeight() {
        return grid[0].length;
    }
}