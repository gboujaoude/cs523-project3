package application.library_of_congress;

import engine.Engine;
import engine.FileHandle;

import java.util.ArrayList;
import java.util.Optional;

public class RecordBook{

    private final String bookName;
    private FileHandle handle;
    private ArrayList<Character> buffer = new ArrayList<>(100);

    public RecordBook(String bookName) {
        this.bookName = Optional.ofNullable(bookName).orElse("unknown-record-name");
        this.handle = Engine.getFileSystem().open(this.bookName + ".txt", true);
    }

    public void add(String record) {
        for (int i = 0; i < record.length(); ++i) buffer.add(record.charAt(i));
    }

    public String getName() {
        return bookName;
    }

    public void close() {
        Engine.getFileSystem().synchronousWrite(handle, buffer);
        Engine.getFileSystem().close(handle);
    }
}
