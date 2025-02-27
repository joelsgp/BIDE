package zezombye.bide;

import javax.swing.*;
import java.awt.Window;
import java.awt.*;


public class FixedInputDialog extends JDialog {

    public void showInputDialog(Window parent, String message, String title, String defaultInputValue) {
        JDialog dialog = new JDialog(parent, title, Dialog.DEFAULT_MODALITY_TYPE);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new FlowLayout());
        JLabel msg = new JLabel(message);
        dialog.add(msg, FlowLayout.LEFT);
        JTextField jtf = new JTextField(defaultInputValue);
        jtf.setSize(100, 25);
        dialog.add(jtf);
        dialog.setTitle(title);
        dialog.pack();
        dialog.setVisible(true);
    }
}
