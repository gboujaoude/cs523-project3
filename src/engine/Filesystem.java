package engine;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The file system provides a very thin abstraction layer between the
 * engine and the underlying Java file I/O.
 *
 * @author Justin Hall
 */
public class Filesystem {
    private static final int _maxFileHandles = 100;
    private final FileHandle[] _handles = new FileHandle[_maxFileHandles];
    private final FileDescriptor[] _descriptors = new FileDescriptor[_maxFileHandles];
    private final ArrayList<Character> _tempBuffer = new ArrayList<>(100);
    private String _cwd;
    private volatile boolean _isInitialized = false;

    // Package-private
    class FileDescriptor {
        private FileHandle _handle;
        private BufferedWriter _writer;
        private BufferedReader _reader;
        private final ArrayList<Character> _buffer = new ArrayList<>(100);
        private final AtomicInteger _refCount = new AtomicInteger(0);
        private final ReentrantLock _lock = new ReentrantLock();
        private volatile boolean _isOpen = false;

        public FileDescriptor(FileHandle handle) {
            _handle = handle;
        }

        public boolean open(boolean createIfNonexistent) {
            try {
                lock();
                if (_isOpen) return true; // Already open
                try {
                    if (createIfNonexistent) {
                        File f = new File(_handle.getFullNameAndPath());
                        f.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(_handle.getFullNameAndPath());
                    FileReader fileReader = new FileReader(_handle.getFullNameAndPath());
                    _writer = new BufferedWriter(fileWriter);
                    _reader = new BufferedReader(fileReader);
                    _refCount.set(0);
                    _isOpen = true;
                    _buffer.clear();
                } catch (Exception e) {
                    System.err.println("Unable to open " + _handle.getFullNameAndPath());
                }
                return _isOpen;
            }
            finally {
                unlock();
            }
        }

        public boolean close() {
            if (!_isOpen) return false;
            return _flushAndClose();
        }

        public void lock() {
            _lock.lock();
        }

        public void unlock() {
            _lock.unlock();
        }

        public void incrementRefCount() {
            _refCount.getAndIncrement();
        }

        public void decrementRefCount() {
            _refCount.getAndDecrement();
        }

        public int getRefCount() {
            return _refCount.get();
        }

        public FileHandle getHandle() {
            return _handle;
        }

        public boolean synchronousWrite(Collection<Character> buffer) {
            try {
                lock();
                if (!isOpen()) return false;
                for (Character c : buffer) _buffer.add(c);
                return true;
            }
            finally {
                unlock();
            }
        }

        public boolean synchronousRead(Collection<Character> buffer) {
            try {
                lock();
                if (!isOpen()) return false;
                buffer.clear();
                try {
                    String line;
                    while ((line = _reader.readLine()) != null && line.length() != 0) {
                        for (int i = 0; i < line.length(); ++i) buffer.add(line.charAt(i));
                    }
                }
                catch (Exception e) {
                    return false;
                }
                buffer.addAll(_buffer);
                return true;
            }
            finally {
                unlock();
            }
        }

        public boolean isOpen() {
            return _refCount.get() > 0 && _isOpen;
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof FileDescriptor) && _handle.equals(other);
        }

        @Override
        public int hashCode() {
            return _handle.hashCode();
        }

        private boolean _flushAndClose() {
            try {
                lock();
                if (!_isOpen) return false; // Already closed
                _isOpen = false;
                try {
                    char[] buffer = new char[_buffer.size()];
                    for (int i = 0; i < _buffer.size(); ++i) buffer[i] = _buffer.get(i);
                    _writer.write(buffer);
                    _writer.close();
                    _reader.close();
                }
                catch (Exception e) {
                    return false;
                }
                return true;
            }
            finally {
                unlock();
            }
        }
    }

    // Package-private
    Filesystem() { }

    /**
     * Initializes the file system for first use. This will probably only be called by
     * the engine module.
     */
    public void init() {
        synchronized (this) {
            if (_isInitialized) return; // Already initialized
            _isInitialized = true;
            _cwd = System.getProperty("user.dir");
            StringBuilder str = new StringBuilder(_cwd.length());
            // Replace all '\\' with '/'
            for (int i = 0; i < _cwd.length(); ++i) {
                char c = _cwd.charAt(i);
                str.append(c == '\\' ? '/' : c);
            }
            _cwd = str.toString();
            for (int i = 0; i < _maxFileHandles; ++i) {
                _handles[i] = null;
                _descriptors[i] = null;
            }
        }
    }

    public void shutdown() {
        synchronized (this) {
            if (!_isInitialized) return; // Already shutdown
            _isInitialized = false;
            // Close all open file descriptors
            for (int i = 0; i < _maxFileHandles; ++i) {
                FileDescriptor descriptor = _descriptors[i];
                if (descriptor != null && descriptor.isOpen()) descriptor.close();
            }
        }
    }

    /**
     * Creates a new directory if it does not exist
     * @param directory path to the directory
     */
    public void createDirectory(String directory) {
        try {
            File dir = new File(directory);
            if (!dir.exists()) dir.mkdir();
        }
        catch (Exception e) {
            System.err.println("Unable to create directory: " + directory);
        }
    }

