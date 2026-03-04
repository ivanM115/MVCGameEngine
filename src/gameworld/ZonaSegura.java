package gameworld;

/**
 * Zona rectangular donde el jugador no recibe daño.
 */
public final class ZonaSegura {

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public ZonaSegura(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width/height deben ser > 0");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double px, double py) {
        return px >= this.x
                && py >= this.y
                && px <= (this.x + this.width)
                && py <= (this.y + this.height);
    }
}
