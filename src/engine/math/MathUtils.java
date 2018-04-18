package engine.math;

import javax.sound.midi.Soundbank;

public class MathUtils {
    /**
     * Solves the equation Ax=b
     */
    public static Vector3 solve(Matrix3 mat, Vector3 ans) {
        return new Vector3(_solve(mat.toArray(), ans.toArray(), 3));
    }

    private static double[] _solve(double[] mat, double[] ans, int n) {
        double[] A = mat;
        double[] b = ans;
        int rowMarker = 0;
        for (int c = 0; c < n; ++c, ++rowMarker) {
            // First we look for the largest value in the column pointed to by i
            int k;
            double max = A[rowMarker * n + c];
            int rowOfMax = rowMarker;
            for (k = rowMarker + 1; k < n; ++k) {
                double elem = A[k * n + c];
                if (max < elem) {
                    max = elem;
                    rowOfMax = k;
                }
            }
            if (max == 0.0) {
                for (int i = 0; i < n; ++i) b[i] = Double.NaN;
                return b;
            }
            // If the max value did not occur in row i, we need to swap row i
            // with the row containing the max
            if (c != rowOfMax) {
                for (int col = 0; col < n; ++col) {
                    double temp = A[c * n + col];
                    A[c * n + col] = A[rowOfMax * n + col];
                    A[rowOfMax * n + col] = temp;
                }
                double temp = b[c];
                b[c] = b[rowOfMax];
                b[rowOfMax] = temp;
            }
            // Now normalize the current pivot row
            double invDiag = 1 / A[c * n + c];
            for (k = 0; k < n; ++k) {
                A[c * n + k] *= invDiag;
            }
            b[c] *= invDiag;
            // Now the lower entries can be removed
            for (int r = c + 1; r < n; ++r) {
                double scale = A[r * n + c];
                for (k = 0; k < n; ++k) {
                    int index = r * n + k;
                    A[index] = A[c * n + k]*scale - A[index];
                }
                b[r] = b[c] * scale - b[r];
            }
        }
        // This is the back substitution step
        for (int row = n - 2; row >= 0; --row) {
            for (int col = row + 1; col < n; ++col) {
                b[row] = b[row] - A[row * n + col] * b[col];
            }
        }
        return b;
    }

    public static void main(String ... args) {
        Vector3 col0 = new Vector3(1,2,3);
        Vector3 col1 = new Vector3(4,3,5);
        Vector3 col2 = new Vector3(7,6,9);
        Matrix3 mat = new Matrix3(col0, col1, col2);
        System.out.println(mat);
        System.out.println(solve(mat, new Vector3(1,2,3)));
        System.out.println(solve(new Matrix3(1,2,3,4,5,6,7,8,9), new Vector3(1,2,3)));
    }
}
