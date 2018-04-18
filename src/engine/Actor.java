package engine;

import engine.math.Vector3;

/**
 * This class sits at the top of the hierarchy of objects
 * which can be registered with the world. Regardless of
 * the visual representation, these objects share several
 * things in common:
 *      1) They can be transformed into the camera's space
 *         in the event that a camera is being used
 *      2) They support movement and changing movement in
 *         the form of speed and acceleration
 *      3) They can be made static, meaning all updates to
 *         their location are directly related to screen space
 *         rather than camera space (i.e., they will not be
 *         pushed off the screen because the camera moved
 *         away from them)
 *
 * @author Justin Hall
 */
public abstract class Actor {
    private Vector3 _translation = new Vector3(0,0,1); // z-component should stay 1 for 2D
    private Vector3 _speed = new Vector3(0, 0, 0);
    private Vector3 _acceleration = new Vector3(0, 0, 0);
    private Vector3 _scaleWidthHeight = new Vector3(1, 1, 1);
    private double _rotationAngle = 0;
    private double _depth = 0; // NOT the same as the translation z-component
    private boolean _isVisibleOnScreen = true; // Updated by renderer
    private boolean _isStaticActor = false; // If true it will not be transformed into camera space
    private boolean _constrainXMovement = false;
    private boolean _constrainYMovement = false;

    /**
     * Tells the renderer whether this actor should be transformed as the camera
     * moves or if its location should always stay relative to screen pixel coordinates
     * @param value true if it should be static and not be transformed by the camera
     *              and false if it should not be static
     */
    public void setAsStaticActor(boolean value)
    {
        _isStaticActor = value;
    }

    public boolean isStaticActor()
    {
        return _isStaticActor;
    }

    /**
     * @return true if the object was visible on the screen during the
     *         last frame or if it was somewhere off screen
     */
    public boolean isVisibleOnScreen()
    {
        return _isVisibleOnScreen;
    }

    // Package private
    void setScreenVisibility(boolean value)
    {
        _isVisibleOnScreen = value;
    }

    /**
     * Adds this actor to the world so that it can be seen and interacted
     * with
     */
    public abstract void addToWorld();

    /**
     * Removes this actor from the world so that it can no longer be seen
     * or interacted with
     */
    public abstract void removeFromWorld();

    /**
     * Sets the x-y speed of the actor in feet per second
     */
    public void setSpeedXY(double speedX, double speedY)
    {
        _speed.setXYZ(speedX, speedY, 0);
    }

    /**
     * Sets the acceleration which will automatically change
     * the speed over time as expected
     */
    public void setAccelerationXY(double accelX, double accelY)
    {
        _acceleration.setXYZ(accelX, accelY, 0);
    }

    /**
     * @param x x location
     * @param y y location
     * @param depth depth, which determines which objects are in front of or behind it
     */
    public void setLocationXYDepth(double x, double y, double depth)
    {
        _translation.setXYZ(x, y, 1);
        _depth = depth;
    }

    /*
     * Various setters and getters whose names reflect which data they work with
     */

    /**
     * Determines if the system should restrict this actor to movement along one
     * axis or both axes (meaning it will remain stationary).
     * @param constrainX true if the actor's movement in the x direction should be disallowed
     * @param constrainY true if the actor's movement in the y direction should be disallowed
     */
    public void setConstrainXYMovement(boolean constrainX, boolean constrainY)
    {
        _constrainXMovement = constrainX;
        _constrainYMovement = constrainY;
    }

    /**
     * Sets the width and height (scale) of this actor
     */
    public void setWidthHeight(double width, double height)
    {
        _scaleWidthHeight.setXYZ(width, height, 0);
    }

    /**
     * Sets the rotation (in degrees) of this actor
     */
    public void setRotation(double angleDeg)
    {
        _rotationAngle = angleDeg;
    }

    /**
     * Gets the world x-location for this actor
     */
    public double getLocationX()
    {
        return _translation.x();
    }

    /**
     * Gets the world y-location for this actor
     */
    public double getLocationY()
    {
        return _translation.y();
    }

    /**
     * Gets the depth of the actor. While this does not affect how the
     * actor ultimately looks, it does determine in what order this actor
     * will be drawn in. Further back actors are drawn sooner and are
     * eclipsed by closer actors and vice versa.
     */
    public double getDepth()
    {
        return _depth;
    }

    /**
     * Gets the rotation in degrees.
     */
    public double getRotation()
    {
        return _rotationAngle;
    }

    /**
     * @return the current speed of this actor along the x-axis
     */
    public double getSpeedX()
    {
        return _speed.x();
    }

    /**
     * @return the current speed of this actor along the y-axis
     */
    public double getSpeedY()
    {
        return _speed.y();
    }

    /**
     * @return the current acceleration of this actor for the x-component
     */
    public double getAccelerationX()
    {
        return _acceleration.x();
    }

    /**
     * @return the current speed of this actor for the y-component
     */
    public double getAccelerationY()
    {
        return _acceleration.y();
    }

    /**
     * @return the width of the actor
     */
    public double getWidth()
    {
        return _scaleWidthHeight.x();
    }

    /**
     * @return the height of the actor
     */
    public double getHeight()
    {
        return _scaleWidthHeight.y();
    }

    /*
     * The following are package private
     */
    Vector3 getTranslationVec()
    {
        return _translation;
    }

    Vector3 getSpeedVec()
    {
        return _speed;
    }

    Vector3 getAccelerationVec()
    {
        return _acceleration;
    }

    Vector3 getScaleVec()
    {
        return _scaleWidthHeight;
    }

    boolean shouldConstrainXMovement()
    {
        return _constrainXMovement;
    }

    boolean shouldConstrainYMovement()
    {
        return _constrainYMovement;
    }
}