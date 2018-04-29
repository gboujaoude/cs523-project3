package engine;

/**
 * A file handle represents an open file that is registered with
 * the file system. With a file handle you can interface with a file system
 * to write and read data to/from the referenced file.
 *
 * @author Justin Hall
 */
public class FileHandle {
    private String _fullPathAndName;
    private String _fileName;
    private String _filePath;
    private String _extension;
    private final int _handle;

    /**
     * Package-private
     *
     * Creates a new file handle for use inside the file system. It represents
     * a link back to the open file along with some of its characteristics (such as file name,
     * path, extension).
     *
     * @param filePath file path (path + name + extension) of the file
     * @param handle handle assigned by the file system
     */
    FileHandle(String filePath, int handle) {
        _parseFilePath(filePath);
        _handle = handle;
    }

    /**
     * @return the combined path + name + extension of the file.
     */
    public String getFullNameAndPath() {
        return _fullPathAndName;
    }

    /**
     * @return the file name referenced by this handle
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * @return the file path (excluding name + extension) of this file
     */
    public String getFilePath() {
        return _filePath;
    }

    /**
     * @return file extension
     */
    public String getFileExtension() {
        return _extension;
    }

    /**
     * @return underlying integer handle used mainly by the file system
     */
    public int getHandle() {
        return _handle;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof FileHandle) && hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(_handle);
    }

    private void _parseFilePath(String filePath) {
        _fileName = "";
        _filePath = "";
        _extension = "";
        StringBuilder str = new StringBuilder();
        boolean foundExtension = false;
        for (int i = filePath.length() - 1; i >= 0; --i) {
            char c = filePath.charAt(i);
            if (c == '.') {
                foundExtension = true;
                _extension = str.reverse().toString();
            }
            else if (c == '\\' || c == '/' && foundExtension) {
                _fileName = str.reverse().toString();
                foundExtension = false;
                str.delete(0, str.length());
            }
            str.append(c);
        }
        _filePath = str.reverse().toString();
        _fullPathAndName = _filePath + _fileName;
    }
}
