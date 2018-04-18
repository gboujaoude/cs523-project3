package engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

// Almost done I think?
public class QuadTree<E extends Actor> implements Iterable<E> {
    private class QuadNode {
        private int _startX;
        private int _startY;
        private int _widthHeight;
        private int _edgeX;
        private int _edgeY;
        private final int _loadFactor;
        private final int _threshold;
        private ArrayList<QuadNode> _children = null;
        private HashSet<E> _objects = new HashSet<>();

        public QuadNode(int startX, int startY, int widthHeight, int loadFactor, int threshold) {
            _startX = startX;
            _startY = startY;
            _widthHeight = widthHeight;
            _loadFactor = loadFactor;
            _threshold = threshold;
            _edgeX = _startX + _widthHeight;
            _edgeY = _startY + _widthHeight;
        }

        public boolean add(E a) {
            if (!intersects(a)) return false; // Actor not within this node's region
            if (!_objects.add(a)) return true; // Already added
            if (_children == null) {
                if (_objects.size() >= _loadFactor && (_widthHeight / 2) > _threshold) {
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

        public int size() {
            return _objects.size();
        }

        public boolean intersects(E a) {
            // Check and see if the object should even be added here
            int width = (int)a.getWidth();
            int height = (int)a.getHeight();
            int x = (int)a.getLocationX();
            int y = (int)a.getLocationY();
            return intersects(x, y, width, height);
        }

        public boolean intersects(int x, int y, int width, int height) {
            // Check and see if the object should even be added here
            int widthHeight = width > height ? width : height;
            int edgeX = x + widthHeight;
            int edgeY = y + widthHeight;
            /**
            boolean insideOtherStartXTest = x >= _startX && x <= _edgeX;
            boolean insideOtherStartYTest = y >= _startY && y <= _edgeY;
            boolean insideOtherEdgeXTest = edgeX >= _startX && edgeX <= _edgeX;
            boolean insideOtherEdgeYTest = edgeY >= _startY && edgeY <= _edgeY;

            boolean insideThisStartXTest = _startX >= x && _startX <= edgeX;
            boolean insideThisStartYTest = _startY >= y && _startY <= edgeY;
            boolean insideThisEdgeXTest = _edgeX >= x && _edgeX <= edgeX;
            boolean insideThisEdgeYTest = _edgeY >= y && _edgeY <= edgeY;
            // Test the 4 corners of the region enclosed by this node against the actor's
            // bounds (we have 2 cases: this node is within the given volume, the given volume is within
            // this node, or the two areas are just intersecting)
            return (insideThisStartXTest && insideThisStartYTest) || (insideThisEdgeXTest && insideThisStartYTest) ||
                    (insideThisStartXTest && insideThisEdgeYTest) || (insideThisEdgeXTest && insideThisEdgeYTest) ||

                    (insideOtherStartXTest && insideOtherStartYTest) || (insideOtherEdgeXTest && insideOtherStartYTest) ||
                    (insideOtherStartXTest && insideOtherEdgeYTest) || (insideOtherEdgeXTest && insideOtherEdgeYTest);
             */
            if ((_startX > edgeX) || (x > _edgeX) || (_startY > edgeY) || (y > _edgeY)) return false;
            return true;
        }

        public void clear() {
            _objects.clear();
            if (_children == null) return;
            for (QuadNode node : _children) {
                node.clear();
            }
        }

        public HashSet<E> getActors() {
            return _objects;
        }

        public ArrayList<QuadNode> getChildren() {
            return _children;
        }

        private void _split() {
            _children = new ArrayList<>();
            int newWidthHeight = _widthHeight / 2;
            // Add the 4 new children
            _children.add(new QuadNode(_startX, _startY, newWidthHeight, _loadFactor, _threshold));
            _children.add(new QuadNode(_startX + newWidthHeight, _startY, newWidthHeight, _loadFactor, _threshold));
            _children.add(new QuadNode(_startX, _startY + newWidthHeight, newWidthHeight, _loadFactor, _threshold));
            _children.add(new QuadNode(_startX + newWidthHeight, _startY + newWidthHeight, newWidthHeight,
                    _loadFactor, _threshold));
            for (E obj : _objects) {
                for (QuadNode node : _children) {
                    node.add(obj);
                }
            }
        }
    }

    public class LeafIterator implements Iterator<HashSet<E>> {
        private LinkedList<HashSet<E>> _lists = new LinkedList<>();

        private LeafIterator(QuadNode node) {
            _buildRecursive(node);
        }

        @Override
        public boolean hasNext() {
            return _lists.size() > 0;
        }

        @Override
        public HashSet<E> next() {
            return _lists.pollFirst();
        }

        private void _buildRecursive(QuadNode node) {
            ArrayList<QuadNode> children = node.getChildren();
            if (children == null) {
                _lists.add(node.getActors());
                return;
            }
            for (QuadNode childNode : children) _buildRecursive(childNode);
        }
    }

    private QuadNode _root;
    private final int _loadFactor;
    private final int _splitThreshold;

    public QuadTree(int startX, int startY, int widthHeight) {
        _loadFactor = 10;
        _splitThreshold = 100;
        _root = new QuadNode(startX, startY, widthHeight, _loadFactor, _splitThreshold);
    }

    public QuadTree(int startX, int startY, int widthHeight,
                    int loadFactor, int splitThreshold) {
        _loadFactor = loadFactor;
        _splitThreshold = splitThreshold;
        _root = new QuadNode(startX, startY, widthHeight, _loadFactor, _splitThreshold);
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

    public void clear() {
        _root.clear();
    }

    public int size() {
        return _root.size();
    }

    HashSet<E> getAllActors() {
        return _root.getActors();
    }

    HashSet<E> getActorsWithinArea(int x, int y, int width, int height) {
        HashSet<E> set = new HashSet<>(size());
        _getActorsWithinAreaRecursive(set, _root, x, y, width, height);
        return set;
    }

    public Iterator<HashSet<E>> getLeafIterator() {
        return new LeafIterator(_root);
    }

    @Override
    public Iterator<E> iterator() {
        return _root.getActors().iterator();
    }

    private void _getActorsWithinAreaRecursive(HashSet<E> set, QuadNode node, int x, int y, int width, int height) {
        ArrayList<QuadNode> childNodes = node.getChildren();
        boolean intersects = node.intersects(x, y, width, height);
        if (childNodes == null && intersects) set.addAll(node.getActors());
        else if (intersects) {
            for (QuadNode childNode : childNodes) {
                _getActorsWithinAreaRecursive(set, childNode, x, y, width, height);
            }
        }
    }
}
