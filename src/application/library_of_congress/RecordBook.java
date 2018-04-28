package application.library_of_congress;

import java.util.ArrayList;
import java.util.Optional;

public class RecordBook<E>{

    private final String bookName;
    private ArrayList<E> records = new ArrayList<>();

    public RecordBook(String bookName) {
        this.bookName = Optional.ofNullable(bookName).orElse("unknown-record-name");
    }

    public void add(E record) {
        records.add(record);
    }

    public String getName() {
        return bookName;
    }

    public ArrayList<E> getRecords() {
        return records;
    }
}
