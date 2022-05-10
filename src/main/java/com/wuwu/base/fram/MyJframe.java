package com.wuwu.base.fram;

import com.wuwu.base.client.WuRedisClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class MyJframe {

    private static WuRedisClient client = new WuRedisClient();


    public static void main(String[] args) {

        new MyJframe().crateFrame();

        System.out.println("11");
    }

    private void crateFrame() {
//        JFrame.setDefaultLookAndFeelDecorated(true);
        // 创建及设置窗口
        JFrame frame = new JFrame();

        URL resource = MyJframe.class.getResource("/img3.png");
        ImageIcon imageIcon = new ImageIcon("src/main/resources/img4.png");
        ImageIcon imageIcon2 = new ImageIcon(resource);

        frame.setIconImage(imageIcon2.getImage());

        frame.setTitle("wuma redis client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,200);
        frame.setLocation(500,500);

        JPanel panel = new JPanel();
        panel.setSize(300,100);
        JLabel lable = new JLabel("命令:");
        JLabel lable2 = new JLabel("结果:");

        JTextField text1 = new JTextField();
        text1.setSize(100,50);
        JTextField text2 = new JTextField();
        text2.setSize(100,50);

        JButton button = new JButton("点击");

        buttonClick(text1, text2, button);

        panel.setLayout(new GridLayout(3,2,5,5));
        panel.add(lable);
        panel.add(text1);
        panel.add(lable2);
        panel.add(text2);
        panel.add(button);

        frame.add(panel);
        // 显示窗口
        frame.setVisible(true);
    }

    private static void buttonClick(JTextField text1, JTextField text2, JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    String common = text1.getText();
                    client.getResponse().getCommons().add(common);
                    String res = client.getResponse().getResult().take();
                    text2.setText(res);
                }catch (Exception exception){
                    client.getResponse().getCommons().clear();
                    client.getResponse().getResult().clear();
                    exception.printStackTrace();
                }
            }
        });
    }

}
