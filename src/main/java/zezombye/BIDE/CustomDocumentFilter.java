package zezombye.BIDE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class CustomDocumentFilter extends DocumentFilter {
    private final StyledDocument styledDocument;

    public JTextPane textPane;
    public ArrayList<ColorationPattern> regexes;
    
    public boolean isTooLaggy = false;
    public int type = 0;
    
    public CustomDocumentFilter(JTextPane jtp, ColorationPattern[] regexes, int type) {
    	this.textPane = jtp;
    	this.regexes = new ArrayList<>(Arrays.asList(regexes));
    	this.type = type;
    	styledDocument = textPane.getStyledDocument();
    }
    
    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(fb, offset, text, attributeSet);

        handleTextChanged();
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);

        handleTextChanged();
    }
    
    @Override
    public void replace(final FilterBypass fb, final int offs, final int length, final String str, final AttributeSet a) throws BadLocationException {
    	if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
            switch (str) {
                case "'" -> super.replace(fb, offs, length, "▀", a);
                case "," -> super.replace(fb, offs, length, "▄", a);
                case ":" -> super.replace(fb, offs, length, "█", a);
                default -> super.replace(fb, offs, length, str, a);
            }
    	} else {
            super.replace(fb, offs, length, str, a);
    	}
        
        handleTextChanged();
    }

    /**
     * Runs your updates later, not during the event notification.
     */
    private void handleTextChanged()
    {
    	if (!isTooLaggy) {
    		updateTextStyles();
    	}
    }

    private void updateTextStyles() {
        // Look for tokens and highlight them
    	String textPaneText = textPane.getText();
    	MutableAttributeSet sas = new SimpleAttributeSet();
    	StyleConstants.setForeground(sas, Color.BLACK);
    	
        // Clear existing styles
        styledDocument.setCharacterAttributes(0, textPane.getText().length(), sas, true);

        for (ColorationPattern regex : regexes) {
            StyleConstants.setForeground(sas, regex.color);
            //StyleConstants.setBold(sas, regexes[i].isBold);
            Matcher matcher = regex.pattern.matcher(textPaneText);
            while (matcher.find()) {
                // Change the color of recognized tokens
                styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), sas, false);
            }
        }
    }
}
