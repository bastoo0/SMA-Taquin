package alle.dupuch.tp1;

import java.util.Objects;

import static java.lang.Math.abs;

public class Point2D {
    protected int x; // largeur (toujours)
    protected int y; // hauteur (toujours)

    public Point2D () {
        this (0, 0);
    }

    public Point2D (int x, int y) {
        setX (x);
        setY (y);
    }

    public Point2D (Point2D point) {
        this (point.x, point.y);
    }

    public int getX () {
        return x;
    }

    public int getY () {
        return y;
    }

    public void setX (int x) {
        this.x = x;
    }

    public void setY (int y) {
        this.y = y;
    }

    public Point2D add (Point2D otherPoint) {
        Point2D newPoint = new Point2D (this);
        newPoint.setX (x + otherPoint.x);
        newPoint.setY (y + otherPoint.y);
        return newPoint;
    }

    public int manhattanDistance (Point2D otherPoint) {
        return abs (otherPoint.x - x) + abs (otherPoint.y - y);
    }

    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point2D that = (Point2D) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString () {
        return "(" + x + ", " + y + ")";
    }
}