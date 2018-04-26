package application.utils;

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
}
