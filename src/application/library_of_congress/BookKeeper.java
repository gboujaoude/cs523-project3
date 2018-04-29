package application.library_of_congress;

import engine.Engine;
import engine.FileHandle;

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

        FileHandle vfSticky = Engine.getFileSystem().open("StickyNotes.txt", true);
        ArrayList<Character> buffer = new ArrayList<>(100);
        for (StickyNotes note: notes) {
            String msg = note.getMsg();
            for (int i = 0; i < msg.length(); ++i) buffer.add(msg.charAt(i));
            buffer.add('\n');
        }
        Engine.getFileSystem().synchronousWrite(vfSticky, buffer);
        Engine.getFileSystem().close(vfSticky);
    }

    public void addNote(StickyNotes note) {
        notes.add(note);
    }
}
