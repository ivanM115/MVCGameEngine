package gameai;

import engine.world.ports.DefItemDTO;

/**
 * Enemigo estándar balanceado.
 */
public final class EnemigoBasico extends Enemigo {

    public EnemigoBasico() {
        super("EnemigoBasico", 1.0, 1.0, 1.0, 1.0);
    }

    @Override
    public DefItemDTO patrullar(DefItemDTO baseDefinition) {
        return baseDefinition;
    }

    @Override
    public DefItemDTO atacar(DefItemDTO baseDefinition) {
        return new DefItemDTO(
                baseDefinition.assetId,
                baseDefinition.size,
                baseDefinition.angle,
                baseDefinition.posX,
                baseDefinition.posY,
                baseDefinition.density,
                baseDefinition.speedX * 1.1,
                baseDefinition.speedY * 1.1,
                baseDefinition.angularSpeed,
                baseDefinition.thrust);
    }

    @Override
    public DefItemDTO perseguirJugador(DefItemDTO baseDefinition, double jugadorX, double jugadorY) {
        return this.redirigirHaciaJugador(baseDefinition, jugadorX, jugadorY, 1.25);
    }
}
