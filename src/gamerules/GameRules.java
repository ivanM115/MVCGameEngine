package gamerules;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import engine.actions.ActionDTO;
import engine.actions.ActionType;
import engine.controller.ports.ActionsGenerator;
import engine.events.domain.ports.DomainEventType;
import engine.events.domain.ports.eventtype.CollisionEvent;
import engine.events.domain.ports.eventtype.DomainEvent;
import engine.events.domain.ports.eventtype.EmitEvent;
import engine.events.domain.ports.eventtype.LifeOver;
import engine.events.domain.ports.eventtype.LimitEvent;
import engine.model.bodies.ports.BodyType;

/**
 * Reglas del modo supervivencia con score, derrota y victoria por oleadas.
 */
public final class GameRules implements ActionsGenerator {

    private final WaveRules waveRules;
    private final AtomicInteger enemiesKilled = new AtomicInteger();
    private final AtomicInteger wavesCleared = new AtomicInteger();
    private final AtomicInteger score = new AtomicInteger();
    private final AtomicBoolean gameOver = new AtomicBoolean(false);
    private final AtomicBoolean victory = new AtomicBoolean(false);

    public GameRules(WaveRules waveRules) {
        if (waveRules == null) {
            throw new IllegalArgumentException("waveRules no puede ser nulo");
        }
        this.waveRules = waveRules;
    }

    @Override
    public void provideActions(List<DomainEvent> domainEvents, List<ActionDTO> actions) {
        if (domainEvents == null) {
            return;
        }
        for (DomainEvent event : domainEvents) {
            this.applyGameRules(event, actions);
        }
    }

    public boolean isGameOver() {
        return this.gameOver.get();
    }

    public boolean isVictory() {
        return this.victory.get();
    }

    public int getEnemiesKilled() {
        return this.enemiesKilled.get();
    }

    public int getWavesCleared() {
        return this.wavesCleared.get();
    }

    public int getScore() {
        return this.score.get();
    }

    public void onEnemyKilled() {
        this.enemiesKilled.incrementAndGet();
        this.score.addAndGet(10);
    }

    public void onWaveCleared(int waveNumber) {
        this.wavesCleared.incrementAndGet();
        this.score.addAndGet(100 * Math.max(1, waveNumber));

        if (this.wavesCleared.get() >= this.waveRules.getWaveCount()) {
            this.victory.set(true);
        }
    }

    public void onPlayerDead() {
        this.gameOver.set(true);
    }

    private void applyGameRules(DomainEvent event, List<ActionDTO> actions) {
        switch (event) {
            case LimitEvent limitEvent -> {
                if (limitEvent.primaryBodyRef.type() == BodyType.DYNAMIC) {
                    actions.add(new ActionDTO(
                            limitEvent.primaryBodyRef.id(),
                            limitEvent.primaryBodyRef.type(),
                            ActionType.MOVE_TO_CENTER,
                            event));
                    return;
                }

                ActionType action;
                switch (limitEvent.type) {
                    case REACHED_EAST_LIMIT -> action = ActionType.MOVE_REBOUND_IN_EAST;
                    case REACHED_WEST_LIMIT -> action = ActionType.MOVE_REBOUND_IN_WEST;
                    case REACHED_NORTH_LIMIT -> action = ActionType.MOVE_REBOUND_IN_NORTH;
                    case REACHED_SOUTH_LIMIT -> action = ActionType.MOVE_REBOUND_IN_SOUTH;
                    default -> action = ActionType.NO_MOVE;
                }

                actions.add(new ActionDTO(
                        limitEvent.primaryBodyRef.id(),
                        limitEvent.primaryBodyRef.type(),
                        action,
                        event));
            }

            case LifeOver lifeOver -> {
                actions.add(new ActionDTO(
                        lifeOver.primaryBodyRef.id(),
                        lifeOver.primaryBodyRef.type(),
                        ActionType.DIE,
                        event));

                if (lifeOver.primaryBodyRef.type() == BodyType.PLAYER) {
                    this.onPlayerDead();
                }
            }

            case EmitEvent emitEvent -> {
                ActionType action = emitEvent.type == DomainEventType.EMIT_REQUESTED
                        ? ActionType.SPAWN_BODY
                        : ActionType.SPAWN_PROJECTILE;

                actions.add(new ActionDTO(
                        emitEvent.primaryBodyRef.id(),
                        emitEvent.primaryBodyRef.type(),
                        action,
                        event));
            }

            case CollisionEvent collisionEvent -> this.resolveCollision(collisionEvent, actions);
            default -> {
            }
        }
    }

    private void resolveCollision(CollisionEvent event, List<ActionDTO> actions) {
        BodyType primary = event.primaryBodyRef.type();
        BodyType secondary = event.secondaryBodyRef.type();

        if (primary == BodyType.DECORATOR || secondary == BodyType.DECORATOR) {
            return;
        }

        if (event.payload.haveImmunity) {
            return;
        }

        actions.add(new ActionDTO(
                event.primaryBodyRef.id(),
                event.primaryBodyRef.type(),
                ActionType.DIE,
                event));

        actions.add(new ActionDTO(
                event.secondaryBodyRef.id(),
                event.secondaryBodyRef.type(),
                ActionType.DIE,
                event));

        if (primary == BodyType.DYNAMIC && (secondary == BodyType.PLAYER || secondary == BodyType.PROJECTILE)) {
            this.onEnemyKilled();
        }
        if (secondary == BodyType.DYNAMIC && (primary == BodyType.PLAYER || primary == BodyType.PROJECTILE)) {
            this.onEnemyKilled();
        }
    }
}
