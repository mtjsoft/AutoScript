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
        String debug = "";
        String alpha = "";
        String release = "";
        if (ns != null) {
            resPath = ns.getString("resPath");
            projectPath = ns.getString("projectPath");
        }
        new AutoWindow().showWindow(resPath, projectPath, debug, alpha, release);
    }

    /**
     * @param args
     * @return
     */
    private static Namespace start(String[] args) {
        ArgumentParser parser =
                ArgumentParsers.newFor("").build().defaultHelp(true).description("Calculate checksum of given files.");
        parser.addArgument("-rp", "--resPath").help("资源文件路径");
        parser.addArgument("-pp", "--projectPath").help("项目路径");
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
