package engine.math;

/**
 * Represents a 3 dimensional vector
 *
 * @author Justin Hall
 */
public class Vector3 {
    private double _x = 0.0;
    private double _y = 0.0;
    private double _z = 0.0;

    public Vector3()
    {
    }

    @Override
    public String toString() {
        return "[" + _x + ", " + _y + ", " + _z + "]";
    }

    public Vector3(double val)
    {
        setXYZ(val, val, val);
    }

    public Vector3(double x, double y, double z)
    {
        setXYZ(x, y, z);
    }

    public Vector3(double ... vals) {
        if (vals.length != 3) throw new IllegalArgumentException("Array of invalid size passed to Vector3");
        setXYZ(vals[0], vals[1], vals[2]);
    }

    /**
     * Copy constructor
     * @param other vector to copy from
     */
    public Vector3(Vector3 other)
    {
        setXYZ(other._x, other._y, other._z);
    }

    /**
     * Allows you to set all 3 values at once
     */
    public void setXYZ(double x, double y, double z)
    {
        _x = x;
        _y = y;
        _z = z;
    }

    /**
     * Getters for x, y and z
     */
    public double x()
    {
        return _x;
    }

    public double y()
    {
        return _y;
    }

    public double z()
    {
        return _z;
    }

    public double magnitude() {
        return Math.sqrt(_x*_x + _y*_y + _z*_z);
    }

    public void normalizeThis() {
        multiplyThis(1 / magnitude());
    }

    /**
     * Multiply the vector by a scalar value
     * @param scalar scalar value
     */
    public Vector3 multiply(double scalar)
    {
        return new Vector3(_x*scalar, _y*scalar, _z*scalar);
    }

    /**
     * Same concept as multiply(scalar), but this one is more efficient
     * because it modifies this vector rather than creating a new one.
     * @param scalar scalar value
     */
    public void multiplyThis(double scalar)
    {
        _x *= scalar;
        _y *= scalar;
        _z *= scalar;
    }

    /**
     * Performs the dot product between this vector and another vector.
     * @param other vector to perform the dot product on
     */
    public double dot(Vector3 other)
    {
        return _x*other._x + _y*other._y + _z*other._z;
    }

    /**
     * Computes the cross product between this vector and another vector
     * and returns the result
     * @return vector orthogonal to this and the other vector
     */
    public Vector3 cross(Vector3 other)
    {
        return new Vector3(_y*other._z - _z*other._y,
                           _z*other._x - _x*other._z,
                           _x*other._y - _y*other._x);
    }

    public Vector3 subtract(Vector3 other)
    {
        return new Vector3(_x - other._x, _y - other._y, _z - other._z);
    }

    public void subtractThis(Vector3 other)
    {
        _x -= other._x;
        _y -= other._y;
        _z -= other._z;
    }

    public Vector3 add(Vector3 other)
    {
        return new Vector3(_x + other._x, _y + other._y, _z + other._z);
    }

    public void addThis(Vector3 other)
    {
        _x += other._x;
        _y += other._y;
        _z += other._z;
    }

    public double[] toArray() {
        double[] result = new double[3];
        result[0] = _x;
        result[1] = _y;
        result[2] = _z;
        return result;
    }
}
