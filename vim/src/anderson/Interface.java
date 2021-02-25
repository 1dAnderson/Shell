package anderson;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import static java.lang.Boolean.TRUE;

public class Interface {
    public static JTextArea textArea;
    public static JTextField textField;
    public static JFrame frame;
    Interface() throws IOException {
        textArea = new JTextArea();
        textField = new JTextField();
        frame = new JFrame();
        frame.setSize(600,600);
        frame.add(textArea,BorderLayout.CENTER);
        frame.add(textField,BorderLayout.SOUTH);
        frame.add(new JScrollPane(textArea));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                textArea.setFont(new Font("楷体", Font.BOLD, 16));
                textArea.requestFocusInWindow();
                textArea.setCaret(new DefaultCaret(){
                    public boolean isVisible(){
                        return true;
                    }
                });
//                textArea.dispatchEvent(new FocusEvent(textArea,FocusEvent.FOCUS_GAINED,TRUE));
                textArea.setCaretPosition(textArea.getDocument().getLength());
                textArea.setEditable(false);
                textField.setEditable(false);
                textField.setFont(new Font("宋体",Font.ITALIC,16));
                textArea.setLineWrap(true);
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setVisible(true);
        //恢复文本
        RecoverText();

    }
    //打开时从txt文件恢复文本
    public void RecoverText() throws IOException {
        String path = System.getProperty("user.dir");
        File file = new File(path,"text.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        String text = "";
        while ((line=reader.readLine())!=null) {
            if(line!=null)
                text = text+line+'\n';
        }
        textArea.setText(text);
    }
}
