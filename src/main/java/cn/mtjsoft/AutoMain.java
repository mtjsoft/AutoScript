package cn.mtjsoft;

import cn.mtjsoft.view.AutoWindow;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.swing.*;
import java.util.List;

public class AutoMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Namespace ns = start(args);
        String resPath = "";
        String projectPath = "";
        Boolean debug = false;
        Boolean alpha = false;
        Boolean release = false;
        if (ns != null) {
            resPath = ns.getString("resPath");
            projectPath = ns.getString("projectPath");
            debug = ns.getBoolean("assembleDebug");
            alpha = ns.getBoolean("assembleAlpha");
            release = ns.getBoolean("assembleRelease");
        }
        new AutoWindow().showWindow(resPath, projectPath, debug, alpha, release);
    }

    /**
     * @param args
     * @return
     */
    private static Namespace start(String[] args) {
        ArgumentParser parser =
                ArgumentParsers.newFor("AutoScript").build().defaultHelp(true).description("Calculate checksum of given files.");
        parser.addArgument("-rp", "--resPath")
                .required(true)
                .type(String.class)
                .dest("resPath")
                .help("��Դ�ļ�·��");
        parser.addArgument("-pp", "--projectPath")
                .required(true)
                .type(String.class)
                .dest("projectPath")
                .help("��Ŀ·��");
        parser.addArgument("-debug", "--assembleDebug")
                .type(Boolean.class)
                .nargs("?")
                .setConst(true).dest("assembleDebug")
                .help("�Զ�Debug���,�����ַ�Ĭ����Ŀ·��");
        parser.addArgument("-alpha", "--assembleAlpha")
                .type(Boolean.class).nargs("?")
                .setConst(true)
                .dest("assembleAlpha")
                .help("�Զ�Alpha���,�����ַ�Ĭ����Ŀ·��");
        parser.addArgument("-release", "--assembleRelease")
                .type(Boolean.class)
                .nargs("?")
                .setConst(true)
                .dest("assembleRelease")
                .help("�Զ�Release���,�����ַ�Ĭ����Ŀ·��");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
            System.out.println(ns.toString());
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(0);
        }
        return ns;
    }
}
