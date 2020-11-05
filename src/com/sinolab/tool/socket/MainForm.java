package com.sinolab.tool.socket;

import com.sinolab.tool.socket.client.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainForm implements MessageListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    public JPanel panel1;
    private JComboBox ipComboBox;
    private JComboBox portComboBox;
    private JRadioButton modeRadio;
    private JButton startButton;
    private JTextArea receiveArea;
    private JButton sendButton;
    private JTextArea sendTextArea;
    private JScrollPane receiveScroll;

    private SendAndReceiveAbstractObject channel = null;




    public MainForm() {
        MainForm me = this;

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (channel != null) {
                    channel.close();
                    channel = null;
                    startButton.setText("启动");
                } else {
                    startButton.setEnabled(false);
                    if (modeRadio.isSelected()) {
                        //服务端模式
                        channel = new Server(me);
                        onReceive(new Message("以服务端模式启动，监听的端口为" + portComboBox.getSelectedItem()));
                    } else {
                        //客户端模式
                        channel = new Client(me);
                        onReceive(new Message("以客户端模式启动，连接的IP为" + ipComboBox.getSelectedItem() + "，端口为" + portComboBox.getSelectedItem()));
                    }

                    try {
                        channel.open(ipComboBox.getSelectedItem().toString(),
                                Integer.parseInt(portComboBox.getSelectedItem().toString()));
                    } catch (Exception ae) {
                        ae.printStackTrace();
                        JOptionPane.showMessageDialog(null, ae.getMessage());
                        return;
                    }
                    if(modeRadio.isSelected()){
                        onReceive(new Message("等待客户端连接..."));
                    }else{
                        onReceive("连接中...");
                    }
                    startButton.setEnabled(true);
                    startButton.setText("终止");
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = sendTextArea.getText();
                text = text.replace(" ", "");
                Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                Matcher m = p.matcher(text);
                text = m.replaceAll("");
                if(channel != null){
                    channel.send(Hex.HexStringToBytes(text));
                    logger.info("发送数据：" + text);
                }else{
                    alert("未建立连接对象，请先启动连接！");
                }
            }
        });
    }

    @Override
    public void onReceive(Message message) {
        receiveArea.append(message.toString());
        receiveArea.append("\n");
        receiveScroll.getVerticalScrollBar().setValue(receiveScroll.getVerticalScrollBar().getMaximum());
    }

    public void onReceive(String str){
        this.onReceive(new Message(str));
    }

    private void alert(String text){
        JOptionPane.showMessageDialog(null, text);
    }
}
