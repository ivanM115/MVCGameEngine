package gamelevel;

import java.awt.Point;
import java.util.ArrayList;

import engine.controller.ports.WorldManager;
import engine.generators.AbstractLevelGenerator;
import engine.world.ports.DefEmitterDTO;
import engine.world.ports.DefItem;
import engine.world.ports.DefItemDTO;
import engine.world.ports.WorldDefinition;

/**
 * Nivel de supervivencia con mapa cerrado.
 */
public final class Level extends AbstractLevelGenerator {

    private final Mapa mapa;

    public Level(WorldManager worldManager, WorldDefinition worldDef, Mapa mapa) {
        super(worldManager, worldDef);
        if (mapa == null) {
            throw new IllegalArgumentException("mapa no puede ser nulo");
        }
        this.mapa = mapa;
        this.generateWorld();
    }

    @Override
    protected void createDecorators() {
        ArrayList<DefItem> decorators = this.getWorldDefinition().spaceDecorators;
        for (DefItem def : decorators) {
            DefItemDTO deco = this.defItemToDTO(def);
            this.addDecoratorIntoTheGame(deco);
        }
    }

    @Override
    protected void createDynamics() {
    }

    @Override
    protected void createStatics() {
        ArrayList<DefItem> bodyDefs = this.getWorldDefinition().gravityBodies;
        for (DefItem def : bodyDefs) {
            DefItemDTO body = this.defItemToDTO(def);
            this.addStaticIntoTheGame(body);
        }
    }

    @Override
    protected void createPlayers() {
        WorldDefinition worldDef = this.getWorldDefinition();
        ArrayList<DefItem> shipDefs = worldDef.spaceships;
        ArrayList<DefEmitterDTO> weaponDefs = worldDef.weapons;
        ArrayList<DefEmitterDTO> trailDefs = worldDef.trailEmitters;

        Point safePoint = this.mapa.getSafeZones().isEmpty() ? null : this.mapa.getSafeZones().getFirst();

        for (DefItem def : shipDefs) {
            DefItemDTO body = this.defItemToDTO(def);

            DefItemDTO playerInSafeZone = safePoint == null
                    ? body
                    : new DefItemDTO(
                            body.assetId,
                            body.size,
                            body.angle,
                            safePoint.x,
                            safePoint.y,
                            body.density,
                            body.speedX,
                            body.speedY,
                            body.angularSpeed,
                            body.thrust);

            this.addLocalPlayerIntoTheGame(playerInSafeZone, weaponDefs, trailDefs);
        }
    }
}
