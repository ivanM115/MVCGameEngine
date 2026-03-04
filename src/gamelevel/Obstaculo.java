package gamelevel;

import engine.world.ports.DefItemDTO;

/**
 * Obstáculo estático de escenario.
 */
public final class Obstaculo {

    private final String assetId;
    private final double size;
    private final double posX;
    private final double posY;
    private final double angle;

    public Obstaculo(String assetId, double size, double posX, double posY, double angle) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId no puede ser nulo o vacío");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size debe ser > 0");
        }
        this.assetId = assetId;
        this.size = size;
        this.posX = posX;
        this.posY = posY;
        this.angle = angle;
    }

    public DefItemDTO toDefinition() {
        return new DefItemDTO(this.assetId, this.size, this.angle, this.posX, this.posY, 100.0);
    }
}
