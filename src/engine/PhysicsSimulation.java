package engine;

import engine.math.Vector3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PhysicsSimulation implements Task, MessageHandler {
    private static final Object _obj = new Object();
    private ConcurrentHashMap<ActorGraph, Object> _actors;
    private HashSet<ActorGraph> _rootSet;
    private QuadTree<ActorGraph> _actorTree;
    private AtomicReference<Double> _deltaSeconds = new AtomicReference<>(0.0);
    private ConcurrentHashMap<Actor, HashSet<Actor>> _collisions;

    public void init() {
        Engine.getMessagePump().signalInterest(Constants.ADD_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Constants.REMOVE_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Constants.CONSOLE_VARIABLE_CHANGED, this);
        _actors = new ConcurrentHashMap<>();
        _rootSet = new HashSet<>();
        _collisions = new ConcurrentHashMap<>(100);
        int worldStartX = Engine.getConsoleVariables().find(Constants.WORLD_START_X).getcvarAsInt();
        int worldStartY = Engine.getConsoleVariables().find(Constants.WORLD_START_Y).getcvarAsInt();
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
        int worldWidthHeight = worldWidth > worldHeight ? worldWidth : worldHeight;
        _actorTree = new QuadTree<>(worldStartX, worldStartY, worldWidthHeight, 10, 100);
    }

    public void setDeltaSeconds(double deltaSeconds) {
        _deltaSeconds.set(deltaSeconds);
    }

    // Returns the collisions that resulted during the previous physics simulation update
    public ConcurrentHashMap<Actor, HashSet<Actor>> getPreviousCollisions() {
        return _collisions;
    }

    @Override
    public void execute() {
        _updateEntities(_deltaSeconds.get());
        _checkForCollisions();
    }

    @Override
    public void handleMessage(Message message) {
        switch(message.getMessageName()) {
            case Constants.ADD_GRAPHICS_ENTITY:
            {
                _actors.putIfAbsent((ActorGraph)message.getMessageData(), _obj);
                _collisions.putIfAbsent((ActorGraph)message.getMessageData(), new HashSet<>(25));
                break;
            }
            case Constants.REMOVE_GRAPHICS_ENTITY:
            {
                Object obj = message.getMessageData();
                if (obj == null) return;
                _actors.remove(obj);
                _collisions.remove(obj);
                break;
            }
            case Constants.CONSOLE_VARIABLE_CHANGED:
            {
                ConsoleVariable var = (ConsoleVariable)message.getMessageData();
                int worldX = Engine.getConsoleVariables().find(Constants.WORLD_START_X).getcvarAsInt();
                int worldY = Engine.getConsoleVariables().find(Constants.WORLD_START_Y).getcvarAsInt();
                int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
                int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
                if (var.getcvarName().equals(Constants.WORLD_WIDTH) || var.getcvarName().equals(Constants.WORLD_HEIGHT)
                        || var.getcvarName().equals(Constants.WORLD_START_X) || var.getcvarName().equals(Constants.WORLD_START_Y)) {
                    _actorTree = new QuadTree<>(worldX, worldY, worldWidth > worldHeight ? worldWidth : worldHeight,
                            10, 100);
                }
            }
        }
    }

    private void _updateEntities(double deltaSeconds)
    {
        _rootSet.clear();
        _actorTree.clear();
        int worldStartX = Engine.getConsoleVariables().find(Constants.WORLD_START_X).getcvarAsInt();
        int worldStartY = Engine.getConsoleVariables().find(Constants.WORLD_START_Y).getcvarAsInt();
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
        // Account for the fact that worldStartX/worldStartY may not simply be 0
        worldWidth += worldStartX;
        worldHeight += worldStartY;
        // Actors form a graph so we need to start at the roots and move down to ensure
        // that attached nodes inherit the base actor's speed and acceleration
        for (Map.Entry<ActorGraph, Object> graphEntry : _actors.entrySet())
        {
            ActorGraph graph = graphEntry.getKey();
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
            _actorTree.add(graph);
            for (Map.Entry<ActorGraph, Object> attached : graph.getActors().entrySet())
            {
                _updateGraphEntitiesRecursive(attached.getKey(), worldStartX, worldStartY, worldWidth,
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
        if (_actors.containsKey(actor) && !_rootSet.contains(actor))
        {
            actor.setLocationXYDepth(actor.getLocationX() + deltaSpeedX * (actor.shouldConstrainXMovement() ? 0 : 1),
                    actor.getLocationY() + deltaSpeedY * (actor.shouldConstrainYMovement() ? 0 : 1),
                    actor.getDepth());
            _checkAndCorrectOutOfBounds(actor, worldStartX, worldStartY, worldWidth, worldHeight);
            _actorTree.add(actor);
        }
        _rootSet.add(actor);
        // Process its attached actors regardless
        for (Map.Entry<ActorGraph, Object> attached : actor.getActors().entrySet())
        {
            _updateGraphEntitiesRecursive(attached.getKey(), worldStartX, worldStartY, worldWidth,
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

    private void _checkForCollisions() {
        Iterator<HashSet<ActorGraph>> iterator = _actorTree.getLeafIterator();
        // Clear out the collisions from the previous iteration
        for (Map.Entry<Actor, HashSet<Actor>> entry : _collisions.entrySet()) entry.getValue().clear();
        while (iterator.hasNext()) {
            HashSet<ActorGraph> set = iterator.next();
            for (ActorGraph outer : set) {
                HashSet<Actor> outerCollisions = _collisions.get(outer);
                if (outerCollisions == null) continue;
                double depth = outer.getDepth();
                for (ActorGraph inner : set) {
                    if (outer == inner || depth != inner.getDepth()) continue;
                    HashSet<Actor> innerCollisions = _collisions.get(inner);
                    if (innerCollisions == null) continue;
                    if (_collided(outer, inner)) {
                        outerCollisions.add(inner);
                        innerCollisions.add(outer);
                        //System.out.println(outer + " collided with " + inner);
                    }
                }
            }
        }
    }

    private boolean _collided(ActorGraph first, ActorGraph second) {
        double x1 = first.getLocationX();
        double y1 = first.getLocationY();
        double endX1 = x1 + first.getWidth();
        double endY1 = y1 + first.getHeight();

        double x2 = second.getLocationX();
        double y2 = second.getLocationY();
        double endX2 = x2 + second.getWidth();
        double endY2 = y2 + second.getHeight();

        //(_startX > edgeX) || (x > _edgeX) || (_startY > edgeY) || (y > _edgeY)
        // If any of the following are true, then we are not colliding, and if
        // not we assume a collision has taken place
        return !((x1 > endX2) || (x2 > endX1) || (y1 > endY2) || (y2 > endY1));
    }
}
