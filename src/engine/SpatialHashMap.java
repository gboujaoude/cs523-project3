package engine;

import java.util.ArrayList;

/**
 * This data structure allows you to insert based on locations
 * within a world. Their location/width/height determine which buckets
 * they go into, and this same location/width/height can be used to retrieve
 * them.
 */
public class SpatialHashMap {
    private class Bucket {
        private int _iteration;
        private ArrayList<Actor> _actors = new ArrayList<>();

        // Used to determine if this bucket is "dirty", meaning it needs
        // to be cleared out
        public int getIterationMarker() {
            return _iteration;
        }

        public boolean add(Actor a) {
            return _actors.add(a);
        }

        public ArrayList<Actor> get() {
            return _actors;
        }

        public void clear() {
            _actors.clear();
        }
    }

    private static final double _BOX_SIZE = 50.0;
    private int _numBoxesWidth;
    private int _numBoxesHeight;
    private Bucket[][] _buckets;

    public SpatialHashMap(int width, int height) {
        _numBoxesWidth = (int)Math.ceil(width / _BOX_SIZE);
        _numBoxesHeight = (int)Math.ceil(height / _BOX_SIZE);
        _buckets = new Bucket[_numBoxesWidth][_numBoxesHeight];
        _allocBoxes();
    }

    public void add(Actor a) {
        int width = _determineWidth(a);
        int height = _determineHeight(a);
        int x = _determineXLoc(a);
        int y = _determineYLoc(a);
    }

    private int _determineXLoc(Actor a) {
        double xLoc = a.getLocationX();
        double remainder = xLoc % _BOX_SIZE;
        if (remainder == 0) return (int)xLoc;
        xLoc -= remainder;
        return (int)xLoc;
    }

    private int _determineYLoc(Actor a) {
        double yLoc = a.getLocationY();
        double remainder = yLoc % _BOX_SIZE;
        if (remainder == 0) return (int)yLoc;
        yLoc -= remainder;
        return (int)yLoc;
    }

    // Ensures that the width is a power of the box size
    private int _determineWidth(Actor a) {
        double width = a.getWidth();
        double remainder = width % _BOX_SIZE;
        if (remainder == 0) return (int)width;
        width += (_BOX_SIZE - remainder);
        return (int)width;
    }

    private int _determineHeight(Actor a) {
        double height = a.getHeight();
        double remainder = height % _BOX_SIZE;
        if (remainder == 0) return (int)height;
        height += (_BOX_SIZE - remainder);
        return (int)height;
    }

    private void _allocBoxes() {
        for (int x = 0; x < _numBoxesWidth; ++x) {
            for (int y = 0; y < _numBoxesHeight; ++y) {
                _buckets[x][y] = new Bucket();
            }
        }
    }
}
