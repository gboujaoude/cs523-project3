package engine;

import javafx.scene.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is completely thread safe!
 *
 * A scene manager maintains references to all render entities, logic entities,
 * pulse entities and GUI elements that should be active during a given
 * "frame". In this case a frame is just a bundle of components that need
 * to be on screen/processing data/within the world at the same time. A frame
 * may be active indefinitely, or it may only be active for a short period of time
 * before being phased out in favor of a new frame, or it may by active/inactive many
 * times during the span of the program.
 *
 * @author Justin Hall
 */
public class SceneManager {
    private final Object _defaultValue = new Object();
    private final ConcurrentHashMap<GraphicsEntity, Object> _renderEntities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PulseEntity, Object> _pulseEntities = new ConcurrentHashMap<>();
    // This needs to be an array list because the order that the GUI elements are inserted determines
    // the order they are displayed, but a hash map can't guarantee anything about the order
    private final ArrayList<Node> _guiElements = new ArrayList<>();
    private final ConcurrentHashMap<LogicEntity, Object> _logicEntities = new ConcurrentHashMap<>();

    /**
     * Adds a RenderEntity to the frame but does not activate it
     */
    public void add(GraphicsEntity entity) {
        _renderEntities.putIfAbsent(entity, _defaultValue);
    }

    /**
     * Adds a pulse entity to the frame but does not activate it
     */
    public void add(PulseEntity entity) {
        _pulseEntities.putIfAbsent(entity, _defaultValue);
    }

    /**
     * Adds a gui element to the frame but does not activate it
     */
    public void add(Node guiElement) {
        synchronized(_guiElements) {
            _guiElements.add(guiElement);
        }
    }

    /**
     * Adds a logic entity to the frame but does not activate it
     */
    public void add(LogicEntity entity) {
        _logicEntities.putIfAbsent(entity, _defaultValue);
    }

    /**
     * Activates all entities that have been added to this frame. This means
     * that all pulse, render, logic entities and GUI elements will be registered
     * with the engine.
     */
    public void activateAll() {
        activateRenderEntities();
        activatePulseEntities();
        activateGUIElements();
        activateLogicEntities();
    }

    /**
     * Deactivates all entities that are associated with this frame. This will not
     * remove them from the internal data structures of the Frame object, but the
     * engine will be told to remove them from its internal state.
     */
    public void deactivateAll() {
        deactivateRenderEntities();
        deactivatePulseEntities();
        deactivateGUIElements();
        deactivateLogicEntities();
    }

    /**
     * Activates only the render entities
     */
    public void activateRenderEntities() {
        for (Map.Entry<GraphicsEntity, Object> entry : _renderEntities.entrySet()) {
            entry.getKey().addToWorld();
        }
    }

    /**
     * Activates only the pulse entities
     */
    public void activatePulseEntities() {
        for (Map.Entry<PulseEntity, Object> entry : _pulseEntities.entrySet()) {
            _sendMessage(Singleton.ADD_PULSE_ENTITY, entry.getKey());
        }
    }

    /**
     * Activates only the GUI elements
     */
    public void activateGUIElements() {
        synchronized (_guiElements) {
            for (Node node : _guiElements) {
                _sendMessage(Singleton.ADD_UI_ELEMENT, node);
            }
        }
    }

    /**
     * Activates only the logic entities
     */
    public void activateLogicEntities() {
        for (Map.Entry<LogicEntity, Object> entry : _logicEntities.entrySet()) {
            _sendMessage(Singleton.ADD_LOGIC_ENTITY, entry.getKey());
        }
    }

    /**
     * Deactivates only the render entities
     */
    public void deactivateRenderEntities() {
        for (Map.Entry<GraphicsEntity, Object> entry : _renderEntities.entrySet()) {
            entry.getKey().removeFromWorld();
        }
    }

    /**
     * Deactivates only the pulse entities
     */
    public void deactivatePulseEntities() {
        for (Map.Entry<PulseEntity, Object> entry : _pulseEntities.entrySet()) {
            _sendMessage(Singleton.REMOVE_PULSE_ENTITY, entry.getKey());
        }
    }

    /**
     * Deactivates only the GUI elements
     */
    public void deactivateGUIElements() {
        synchronized (_guiElements) {
            for (Node node : _guiElements) {
                _sendMessage(Singleton.REMOVE_UI_ELEMENT, node);
            }
        }
    }

    /**
     * Deactivates only the logic entities
     */
    public void deactivateLogicEntities() {
        for (Map.Entry<LogicEntity, Object> entry : _logicEntities.entrySet()) {
            _sendMessage(Singleton.REMOVE_LOGIC_ENTITY, entry.getKey());
        }
    }

    /**
     * Keep in mind that any changes you make to the returned list (adding/removing) will
     * not have any impact on the internal state of the frame itself.
     *
     * @return HashSet of all render entities at the time of calling - this is thread
     *         safe but weakly consistent, meaning the list you get is only a snapshot
     *         of the state of the frame when you asked for it.
     */
    public ArrayList<GraphicsEntity> getGraphicsEntities() {
        ArrayList<GraphicsEntity> entities = new ArrayList<>();
        for (Map.Entry<GraphicsEntity, Object> entry : _renderEntities.entrySet()) {
            entities.add(entry.getKey());
        }
        return entities;
    }

    /**
     * Keep in mind that any changes you make to the returned list (adding/removing) will
     * not have any impact on the internal state of the frame itself.
     *
     * @return HashSet of all pulse entities at the time of calling - this is thread
     *         safe but weakly consistent, meaning the list you get is only a snapshot
     *         of the state of the frame when you asked for it.
     */
    public ArrayList<PulseEntity> getPulseEntities() {
        ArrayList<PulseEntity> entities = new ArrayList<>();
        for (Map.Entry<PulseEntity, Object> entry : _pulseEntities.entrySet()) {
            entities.add(entry.getKey());
        }
        return entities;
    }

    /**
     * Keep in mind that any changes you make to the returned list (adding/removing) will
     * not have any impact on the internal state of the frame itself.
     *
     * @return HashSet of all GUI elements at the time of calling - this is thread
     *         safe but weakly consistent, meaning the list you get is only a snapshot
     *         of the state of the frame when you asked for it.
     */
    public ArrayList<Node> getGUIElements() {
        synchronized (_guiElements) {
            return new ArrayList<>(_guiElements);
        }
    }

    /**
     * Keep in mind that any changes you make to the returned list (adding/removing) will
     * not have any impact on the internal state of the frame itself.
     *
     * @return HashSet of all GUI elements at the time of calling - this is thread
     *         safe but weakly consistent, meaning the list you get is only a snapshot
     *         of the state of the frame when you asked for it.
     */
    public ArrayList<LogicEntity> getLogicEntities() {
        ArrayList<LogicEntity> entities = new ArrayList<>();
        for (Map.Entry<LogicEntity, Object> entry : _logicEntities.entrySet()) {
            entities.add(entry.getKey());
        }
        return entities;
    }

    private void _sendMessage(String subject, Object entity) {
        Engine.getMessagePump().sendMessage(new Message(subject, entity));
    }
}
