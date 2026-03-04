package gameworld;

/**
 * Base para power-ups, cofres u otras recompensas dinámicas.
 */
public abstract class ElementoInteractivo {

    private final double x;
    private final double y;
    private boolean activo = true;

    protected ElementoInteractivo(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final boolean isActivo() {
        return this.activo;
    }

    protected final void consumir() {
        this.activo = false;
    }

    public abstract void onPlayerContact(String playerId);
}
