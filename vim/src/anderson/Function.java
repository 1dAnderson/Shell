package anderson;

import javafx.scene.input.KeyCode;
import java.util.Timer;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

public class Function {
    //全局变量
    static Interface i;

    static {
        try {
            i = new Interface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean flag_editable;

    //按下i进入编辑模式,按下ESC退出编辑编辑模式
    public static void SelectMode(){
        i.textArea.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_I){
                    i.textArea.setEditable(true);
                    flag_editable = true;
                    i.textField.setText("--INSERT--");

                }
                else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    i.textArea.setEditable(false);
                    i.textArea.requestFocusInWindow();
                    flag_editable = false;
                    i.textField.setText(" ");
                }
            }
        });

    }
    //通过h,j,k,l来控制光标移动
    public static void MoveCursor(){
            Robot myRobot = null;
            try
            {
                myRobot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            Robot finalMyRobot = myRobot;
            i.textArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if(e.getKeyChar() == 'h'&& !flag_editable){
                        finalMyRobot.keyPress(KeyEvent.VK_LEFT);
                    }
                    else if(e.getKeyChar() == 'j' && !flag_editable){
                        finalMyRobot.keyPress(KeyEvent.VK_UP);
                    }
                    else if(e.getKeyChar() == 'k' && !flag_editable){
                        finalMyRobot.keyPress(KeyEvent.VK_DOWN);
                    }
                    else if(e.getKeyChar() == 'l' && !flag_editable){
                        finalMyRobot.keyPress(KeyEvent.VK_RIGHT);
                    }
                }
            });
    }
    //普通模式下按下“：”进入命令模式，并且执行dd,yy,p命令
    public static void OrderOnTextArea(){
        if(!flag_editable){
            i.textArea.addKeyListener(new KeyAdapter() {
                //命令
                String order = "";
                //用于p粘贴
                String copy = "";
                //一直记录操作
                String recordOrder = "";
                //最终要进行的操作
                String finalOrder = "";
                //开始记录标志
                boolean flagRecording = false;
                public void keyTyped(KeyEvent e) {
                    order = order + e.getKeyChar();
                    recordOrder = recordOrder + e.getKeyChar();
//                    System.out.print(order);
                    //切换到命令模式
                    if(e.getKeyChar() == ':'&&!flag_editable){
                        i.textField.setText(":");
                        i.textField.setEditable(true);
                        i.textField.requestFocus();
                        i.textField.setCaretPosition(1);
                    }
                    else if(e.getKeyChar() =='/'&&!flag_editable){
                        i.textField.setText("/");
                        i.textField.setEditable(true);
                        i.textField.requestFocus();
                        i.textField.setCaretPosition(1);
                    }
                    //粘贴到行后面
                    else if(order.equals("p")&&!flag_editable){
                        try {
                            LinePaste(copy);
                        } catch (BadLocationException badLocationException) {
                            badLocationException.printStackTrace();
                        }
                        order = "";
                    }
                    //删除行
                    else if(order.equals("dd")&&!flag_editable){
                        try {
                            LineDelete();
                        } catch (BadLocationException badLocationException) {
                            badLocationException.printStackTrace();
                        }
                        order = "";
                    }
                    //复制一行
                    else if(order.equals("yy")&&!flag_editable){
                        try {
                            copy = LineCopy();
                        } catch (BadLocationException badLocationException) {
                            badLocationException.printStackTrace();
                        }
                        order = "";
                    }
                    //记录操作
                    else if(order.equals("qa") && !flag_editable ){
                        recordOrder = "";
                        order = "";
                        flagRecording = true;
                    }
                    else if(order.equals("a") && !flag_editable && flagRecording){
                        finalOrder = recordOrder.substring(0,recordOrder.length()-1);
//                        System.out.print(finalOrder);
                        order = "";
                        flagRecording = false;
                    }
                    else if(order.equals("@a") && !flag_editable){
                        order = "";
                        ExecuteFinalOrder(finalOrder);

                    }

                    //如果输入不是指令的操作，清空指令
                    else if(order.length() == 1){
                        if(!order.equals("d") && !order.equals("y") && !order.equals("q") && !order.equals("@"))
                            order = "";
                    }
                    //如果输入不是指令的操作，清空指令
                    else if(order.length() == 2){
                        if(!order.equals("dd") && !order.equals("yy") && !order.equals("qa") && !order.equals("@a"))
                            order = "";
                    }
                }
            });
        }
    }

    //粘贴行p
    public static void LinePaste(String copy) throws BadLocationException {
        //当前光标位置
        int pos = i.textArea.getCaretPosition();
        //当前光标行
        int lineIndex = i.textArea.getLineOfOffset(pos);
        //文本行
        int lineCount = i.textArea.getLineCount();
        //这一行end
        int colEnd = i.textArea.getLineEndOffset(lineIndex);
        if(lineIndex != lineCount)
            i.textArea.insert(copy+"\n",colEnd);
        else{
            i.textArea.append("\n");
            i.textArea.append(copy);
        }


    }

    //删除行dd
    public static void LineDelete() throws BadLocationException {
        //当前光标位置
        int pos = i.textArea.getCaretPosition();
        //当前光标行
        int lineIndex = i.textArea.getLineOfOffset(pos);
        //当前光标列
        int colIndex = pos-i.textArea.getLineStartOffset(lineIndex);
        //上一行start
        int lastColStart = 0;
        //上一行end
        int lastColEnd = 0;
        if(lineIndex!=0){
            lastColStart = i.textArea.getLineStartOffset(lineIndex-1);
            lastColEnd = i.textArea.getLineEndOffset(lineIndex-1);
        }
        String text = i.textArea.getText();
        String[] splitText = text.split("\n");
        String newText = "";
        for(int i=0; i<splitText.length; i++){
            String s = splitText[i];
            if(i != lineIndex){
                newText = newText+s+"\n";
            }
        }
        i.textArea.setText(newText);
        if((lastColStart+colIndex)>lastColEnd){
            if(lastColEnd>0)
                i.textArea.setCaretPosition(lastColEnd-1);
            else
                i.textArea.setCaretPosition(0);
        }
        else
            i.textArea.setCaretPosition(lastColStart+colIndex);
    }

    //复制行yy
    public static String LineCopy() throws BadLocationException {
        //当前光标位置
        int pos = i.textArea.getCaretPosition();
        //当前光标行
        int lineIndex = i.textArea.getLineOfOffset(pos);
        String text = i.textArea.getText();
        String[] splitText = text.split("\n");
        String newText = splitText[lineIndex];
        return newText;
    }

    //在TextField添加监听器执行命令,":q,w,q!,x,wq"
    public static void OrderOnTextField(){
        if(!flag_editable){
            i.textField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                        String order = i.textField.getText();
                        String s = i.textArea.getText();
                        //保存
                        if(order.equals(":w")){
                            try {
                                ClearText();
                                SaveMessage(s);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        //保存并退出
                        else if(order.equals(":x") || order.equals(":wq")){
                            try {
                                ClearText();
                                SaveMessage(s);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                            System.exit(0);
                        }
                        //:q退出提示
                        else if(order.equals(":q")){
                            i.textField.setText("No write since last change (add ! to override)");
                        }
                        //强制退出不保存
                        else if(order.equals(":q!")){
                            System.exit(0);
                        }
                        //进入匹配字符串
                        else if(order.charAt(0) == '/'){
//                            System.out.println("进入匹配");
                            //需要匹配的字符串
                            String sub = order.substring(1,order.length());
//                            System.out.print(sub);
                            try {
                                FindStr(sub);
                            } catch (BadLocationException badLocationException) {
                                badLocationException.printStackTrace();
                            }

                        }
                        //替换字符串
                        else if(order.substring(0,4).equals(":%s/")){
                            String str = order.substring(4);
                            try {
                                ReplaceStr(str);
                            } catch (BadLocationException badLocationException) {
                                badLocationException.printStackTrace();
                            }
                        }
                        //对于特殊情况的界面处理
                        if(!i.textField.getText().isEmpty()&& !order.equals(":q")&&!(order.charAt(0) == '/')){
                            i.textField.setText(":");
                            i.textField.setCaretPosition(1);
                        }
                        else if(i.textField.getText().equals("not found!")){
                            i.textField.setText("/");
                            i.textField.setCaretPosition(1);
                        }
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                        i.textField.setText("");
                        i.textArea.dispatchEvent(new FocusEvent(i.textArea,FocusEvent.FOCUS_GAINED,TRUE));
                        i.textArea.requestFocusInWindow();
                        i.textArea.setCaretPosition(i.textArea.getDocument().getLength());
                    }
                }
            });
        }
    }

    //保存每一行信息，在在OrderOnTextField被调用
    public static void SaveMessage(String msg) throws IOException {
        String path = System.getProperty("user.dir");
        File file = new File(path);
        File file1 = new File(file,"text.txt");
        if(!file1.exists()) {
            file1.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file1,true));
        bw.write(msg);
        bw.flush();
        bw.close();
    }
    //把text文本清空,在OrderOnTextField被调用
    public static void ClearText() throws IOException {
        String path = System.getProperty("user.dir");
        File file = new File(path,"text.txt");
        RandomAccessFile rf = new RandomAccessFile(file, "rw");
        FileChannel fc = rf.getChannel();
        //截取为0
        fc.truncate(0);
    }
    //定时备份,每100秒备份一次
    public static void BackUp(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ClearText();
                    SaveMessage(i.textArea.getText());
                    System.out.println("backup!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },100000,100000);

    }
    //字符串匹配
    public static void FindStr(String s) throws BadLocationException {
        Pattern pattern = Pattern.compile(s);
        String text = i.textArea.getText();
        String[] splitText = text.split("\n");
        int lineIndex = 0;
        int colIndex = 0;
        int line = 0;
        Matcher matcher = null;
        for(int m=0; m<splitText.length; m++){
            matcher = pattern.matcher(splitText[m]);
//            System.out.print(splitText[m]+'\n');
            if(matcher.find()){
                line = m;
//                System.out.print(m);
                if(m!=0){
                    lineIndex = i.textArea.getLineEndOffset(line-1);
                }

                String str = splitText[m]+" ";
                for(int n=0; n<str.length()-s.length(); n++){
                    if(s.equals(str.substring(n,n+s.length()))){
//                        System.out.println("this has into deep");
                        colIndex = n;
                        i.textArea.dispatchEvent(new FocusEvent(i.textArea,FocusEvent.FOCUS_GAINED,TRUE));
                        i.textArea.requestFocusInWindow();
                        i.textArea.setCaretPosition(lineIndex+colIndex);
//                        System.out.print(lineIndex+colIndex);
                        i.textField.setText("");
                        return;
                    }

                }
            }
        }
        if(!matcher.find()){
            i.textField.setText("not found!");
//            i.textArea.dispatchEvent(new FocusEvent(i.textArea,FocusEvent.FOCUS_GAINED,TRUE));
//            i.textArea.requestFocusInWindow();
//            i.textArea.setCaretPosition(i.textArea.getDocument().getLength());
            
        }

    }
    //字符串替代
    public static void ReplaceStr(String s) throws BadLocationException {
        String oldStr = "";
        String newStr = "";
        for(int index = 0; index<s.length(); index++){
            if(s.charAt(index)=='/'){
                oldStr = s.substring(0,index);
                newStr = s.substring(index+1);
            }
        }
        Pattern pattern = Pattern.compile(oldStr);
        String text = i.textArea.getText();
        String[] splitText = text.split("\n");
        String newText = "";
        for(int m=0; m<splitText.length; m++){
            Matcher matcher = pattern.matcher(splitText[m]);
            String newLine = "";
            String str = splitText[m]+" ";
            if(matcher.find()){
                for(int n=0; n<str.length()-oldStr.length(); n++){
                    if(oldStr.equals(str.substring(n,n+oldStr.length()))){
                        if(n!=0){
                            newLine = str.substring(0,n-1) +" "+newStr +str.substring(n+oldStr.length());
                        }
                        else
                            newLine = newStr+str.substring(oldStr.length());
                    }

                }
            }
            else
                newLine = str;
            newText = newText + newLine +'\n';
        }
        i.textArea.setText(newText);
    }
    //执行记录的操作
    public static void ExecuteFinalOrder(String s){
        System.out.print(s);
        Robot myRobot = null;
        try
        {
            myRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        for (int i=0; i<s.length(); i++){
            switch (s.charAt(i)){
                case 'h':{myRobot.keyPress(KeyEvent.VK_LEFT); myRobot.keyRelease(KeyEvent.VK_LEFT); break;}
                case 'j':{myRobot.keyPress(KeyEvent.VK_UP); myRobot.keyRelease(KeyEvent.VK_UP); break;}
                case 'k':{myRobot.keyPress(KeyEvent.VK_DOWN); myRobot.keyRelease(KeyEvent.VK_DOWN); break;}
                case 'l':{myRobot.keyPress(KeyEvent.VK_RIGHT); myRobot.keyRelease(KeyEvent.VK_RIGHT); break;}
                case 'y':{myRobot.keyPress(KeyEvent.VK_Y); myRobot.keyRelease(KeyEvent.VK_Y); break;}
                case 'd':{myRobot.keyPress(KeyEvent.VK_D); myRobot.keyRelease(KeyEvent.VK_D); break;}
                case 'p':{myRobot.keyPress(KeyEvent.VK_P); myRobot.keyRelease(KeyEvent.VK_Y); break;}
            }
        }
    }

    public static void main(String args[]){
        SelectMode();
        MoveCursor();
        OrderOnTextArea();
        OrderOnTextField();
//        BackUp();
    }
}
