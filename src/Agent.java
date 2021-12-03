public class Agent implements Runnable {
    private int currentX;
    private int currentY;
    private int destX;
    private int destY;
    private String color;

    private Environment environment;

    @Override
    public void run() {

    }

    private void move() {

    }



    public void setFinalCoords(int finalX, int finalY) {
        this.destX = finalX;
        this.destY = finalY;
    }

    public void setCoords(int x, int y) {
        this.currentX = x;
        this.currentY = y;
    }

    public void setEnvironnment(Environment environment) {
        this.environment = environment;
    }

    public void setColor(String s) {
        this.color = s;
    }
}
