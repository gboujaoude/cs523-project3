package application.library_of_congress;

import java.util.ArrayList;

public class BookKeeper {
    ArrayList<RecordBook> records = new ArrayList<>();
    ArrayList<StickyNotes> notes = new ArrayList<>();

    public void addBook(RecordBook recordBook) {
        records.add(recordBook);
    }

    public void pushToPaper() {
        for(RecordBook rb: records) {
            System.out.println("---- " + rb.getName() + " ----");
            for(Object record : rb.getRecords()) {
                System.out.println(record);
            }
        }

        System.out.println("===== Sticky Notes ====");
        for (StickyNotes note: notes) {
            System.out.println(note.getMsg());
        }
    }

    public void closeBooks() {
        for(RecordBook book: records) {
            book.close();
        }
    }

    public void addNote(StickyNotes note) {
        notes.add(note);
    }
}
