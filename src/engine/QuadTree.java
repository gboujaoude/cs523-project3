package engine;

import java.util.ArrayList;
import java.util.HashSet;

// Almost done I think?
public class QuadTree<E extends Actor> {
    private class QuadNode {
        private int _startX;
        private int _startY;
        private int _widthHeight;
        private int _edgeX;
        private int _edgeY;
        private final int _threshold;
        private ArrayList<QuadNode> _children = null;
        private HashSet<E> _objects = new HashSet<>();

        public QuadNode(int startX, int startY, int widthHeight, int threshold) {
            _startX = startX;
            _startY = startY;
            _widthHeight = widthHeight;
            _threshold = threshold;
            _edgeX = _startX + _widthHeight;
            _edgeY = _startY + _widthHeight;
        }

        public boolean add(E a) {
            if (!intersects(a)) return false; // Actor not within this node's region
            _objects.add(a);
            if (_children == null) {
                if (_objects.size() >= _threshold && (_widthHeight / 2) > 0) {
                    _split();
                }
            }
            else {
                for (QuadNode node : _children) node.add(a);
            }
            return true;
        }

        public boolean contains(E a) {
            return _objects.contains(a);
        }

        public boolean remove(E a) {
            boolean removed = _objects.remove(a);
            if (removed && _children != null) {
                for (QuadNode node : _children) node.remove(a);
            }
            return removed;
        }

        public boolean intersects(E a) {
            // Check and see if the object should even be added here
            int width = (int)a.getWidth();
            int height = (int)a.getHeight();
            int widthHeight = width > height ? width : height;
            int x = (int)a.getLocationX();
            int y = (int)a.getLocationY();
            int edgeX = x + widthHeight;
            int edgeY = y + widthHeight;
            boolean startXTest = _startX > x && _startX < edgeX;
            boolean startYTest = _startY > y && _startY < edgeY;
            boolean edgeXTest = _edgeX > x && _edgeX < edgeX;
            boolean edgeYTest = _edgeY > y && _edgeY < edgeY;
            // Test the 4 corners of the region enclosed by this node against the actor's
            // bounds
            return (startXTest && startYTest) || (edgeXTest && startYTest) ||
                    (startXTest && edgeYTest) || (edgeXTest && edgeYTest);
        }

        public void clear() {
            _objects.clear();
            if (_children == null) return;
            for (QuadNode node : _children) {
                node.clear();
            }
        }

        private void _split() {
            _children = new ArrayList<>();
            int newWidthHeight = _widthHeight / 2;
            // Add the 4 new children
            _children.add(new QuadNode(_startX, _startY, newWidthHeight, _threshold));
            _children.add(new QuadNode(_startX + newWidthHeight, _startY, newWidthHeight, _threshold));
            _children.add(new QuadNode(_startX, _startY + newWidthHeight, newWidthHeight, _threshold));
            _children.add(new QuadNode(_startX + newWidthHeight, _startY + newWidthHeight, newWidthHeight, _threshold));
            for (E obj : _objects) {
                for (QuadNode node : _children) {
                    node.add(obj);
                }
            }
        }
    }

    private QuadNode _root;

    public QuadTree(int startX, int startY, int widthHeight) {
        _root = new QuadNode(startX, startY, widthHeight, 10);
    }

    public boolean add(E a) {
        return _root.add(a);
    }

    public boolean contains(E a) {
        return _root.contains(a);
    }

    public boolean remove(E a) {
        return _root.remove(a);
    }
}
