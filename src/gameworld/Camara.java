package gameworld;

/**
 * Cámara 2D que sigue al jugador y respeta límites del mapa.
 */
public final class Camara {

    private final double worldWidth;
    private final double worldHeight;
    private final double viewWidth;
    private final double viewHeight;

    private double x;
    private double y;

    public Camara(double worldWidth, double worldHeight, double viewWidth, double viewHeight) {
        if (worldWidth <= 0 || worldHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
            throw new IllegalArgumentException("Dimensiones de cámara/mundo inválidas");
        }
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.x = 0;
        this.y = 0;
    }

    public void seguir(double jugadorX, double jugadorY) {
        double targetX = jugadorX - (this.viewWidth / 2.0);
        double targetY = jugadorY - (this.viewHeight / 2.0);
        this.x = clamp(targetX, 0, Math.max(0, this.worldWidth - this.viewWidth));
        this.y = clamp(targetY, 0, Math.max(0, this.worldHeight - this.viewHeight));
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
