package gameworld;

import engine.assets.ports.AssetType;
import engine.utils.helpers.DoubleVector;
import engine.world.core.AbstractWorldDefinitionProvider;

/**
 * Proveedor de mundo finito para modo supervivencia.
 */
public final class SurvivalWorldDefinitionProvider extends AbstractWorldDefinitionProvider {

    public SurvivalWorldDefinitionProvider(DoubleVector worldDimension, ProjectAssets assets) {
        super(worldDimension, assets);
    }

    @Override
    protected void define() {
        this.setBackgroundStatic("llanura");

        this.addAsteroidPrototypeAnywhereRandomAsset(
                1,
                AssetType.ASTEROID,
                20,
                45,
                40,
                180,
                0,
                200);

        this.addSpaceship("spaceship_01", this.worldWidth / 2.0, this.worldHeight / 2.0, 55, 0, 100);

        this.addWeaponPresetBulletRandomAsset(AssetType.BULLET);
        this.addWeaponPresetBurstRandomAsset(AssetType.BULLET);
        this.addWeaponPresetMineLauncherRandomAsset(AssetType.MINE);
        this.addWeaponPresetMissileLauncherRandomAsset(AssetType.MISSILE);
    }
}