    /**
     * Opens a new file for reading/writing.
     * @param file file to open
     * @param createIfNonexistent true if the file should be created if not found
     * @return a valid FileHandle reference upon success and null if it failed for any reason
     */
    public FileHandle open(String file, boolean createIfNonexistent) {
        synchronized (this) {
            file = _preprocessFile(file);
            FileHandle handle = _findValidHandle(file);
            if (handle == null) {
                throw new RuntimeException("Unable to open file - maximum number of open files exceeded");
            }
            FileDescriptor descriptor = _descriptors[handle.getHandle()];
            boolean result = true;
            if (!descriptor.isOpen()) {
                result = descriptor.open(createIfNonexistent);
                descriptor.incrementRefCount(); // Make sure this gets incremented
            }
            return result ? handle : null; // Check the result of open
        }
    }

    /**
     * Closes an open file. This will ensure that it is properly flushed to disk
     * once the last file handle referencing that file is closed.
     * @param handle valid file handle returned from open()
     * @return true if the handle was open and then successfully closed or false if the
     *         file handle was already closed/did not reference a valid file
     */
    public boolean close(FileHandle handle) {
        synchronized (this) {
            FileDescriptor descriptor = _descriptors[handle.getHandle()];
            if (descriptor == null || !descriptor.isOpen()) return false;
            descriptor.decrementRefCount();
            int refCount = descriptor.getRefCount();
            boolean result = true;
            if (refCount == 0) {
                result = descriptor.close();
                _handles[handle.getHandle()] = null;
                _descriptors[handle.getHandle()] = null;
            }
            return result;
        }
    }

    /**
     * Performs a synchronous write operation on the file referenced by the given handle
     * and the contents of the String buffer. It is synchronous in that it will block
     * whatever thread calls it until the operation completes.
     * @param handle valid file handle returned from open()
     * @param buffer buffer containing the data to write
     * @return true if the write was successful and false if not
     */
    public boolean synchronousWrite(FileHandle handle, String buffer) {
        synchronized (this) {
            FileDescriptor descriptor = _descriptors[handle.getHandle()];
            if (descriptor == null || !descriptor.isOpen()) return false;
            _tempBuffer.clear();
            for (int i = 0; i < buffer.length(); ++i) _tempBuffer.add(buffer.charAt(i));
            return descriptor.synchronousWrite(_tempBuffer);
        }
    }

    /**
     * Performs a synchronous write operation on the file referenced by the given handle
     * and the contents of the Collection<Character> buffer. It is synchronous in that it will block
     * whatever thread calls it until the operation completes.
     * @param handle valid file handle returned from open()
     * @param buffer buffer containing the data to write
     * @return true if the write was successful and false if not
     */
    public boolean synchronousWrite(FileHandle handle, Collection<Character> buffer) {
        synchronized (this) {
            FileDescriptor descriptor = _descriptors[handle.getHandle()];
            if (descriptor == null || !descriptor.isOpen()) return false;
            return descriptor.synchronousWrite(buffer);
        }
    }

    /**
     * Reads the entire contents of the file to the given buffer. It is synchronous in that it
     * will block whatever thread calls this until the operation completes.
     * @param handle valid file handle returned from open()
     * @param buffer buffer containing the data to write
     * @return true if the read was successful and false if not
     */
    public boolean synchronousRead(FileHandle handle, Collection<Character> buffer) {
        synchronized (this) {
            FileDescriptor descriptor = _descriptors[handle.getHandle()];
            if (descriptor == null || !descriptor.isOpen()) return false;
            return descriptor.synchronousRead(buffer);
        }
    }

    private String _preprocessFile(String file) {
        StringBuilder str = new StringBuilder(file.length());
        // Replace all '\\' with '/'
        for (int i = 0; i < file.length(); ++i) {
            char c = file.charAt(i);
            str.append(c == '\\' ? '/' : c);
        }
        file = str.toString();
        FileHandle handle = new FileHandle(file,-1);
        if (handle.getFullNameAndPath().contains(_cwd)) return file;
        char firstChar = file.charAt(0);
        return (firstChar == '/') ? _cwd + file : _cwd + "/" + file;
    }

    private FileHandle _findValidHandle(String file) {
        FileDescriptor descriptor;
        int index = 0;
        for (int i = 0; i < _maxFileHandles; ++i) {
            descriptor = _descriptors[i];
            if (descriptor != null) {
                try {
                    descriptor.lock();
                    // Found a matching file descriptor/handle combo
                    if (descriptor.getHandle().getFullNameAndPath().equals(file)) {
                        index = i;
                        descriptor.incrementRefCount();
                        break;
                    }
                }
                finally {
                    descriptor.unlock();
                }
            }
            else index = i;
        }
        FileHandle handle = _handles[index];
        if (handle == null) {
            handle = new FileHandle(file, index);
            descriptor = new FileDescriptor(handle);
            _handles[index] = handle;
            _descriptors[index] = descriptor;
        }
        return handle;
    }
}
