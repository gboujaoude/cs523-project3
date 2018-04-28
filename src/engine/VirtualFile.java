package engine;

import java.util.Collection;

/**
 * A virtual file abstracts the concept of reading and writing to files
 * by not allowing the user to distinguish the difference between writing
 * to memory and actually flushing to file.
 *
 * @author Justin Hall
 */
public class VirtualFile {
    private Filesystem.FileDescriptor _descriptor;
    private Filesystem.FileHandle _handle;
    private volatile boolean _isValid = false;

    VirtualFile(Filesystem.FileDescriptor descriptor, Filesystem.FileHandle handle) {
        _descriptor = descriptor;
        _handle = handle;
        _isValid = true;
    }

    public void add(char c) {
        _validityCheck();
        _descriptor.write(c);
    }

    public void add(String str) {
        _validityCheck();
        _descriptor.write(str);
    }

    public void addAll(char ... chars) {
        _validityCheck();
        _descriptor.write(chars);
    }

    public void addAll(Collection<Character> chars) {
        _validityCheck();
        _descriptor.write(chars);
    }

    public char get(int index) {
        _validityCheck();
        return _descriptor.get(index);
    }

    public void set(int index, char value) {
        _validityCheck();
        _descriptor.set(index, value);
    }

    public void close() {
        synchronized (this) {
            _validityCheck();
            _isValid = false;
            _descriptor.decrementRefCount();
            if (!_descriptor.isOpen()) {
                new Thread(() ->
                {
                    _descriptor.flush();
                }).start();
            }
        }
    }

    /**
     * @return returns the length of the file (in bytes)
     */
    public int size() {
        return _descriptor.getFileSize();
    }

    private void _validityCheck() {
        if (!_isValid) throw new RuntimeException("VirtualFile used after close() called");
    }
}
