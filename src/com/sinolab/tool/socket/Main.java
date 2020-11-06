package com.sinolab.tool.socket;

import javax.swing.*;
import java.io.File;
import java.util.logging.Logger;

public class Main {


    public static void main(String[] args) {
       Logger logger = Logger.getLogger("main");

        JFrame frame = new JFrame("SOCKET调试工具");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        File file = new File("config.json");
        if(file.exists()){
            logger.info("找到配置文件，文件路径：" + file.getAbsolutePath());
        }else{
            logger.warning("未在同目录下找到名为config.json的配置文件！");
            logger.warning("当前目录为: " + file.getAbsolutePath());
        }


        frame.setContentPane(new MainForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
