
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.concurrent.locks.LockSupport;

import engine.controller.impl.Controller;
import engine.model.impl.Model;
import engine.utils.helpers.DoubleVector;
import engine.view.core.View;
import engine.world.ports.WorldDefinition;
import engine.world.ports.WorldDefinitionProvider;
import gameai.SpawnPattern;
import gameai.WaveManager;
import gamelevel.Mapa;
import gamerules.GameRules;
import gamerules.WaveRules;
import gameworld.ProjectAssets;
import gameworld.SurvivalWorldDefinitionProvider;
import gameworld.World;
import gameworld.ZonaSegura;

public class Main {

	private enum SessionEndReason {
		RESTART,
		EXIT
	}

	private enum SurvivalGameState {
		PREPARING,
		WAVE_ACTIVE,
		WAVE_CLEARED,
		GAME_OVER,
		VICTORY
	}

	public static void main(String[] args) {
		while (runSingleSession() == SessionEndReason.RESTART) {
			System.out.println("Reiniciando partida...");
		}
	}

	private static SessionEndReason runSingleSession() {

		// region Graphics configuration
		System.setProperty("sun.java2d.uiScale", "1.0");
		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.d3d", "false"); // OpenGL
		// endregion
		
		// region Dimensions and limits
		// Due a recognized issue with BufferStrategy when
		// Canvas size > screen size causes BufferStrategy to fail (blank window)
		// in that case engine whill throw an error and exit.
		//
		// => **********************************************************
		// => *** Keep viewDimension smaller than actual screen size ***
		// => *** or... no set viewDimension                         ***
		// => **********************************************************
		DoubleVector desiredViewDimension = new DoubleVector(2400, 1500);
		DoubleVector viewDimension = resolveSafeViewDimension(desiredViewDimension);
		DoubleVector worldDimension = new DoubleVector(12000, 8000);
		// endregion

		int maxBodies = 2000;
		int maxEnemyCreationDelay = 8;

		ProjectAssets projectAssets = new ProjectAssets();
		WaveRules waveRules = WaveRules.defaultRules();
		GameRules gameRules = new GameRules(waveRules);

		// *** WORLD DEFINITION PROVIDER ***
		WorldDefinitionProvider worldProv = new SurvivalWorldDefinitionProvider(
				worldDimension, projectAssets);

		// *** CORE ENGINE ***

		// region Controller
		View view = new View();
		Controller controller = new Controller(
				worldDimension, viewDimension, maxBodies,
				view, new Model(worldDimension, maxBodies),
				gameRules);

		controller.activate();
		// endregion

		// *** SCENE ***

		// region World definition
		WorldDefinition worldDef = worldProv.provide();
		// endregion

		int tileSize = 300;
		Mapa mapa = Mapa.generarBasico(
				(int) (worldDimension.x / tileSize),
				(int) (worldDimension.y / tileSize),
				tileSize,
				System.currentTimeMillis());

		java.awt.Point mapaSafePoint = mapa.getSafeZones().isEmpty()
				? new java.awt.Point((int) (worldDimension.x * 0.2), (int) (worldDimension.y * 0.2))
				: mapa.getSafeZones().getFirst();

		ZonaSegura zonaSegura = new ZonaSegura(
				mapaSafePoint.x - 300,
				mapaSafePoint.y - 300,
				600,
				600);
		World survivalWorld = new World(worldDimension, viewDimension, mapa, zonaSegura);

		// region Level generator (Level***)
		gamelevel.Level survivalLevel = new gamelevel.Level(controller, worldDef, mapa);
		System.out.println("Nivel cargado: " + survivalLevel.getClass().getSimpleName());
		// endregion

		WaveManager waveManager = new WaveManager(
				controller,
				worldDef,
				waveRules,
				gameRules,
				SpawnPattern.FROM_BORDERS,
				maxEnemyCreationDelay);
		waveManager.activate();

		return runSurvivalLoop(controller, view, waveManager, gameRules, survivalWorld);
	}

