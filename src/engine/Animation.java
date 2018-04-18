package engine;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This represents a simple sequence of animations which can
 * be further divided into categories. For example, you can
 * have the "car drive" category which consists of 10 items.
 * When switched on, the animation will loop between those
 * 10 frames.
 *
 * If you also had another one called "car explode", when you
 * switch to that the animation will instead sample from that
 * sequence of frames.
 *
 * Note that the order that you input the frames determines
 * the order that they are switched to.
 *
 * @author Justin Hall
 */
public class Animation implements MessageHandler {
    private RenderEntity _managedEntity;
    private HashMap<String, Integer> _animationIndexHistories = new HashMap<>();
    private HashMap<String, ArrayList<String>> _animationCategories = new HashMap<>();
    private double _changeRate = 1.0; // If this is 1.0 (for example) it means that every second the frame will change
    private double _elapsedSeconds = 0.0;
    private int _currentAnimIndex = 0;
    private String _currentCategory = "";
    private ArrayList<String> _currentAnimationSequence;
    private boolean _animate;

    /**
     * @param entity entity that this animation manages
     * @param rateOfChange how frequently we want the animation to change - a value
     *                     of 1.0 means the animation changes once per second
     */
    public Animation(RenderEntity entity, double rateOfChange)
    {
        _managedEntity = entity;
        setAnimationRate(rateOfChange);
        Engine.getMessagePump().signalInterest(Singleton.CONSOLE_VARIABLE_CHANGED, this);
        _animate = Boolean.parseBoolean(Engine.getConsoleVariables().find(Singleton.CALCULATE_MOVEMENT).getcvarValue());
    }

    /**
     * Allow the animation to update - if it determines that the next frame
     * needs to begin then it will update the RenderEntity accordingly
     * @param deltaSeconds change in seconds since the last frame
     */
    public void update(double deltaSeconds)
    {
        if (_currentAnimationSequence == null) return; // No images specified
        if (!_animate) return;
        _elapsedSeconds += deltaSeconds;
        if (_elapsedSeconds >= _changeRate)
        {
            _elapsedSeconds = 0.0;
            ++_currentAnimIndex;
            if (_currentAnimIndex >= _currentAnimationSequence.size())
            {
                _currentAnimIndex = 0;
            }
            _managedEntity.setTexture(_currentAnimationSequence.get(_currentAnimIndex));
        }
    }

    /**
     * Sets the rate at which the animation switched from frame to frame.
     * @param rateOfChange a value of 2.0 means that every 2 seconds a new frame is switched to
     */
    public void setAnimationRate(double rateOfChange)
    {
//        if (rateOfChange < 0.0) rateOfChange *= -1;
        _changeRate = rateOfChange;
    }

    /**
     * Sets the current category of animation. Since you can register multiple
     * categories of animation which each have different sets of images, this
     * function provides an easy way to switch between them.
     */
    public void setCategory(String category)
    {
        if (!_animationCategories.containsKey(category))
        {
            System.err.println("ERROR: " + category + " does not exist");
            return;
        }
        _currentAnimationSequence = _animationCategories.get(category);
        //_elapsedSeconds = 0.0;
        if (!_currentCategory.equals("")) _animationIndexHistories.put(_currentCategory, _currentAnimIndex);
        _currentCategory = category;
        _currentAnimIndex = _animationIndexHistories.get(category);
    }

    /**
     * Adds a new animation frame for the given category (category will automatically be
     * added if it doesn't exist)
     * @param category category to place the animation into
     * @param file file to load the animation from
     */
    public void addAnimationFrame(String category, String file)
    {
        if (!_animationCategories.containsKey(category))
        {
            _animationCategories.put(category, new ArrayList<>());
            _animationIndexHistories.put(category, 0);
        }
        _animationCategories.get(category).add(file);
        Engine.getMessagePump().sendMessage(new Message(Singleton.REGISTER_TEXTURE, file));
        // If we do not have a current animation sequence, set it to the recently
        // added category and make sure to set the entity's texture
        if (_currentAnimationSequence == null)
        {
            _currentAnimationSequence = _animationCategories.get(category);
            _managedEntity.setTexture(_currentAnimationSequence.get(0));
        }
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.getMessageName())
        {
            case Singleton.CONSOLE_VARIABLE_CHANGED:
            {
                ConsoleVariable cvar = (ConsoleVariable)message.getMessageData();
                if (cvar.getcvarName().equals(Singleton.CALCULATE_MOVEMENT))
                {
                    _animate = Boolean.parseBoolean(Engine.getConsoleVariables().find(Singleton.CALCULATE_MOVEMENT).getcvarValue());
                }
            }
        }
    }
}
