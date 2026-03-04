package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuración de oleadas para modo supervivencia.
 */
public final class WaveRules {

    private final List<WaveDefinition> waves;

    public WaveRules(List<WaveDefinition> waves) {
        if (waves == null || waves.isEmpty()) {
            throw new IllegalArgumentException("waves no puede ser nulo o vacío");
        }
        this.waves = Collections.unmodifiableList(new ArrayList<>(waves));
    }

    public static WaveRules defaultRules() {
        List<WaveDefinition> defaultWaves = new ArrayList<>();
        defaultWaves.add(new WaveDefinition(1, 8, 0, 0, 8_000, 1.00, 1.00, 1.00));
        defaultWaves.add(new WaveDefinition(2, 10, 3, 1, 8_000, 1.10, 1.10, 1.10));
        defaultWaves.add(new WaveDefinition(3, 12, 5, 2, 7_000, 1.20, 1.15, 1.20));
        defaultWaves.add(new WaveDefinition(4, 14, 6, 3, 7_000, 1.30, 1.20, 1.25));
        defaultWaves.add(new WaveDefinition(5, 16, 8, 4, 6_000, 1.40, 1.30, 1.35));
        return new WaveRules(defaultWaves);
    }

    public int getWaveCount() {
        return this.waves.size();
    }

    public WaveDefinition getWave(int waveIndex) {
        if (waveIndex < 0 || waveIndex >= this.waves.size()) {
            throw new IllegalArgumentException("waveIndex fuera de rango: " + waveIndex);
        }
        return this.waves.get(waveIndex);
    }

    /**
     * DTO inmutable de composición de oleada.
     */
    public static final class WaveDefinition {
        public final int waveNumber;
        public final int basicEnemies;
        public final int fastEnemies;
        public final int tankEnemies;
        public final long restTimeMillis;
        public final double lifeMultiplier;
        public final double damageMultiplier;
        public final double speedMultiplier;

        public WaveDefinition(
                int waveNumber,
                int basicEnemies,
                int fastEnemies,
                int tankEnemies,
                long restTimeMillis,
                double lifeMultiplier,
                double damageMultiplier,
                double speedMultiplier) {

            if (waveNumber <= 0) {
                throw new IllegalArgumentException("waveNumber debe ser > 0");
            }
            if (basicEnemies < 0 || fastEnemies < 0 || tankEnemies < 0) {
                throw new IllegalArgumentException("La cantidad de enemigos no puede ser negativa");
            }
            if (restTimeMillis < 0) {
                throw new IllegalArgumentException("restTimeMillis no puede ser negativo");
            }
            if (lifeMultiplier <= 0 || damageMultiplier <= 0 || speedMultiplier <= 0) {
                throw new IllegalArgumentException("Los multiplicadores deben ser > 0");
            }

            this.waveNumber = waveNumber;
            this.basicEnemies = basicEnemies;
            this.fastEnemies = fastEnemies;
            this.tankEnemies = tankEnemies;
            this.restTimeMillis = restTimeMillis;
            this.lifeMultiplier = lifeMultiplier;
            this.damageMultiplier = damageMultiplier;
            this.speedMultiplier = speedMultiplier;
        }

        public int totalEnemies() {
            return this.basicEnemies + this.fastEnemies + this.tankEnemies;
        }
    }
}
