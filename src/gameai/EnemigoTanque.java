package gameai;

import engine.world.ports.DefItemDTO;

/**
 * Enemigo resistente y lento.
 */
public final class EnemigoTanque extends Enemigo {

    public EnemigoTanque() {
        super("EnemigoTanque", 2.2, 1.6, 0.6, 1.4);
    }

    @Override
    public DefItemDTO patrullar(DefItemDTO baseDefinition) {
        return new DefItemDTO(
                baseDefinition.assetId,
                baseDefinition.size,
                baseDefinition.angle,
                baseDefinition.posX,
                baseDefinition.posY,
                baseDefinition.density,
                baseDefinition.speedX * 0.7,
                baseDefinition.speedY * 0.7,
                baseDefinition.angularSpeed,
                baseDefinition.thrust);
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
                baseDefinition.speedX * 0.8,
                baseDefinition.speedY * 0.8,
                baseDefinition.angularSpeed,
                baseDefinition.thrust);
    }

    @Override
    public DefItemDTO perseguirJugador(DefItemDTO baseDefinition, double jugadorX, double jugadorY) {
        return this.redirigirHaciaJugador(baseDefinition, jugadorX, jugadorY, 0.95);
    }
}
