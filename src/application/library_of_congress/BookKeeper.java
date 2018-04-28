package application.library_of_congress;

import engine.Engine;
import engine.VirtualFile;

import java.util.ArrayList;

public class BookKeeper {
    ArrayList<RecordBook> records = new ArrayList<>();
    ArrayList<StickyNotes> notes = new ArrayList<>();

    public void addBook(RecordBook recordBook) {
        records.add(recordBook);
    }

    public void pushToPaper() {
        System.out.println("===== Sticky Notes ====");
        for (StickyNotes note: notes) {
            System.out.println(note.getMsg());
        }
    }

    public void closeBooks() {
        for(RecordBook book: records) {
            book.close();
        }

        VirtualFile vfSticky = Engine.getFileSystem().open("StickyNotes.txt");
        for (StickyNotes note: notes) {
            vfSticky.add(note + "\n");
        }
    }

    public void addNote(StickyNotes note) {
        notes.add(note);
    }
}
