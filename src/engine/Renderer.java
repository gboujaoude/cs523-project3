package engine;

import engine.math.Vector3;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private QuadTree<GraphicsEntity> _graphicsEntities;
    private TreeMap<Integer, ArrayList<GraphicsEntity>> _drawOrder = new TreeMap<>();
    private Camera _worldCamera;// = new Camera(); // Start with a default camera
    private Pair<Double, Double> _lastManualCameraOffsetXY;
    private Rotate _rotation = new Rotate(0);
    private PhysicsSimulation _collision;
    private double _zoom;
    private volatile boolean _renderedScene;
    private volatile boolean _updatingEntities;
    private volatile boolean _headless;

    public void init(GraphicsContext gc)
    {
        _gc = gc;
        _rotation.setAxis(new Point3D(0, 0, 1)); // In 2D we rotate about the z-axis
        _collision = new PhysicsSimulation();
        _collision.init();
        _renderedScene = false;
        _headless = _gc == null;
        _renderedScene = false;
        _updatingEntities = false;
        _zoom = 1.0;
        _worldCamera = new Camera(); // Start with a default camera
        _lastManualCameraOffsetXY = _worldCamera.getManualOffsetXY();
        int worldX = Engine.getConsoleVariables().find(Constants.WORLD_START_X).getcvarAsInt();
        int worldY = Engine.getConsoleVariables().find(Constants.WORLD_START_Y).getcvarAsInt();
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
        _graphicsEntities = new QuadTree<>(worldX, worldY, worldWidth > worldHeight ? worldWidth : worldHeight,
                10, 100);
        //_updatingEntities = false;
        // Signal interest
        Engine.getMessagePump().signalInterest(Constants.ADD_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Constants.REMOVE_GRAPHICS_ENTITY, this);
        Engine.getMessagePump().signalInterest(Constants.REGISTER_TEXTURE, this);
        Engine.getMessagePump().signalInterest(Constants.SET_MAIN_CAMERA, this);
        Engine.getMessagePump().signalInterest(Engine.R_RENDER_SCENE, this);
        Engine.getMessagePump().signalInterest(Engine.R_UPDATE_ENTITIES, this);
        Engine.getMessagePump().signalInterest(Constants.REMOVE_ALL_RENDER_ENTITIES, this);
        Engine.getMessagePump().signalInterest(Constants.INCREMENT_CAMERA_X_OFFSET, this);
        Engine.getMessagePump().signalInterest(Constants.INCREMENT_CAMERA_Y_OFFSET, this);
        Engine.getMessagePump().signalInterest(Constants.RESET_CAMERA_XY_OFFSET, this);
        Engine.getMessagePump().signalInterest(Constants.CONSOLE_VARIABLE_CHANGED, this);
        Engine.getMessagePump().signalInterest(Constants.SET_CAMERA_ZOOM, this);
        Engine.getMessagePump().signalInterest(Constants.SET_CAMERA_X_OFFSET, this);
        Engine.getMessagePump().signalInterest(Constants.SET_CAMERA_Y_OFFSET, this);
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
                Engine.scheduleLogicTasks(() -> _updatingEntities = false, _collision);
                break;
            case Constants.ADD_GRAPHICS_ENTITY:
                _entities.add((GraphicsEntity) message.getMessageData());
                break;
            case Constants.REMOVE_GRAPHICS_ENTITY:
                _entities.remove((GraphicsEntity)message.getMessageData());
                break;
            case Constants.REMOVE_ALL_RENDER_ENTITIES:
                _entities.clear();
                break;
            case Constants.REGISTER_TEXTURE: {
                if (_headless) break; // We are not running with graphics enabled
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
            case Constants.SET_MAIN_CAMERA:
                _worldCamera = (Camera)message.getMessageData();
                break;
            case Constants.INCREMENT_CAMERA_X_OFFSET:
                _worldCamera.incrementManualOffsetX((Double)message.getMessageData());
                break;
            case Constants.INCREMENT_CAMERA_Y_OFFSET:
                _worldCamera.incrementManualOffsetY((Double)message.getMessageData());
                break;
            case Constants.SET_CAMERA_X_OFFSET:
                _worldCamera.setManualOffsetXY((Double)message.getMessageData(), _worldCamera.getManualOffsetY());
                break;
            case Constants.SET_CAMERA_Y_OFFSET:
                _worldCamera.setManualOffsetXY(_worldCamera.getManualOffsetX(), (Double)message.getMessageData());
                break;
            case Constants.RESET_CAMERA_XY_OFFSET:
                _worldCamera.setManualOffsetXY(0.0, 0.0);
                break;
            case Constants.CONSOLE_VARIABLE_CHANGED:
            {
                ConsoleVariable var = (ConsoleVariable)message.getMessageData();
                if (var.getcvarName().equals(Constants.WORLD_WIDTH) || var.getcvarName().equals(Constants.WORLD_HEIGHT)
                || var.getcvarName().equals(Constants.WORLD_START_X) || var.getcvarName().equals(Constants.WORLD_START_Y)) {
                    int worldX = Engine.getConsoleVariables().find(Constants.WORLD_START_X).getcvarAsInt();
                    int worldY = Engine.getConsoleVariables().find(Constants.WORLD_START_Y).getcvarAsInt();
                    int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
                    int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
                    _graphicsEntities = new QuadTree<>(worldX, worldY, worldWidth > worldHeight ? worldWidth : worldHeight,
                            10, 100);
                }
                break;
            }
            case Constants.SET_CAMERA_ZOOM:
            {
                Double zoom = (Double)message.getMessageData();
                if (zoom > 0) {
                    _zoom = zoom;
                    Engine.getMessagePump().sendMessage(new Message(Constants.CAMERA_ZOOM_CHANGED, _zoom));
                }
                break;
            }
        }
    }

    private void _render(double deltaSeconds)
    {
        if (_updatingEntities) return; // Not done with collisions/movement simulation
        _collision.setDeltaSeconds(deltaSeconds);

        // See if the manual world camera offset x/y values changed
        Pair<Double, Double> cameraOffsetsXY = _worldCamera.getManualOffsetXY();
        if (!cameraOffsetsXY.equals(_lastManualCameraOffsetXY)) {
            _lastManualCameraOffsetXY = cameraOffsetsXY;
            Engine.getMessagePump().sendMessage(new Message(Constants.CAMERA_OFFSET_CHANGED, _lastManualCameraOffsetXY));
        }
        // What values to offset everything in the world by to
        // determine camera-space coordinates
        double xOffset;
        double yOffset;
        Vector3 translate = _worldCamera.getWorldTranslate();
        xOffset = translate.x();
        yOffset = translate.y();
        int xOffsetInt = (int)xOffset;
        int yOffsetInt = (int)yOffset;
        // Now transform everyone to camera space and determine if they
        // are visible and need to be drawn
        double screenX;
        double screenY;
        double rotation;
        double width;
        double height;
        Vector3 location;
        int screenWidth = Engine.getConsoleVariables().find(Constants.SCR_WIDTH).getcvarAsInt();
        int screenHeight = Engine.getConsoleVariables().find(Constants.SCR_HEIGHT).getcvarAsInt();
        _graphicsEntities.clear();
        // Dispatch all collision events and update any graphics entities
        ConcurrentHashMap<Actor, HashSet<Actor>> collisions = _collision.getPreviousCollisions();
        for (Map.Entry<Actor, HashSet<Actor>> entry : collisions.entrySet()) {
            HashSet<Actor> actors = entry.getValue();
            Actor a = entry.getKey();
            if (a instanceof GraphicsEntity && !_headless) {
                GraphicsEntity entity = (GraphicsEntity)a;
                location = entity.getTranslationVec();
                boolean isStatic = entity.isStaticActor();
                double zoom = isStatic ? 1 : _zoom;
                double origX = location.x() * zoom;
                double origY = location.y() * zoom;
                double z = location.z();
                double origWidth = entity.getWidth() * zoom;
                double origHeight = entity.getHeight() * zoom;
                screenX = origX + (isStatic ? 0 : xOffset);
                screenY = origY + (isStatic ? 0 : yOffset);
                location.setXYZ(screenX, screenY, z);
                entity.setWidthHeight(origWidth, origHeight);
                _graphicsEntities.add(entity);
                entity.setScreenVisibility(false); // Assume false for now
                location.setXYZ(origX / zoom, origY / zoom, z);
                entity.setWidthHeight(origWidth / zoom, origHeight / zoom);
            }
            if (actors.size() > 0) {
                a.onActorOverlapped(a, actors);
                HashSet<CollisionEventCallback> callbacks = a.getCollisionEventCallbacks();
                for (CollisionEventCallback callback : callbacks) callback.onActorOverlapped(a, actors);
            }
        }
        /*
        for (GraphicsEntity entity : _entities) {
            location = entity.getTranslationVec();
            boolean isStatic = entity.isStaticActor();
            double origX = location.x();
            double origY = location.y();
            double z = location.z();
            screenX = origX + (isStatic ? 0 : xOffset);
            screenY = origY + (isStatic ? 0 : yOffset);
            location.setXYZ(screenX, screenY, z);
            _graphicsEntities.add(entity);
            entity.setScreenVisibility(false); // Assume false for now
            location.setXYZ(origX, origY, z);
        }
        */
        if (_headless) {
            _renderedScene = true; // Make sure this gets set
            return; // The rest requires a valid graphics context
        }
        // Clear the screen
        _gc.setFill(Color.WHITE);
        _gc.fillRect(0, 0,
                Engine.getConsoleVariables().find(Constants.SCR_WIDTH).getcvarAsFloat(),
                Engine.getConsoleVariables().find(Constants.SCR_HEIGHT).getcvarAsFloat());
        // Reorder scene as needed so things are drawn in the proper order
        //HashSet<GraphicsEntity> actors = _graphicsEntities.getAllActors();
        HashSet<GraphicsEntity> actors = _graphicsEntities.getActorsWithinArea(0, 0, screenWidth, screenHeight);
        //System.out.println("Before: " + _entities.size() + "; after: " + actors.size());
        _determineDrawOrder(actors);
        for (Map.Entry<Integer, ArrayList<GraphicsEntity>> entry : _drawOrder.entrySet())
        {
            for (GraphicsEntity entity : entry.getValue())
            {
                location = entity.getTranslationVec();
                boolean isStatic = entity.isStaticActor();
                double zoom = isStatic ? 1 : _zoom;
                screenX = location.x() * zoom + (isStatic ? 0 : xOffset);
                screenY = location.y() * zoom + (isStatic ? 0 : yOffset);
                width = entity.getWidth() * zoom;
                height = entity.getHeight() * zoom;
                rotation = entity.getRotation();
                /**
                if (screenX + width < 0 || screenX > screenWidth ||
                        screenY + height < 0 || screenY > screenHeight)
                {
                    entity.setScreenVisibility(false);
                }
                 */
                //else
                //{
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
                    entity.render(_gc, screenX, screenY, width, height);
                    //_gc.fillRect(screenX, screenY, width, height);
                }
                //}
            }
        }
        _renderedScene = true;
    }

    private void _determineDrawOrder(HashSet<GraphicsEntity> graphicsEntities)
    {
        for (Map.Entry<Integer, ArrayList<GraphicsEntity>> entry : _drawOrder.entrySet())
        {
            entry.getValue().clear();
        }

        //QuadTree<ActorGraph> actors = _collision.getLatestQuadTree();
        //for (GraphicsEntity entity : _entities)
        for (ActorGraph actor : graphicsEntities)
        {
            GraphicsEntity entity;
            if (actor instanceof GraphicsEntity) entity = (GraphicsEntity)actor;
            else continue;
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
}