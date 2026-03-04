# Survival Mode Restructure

## Overview

This update transforms the default game flow from free asteroid spawning into **wave-based survival** with finite map support.

Main goals implemented:

- Enemy hierarchy (`Enemigo` + variants)
- Wave orchestration (`WaveManager`)
- Survival game rules and score (`gamerules.GameRules`)
- Closed map model (`Mapa`, `Tile`, `Obstaculo`)
- Finite world helpers (`World`, `Camara`, `ZonaSegura`)
- Main bootstrap adapted to survival mode

## Main Class Changes

- [Main.java](../src/Main.java) now boots `SurvivalWorldDefinitionProvider`
- Creates a procedural closed map through `Mapa.generarBasico(...)`
- Uses `gamelevel.Level` (survival level) instead of `LevelBasic`
- Activates `WaveManager` with states:
  - `PREPARING`
  - `WAVE_ACTIVE`
  - `WAVE_CLEARED`
  - `GAME_OVER`
  - `VICTORY`

## New/Updated Packages

### gameai

- `Enemigo` (abstract base)
- `EnemigoBasico`, `EnemigoRapido`, `EnemigoTanque`
- `ComportamientoEnemigo`
- `SpawnPattern`
- `WaveManager`

### gamelevel

- `Level` (survival map-aware level)
- `Mapa`
- `Tile`
- `Obstaculo`

### gamerules

- `GameRules` (survival-oriented rules + score)
- `WaveRules` (wave composition, rest time, difficulty multipliers)
- `PowerUp` (optional upgrades enum)

### gameworld

- `SurvivalWorldDefinitionProvider`
- `World`
- `Camara`
- `ZonaSegura`
- `ElementoInteractivo`

## Class Diagram (Mermaid)

```mermaid
classDiagram
    class Enemigo {
      <<abstract>>
      +crearSpawn(...)
    }
    class EnemigoBasico
    class EnemigoRapido
    class EnemigoTanque
    class ComportamientoEnemigo {
      <<interface>>
      +patrullar(...)
      +atacar(...)
      +perseguirJugador(...)
    }
    class WaveManager
    class SpawnPattern

    class WaveRules
    class GameRules
    class PowerUp

    class Level
    class Mapa
    class Tile
    class Obstaculo

    class World
    class Camara
    class ZonaSegura
    class ElementoInteractivo {
      <<abstract>>
    }

    Enemigo <|-- EnemigoBasico
    Enemigo <|-- EnemigoRapido
    Enemigo <|-- EnemigoTanque
    ComportamientoEnemigo <|.. Enemigo

    WaveManager --> Enemigo
    WaveManager --> WaveRules
    WaveManager --> GameRules
    WaveManager --> SpawnPattern

    GameRules --> WaveRules

    Level --> Mapa
    Mapa --> Tile
    Mapa --> Obstaculo

    World --> Camara
    World --> ZonaSegura
    World --> Mapa
    World --> ElementoInteractivo
```

## Build and Run

Prerequisites:

- Java 21
- Maven 3.9+

Commands:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=Main
```

> Note: In this environment Maven CLI is not installed, so full command execution could not be validated here.
