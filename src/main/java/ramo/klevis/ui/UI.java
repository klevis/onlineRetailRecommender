package ramo.klevis.ui;

import ramo.klevis.data.Item;
import ramo.klevis.data.PrepareData;
import ramo.klevis.data.User;
import ramo.klevis.ml.IFCollaborativeFiltering;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * Created by klevis.ramo on 10/29/2017.
 */
public class UI {
    private static final int FRAME_WIDTH = 1200;
    private static final int FRAME_HEIGHT = 628;

    private JFrame mainFrame;
    private JPanel mainPanel;
    private JProgressBar progressBar;
    private final Font sansSerifBold = new Font("SansSerif", Font.BOLD, 14);
    private final Font sansSerifItalic = new Font("SansSerif", Font.ITALIC, 14);
    private final Font serifItalic = new Font("Serif", Font.ITALIC, 14);
    private PrepareData prepareData;
    private JPanel selectedUserInfoPanel;
    private JPanel suggestingItemsPanel;
    private List<User> users;

    public UI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Table.font", new FontUIResource(new Font("Dialog", Font.ITALIC, 14)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 16)));
        initUI();
    }

    private void initUI() throws Exception {
        mainFrame = createMainFrame();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        prepareData = new PrepareData();

        addTopPanel();
        addCenterPanel();
        addSignature();

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private void addCenterPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2));
        selectedUserInfoPanel = new JPanel();
        selectedUserInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Selected User Buying History",
                TitledBorder.LEFT,
                TitledBorder.TOP, sansSerifBold, Color.BLUE));
        contentPanel.add(new JScrollPane(selectedUserInfoPanel));
        suggestingItemsPanel = new JPanel();
        suggestingItemsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Suggested Items for selected User",
                TitledBorder.CENTER,
                TitledBorder.TOP, sansSerifBold, Color.BLUE));
        contentPanel.add(new JScrollPane(suggestingItemsPanel));
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void addTopPanel() throws Exception {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Select User");
        label.setFont(sansSerifItalic);
        topPanel.add(label);

        JComboBox<User> comboBox = new JComboBox<>();
        comboBox.setFont(sansSerifBold);
        users = prepareData.readData();
        users.stream().forEach(e -> comboBox.addItem(e));
        topPanel.add(comboBox);
        comboBox.addItemListener(e -> {
            int stateChange = e.getStateChange();
            if (stateChange == 1) {
                selectedUserInfoPanel.removeAll();
                User user = (User) e.getItem();
                List<Item> items = user.getItems();
                selectedUserInfoPanel.setLayout(new GridLayout(25, 1));
                for (Item item : items) {
                    JLabel info = new JLabel(item.getDescription() + " - " + item.getSize() + " - " + item.getPrice() + " $");
                    info.setFont(serifItalic);
                    selectedUserInfoPanel.add(info);
                }
                selectedUserInfoPanel.updateUI();
            }
        });

        JButton trainButton = new JButton("Train Algorithm");
        trainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new IFCollaborativeFiltering().train();
            }
        });
        topPanel.add(trainButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Book Recommender");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        ImageIcon imageIcon = new ImageIcon("icon.png");
        mainFrame.setIconImage(imageIcon.getImage());

        return mainFrame;
    }


    private void showProgressBar() {
        SwingUtilities.invokeLater(() -> {
            progressBar = createProgressBar(mainFrame);
            progressBar.setString("Training Algorithm!Please wait it may take one or two minutes");
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            mainFrame.repaint();
        });
    }

    private JProgressBar createProgressBar(JFrame mainFrame) {
        JProgressBar jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        jProgressBar.setVisible(false);
        mainFrame.add(jProgressBar, BorderLayout.NORTH);
        return jProgressBar;
    }

    private void addSignature() {
        JLabel signature = new JLabel("ramok.tech", JLabel.HORIZONTAL);
        signature.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 20));
        signature.setForeground(Color.BLUE);
        mainPanel.add(signature, BorderLayout.SOUTH);
    }
}
