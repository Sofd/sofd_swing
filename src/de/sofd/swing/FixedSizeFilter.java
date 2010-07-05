package de.sofd.swing;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * JTextComponent textComp = new JTextField();
 * textComp.setDocument(new FixedSizePlainDocument(10));
 *
 * @author sofd GmbH
 */
public class FixedSizeFilter extends DocumentFilter {

    int maxSize;

    public FixedSizeFilter(int limit) {
        super();
        maxSize = limit;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
        replace(fb, offset, 0, str, attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
        int newLength = fb.getDocument().getLength() - length + str.length();
        if (newLength <= maxSize) {
            fb.replace(offset, length, str, attrs);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}

