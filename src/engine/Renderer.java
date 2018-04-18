package engine;

import engine.math.Vector3;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.util.*;

/**
 * The renderer manages all drawable entities in the scene and also
 * simulates their movement based on location, speed and acceleration.
 * Along with this, all textures are cached as needed by this class
 * for fast lookup later on.
 *
 * The movement simulation and rendering are two distinct stages of
 * the rendering pipeline which are triggered by separate engine messages.
 * This means that disabling one or the other or both is very easy.
 *
 * @author Justin Hall
 */
public class Renderer implements MessageHandler {
    private GraphicsContext _gc;
    private HashMap<String, ImageView> _textures = new HashMap<>();
    private HashSet<GraphicsEntity> _entities = new HashSet<>();
    private HashSet<ActorGraph> _rootSet = new HashSet<>();
    private TreeMap<Integer, ArrayList<GraphicsEntity>> _drawOrder = new TreeMap<>();
    private Camera _worldCamera = new Camera(); // Start with a default camera
    private Rotate _rotation = new Rotate(0);
    private ArrayList<Task> _collisionTask;
    private CollisionDetection _collision;
    private volatile boolean _renderedScene = false;
    private volatile boolean _updatingEntities = false;

    public void init(GraphicsContext gc)
    {
        _gc = gc;
        _rotation.setAxis(new Point3D(0, 0, 1)); // In 2D we rotate about the z-axis
        _collision = new CollisionDetection();
        _collision.init();
        _collisionTask = new ArrayList<>();
        _collisionTask.add(_collision);
        _renderedScene = false;
        //_updatingEntities = false;
        // Signal interest
        Engine.getMessagePump().signalInterest(Singleton.ADD_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Singleton.REMOVE_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Singleton.REGISTER_TEXTURE, this);
        Engine.getMessagePump().signalInterest(Singleton.SET_MAIN_CAMERA, this);
        Engine.getMessagePump().signalInterest(Engine.R_RENDER_SCENE, this);
        Engine.getMessagePump().signalInterest(Engine.R_UPDATE_ENTITIES, this);
        Engine.getMessagePump().signalInterest(Singleton.REMOVE_ALL_RENDER_ENTITIES, this);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.getMessageName())
        {
            case Engine.R_RENDER_SCENE:
                _render((Double)message.getMessageData());
                break;
            case Engine.R_UPDATE_ENTITIES:
                if (_updatingEntities || !_renderedScene) return; // Already checking for collisions/simulating movement
                //_updateEntities((Double)message.getMessageData());
                _updatingEntities = true;
                _renderedScene = false;
                Engine.scheduleLogicTasks(_collisionTask, () -> _updatingEntities = false);
                break;
            case Singleton.ADD_GRAPHICS_ENTITY:
                _entities.add((GraphicsEntity) message.getMessageData());
                break;
            case Singleton.REMOVE_GRAPHICS_ENTITY:
                _entities.remove((GraphicsEntity)message.getMessageData());
                break;
            case Singleton.REMOVE_ALL_RENDER_ENTITIES:
                _entities.clear();
                break;
            case Singleton.REGISTER_TEXTURE: {
                String texture = (String)message.getMessageData();
                if (!_textures.containsKey(texture)) {
                    try {
                        System.out.println("Registering " + texture);
                        Image image = new Image(texture);
                        ImageView imageView = new ImageView(image);
                        imageView.setRotationAxis(new Point3D(0.0, 0.0, 1.0));
                        _textures.put((String) message.getMessageData(), imageView);
                    } catch (Exception e) {
                        System.err.println("ERROR: Unable to load " + texture);
                    }
                }
                break;
            }
            case Singleton.SET_MAIN_CAMERA:
                _worldCamera = (Camera)message.getMessageData();
                break;

        }
    }

    private void _render(double deltaSeconds)
    {
        if (_updatingEntities) return; // Not done with collisions/movement simulation
        _collision.setDeltaSeconds(deltaSeconds);
        // Clear the screen
        _gc.setFill(Color.WHITE);
        _gc.fillRect(0, 0,
                Engine.getConsoleVariables().find(Singleton.SCR_WIDTH).getcvarAsFloat(),
                Engine.getConsoleVariables().find(Singleton.SCR_HEIGHT).getcvarAsFloat());

        // Reorder scene as needed so things are drawn in the proper order
        _determineDrawOrder();
        // What values to offset everything in the world by to
        // determine camera-space coordinates
        double xOffset;
        double yOffset;
        Vector3 translate = _worldCamera.getWorldTranslate();
        xOffset = translate.x();
        yOffset = translate.y();
        // Now transform everyone to camera space and determine if they
        // are visible and need to be drawn
        double screenX;
        double screenY;
        double rotation;
        double width;
        double height;
        Vector3 location;
        int screenWidth = Engine.getConsoleVariables().find(Singleton.SCR_WIDTH).getcvarAsInt();
        int screenHeight = Engine.getConsoleVariables().find(Singleton.SCR_HEIGHT).getcvarAsInt();
        for (Map.Entry<Integer, ArrayList<GraphicsEntity>> entry : _drawOrder.entrySet())
        {
            for (GraphicsEntity entity : entry.getValue())
            {
                location = entity.getTranslationVec();
                boolean isStatic = entity.isStaticActor();
                screenX = location.x() + (isStatic ? 0 : xOffset);
                screenY = location.y() + (isStatic ? 0 : yOffset);
                width = entity.getWidth();
                height = entity.getHeight();
                rotation = entity.getRotation();
                if (screenX + width < 0 || screenX > screenWidth ||
                        screenY + height < 0 || screenY > screenHeight)
                {
                    entity.setScreenVisibility(false);
                }
                else
                {
                    entity.setScreenVisibility(true);
                    _rotation.setAngle(rotation);
                    _rotation.setPivotX(screenX + width / 2);
                    _rotation.setPivotY(screenY + height / 2);
                    // See https://stackoverflow.com/questions/18260421/how-to-draw-image-rotated-on-javafx-canvas
                    _gc.setTransform(_rotation.getMxx(), _rotation.getMyx(),
                            _rotation.getMxy(), _rotation.getMyy(), _rotation.getTx(), _rotation.getTy());
                    if (_textures.containsKey(entity.getTexture()))
                    {
                        ImageView imageView = _textures.get(entity.getTexture());
                        _gc.drawImage(imageView.getImage(), screenX, screenY, width, height);
                    }
                    else
                    {
                        _gc.setFill(entity.getColor());
                        entity.render(_gc, screenX, screenY);
                        //_gc.fillRect(screenX, screenY, width, height);
                    }
                }
            }
        }
        _renderedScene = true;
    }

    /*
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
        for (ActorGraph graph : _entities)
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
        if (_entities.contains(actor) && !_rootSet.contains(actor))
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
    */

    private void _determineDrawOrder()
    {
        for (Map.Entry<Integer, ArrayList<GraphicsEntity>> entry : _drawOrder.entrySet())
        {
            entry.getValue().clear();
        }

        for (GraphicsEntity entity : _entities)
        {
            int depth = (int)entity.getDepth() * -1; // * -1 because if the depth is negative it needs to come
            // later in the list so that it gets drawn last and
            // will then appear to be on top of other objects
            if (!_drawOrder.containsKey(depth))
            {
                _drawOrder.put(depth, new ArrayList<>());
            }
            _drawOrder.get(depth).add(entity);
        }
    }

    /*
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
    */
}