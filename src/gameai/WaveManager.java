package gameai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import engine.controller.impl.Controller;
import engine.generators.AbstractIAGenerator;
import engine.generators.DefItemMaterializer;
import engine.model.bodies.ports.BodyData;
import engine.model.bodies.ports.BodyType;
import engine.utils.helpers.DoubleVector;
import engine.world.ports.DefItem;
import engine.world.ports.DefItemDTO;
import engine.world.ports.WorldDefinition;
import gamerules.GameRules;
import gamerules.WaveRules;

/**
 * Gestiona oleadas de enemigos con dificultad progresiva.
 */
public final class WaveManager extends AbstractIAGenerator {

    public enum SurvivalState {
        PREPARING,
        WAVE_ACTIVE,
        WAVE_CLEARED,
        GAME_OVER,
        VICTORY
    }

    private final Controller controller;
    private final GameRules gameRules;
    private final WaveRules waveRules;
    private final SpawnPattern spawnPattern;
    private final DefItemMaterializer materializer = new DefItemMaterializer();
    private final Random random = new Random();

    private final Enemigo enemigoBasico = new EnemigoBasico();
    private final Enemigo enemigoRapido = new EnemigoRapido();
    private final Enemigo enemigoTanque = new EnemigoTanque();

    private volatile SurvivalState state = SurvivalState.PREPARING;
    private volatile int currentWaveIndex = -1;
    private volatile int spawnedInWave = 0;
    private volatile int aliveEstimated = 0;
    private volatile long nextWaveStartMillis = 0;
    private volatile long lastSpawnMillis = 0;
    private volatile int lastDeadCount = 0;

    public WaveManager(
            Controller controller,
            WorldDefinition worldDefinition,
            WaveRules waveRules,
            GameRules gameRules,
            SpawnPattern spawnPattern,
            int maxCreationDelay) {

        super(controller, worldDefinition, maxCreationDelay);
        if (controller == null) {
            throw new IllegalArgumentException("controller no puede ser nulo");
        }
        if (waveRules == null) {
            throw new IllegalArgumentException("waveRules no puede ser nulo");
        }
        if (gameRules == null) {
            throw new IllegalArgumentException("gameRules no puede ser nulo");
        }
        if (spawnPattern == null) {
            throw new IllegalArgumentException("spawnPattern no puede ser nulo");
        }
        this.controller = controller;
        this.waveRules = waveRules;
        this.gameRules = gameRules;
        this.spawnPattern = spawnPattern;
    }

    @Override
    protected String getThreadName() {
        return "WaveManager";
    }

    @Override
    protected void onActivate() {
        this.lastDeadCount = this.controller.getEntityDeadQuantity();
        this.nextWaveStartMillis = System.currentTimeMillis() + 3_000;
        this.state = SurvivalState.PREPARING;
    }

    @Override
    protected void onTick() {
        if (this.gameRules.isGameOver()) {
            this.state = SurvivalState.GAME_OVER;
            return;
        }
        if (this.gameRules.isVictory()) {
            this.state = SurvivalState.VICTORY;
            return;
        }

        this.updateAliveEstimated();
        this.applyContinuousHoming();
        long now = System.currentTimeMillis();

        if (this.state == SurvivalState.PREPARING || this.state == SurvivalState.WAVE_CLEARED) {
            if (now >= this.nextWaveStartMillis) {
                this.startNextWave();
            }
            return;
        }

        if (this.state != SurvivalState.WAVE_ACTIVE) {
            return;
        }

        WaveRules.WaveDefinition wave = this.waveRules.getWave(this.currentWaveIndex);
        int targetEnemiesInWave = this.effectiveTotalEnemies(wave);

        if (this.spawnedInWave < targetEnemiesInWave) {
            if (now - this.lastSpawnMillis >= 120) {
                this.spawnEnemy(wave);
                this.lastSpawnMillis = now;
            }
            return;
        }

        if (this.aliveEstimated <= 0) {
            this.state = SurvivalState.WAVE_CLEARED;
            this.gameRules.onWaveCleared(wave.waveNumber);
            if (this.currentWaveIndex >= this.waveRules.getWaveCount() - 1) {
                this.state = SurvivalState.VICTORY;
                return;
            }
            this.nextWaveStartMillis = now + wave.restTimeMillis;
        }
    }

    public SurvivalState getState() {
        return this.state;
    }

    public int getCurrentWaveNumber() {
        return Math.max(0, this.currentWaveIndex + 1);
    }

    public int getSpawnedInWave() {
        return this.spawnedInWave;
    }

    public int getAliveEstimated() {
        return this.aliveEstimated;
    }

    public long getMillisToNextWave() {
        long diff = this.nextWaveStartMillis - System.currentTimeMillis();
        return Math.max(diff, 0);
    }

