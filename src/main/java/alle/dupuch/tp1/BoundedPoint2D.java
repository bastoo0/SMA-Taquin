package alle.dupuch.tp1;

public class BoundedPoint2D extends Point2D {
    protected static Point2D bottomLeft;
    protected static Point2D topRight;

    public static void setBottomLeft (Point2D bottomLeft) {
        BoundedPoint2D.bottomLeft = bottomLeft;
    }
    public static void setTopRight (Point2D topRight) {
        BoundedPoint2D.topRight = topRight;
    }

    public BoundedPoint2D () {
        this (bottomLeft);
    }

    public BoundedPoint2D (int x, int y) throws ArithmeticException {
        super (x, y);
    }

    public BoundedPoint2D (Point2D point) throws ArithmeticException {
        super (point.x, point.y);
    }

    public void setX (int x) throws ArithmeticException {
        if (x < bottomLeft.x || x > topRight.x) throw new ArithmeticException ("x (" + x + ") doit être compris entre " + bottomLeft.x + " et " + topRight.x + ".");
        this.x = x;
    }

    public void setY (int y) throws ArithmeticException{
        if (y < bottomLeft.y || y > topRight.y) throw new ArithmeticException ("y (" + y + ") doit être compris entre " + bottomLeft.y + " et " + topRight.y + ".");
        this.y = y;
    }

    public BoundedPoint2D add (Point2D otherPoint) throws ArithmeticException {
        Point2D newPoint = super.add (otherPoint);
        return new BoundedPoint2D (newPoint);
    }
}