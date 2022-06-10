package ru.vsu.css.vorobcov_i_a;

import ru.vsu.css.vorobcov_i_a.types.RTreeRectangle;

import java.util.List;

public class HilbertCurves {

    private static double toHilbertCoordinates(int maxCoordinate, double x, double y) {
        int BITS_PER_DIM = maxCoordinate;
        long res = 0;
        int x1 = (int) x;
        int x2 = (int) y;

        for (int ix = BITS_PER_DIM - 1; ix >= 0; ix--) {
            long h = 0;
            long b1 = (x1 & (1 << ix)) >> ix;
            long b2 = (x2 & (1 << ix)) >> ix;

            if (b1 == 0 && b2 == 0) {
                h = 0;
            } else if (b1 == 0 && b2 == 1) {
                h = 1;
            } else if (b1 == 1 && b2 == 0) {
                h = 3;
            } else if (b1 == 1 && b2 == 1) {
                h = 2;
            }
            res += h << (2 * ix);
        }
        return res;
    }
}
