package zezombye.bide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rtextarea.RTextScrollPane;

public class G1MPart {

    public CasioString binaryContent = null;
    public boolean isEditedSinceLastSaveToG1M = true;
    public JComponent comp;
    public String name;
    public String option;
    public Object content;
    public int type = 0;

    // Warning: do not use name, option or content to retrieve information because they may have changed!
    // For example, if the user modifies the content to modify the name, that change will not be reflected in name (nor in content).
    public G1MPart(String name, String option, String content, int type) {
        this.name = name;
        this.option = option;
        this.type = type;
        // Check if content is missing headers
        if (type != BIDE.TYPE_PICT && type != BIDE.TYPE_CAPT && !content.startsWith("#")) {
            if (type == BIDE.TYPE_PROG) {
                content = "#Program name: "+name+"\n#Password: "+option+"\n"+content;
            }
        }
        this.content = content;

        if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
            this.comp = new Picture(type, name, Integer.valueOf(option, 16), content.getBytes()).jsp;
        } else {
            if (!BIDE.isCLI) {
                ProgramTextPane textPane = new ProgramTextPane(type);
                textPane.setText(content);
                textPane.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        isEditedSinceLastSaveToG1M = true;
                    }

                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        isEditedSinceLastSaveToG1M = true;
                    }

                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        isEditedSinceLastSaveToG1M = true;
                    }

                });
                this.comp = new ProgScrollPane(textPane, type);
                ((JScrollPane)comp).getVerticalScrollBar().setUnitIncrement(30);
                comp.setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }
}


class ProgScrollPane extends RTextScrollPane {

    ProgramTextPane textPane;
    int type;

    public ProgScrollPane(ProgramTextPane textPane, int type) {
        super(textPane);
        this.textPane = textPane;
        this.type = type;
    }
}
