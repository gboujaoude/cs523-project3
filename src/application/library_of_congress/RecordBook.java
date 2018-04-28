package application.library_of_congress;

import engine.Engine;
import engine.VirtualFile;

import java.util.ArrayList;
import java.util.Optional;

public class RecordBook{

    private final String bookName;
    private ArrayList<String> records = new ArrayList<>();
    private VirtualFile vf;

    public RecordBook(String bookName) {
        this.bookName = Optional.ofNullable(bookName).orElse("unknown-record-name");
        this.vf = Engine.getFileSystem().open(this.bookName + ".txt");
    }

    public void add(String record) {
        records.add(record);
        vf.add(record);
    }

    public String getName() {
        return bookName;
    }

    public ArrayList<String> getRecords() {
        return records;
    }

    public void close() {
        vf.close();
    }
}
