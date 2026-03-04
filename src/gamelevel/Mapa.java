package gamelevel;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import engine.world.ports.DefItemDTO;

/**
 * Representa un mapa cerrado con tiles, spawns, zonas seguras y obstáculos.
 */
public final class Mapa {

    private final int widthTiles;
    private final int heightTiles;
    private final int tileSize;
    private final Tile[][] tiles;
    private final ArrayList<Point> spawnPoints = new ArrayList<>();
    private final ArrayList<Point> safeZones = new ArrayList<>();
    private final ArrayList<Obstaculo> obstacles = new ArrayList<>();

    public Mapa(int widthTiles, int heightTiles, int tileSize) {
        if (widthTiles <= 0 || heightTiles <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("Dimensiones del mapa inválidas");
        }
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tileSize = tileSize;
        this.tiles = new Tile[heightTiles][widthTiles];

        for (int y = 0; y < heightTiles; y++) {
            for (int x = 0; x < widthTiles; x++) {
                this.tiles[y][x] = Tile.EMPTY;
            }
        }
    }

    public static Mapa generarBasico(int widthTiles, int heightTiles, int tileSize, long seed) {
        Mapa mapa = new Mapa(widthTiles, heightTiles, tileSize);
        mapa.generar(seed);
        return mapa;
    }

    public static Mapa cargarDesdeArchivo(Path path, int tileSize) {
        if (path == null) {
            throw new IllegalArgumentException("path no puede ser nulo");
        }
        try {
            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty()) {
                throw new IllegalArgumentException("Archivo de mapa vacío");
            }

            int height = lines.size();
            int width = lines.getFirst().length();
            Mapa mapa = new Mapa(width, height, tileSize);

            for (int y = 0; y < height; y++) {
                String row = lines.get(y);
                if (row.length() != width) {
                    throw new IllegalArgumentException("Mapa inválido: filas de longitud desigual");
                }
                for (int x = 0; x < width; x++) {
                    char ch = row.charAt(x);
                    switch (ch) {
                        case '#' -> {
                            mapa.setTile(x, y, Tile.OBSTACLE);
                            mapa.obstacles.add(new Obstaculo("moon_05", tileSize * 0.9, mapa.toWorldX(x), mapa.toWorldY(y), 0));
                        }
                        case 'S' -> {
                            mapa.setTile(x, y, Tile.SPAWN_POINT);
                            mapa.spawnPoints.add(new Point((int) mapa.toWorldX(x), (int) mapa.toWorldY(y)));
                        }
                        case 'Z' -> {
                            mapa.setTile(x, y, Tile.SAFE_ZONE);
                            mapa.safeZones.add(new Point((int) mapa.toWorldX(x), (int) mapa.toWorldY(y)));
                        }
                        default -> mapa.setTile(x, y, Tile.EMPTY);
                    }
                }
            }
            return mapa;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar el mapa desde archivo", ex);
        }
    }

    public void generar(long seed) {
        Random rnd = new Random(seed);

        for (int x = 0; x < this.widthTiles; x++) {
            this.setTile(x, 0, Tile.OBSTACLE);
            this.setTile(x, this.heightTiles - 1, Tile.OBSTACLE);
        }
        for (int y = 0; y < this.heightTiles; y++) {
            this.setTile(0, y, Tile.OBSTACLE);
            this.setTile(this.widthTiles - 1, y, Tile.OBSTACLE);
        }

        this.safeZones.clear();
        int safeTileX = Math.max(2, this.widthTiles / 6);
        int safeTileY = Math.max(2, this.heightTiles / 6);

        Point safePoint = new Point(
            (int) this.toWorldX(safeTileX),
            (int) this.toWorldY(safeTileY));
        this.safeZones.add(safePoint);
        this.setTile(safeTileX, safeTileY, Tile.SAFE_ZONE);

        this.spawnPoints.clear();
        this.spawnPoints.add(new Point((int) this.toWorldX(1), (int) this.toWorldY(this.heightTiles / 2)));
        this.spawnPoints.add(new Point((int) this.toWorldX(this.widthTiles - 2), (int) this.toWorldY(this.heightTiles / 2)));
        this.spawnPoints.add(new Point((int) this.toWorldX(this.widthTiles / 2), (int) this.toWorldY(1)));
        this.spawnPoints.add(new Point((int) this.toWorldX(this.widthTiles / 2), (int) this.toWorldY(this.heightTiles - 2)));

        this.obstacles.clear();
        int obstacleCount = Math.max(6, (this.widthTiles * this.heightTiles) / 80);
        for (int i = 0; i < obstacleCount; i++) {
            int x = 2 + rnd.nextInt(Math.max(1, this.widthTiles - 4));
            int y = 2 + rnd.nextInt(Math.max(1, this.heightTiles - 4));

            int attempts = 0;
            while (attempts < 50 && !this.canPlaceObstacle(x, y)) {
                x = 2 + rnd.nextInt(Math.max(1, this.widthTiles - 4));
                y = 2 + rnd.nextInt(Math.max(1, this.heightTiles - 4));
                attempts++;
            }

            if (!this.canPlaceObstacle(x, y)) {
                continue;
            }

            this.setTile(x, y, Tile.OBSTACLE);
            String assetId = switch (i % 3) {
                case 0 -> "moon_05";
                case 1 -> "lab_01";
                default -> "planet_04";
            };
            this.obstacles.add(new Obstaculo(assetId, this.tileSize * 0.9, this.toWorldX(x), this.toWorldY(y), rnd.nextDouble() * 360));
        }
    }

    public boolean collides(double worldX, double worldY, double radius) {
        int tileX = (int) (worldX / this.tileSize);
        int tileY = (int) (worldY / this.tileSize);

        if (!this.inBounds(tileX, tileY)) {
            return true;
        }
        if (this.getTile(tileX, tileY) == Tile.OBSTACLE) {
            return true;
        }

        for (Obstaculo obstacle : this.obstacles) {
            DefItemDTO def = obstacle.toDefinition();
            double dx = worldX - def.posX;
            double dy = worldY - def.posY;
            double minDist = radius + def.size * 0.5;
            if ((dx * dx) + (dy * dy) <= (minDist * minDist)) {
                return true;
            }
        }
        return false;
    }

    public Tile getTile(int x, int y) {
        if (!this.inBounds(x, y)) {
            return Tile.OBSTACLE;
        }
        return this.tiles[y][x];
    }

    public List<Point> getSpawnPoints() {
        return List.copyOf(this.spawnPoints);
    }

    public List<Point> getSafeZones() {
        return List.copyOf(this.safeZones);
    }

    public List<Obstaculo> getObstacles() {
        return List.copyOf(this.obstacles);
    }

    public int getWidthTiles() {
        return this.widthTiles;
    }

    public int getHeightTiles() {
        return this.heightTiles;
    }

    public int getTileSize() {
        return this.tileSize;
    }

    private void setTile(int x, int y, Tile tile) {
        if (!this.inBounds(x, y)) {
            throw new IllegalArgumentException("Posición de tile fuera de rango");
        }
        this.tiles[y][x] = tile;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < this.widthTiles && y >= 0 && y < this.heightTiles;
    }

    private boolean canPlaceObstacle(int x, int y) {
        Tile tile = this.getTile(x, y);
        return tile != Tile.OBSTACLE
                && tile != Tile.SAFE_ZONE
                && tile != Tile.SPAWN_POINT;
    }

    private double toWorldX(int tileX) {
        return (tileX + 0.5) * this.tileSize;
    }

    private double toWorldY(int tileY) {
        return (tileY + 0.5) * this.tileSize;
    }
}
