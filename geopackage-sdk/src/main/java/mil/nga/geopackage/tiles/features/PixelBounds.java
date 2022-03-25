package mil.nga.geopackage.tiles.features;

/**
 * Expanded pixel bounds from a point or location.
 * Stored in directional left, up, right, and down pixels
 *
 * @author osbornb
 * @since 6.3.0
 */
public class PixelBounds {

    /**
     * Pixels left of the location
     */
    private double left;

    /**
     * Pixels up from the location
     */
    private double up;

    /**
     * Pixels right of the location
     */
    private double right;

    /**
     * Pixels down from the location
     */
    private double down;

    /**
     * Empty constructor
     */
    public PixelBounds() {

    }

    /**
     * Constructor
     *
     * @param length length in all directions
     */
    public PixelBounds(double length) {
        this(length, length);
    }

    /**
     * Constructor
     *
     * @param width  length both left and right
     * @param height height both up and down
     */
    public PixelBounds(double width, double height) {
        this(width, height, width, height);
    }

    /**
     * Constructor
     *
     * @param left  left length
     * @param up    up length
     * @param right right length
     * @param down  down length
     */
    public PixelBounds(double left, double up, double right, double down) {
        this.left = left;
        this.up = up;
        this.right = right;
        this.down = down;
    }

    /**
     * Get the left pixels
     *
     * @return left pixels
     */
    public double getLeft() {
        return left;
    }

    /**
     * Set the left pixels
     *
     * @param left left pixels
     */
    public void setLeft(double left) {
        this.left = left;
    }

    /**
     * Expand the left pixels if greater than the current value
     *
     * @param left left pixels
     */
    public void expandLeft(double left) {
        this.left = Math.max(this.left, left);
    }

    /**
     * Get the up pixels
     *
     * @return up pixels
     */
    public double getUp() {
        return up;
    }

    /**
     * Set the up pixels
     *
     * @param up up pixels
     */
    public void setUp(double up) {
        this.up = up;
    }

    /**
     * Expand the up pixels if greater than the current value
     *
     * @param up up pixels
     */
    public void expandUp(double up) {
        this.up = Math.max(this.up, up);
    }

    /**
     * Get the right pixels
     *
     * @return right pixels
     */
    public double getRight() {
        return right;
    }

    /**
     * Set the right pixels
     *
     * @param right right pixels
     */
    public void setRight(double right) {
        this.right = right;
    }

    /**
     * Expand the right pixels if greater than the current value
     *
     * @param right right pixels
     */
    public void expandRight(double right) {
        this.right = Math.max(this.right, right);
    }

    /**
     * Get the down pixels
     *
     * @return down pixels
     */
    public double getDown() {
        return down;
    }

    /**
     * Set the down pixels
     *
     * @param down down pixels
     */
    public void setDown(double down) {
        this.down = down;
    }

    /**
     * Expand the down pixels if greater than the current value
     *
     * @param down down pixels
     */
    public void expandDown(double down) {
        this.down = Math.max(this.down, down);
    }

    /**
     * Expand the width pixels if greater than the current values
     *
     * @param width width pixels
     */
    public void expandWidth(double width) {
        expandLeft(width);
        expandRight(width);
    }

    /**
     * Expand the height pixels if greater than the current values
     *
     * @param height height pixels
     */
    public void expandHeight(double height) {
        expandUp(height);
        expandDown(height);
    }

    /**
     * Expand the length pixels in all directions
     *
     * @param length length pixels
     */
    public void expandLength(double length) {
        expandWidth(length);
        expandHeight(length);
    }

}
