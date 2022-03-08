package zezombye.bide;

import javax.swing.*;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


public class CustomOutputStream extends OutputStream {
    private final JTextArea output;
    private byte[] bytes = new byte[1024];
    private int currentBytePos = 0;

    public CustomOutputStream(JTextArea ta) {
        this.output = ta;
    }

    @Override
    public void write(int i) {
        bytes[currentBytePos] = (byte) i;
        currentBytePos++;
        if (i == '\n') {
            output.append(new String(bytes, 0, currentBytePos, StandardCharsets.UTF_8));
            output.setCaretPosition(output.getText().length());
            bytes = new byte[1024];
            currentBytePos = 0;
        }
    }
}
