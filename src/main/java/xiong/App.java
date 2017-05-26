package xiong;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

/**
 * Created by johnson on 25/05/2017.
 */
public class App {

    private JPanel rootPnl;
    private JButton fileBth;
    private JTextArea printPnl;
    private JFileChooser jFileChooser = new JFileChooser();
    private JPanel bottomPnl;
    private JButton openResultBtn;
    private JButton clearResultBtn;

    private Business business;

    public App() {
        createUI();
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setFileFilter(new FileNameExtensionFilter("excel", "xls"));

        business = new Business(printPnl);
    }

    private void createUI() {
        rootPnl = new JPanel();
        rootPnl.setPreferredSize(new Dimension(600, 400));
        rootPnl.setLayout(new BorderLayout());
        printPnl = new JTextArea();
        rootPnl.add(printPnl, BorderLayout.CENTER);
        bottomPnl = new JPanel();
        rootPnl.add(bottomPnl, BorderLayout.SOUTH);
        fileBth = new JButton("选excel文件");
        fileBth.addActionListener(e -> {
            if (JFileChooser.APPROVE_OPTION == jFileChooser.showOpenDialog(rootPnl)) {
                business.readFiels(jFileChooser.getSelectedFiles());
            }
        });
        bottomPnl.add(fileBth);
        openResultBtn = new JButton("打开结果目录");
        openResultBtn.addActionListener(e -> {
            business.openResultDir();
        });
        bottomPnl.add(openResultBtn);
        clearResultBtn = new JButton("清空结果目录");
        clearResultBtn.addActionListener(e->{
            business.clearResultDir();
        });
        bottomPnl.add(clearResultBtn);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("xiong工作室");
        frame.setContentPane(new App().rootPnl);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


}
