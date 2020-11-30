package com.sinolab.tool.socket;

import com.sinolab.tool.socket.util.Util;
import kotlin.text.Regex;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;
import java.util.logging.Logger;

public class InstructionWin {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private JTextField nameField;
    private JTextArea requestSampleTextArea;
    private JTextArea responseDefinitionTextArea;
    private JButton saveButton;
    public JPanel panel;
    private JTextArea sampleArea;
    private JTextField signIndexText;
    private JTextArea requestDefinitionTextArea;
    private DefaultTableModel model;
    private TabConfig tabConfig;
    private Integer rowIndex;
    private JDialog dialog;

    public InstructionWin(){
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rowIndex==null){
                    RowConfig rowConfig = new RowConfig();
                    rowConfig.setName(nameField.getText());
                    rowConfig.setRequestDefinition(requestDefinitionTextArea.getText());    // 设置请求命令的定义
                    rowConfig.setRequestSample(requestSampleTextArea.getText());            // 设置请求命令的样例
                    rowConfig.setResponseDefinition(responseDefinitionTextArea.getText());  // 设置与其返回的定义
//                    rowConfig.setSignIndex(Util.INSTANCE.stringToIntArray(signIndexText.getText(),new Regex(",|，")));
                    tabConfig.getRows().add(rowConfig);
                    model.addRow(rowConfig.toArray());
                }else{
                    RowConfig rowConfig = tabConfig.getRows().get(rowIndex);
                    rowConfig.setName(nameField.getText());
                    rowConfig.setRequestDefinition(requestDefinitionTextArea.getText());
                    rowConfig.setRequestSample(requestSampleTextArea.getText());
                    rowConfig.setResponseDefinition(responseDefinitionTextArea.getText());
//                    rowConfig.setSignIndex(Util.INSTANCE.stringToIntArray(signIndexText.getText(),new Regex(",|，")));
                    // 这里写的不好，耦合度太高了
                    Object o = model.getDataVector().toArray()[rowIndex];
                    ((Vector<String>)o).set(1, nameField.getText());
//                    ((String[])o)[2] = instructionArea.getText();
//                    ((String[])o)[3] = responseDefinitionTextArea.getText();
                }
                dialog.dispose();   // 关闭并释放窗口资源
                Main.getConfig().syncToFile(); // 将信息同步至配置文件
            }
        });
        responseDefinitionTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = responseDefinitionTextArea.getText();
                logger.info("预期返回内容改变为：" + text);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = responseDefinitionTextArea.getText();
                logger.info("预期返回内容改变为：" + text);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                String text = responseDefinitionTextArea.getText();
                logger.info("预期返回内容改变为：" + text);
            }
        });
    }

    /**
     *
     * @param model 表格数据模型
     * @param rowIndex 编辑的行，如为空，则新增一条数据
     */
    public InstructionWin(JDialog dialog,DefaultTableModel model, TabConfig config, Integer rowIndex){
        this();
        this.model = model;
        this.tabConfig = config;
        this.rowIndex = rowIndex;
        this.dialog = dialog;
        if(rowIndex!=null){
            // 这里写的不好，耦合度太高了
            RowConfig rowConfig = config.getRows().get(rowIndex);
            nameField.setText(rowConfig.getName());
            requestDefinitionTextArea.setText(rowConfig.getRequestDefinition());
            requestSampleTextArea.setText(rowConfig.getRequestSample());
            responseDefinitionTextArea.setText(rowConfig.getResponseDefinition());
        }

    }
}
