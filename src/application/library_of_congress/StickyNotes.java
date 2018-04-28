package application.library_of_congress;

public class StickyNotes {
    private String msg;
    public StickyNotes(String msg) {
        this.msg = msg;
    }

    public void msg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
