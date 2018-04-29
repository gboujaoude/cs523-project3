package application.library_of_congress;

import engine.ConsoleVariable;
import engine.Engine;
import engine.FileHandle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RecordBook{

    private final String bookName;
    private FileHandle handle;
    private ArrayList<Character> buffer = new ArrayList<>(100);

    public RecordBook(String bookName, String time) {
        if(time != null) {
            String folder = "data/" + time + "/";
            this.bookName = Optional.ofNullable(folder + bookName).orElse("unknown-record-name.txt");
        } else {
            this.bookName = Optional.ofNullable(bookName).orElse("unknown-record-name.txt");
        }
        this.handle = Engine.getFileSystem().open(this.bookName + ".txt", true);
    }

    public RecordBook(String bookName) {
        this(bookName,null);
    }

    public void add(String record) {
        for (int i = 0; i < record.length(); ++i) buffer.add(record.charAt(i));
    }

    public void recordConfig(LinkedList<ConsoleVariable> list) {
        for(int i = 0; i < list.size(); i++) {
            String name = list.get(i).getcvarName();
            String value = list.get(i).getcvarValue();
            add("+ " + name + " = " + value + "\n");
        }
    }

    public String getName() {
        return bookName;
    }

    public void close() {
        Engine.getFileSystem().synchronousWrite(handle, buffer);
        Engine.getFileSystem().close(handle);
    }
}