	private static DoubleVector resolveSafeViewDimension(DoubleVector desired) {
		if (desired == null) {
			throw new IllegalArgumentException("desired no puede ser nulo");
		}

		final double fallbackWidth = 1280;
		final double fallbackHeight = 720;
		final int marginPx = 80;

		double safeWidth = fallbackWidth;
		double safeHeight = fallbackHeight;

		try {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			double maxAllowedWidth = Math.max(800, screen.getWidth() - marginPx);
			double maxAllowedHeight = Math.max(600, screen.getHeight() - marginPx);

			safeWidth = Math.min(desired.x, maxAllowedWidth);
			safeHeight = Math.min(desired.y, maxAllowedHeight);
		} catch (HeadlessException ex) {
			System.out.println("No se pudo leer tamaño de pantalla. Usando dimensión segura por defecto.");
		}

		System.out.println("ViewDimension solicitada: " + desired.x + "x" + desired.y
				+ " | aplicada: " + safeWidth + "x" + safeHeight);

		return new DoubleVector(safeWidth, safeHeight);
	}

	private static SessionEndReason runSurvivalLoop(
			Controller controller,
			View view,
			WaveManager waveManager,
			GameRules gameRules,
			World world) {

		SurvivalGameState lastState = null;
		long lastCountdownPrint = 0;
		boolean gameOverPrinted = false;
		view.setGameOverScreenVisible(false);

		while (controller.getEngineState() != engine.controller.ports.EngineState.STOPPED) {
			WaveManager.SurvivalState waveState = waveManager.getState();
			SurvivalGameState mappedState = switch (waveState) {
				case PREPARING -> SurvivalGameState.PREPARING;
				case WAVE_ACTIVE -> SurvivalGameState.WAVE_ACTIVE;
				case WAVE_CLEARED -> SurvivalGameState.WAVE_CLEARED;
				case GAME_OVER -> SurvivalGameState.GAME_OVER;
				case VICTORY -> SurvivalGameState.VICTORY;
			};

			if (mappedState != lastState) {
				System.out.println("[STATE] " + mappedState + " | Wave=" + waveManager.getCurrentWaveNumber());
				lastState = mappedState;
				world.updateWaveVisuals(Math.max(1, waveManager.getCurrentWaveNumber()));
			}

			if (mappedState == SurvivalGameState.PREPARING || mappedState == SurvivalGameState.WAVE_CLEARED) {
				long now = System.currentTimeMillis();
				if (now - lastCountdownPrint >= 1_000) {
					long seconds = (waveManager.getMillisToNextWave() + 999) / 1000;
					System.out.println("Siguiente oleada en " + seconds + "s");
					lastCountdownPrint = now;
				}
			}

			if (mappedState == SurvivalGameState.GAME_OVER || gameRules.isGameOver()) {
				if (!gameOverPrinted) {
					System.out.println("GAME OVER | Score=" + gameRules.getScore() + " | Kills=" + gameRules.getEnemiesKilled());
					System.out.println("Pulsa ENTER para reiniciar");
					view.setGameOverScreenVisible(true);
					gameOverPrinted = true;
				}

				if (view.consumeRestartRequested()) {
					view.setGameOverScreenVisible(false);
					controller.engineStop();
					return SessionEndReason.RESTART;
				}

				LockSupport.parkNanos(120_000_000L);
				continue;
			}

			if (mappedState == SurvivalGameState.VICTORY || gameRules.isVictory()) {
				System.out.println("VICTORIA | Score=" + gameRules.getScore() + " | Oleadas=" + gameRules.getWavesCleared());
				view.setGameOverScreenVisible(false);
				controller.engineStop();
				return SessionEndReason.EXIT;
			}

			LockSupport.parkNanos(120_000_000L);
			if (Thread.currentThread().isInterrupted()) {
				view.setGameOverScreenVisible(false);
				controller.engineStop();
				return SessionEndReason.EXIT;
			}
		}

		return SessionEndReason.EXIT;
	}
}