    private void startNextWave() {
        this.currentWaveIndex++;
        if (this.currentWaveIndex >= this.waveRules.getWaveCount()) {
            this.state = SurvivalState.VICTORY;
            return;
        }

        this.spawnedInWave = 0;
        this.aliveEstimated = 0;
        this.lastSpawnMillis = 0;
        this.state = SurvivalState.WAVE_ACTIVE;
    }

    private void spawnEnemy(WaveRules.WaveDefinition wave) {
        Enemigo enemyType = this.selectEnemyType(wave);
        DefItem baseDef = this.worldDefinition.asteroids.get(
                this.random.nextInt(this.worldDefinition.asteroids.size()));
        DefItemDTO baseDto = this.materializer.defItemToDTO(baseDef);

        Point spawnPoint = this.resolveSpawnPoint(this.spawnPattern);
        DoubleVector playerPos = this.controller.getLocalPlayerPosition();
        double targetX = playerPos == null ? this.worldDefinition.worldWidth * 0.5 : playerPos.x;
        double targetY = playerPos == null ? this.worldDefinition.worldHeight * 0.5 : playerPos.y;

        DefItemDTO enemyDto = enemyType.crearSpawn(
                baseDto,
                spawnPoint,
                wave.speedMultiplier,
            Enemigo.ComportamientoModo.PERSEGUIR,
            targetX,
            targetY);

        this.addDynamicIntoTheGame(enemyDto);
        this.spawnedInWave++;
        this.aliveEstimated++;
    }

    private Enemigo selectEnemyType(WaveRules.WaveDefinition wave) {
        int total = this.effectiveTotalEnemies(wave);
        int basicLimit = Math.min(total, wave.basicEnemies + Math.max(0, this.currentWaveIndex * 2));
        int fastLimit = Math.min(total, basicLimit + wave.fastEnemies + Math.max(0, this.currentWaveIndex));

        if (this.spawnedInWave < basicLimit) {
            return this.enemigoBasico;
        }
        if (this.spawnedInWave < fastLimit) {
            return this.enemigoRapido;
        }
        return this.enemigoTanque;
    }

    private int effectiveTotalEnemies(WaveRules.WaveDefinition wave) {
        int growthPerWave = 3;
        int extra = Math.max(0, this.currentWaveIndex * growthPerWave);
        return wave.totalEnemies() + extra;
    }

    private Point resolveSpawnPoint(SpawnPattern pattern) {
        double worldW = this.worldDefinition.worldWidth;
        double worldH = this.worldDefinition.worldHeight;

        return switch (pattern) {
            case CIRCULAR -> this.circularPoint(worldW, worldH);
            case LINEAR -> this.linearPoint(worldW, worldH);
            case FROM_BORDERS -> this.borderPoint(worldW, worldH);
        };
    }

    private Point circularPoint(double worldW, double worldH) {
        double cx = worldW / 2.0;
        double cy = worldH / 2.0;
        double radius = Math.min(worldW, worldH) * 0.45;
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        int x = (int) Math.max(0, Math.min(worldW - 1, cx + Math.cos(angle) * radius));
        int y = (int) Math.max(0, Math.min(worldH - 1, cy + Math.sin(angle) * radius));
        return new Point(x, y);
    }

    private Point linearPoint(double worldW, double worldH) {
        int x = (int) (worldW * ((this.spawnedInWave % 10) / 10.0));
        int y = (int) (worldH * 0.1);
        return new Point(Math.max(0, x), Math.max(0, y));
    }

    private Point borderPoint(double worldW, double worldH) {
        int side = this.random.nextInt(4);
        return switch (side) {
            case 0 -> new Point(0, this.random.nextInt((int) worldH));
            case 1 -> new Point((int) worldW - 1, this.random.nextInt((int) worldH));
            case 2 -> new Point(this.random.nextInt((int) worldW), 0);
            default -> new Point(this.random.nextInt((int) worldW), (int) worldH - 1);
        };
    }

    private void updateAliveEstimated() {
        int deadNow = this.controller.getEntityDeadQuantity();
        int deltaDead = deadNow - this.lastDeadCount;
        if (deltaDead > 0 && this.aliveEstimated > 0) {
            this.aliveEstimated = Math.max(0, this.aliveEstimated - deltaDead);
        }
        this.lastDeadCount = deadNow;
    }

    private void applyContinuousHoming() {
        DoubleVector playerPos = this.controller.getLocalPlayerPosition();
        if (playerPos == null) {
            return;
        }

        ArrayList<BodyData> dynamics = this.controller.snapshotDynamicBodies();
        if (dynamics == null || dynamics.isEmpty()) {
            return;
        }

        for (BodyData bodyData : dynamics) {
            if (bodyData == null || bodyData.type != BodyType.DYNAMIC) {
                continue;
            }

            this.controller.steerDynamicBodyTowards(
                    bodyData.entityId,
                    playerPos.x,
                    playerPos.y,
                    0.09,
                    0.92);
        }
    }
}
