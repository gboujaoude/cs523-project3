package engine.math;

import java.util.Arrays;

/**
 * A simple Matrix3x3 implementation with some common operations,
 * though many common operations have been left out.
 *
 * Use the static methods "createTransMat", "createRotXMat", "createRotYMat",
 * "createRotZMat" and "createScaleMat" to create matrices representing
 * those transformations.
 *
 * The format for this is as follows:
 *      row0 indices: 0,1,2
 *      row1 indices: 3,4,5
 *      row2 indices: 6,7,8
 *
 * @author Justin Hall
 */
public class Matrix3 {
    // Identity matrix
    public static final Matrix3 IDENTITY = new Matrix3(1.0);

    // Zero matrix
    public static final Matrix3 ZERO = new Matrix3(0.0);

    /**
     * Creates a translation matrix - only takes x and y because
     * the translation will only work if the final element of the
     * matrix is 1. So, if you want a translation matrix for 3D then
     * you need to upgrade to a Matrix4x4.
     */
    public static Matrix3 createTransMat(double x, double y)
    {
        Matrix3 mat = new Matrix3(1.0);
        mat.setTranslate(x, y);
        return mat;
    }

    /**
     * Creates a matrix whose purpose is to scale objects in the x & y directions.
     */
    public static Matrix3 createScaleMat(double scaleX, double scaleY)
    {
        Matrix3 mat = new Matrix3(1.0);
        mat.setScale(scaleX, scaleY);
        return mat;
    }

    /**
     * Creates a matrix which rotates object about the x-axis by
     * the given angle (in degrees)
     * @param angleDeg angle in degrees
     */
    public static Matrix3 createRotXMatrix(double angleDeg)
    {
        Matrix3 mat = new Matrix3(1.0);
        mat.setRotXAngle(angleDeg);
        return mat;
    }

    /**
     * Creates a matrix which rotates object about the y-axis by
     * the given angle (in degrees)
     * @param angleDeg angle in degrees
     */
    public static Matrix3 createRotYMatrix(double angleDeg)
    {
        Matrix3 mat = new Matrix3(1.0);
        mat.setRotYAngle(angleDeg);
        return mat;
    }

    /**
     * Creates a matrix which rotates object about the z-axis by
     * the given angle (in degrees)
     * @param angleDeg angle in degrees
     */
    public static Matrix3 createRotZMatrix(double angleDeg)
    {
        Matrix3 mat = new Matrix3(1.0);
        mat.setRotZAngle(angleDeg);
        return mat;
    }

    /**
     * The format for this is as follows:
     *      row0 indices: 0,1,2
     *      row1 indices: 3,4,5
     *      row2 indices: 6,7,8
     */
    private final double _mat[] = new double[9];

    /**
     * Creates a new matrix with all _diagonal_ elements set to val
     * @param val value of all diagonal matrix elements
     */
    public Matrix3(double val)
    {
        setAll(0);
        set(val);
    }

    /**
     * Initializes the matrix with its column elements set to be
     * the col0, col1, and col2.
     */
    public Matrix3(Vector3 col0, Vector3 col1, Vector3 col2)
    {
        set(col0, col1, col2);
    }

