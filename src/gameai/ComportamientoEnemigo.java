package gameai;

import engine.world.ports.DefItemDTO;

/**
 * Define estrategias de movimiento y combate para enemigos.
 */
public interface ComportamientoEnemigo {

    /**
     * Ajusta velocidad para patrulla.
     */
    DefItemDTO patrullar(DefItemDTO baseDefinition);

    /**
     * Ajusta velocidad para ataque directo.
     */
    DefItemDTO atacar(DefItemDTO baseDefinition);

    /**
     * Ajusta velocidad para perseguir al jugador.
     */
    DefItemDTO perseguirJugador(DefItemDTO baseDefinition, double jugadorX, double jugadorY);
}
