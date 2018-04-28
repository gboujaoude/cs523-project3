package engine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Very simple file system for opening/reading/writing files. All files
 * are read and written to memory, and only when the last reference to
 * that file closes itself will that file be transferred to disk.
 *
 * @author Justin Hall
 */
public class Filesystem {
    class FileHandle {
        private String _file = "";
        private int _handle;

        public FileHandle(String file, int handle) {
            _file = file;
            _handle = handle;
        }

        public String getFileName() {
            return _file;
        }

        public int getHandle() {
            return _handle;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof FileHandle && other.hashCode() == hashCode();
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(_handle);
        }
    }

    class FileDescriptor {
        private FileHandle _handle;
        //private FileReader _file;
        private BufferedReader _reader;
        private BufferedWriter _writer;
        private ArrayList<Character> _buffer = new ArrayList<>(100);
        private AtomicInteger _refCount = new AtomicInteger(0);
        private AtomicLong _currFileSize = new AtomicLong(0);
        private AtomicLong _totalFileSize = new AtomicLong(0);
        private volatile boolean _isOpen = false;

        public FileDescriptor(FileHandle handle) {
            _handle = handle;
            try {
                FileReader fileReader = new FileReader(handle.getFileName());
                FileWriter fileWriter = new FileWriter(handle.getFileName(), true); // True means append
                _reader = new BufferedReader(fileReader);
                _writer = new BufferedWriter(fileWriter);
                File file = new File(handle.getFileName());
                _totalFileSize.set(file.length());
                _isOpen = true;
                Engine.scheduleLogicTasks(null, () ->
                {
                    while (_currFileSize.get() != _totalFileSize.get()) {
                        try {
                            String line = _reader.readLine();
                            this.write(line);
                        }
                        catch (Exception e) {
                            // Do nothing
                        }
                    }
                });
            }
            catch (Exception e) {
                System.err.println("Could not open " + handle.getFileName());
            }
        }

        public void incrementRefCount() {
            _refCount.addAndGet(1);
        }

        public void decrementRefCount() {
            _refCount.addAndGet(-1);
        }

        public boolean isOpen() {
            return _refCount.get() != 0 && _isOpen;
        }

        public void write(char ... chars) {
            synchronized (this) {
                for (int i = 0; i < chars.length; ++i) {
                    _write(chars[i]);
                }
            }
        }

        public void write(Collection<Character> chars) {
            synchronized (this) {
                for (Character c : chars) _write(c);
            }
        }

        public void write(String str) {
            synchronized (this) {
                for (int i = 0; i < str.length(); ++i) {
                    _write(str.charAt(i));
                }
            }
        }

        public char get(int index) {
            if (index >= _totalFileSize.get()) throw new IndexOutOfBoundsException();
            _waitForBytes(index);
            synchronized (this) {
                return _buffer.get(index);
            }
        }

        public char set(int index, char c) {
            if (index >= _totalFileSize.get()) throw new IndexOutOfBoundsException();
            _waitForBytes(index);
            synchronized (this) {
                return _buffer.set(index, c);
            }
        }

        public char[] read(int bytes) {
            bytes = bytes > _totalFileSize.get() ? (int)_totalFileSize.get() : bytes;
            _waitForBytes(bytes);
            if (bytes < 0) throw new RuntimeException("Critical error - bytes less than 0");
            char[] buffer = new char[bytes];
            for (int i = 0; i < bytes; ++i) buffer[i] = _buffer.get(i);
            return buffer;
        }

        public FileHandle getHandle() {
            return _handle;
        }

        public int getFileSize() {
            return (int)_totalFileSize.get();
        }

        private void _waitForBytes(int bytes) {
            try {
                while (bytes > _currFileSize.get()) {
                    Thread.sleep(1);
                }
            }
            catch (Exception e) {

            }
        }

        private void _write(Character c) {
            _buffer.add(c);
            _currFileSize.getAndIncrement();
            _totalFileSize.getAndIncrement();
        }
    }

    private final int _maxFileHandles = 100;
    private final FileHandle[] _handles = new FileHandle[_maxFileHandles];
    private final FileDescriptor[] _descriptors = new FileDescriptor[_maxFileHandles];

    public VirtualFile open(String filename) {
        int index = _getHandleIndex(filename);
        if (!_descriptors[index].isOpen()) {
            _handles[index] = new FileHandle(filename, index);
            _descriptors[index] = new FileDescriptor(_handles[index]);
        }
        return new VirtualFile(_descriptors[index], _handles[index]);
    }

    private int _getHandleIndex(String filename) {
        int validIndex = -1;
        for (int i = 0; i < _maxFileHandles; ++i) {
            FileDescriptor descriptor = _descriptors[i];
            FileHandle handle = descriptor.getHandle();
            if (handle.getFileName().equals(filename) && descriptor.isOpen()) {
                validIndex = i;
                break;
            }
            else if (!descriptor.isOpen()) validIndex = i;
        }
        return validIndex;
    }

    // Package-private
    Filesystem() { }

    // Package-private
    FileDescriptor _findFileDescriptor(FileHandle handle) {
        for (int i = 0; i < _maxFileHandles; ++i) {
            FileDescriptor descriptor = _descriptors[i];
            if (descriptor.isOpen() && descriptor.getHandle() == handle) return descriptor;
        }
        return null;
    }
}
