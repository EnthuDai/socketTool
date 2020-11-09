package com.sinolab.tool.socket;

import com.sinolab.tool.socket.channel.Client;
import com.sinolab.tool.socket.channel.Message;
import com.sinolab.tool.socket.channel.SendAndReceiveAbstractObject;
import com.sinolab.tool.socket.channel.Server;
import com.sinolab.tool.socket.listener.MessageListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private JTabbedPane collectionTab;


    private SendAndReceiveAbstractObject channel = null;

    /**
     * tab页中的表格
     */
    private List<JTable> instructionCollectionTables;
    private List<DefaultTableModel> instructionCollectionTableModels;

    private Config config = null;




    public MainForm(Config config) {
        MainForm me = this;
        this.config = config;
        renderTabTable(config);
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
        // 在每条消息前添加日期时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        Date date = new Date(System.currentTimeMillis());
        String timeStr = simpleDateFormat.format(date);
        receiveArea.append(timeStr + " "+ message.toString());
        receiveArea.append("\n");
        receiveScroll.getVerticalScrollBar().setValue(receiveScroll.getVerticalScrollBar().getMaximum());
    }

    public void onReceive(String str){
        this.onReceive(new Message(str));
    }

    private void alert(String text){
        JOptionPane.showMessageDialog(null, text);
    }

    /**
     * 渲染表格
     * @param config
     */
    private void renderTabTable(Config config){
        if(config!=null){
            instructionCollectionTables = new ArrayList<>(config.getInstructionTab().size());
            instructionCollectionTableModels = new ArrayList<>(config.getInstructionTab().size());
            List<TabConfig> tabs = config.getInstructionTab();
            for(TabConfig tab : tabs){
                DefaultTableModel tableModel = new DefaultTableModel(tab.toArray(), new String[]{"序号", "名称"});
                JTable table = new JTable(tableModel) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }//表格不允许被编辑
                };
//                JTable table = new JTable(tab.toArray(), new String[]{"序号","名称"}){
//                    public boolean isCellEditable(int row, int column) {
//                        return false;
//                    }//表格不允许被编辑
//                };
                table.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getClickCount() == 2){
                            int row =((JTable)e.getSource()).rowAtPoint(e.getPoint()); //获得行位置
                            logger.info("双击了行：" + row);
                            sendTextArea.setText(tab.getRows().get(row).getSampleInstruction());
                        }else if(e.getButton() == MouseEvent.BUTTON3){
                            // 显示右击菜单
                            table.setRowSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
                            createTableRowMenu(e, table, instructionCollectionTableModels.get(collectionTab.getSelectedIndex()), table.rowAtPoint(e.getPoint()));
                        }
                    }
                });
                JScrollPane scrollPane = new JScrollPane(table);
                table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   //单选
                table.setRowHeight(20);
                instructionCollectionTables.add(table);
                instructionCollectionTableModels.add(tableModel);
                scrollPane.setViewportView(table);   //支持滚动

                collectionTab.add(tab.getTitle(),scrollPane);
            }
        }
    }

    /**
     * 创建表格行右击菜单
     * @param event
     */
    private void createTableRowMenu(MouseEvent event,JTable table, DefaultTableModel model, int rowIndex){
        JPopupMenu menu = new JPopupMenu();
        JMenuItem editMenu = new JMenuItem("修改");
        JMenuItem deleteMenu = new JMenuItem("删除");
        deleteMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.getInstructionTab().get(collectionTab.getSelectedIndex()).getRows().remove(rowIndex);
                model.removeRow(rowIndex);
            }
        });
        menu.add(editMenu);
        menu.add(deleteMenu);
        menu.show(event.getComponent(),event.getX(),event.getY());
    }



}
