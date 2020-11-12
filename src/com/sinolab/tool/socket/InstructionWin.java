package com.sinolab.tool.socket;

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
    private JTextArea instructionArea;
    private JTextArea expectedInstructionArea;
    private JButton saveButton;
    public JPanel panel;
    private JTextArea sampleArea;
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
                    rowConfig.setSampleInstruction(instructionArea.getText());
                    rowConfig.setExpectedResponse(expectedInstructionArea.getText());
                    tabConfig.getRows().add(rowConfig);
                    model.addRow(rowConfig.toArray());
                }else{
                    tabConfig.getRows().get(rowIndex).setName(nameField.getText());
                    tabConfig.getRows().get(rowIndex).setSampleInstruction(instructionArea.getText());
                    tabConfig.getRows().get(rowIndex).setExpectedResponse(expectedInstructionArea.getText());
                    // 这里写的不好，耦合度太高了
                    Object o = model.getDataVector().toArray()[rowIndex];
                    ((Vector<String>)o).set(1, nameField.getText());
//                    ((String[])o)[2] = instructionArea.getText();
//                    ((String[])o)[3] = expectedInstructionArea.getText();
                }
                dialog.dispose();   // 关闭并释放窗口资源
                Main.getConfig().syncToFile(); // 将信息同步至配置文件
            }
        });
        expectedInstructionArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = expectedInstructionArea.getText();
                logger.info("预期返回内容改变为：" + text);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = expectedInstructionArea.getText();
                logger.info("预期返回内容改变为：" + text);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                String text = expectedInstructionArea.getText();
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
            nameField.setText(config.getRows().get(rowIndex).getName());
            instructionArea.setText(config.getRows().get(rowIndex).getSampleInstruction());
            expectedInstructionArea.setText(config.getRows().get(rowIndex).getExpectedResponse());
        }
    }
}
