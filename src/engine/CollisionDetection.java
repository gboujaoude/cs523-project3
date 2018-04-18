package engine;

import engine.math.Vector3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CollisionDetection implements Task, MessageHandler {
    private HashSet<ActorGraph> _actors;
    private HashSet<ActorGraph> _rootSet;
    private AtomicReference<Double> _deltaSeconds = new AtomicReference<>(0.0);

    public void init() {
        Engine.getMessagePump().signalInterest(Singleton.ADD_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Singleton.REMOVE_GRAPHICS_ENTITY, this);
        _actors = new HashSet<>();
        _rootSet = new HashSet<>();
    }

    public void setDeltaSeconds(double deltaSeconds) {
        _deltaSeconds.set(deltaSeconds);
    }

    @Override
    public void execute() {
        _updateEntities(_deltaSeconds.get());
    }

    @Override
    public void handleMessage(Message message) {
        switch(message.getMessageName()) {
            case Singleton.ADD_GRAPHICS_ENTITY:
            {
                _actors.add((ActorGraph)message.getMessageData());
                break;
            }
            case Singleton.REMOVE_GRAPHICS_ENTITY:
            {
                Object obj = message.getMessageData();
                if (obj == null) return;
                _actors.remove(obj);
                break;
            }
        }
    }

    private void _updateEntities(double deltaSeconds)
    {
        _rootSet.clear();
        int worldStartX = Engine.getConsoleVariables().find(Singleton.WORLD_START_X).getcvarAsInt();
        int worldStartY = Engine.getConsoleVariables().find(Singleton.WORLD_START_Y).getcvarAsInt();
        int worldWidth = Engine.getConsoleVariables().find(Singleton.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Singleton.WORLD_HEIGHT).getcvarAsInt();
        // Account for the fact that worldStartX/worldStartY may not simply be 0
        worldWidth += worldStartX;
        worldHeight += worldStartY;
        // Actors form a graph so we need to start at the roots and move down to ensure
        // that attached nodes inherit the base actor's speed and acceleration
        for (ActorGraph graph : _actors)
        {
            if (_rootSet.contains(graph)) continue; // Already processed this actor and its attached actors
            if (graph.isAttached()) continue; // Will be processed later
            Vector3 speed = graph.getSpeedVec();
            Vector3 acceleration = graph.getAccelerationVec();
            speed.setXYZ(speed.x() + acceleration.x() * deltaSeconds,
                    speed.y() + acceleration.y() * deltaSeconds, 0);
            double speedX = speed.x();
            double speedY = speed.y();
            double deltaSpeedX = speedX * deltaSeconds;
            double deltaSpeedY = speedY * deltaSeconds;
            double x = graph.getLocationX();
            double y = graph.getLocationY();
            double depth = graph.getDepth();
            graph.setLocationXYDepth(x + deltaSpeedX * (graph.shouldConstrainXMovement() ? 0 : 1),
                    y + deltaSpeedY * (graph.shouldConstrainYMovement() ? 0 : 1),
                    depth);
            _checkAndCorrectOutOfBounds(graph, worldStartX, worldStartY, worldWidth, worldHeight);
            _rootSet.add(graph);
            for (ActorGraph attached : graph.getActors())
            {
                _updateGraphEntitiesRecursive(attached, worldStartX, worldStartY, worldWidth,
                        worldHeight, deltaSpeedX, deltaSpeedY);
            }
        }
    }

    // We need to do this because actors can be attached to other actors to form a graph
    // structure which inherits speed/acceleration from the root actor
    private void _updateGraphEntitiesRecursive(ActorGraph actor, int worldStartX, int worldStartY,
                                               int worldWidth, int worldHeight,
                                               double deltaSpeedX, double deltaSpeedY)
    {
        // Only process this actor if it is part of the world
        // and has not been processed yet
        if (_actors.contains(actor) && !_rootSet.contains(actor))
        {
            actor.setLocationXYDepth(actor.getLocationX() + deltaSpeedX * (actor.shouldConstrainXMovement() ? 0 : 1),
                    actor.getLocationY() + deltaSpeedY * (actor.shouldConstrainYMovement() ? 0 : 1),
                    actor.getDepth());
            _checkAndCorrectOutOfBounds(actor, worldStartX, worldStartY, worldWidth, worldHeight);
        }
        _rootSet.add(actor);
        // Process its attached actors regardless
        for (ActorGraph attached : actor.getActors())
        {
            _updateGraphEntitiesRecursive(attached, worldStartX, worldStartY, worldWidth,
                    worldHeight, deltaSpeedX, deltaSpeedY);
        }
    }

    // This performs wraparound for an object
    private void _checkAndCorrectOutOfBounds(Actor actor, int worldStartX, int worldStartY,
                                             int worldWidth, int worldHeight)
    {
        Vector3 translation = actor.getTranslationVec();
        double x = translation.x();
        double y = translation.y();
        double width = actor.getWidth();
        double height = actor.getHeight();
        if (x + width < worldStartX) x = worldWidth - width;
        else if (x > worldWidth) x = worldStartX;
        if (y + height < worldStartY) y = worldHeight - height;
        else if (y > worldHeight) y = worldStartY;
        translation.setXYZ(x, y, 1);
    }
}
