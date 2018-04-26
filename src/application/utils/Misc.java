package application.utils;

import java.util.ArrayList;
import java.util.Random;

public class Misc {
    private static Random _rng = new Random();

    public static double offset(double val, double offsetMin, double offsetMax) {
        int sign;
        if (_rng.nextDouble() > 0.5) {
            sign = 1;
        } else {
            sign = -1;
        }
        return val + sign * (offsetMin + (offsetMax- offsetMin) * _rng.nextDouble());
    }

    /**
     * Creates a list with doubles on the range [start, end] where each element is at a linear
     * offset from the previous element. Ex: 1.0, 1.2, 1.4, 1.6,... --> offset = 0.2
     * @param start start (inclusive)
     * @param end end (inclusive)
     * @param numElements number of elements in the list
     */
    public static ArrayList<Double> linearSpacing(double start, double end, int numElements) {
        return linearSpacing(start, end, numElements, new ArrayList<>());
    }

    /**
     * Creates a list with doubles on the range [start, end] where each element is at a linear
     * offset from the previous element. Ex: 1.0, 1.2, 1.4, 1.6,... --> offset = 0.2
     * @param start start (inclusive)
     * @param end end (inclusive)
     * @param numElements number of elements in the list
     * @param list existing list to reuse (does not allocate new memory)
     */
    public static ArrayList<Double> linearSpacing(double start, double end,
                                                  int numElements, ArrayList<Double> list) {
        list.clear();
        double current = 0;
        double distance = end - start;
        double offset = distance / (numElements - 1);
        for (int i = 0; i < numElements; i++) {
            list.add(start + i*offset);
        }
        return list;
    }
}
