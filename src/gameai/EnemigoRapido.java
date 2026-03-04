package gameai;

import engine.world.ports.DefItemDTO;

/**
 * Enemigo liviano con mayor velocidad.
 */
public final class EnemigoRapido extends Enemigo {

    public EnemigoRapido() {
        super("EnemigoRapido", 0.8, 0.9, 1.6, 0.85);
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
                baseDefinition.speedX * 1.15,
                baseDefinition.speedY * 1.15,
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
                baseDefinition.speedX * 1.45,
                baseDefinition.speedY * 1.45,
                baseDefinition.angularSpeed,
                baseDefinition.thrust);
    }

    @Override
    public DefItemDTO perseguirJugador(DefItemDTO baseDefinition, double jugadorX, double jugadorY) {
        return this.redirigirHaciaJugador(baseDefinition, jugadorX, jugadorY, 1.85);
    }
}
