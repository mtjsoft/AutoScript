package cn.mtjsoft;

import cn.mtjsoft.view.AutoWindow;

import javax.swing.*;

public class AutoMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new AutoWindow().showWindow();
    }
}
