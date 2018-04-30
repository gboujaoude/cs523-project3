package application.library_of_congress;

import engine.Engine;
import engine.FileHandle;

import java.util.ArrayList;

public class BookKeeper {
    ArrayList<RecordBook> records = new ArrayList<>();
    ArrayList<StickyNotes> notes = new ArrayList<>();
    private String folderUrl = "";

    public BookKeeper(String folderUrl) {
        this.folderUrl = folderUrl;
    }

    public void addBook(RecordBook recordBook) {
        records.add(recordBook);
    }

    public void closeBooks() {
        for(RecordBook book: records) {
            book.close();
        }
    }

    public void closeNotes() {
        if (notes.size() == 0) {
            notes.add(new StickyNotes("No notes."));
        }
        RecordBook recordBook = new RecordBook("sticky-notes",folderUrl,"txt");
        for(StickyNotes note : notes) {
            recordBook.add(note.getMsg() +"\n");
        }
        recordBook.close();
    }

    public void addNote(StickyNotes note) {
        notes.add(note);
    }
}