    /**
     * Sets a matrix directly from an array whose length matches the dimensions
     * of this array.
     * @param elems 9-length array where the first 3 elements are row0, next 3 row1, last 3 row2
     */
    public Matrix3(double ... elems) {
        if (elems.length != 9) throw new IllegalArgumentException("Array of invalid size given to Matrix3");
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                setElemAt(elems[3 * row + col], row, col);
            }
        }
    }

    /**
     * Matrix copy constructor
     */
    public Matrix3(Matrix3 other)
    {
        set(other);
    }

    /**
     * Transforms this matrix into a rotation matrix - overwrites
     * whatever system is already in place
     * @param angleDeg angle in degrees
     */
    public void setRotXAngle(double angleDeg)
    {
        double degRad = Math.toRadians(angleDeg);
        double cos0 = Math.cos(degRad);
        double sin0 = Math.sin(degRad);
        setAll(0.0);
        set(1.0);
        _mat[4] = cos0;
        _mat[5] = -sin0;
        _mat[7] = sin0;
        _mat[8] = cos0;
    }

    /**
     * Transforms this matrix into a rotation matrix - overwrites
     * whatever system is already in place
     * @param angleDeg angle in degrees
     */
    public void setRotYAngle(double angleDeg)
    {
        double degRad = Math.toRadians(angleDeg);
        double cos0 = Math.cos(degRad);
        double sin0 = Math.sin(degRad);
        setAll(0.0);
        set(1.0);
        _mat[0] = cos0;
        _mat[2] = sin0;
        _mat[6] = -sin0;
        _mat[8] = cos0;
    }

    /**
     * Transforms this matrix into a rotation matrix - overwrites
     * whatever system is already in place
     * @param angleDeg angle in degrees
     */
    public void setRotZAngle(double angleDeg)
    {
        double degRad = Math.toRadians(angleDeg);
        double cos0 = Math.cos(degRad);
        double sin0 = Math.sin(degRad);
        setAll(0.0);
        set(1.0);
        _mat[0] = cos0;
        _mat[1] = -sin0;
        _mat[3] = sin0;
        _mat[4] = cos0;
    }

    /**
     * Sets the translation component of this Matrix - overrides
     * whatever system is already in place
     */
    public void setTranslate(double x, double y)
    {
        setAll(0.0);
        set(1.0);
        _mat[2] = x;
        _mat[5] = y;
        _mat[8] = 1.0;
    }

    /**
     * Transforms this matrix into a scaling matrix - overwrites
     * whatever system is already in place
     */
    public void setScale(double scaleX, double scaleY)
    {
        setAll(0.0);
        set(1.0);
        _mat[0] = scaleX;
        _mat[4] = scaleY;
    }

    /**
     * Allows you to set the element at Matrix3[row][col] to be equal to val
     * @param val new value
     * @param row row index
     * @param col column index
     */
    public void setElemAt(double val, int row, int col)
    {
        _mat[_convertToInternalIndex(row, col)] = val;
    }

    /**
     * @return element at Matrix3[row][col]
     */
    public double getElemAt(int row, int col)
    {
        return _mat[_convertToInternalIndex(row, col)];
    }

    public double determinant()
    {
        return _mat[0] * (_mat[4] * _mat[8] - _mat[5] * _mat[7]) -
               _mat[1] * (_mat[3] * _mat[8] - _mat[5] * _mat[6]) +
               _mat[2] * (_mat[3] * _mat[7] - _mat[4] * _mat[6]);
    }

    /**
     * Multiply this matrix by a scalar value
     */
    public void multiply(double scalar)
    {
        for (int i = 0; i < _mat.length; ++i) _mat[i] *= scalar;
    }

    /**
     * Does not allocate a new vector! Multiplies this matrix by a vector^T
     * and stores the result in the given vector
     * @param vec vec to multiply this matrix by
     * @param result where to store the results of the computations
     */
    public void multiply(Vector3 vec, Vector3 result)
    {
        double x = _mat[0] * vec.x() + _mat[1] * vec.y() + _mat[2] * vec.z();
        double y = _mat[3] * vec.x() + _mat[4] * vec.y() + _mat[5] * vec.z();
        double z = _mat[6] * vec.x() + _mat[7] * vec.y() + _mat[8] * vec.z();
        result.setXYZ(x, y, z);
    }

    /**
     * Multiply this matrix by a vector^T and return a new vector
     * @return new vector containing the result
     */
    public Vector3 multiply(Vector3 vec)
    {
        Vector3 result = new Vector3(0);
        multiply(vec, result);
        return result;
    }

    /**
     * Multiply this matrix by another matrix and returns a new matrix with the result
     * @param other matrix to multiply this by
     */
    public Matrix3 multiply(Matrix3 other)
    {
        Matrix3 result = new Matrix3(0);
        _multiply(other, result);
        return result;
    }

    /**
     * The format for this is as follows:
     *      row0 indices: 0,1,2
     *      row1 indices: 3,4,5
     *      row2 indices: 6,7,8
     */
    private void _multiply(Matrix3 other, Matrix3 result)
    {
        double m00 = _mat[0] * other._mat[0] + _mat[1] * other._mat[3] + _mat[2] * other._mat[6];
        double m01 = _mat[0] * other._mat[1] + _mat[1] * other._mat[4] + _mat[2] * other._mat[7];
        double m02 = _mat[0] * other._mat[2] + _mat[1] * other._mat[5] + _mat[2] * other._mat[8];

        double m10 = _mat[3] * other._mat[0] + _mat[4] * other._mat[3] + _mat[5] * other._mat[6];
        double m11 = _mat[3] * other._mat[1] + _mat[4] * other._mat[4] + _mat[5] * other._mat[7];
        double m12 = _mat[3] * other._mat[2] + _mat[4] * other._mat[5] + _mat[5] * other._mat[8];

        double m20 = _mat[6] * other._mat[0] + _mat[7] * other._mat[3] + _mat[8] * other._mat[6];
        double m21 = _mat[6] * other._mat[1] + _mat[7] * other._mat[4] + _mat[8] * other._mat[7];
        double m22 = _mat[6] * other._mat[2] + _mat[7] * other._mat[5] + _mat[8] * other._mat[8];
        result.setElemAt(m00, 0, 0);
        result.setElemAt(m01, 0, 1);
        result.setElemAt(m02, 0, 2);

        result.setElemAt(m10, 1, 0);
        result.setElemAt(m11, 1, 1);
        result.setElemAt(m12, 1, 2);

        result.setElemAt(m20, 2, 0);
        result.setElemAt(m21, 2, 1);
        result.setElemAt(m22, 2, 2);
    }

    /**
     * Multiplies this matrix by another matrix and overwrites the elements of (this)
     * with the results
     */
    public void multiplyThis(Matrix3 other)
    {
        _multiply(other, this);
    }

    /**
     * Sets _all_ elements of this matrix to val, not just the diagonal
     */
    public void setAll(double val)
    {
        for (int i = 0; i < _mat.length; ++i) _mat[i] = val;
    }

    /**
     * Sets all diagonal elements of the matrix
     * @param val value to set all matrix elements to
     */
    public void set(double val)
    {
        _mat[0] = val;
        _mat[4] = val;
        _mat[8] = val;
    }

    /**
     * Initializes the columns of the matrix to be vectors. The
     * result is Matrix3 = [col0, col1, col2]
     */
    public void set(Vector3 col0, Vector3 col1, Vector3 col2)
    {
        _mat[0] = col0.x();
        _mat[3] = col0.y();
        _mat[6] = col0.z();

        _mat[1] = col1.x();
        _mat[4] = col1.y();
        _mat[7] = col1.z();

        _mat[2] = col2.x();
        _mat[5] = col2.y();
        _mat[8] = col2.z();
    }

    /**
     * Sets all the elements of this matrix to be equal to the elements
     * of the other matrix.
     * @param other matrix to copy
     */
    public void set(Matrix3 other)
    {
        for (int i = 0; i < _mat.length; ++i) _mat[i] = other._mat[i];
    }

    private int _convertToInternalIndex(int x, int y)
    {
        int index = 3 * x + y;
        if (index >= _mat.length)
        {
            throw new IllegalArgumentException("ERROR: Matrix3 indices out of range");
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j) {
                str.append(getElemAt(i, j));
                str.append(" ");
            }
            str.append("\n");
        }
        return str.toString();
    }

    public double[] toArray() {
        return Arrays.copyOf(_mat, _mat.length);
    }
}
