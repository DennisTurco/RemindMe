package remindme;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitDocument extends PlainDocument {
    private final int limit;

    public LimitDocument(int limit) {
        this.limit = limit;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) return;

        // Controlla che la lunghezza attuale più la nuova non superi il limite
        if ((getLength() + str.length()) <= limit) {
            super.insertString(offset, str, attr);
        }
    }
}