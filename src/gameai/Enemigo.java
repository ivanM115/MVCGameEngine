package gameai;

import java.awt.Point;
import java.util.Objects;

import engine.world.ports.DefItemDTO;

/**
 * Clase base abstracta para enemigos del modo supervivencia.
 */
public abstract class Enemigo implements ComportamientoEnemigo {

    private final String nombre;
    private final double multiplicadorVida;
    private final double multiplicadorDanio;
    private final double multiplicadorVelocidad;
    private final double multiplicadorTamano;

    protected Enemigo(
            String nombre,
            double multiplicadorVida,
            double multiplicadorDanio,
            double multiplicadorVelocidad,
            double multiplicadorTamano) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre no puede ser nulo o vacío");
        }
        if (multiplicadorVida <= 0 || multiplicadorDanio <= 0
                || multiplicadorVelocidad <= 0 || multiplicadorTamano <= 0) {
            throw new IllegalArgumentException("Los multiplicadores deben ser > 0");
        }

        this.nombre = nombre;
        this.multiplicadorVida = multiplicadorVida;
        this.multiplicadorDanio = multiplicadorDanio;
        this.multiplicadorVelocidad = multiplicadorVelocidad;
        this.multiplicadorTamano = multiplicadorTamano;
    }

    public final String getNombre() {
        return this.nombre;
    }

    public final double getMultiplicadorVida() {
        return this.multiplicadorVida;
    }

    public final double getMultiplicadorDanio() {
        return this.multiplicadorDanio;
    }

    public final double getMultiplicadorVelocidad() {
        return this.multiplicadorVelocidad;
    }

    public final DefItemDTO crearSpawn(
            DefItemDTO baseDefinition,
            Point spawnPoint,
            double waveDifficultyMultiplier,
            ComportamientoModo modo,
            double jugadorX,
            double jugadorY) {

        Objects.requireNonNull(baseDefinition, "baseDefinition no puede ser nulo");
        Objects.requireNonNull(spawnPoint, "spawnPoint no puede ser nulo");
        Objects.requireNonNull(modo, "modo no puede ser nulo");

        double speedX = baseDefinition.speedX * this.multiplicadorVelocidad * waveDifficultyMultiplier;
        double speedY = baseDefinition.speedY * this.multiplicadorVelocidad * waveDifficultyMultiplier;

        if (Math.abs(speedX) < 0.01 && Math.abs(speedY) < 0.01) {
            speedX = 40.0 * this.multiplicadorVelocidad;
            speedY = 20.0 * this.multiplicadorVelocidad;
        }

        DefItemDTO candidate = new DefItemDTO(
                baseDefinition.assetId,
                Math.max(8.0, baseDefinition.size * this.multiplicadorTamano),
                baseDefinition.angle,
                spawnPoint.x,
                spawnPoint.y,
                baseDefinition.density,
                speedX,
                speedY,
                baseDefinition.angularSpeed,
                baseDefinition.thrust);

        return switch (modo) {
            case PATRULLAR -> this.patrullar(candidate);
            case ATACAR -> this.atacar(candidate);
            case PERSEGUIR -> this.perseguirJugador(candidate, jugadorX, jugadorY);
        };
    }

    protected final DefItemDTO redirigirHaciaJugador(
            DefItemDTO baseDefinition,
            double jugadorX,
            double jugadorY,
            double speedFactor) {

        double dx = jugadorX - baseDefinition.posX;
        double dy = jugadorY - baseDefinition.posY;
        double length = Math.hypot(dx, dy);

        if (length < 0.0001) {
            return baseDefinition;
        }

        double unitX = dx / length;
        double unitY = dy / length;
        double baseSpeed = Math.hypot(baseDefinition.speedX, baseDefinition.speedY);
        if (baseSpeed < 1.0) {
            baseSpeed = 120.0;
        }

        double targetSpeed = baseSpeed * Math.max(0.2, speedFactor);

        return new DefItemDTO(
                baseDefinition.assetId,
                baseDefinition.size,
                baseDefinition.angle,
                baseDefinition.posX,
                baseDefinition.posY,
                baseDefinition.density,
                unitX * targetSpeed,
                unitY * targetSpeed,
                baseDefinition.angularSpeed,
                baseDefinition.thrust);
    }

    public enum ComportamientoModo {
        PATRULLAR,
        ATACAR,
        PERSEGUIR
    }
}
