package gameworld;

import java.util.ArrayList;
import java.util.List;

import engine.utils.helpers.DoubleVector;
import gamelevel.Mapa;

/**
 * Mundo finito y estático del modo supervivencia.
 */
public final class World {

    private final DoubleVector dimension;
    private final Mapa mapa;
    private final Camara camara;
    private final ZonaSegura zonaSegura;
    private final ArrayList<ElementoInteractivo> elementosInteractivos = new ArrayList<>();
    private boolean nightMode;

    public World(DoubleVector dimension, DoubleVector viewDimension, Mapa mapa, ZonaSegura zonaSegura) {
        if (dimension == null || viewDimension == null || mapa == null || zonaSegura == null) {
            throw new IllegalArgumentException("Parámetros de mundo no pueden ser nulos");
        }
        this.dimension = dimension;
        this.mapa = mapa;
        this.zonaSegura = zonaSegura;
        this.camara = new Camara(dimension.x, dimension.y, viewDimension.x, viewDimension.y);
        this.nightMode = false;
    }

    public DoubleVector getDimension() {
        return this.dimension;
    }

    public Mapa getMapa() {
        return this.mapa;
    }

    public Camara getCamara() {
        return this.camara;
    }

    public boolean isNightMode() {
        return this.nightMode;
    }

    public boolean isInSafeZone(double x, double y) {
        return this.zonaSegura.contains(x, y);
    }

    public void addElementoInteractivo(ElementoInteractivo elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("elemento no puede ser nulo");
        }
        this.elementosInteractivos.add(elemento);
    }

    public List<ElementoInteractivo> getElementosInteractivos() {
        return List.copyOf(this.elementosInteractivos);
    }

    /**
     * Alterna estado visual para representar progresión de oleadas.
     */
    public void updateWaveVisuals(int waveNumber) {
        this.nightMode = (waveNumber % 2 == 0);
    }
}
