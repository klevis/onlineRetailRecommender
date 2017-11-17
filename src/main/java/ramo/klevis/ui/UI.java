package ramo.klevis.ui;

import com.sun.org.apache.xml.internal.utils.res.XResources_zh_CN;
import org.apache.spark.sql.Row;
import ramo.klevis.data.Item;
import ramo.klevis.data.PrepareData;
import ramo.klevis.data.User;
import ramo.klevis.ml.IFCollaborativeFiltering;
import scala.collection.mutable.WrappedArray;

import javax.sound.midi.Soundbank;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by klevis.ramo on 10/29/2017.
 */
public class UI {
    private static final int FRAME_WIDTH = 1200;
    private static final int FRAME_HEIGHT = 628;
    private static final int FEATURE_SIZE = 150;
    private static final int TRAIN_SIZE = 80;
    private static final int TEST_SIZE = 20;
    private static final double REG_SIZE = 0.01;

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
    private HashMap<Integer, User> userHashMap;
    private HashMap<Integer, Item> itemHashMap;
    private SpinnerNumberModel model;
    private JSpinner featureField;
    private SpinnerNumberModel modelTrainSize;
    private JSpinner trainField;
    private SpinnerNumberModel modelTestSize;
    private JSpinner testField;
    private JLabel rmse;
    private SpinnerNumberModel modelRegSize;
    private JSpinner regField;

    public UI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Table.font", new FontUIResource(new Font("Dialog", Font.ITALIC, 14)));
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 14)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 16)));
        initUI();
    }

    private void initUI() throws Exception {
        mainFrame = createMainFrame();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        prepareData = new PrepareData();

        addCenterPanel();
        addTopPanel();
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

        addLabel(topPanel, "Select User", sansSerifItalic);

        JComboBox<User> comboBox = new JComboBox<>();
        comboBox.setFont(sansSerifBold);
        users = prepareData.readData();
        userHashMap = prepareData.getUserHashMap();
        itemHashMap = prepareData.getItemHashMap();
        users.stream().forEach(e -> comboBox.addItem(e));
        topPanel.add(comboBox);
        comboBox.addItemListener(e -> {
            int stateChange = e.getStateChange();
            if (stateChange == 1) {
                selectedUserInfoPanel.removeAll();
                selectedUserInfoPanel.setLayout(new GridLayout(25, 1));
                suggestingItemsPanel.removeAll();
                suggestingItemsPanel.setLayout(new GridLayout(15, 1));

                User user = (User) e.getItem();
                List<Item> items = user.getItems();
                for (Item item : items) {
                    updateSelectedPanel(item);
                }
                selectedUserInfoPanel.updateUI();

                List<Item> suggestedItems = user.getSuggestedItems();
                int i = 1;
                for (Item suggestedItem : suggestedItems) {
                    JLabel jLabel = addLabel(suggestingItemsPanel, i + " - " + suggestedItem.getDescription() + " - " + suggestedItem.getPrice() + " $", sansSerifItalic);
                    jLabel.setForeground(new Color(0, 0, 128));
                    suggestingItemsPanel.updateUI();
                    i++;
                }
                suggestingItemsPanel.updateUI();
            }
        });
        comboBox.setSelectedIndex(1);
        comboBox.setSelectedIndex(0);

        JButton trainButton = new JButton("Train Algorithm");
        trainButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> showProgressBar());
            new Thread(() -> {
                IFCollaborativeFiltering ifCollaborativeFiltering = new IFCollaborativeFiltering();
                List<Row> train;
                try {
                    train = ifCollaborativeFiltering.train((Integer) trainField.getValue(),
                            (Integer) testField.getValue(),
                            (Integer) featureField.getValue(),
                            (Double) regField.getValue());
                    updateUserWithSuggestion(train);
                    rmse.setText("" + ifCollaborativeFiltering.getRmse());
                    rmse.updateUI();
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                } finally {
                    progressBar.setVisible(false);
                }

            }).start();
        });
        topPanel.add(trainButton);

        addLabel(topPanel, "Feature Size", sansSerifBold);
        model = new SpinnerNumberModel(FEATURE_SIZE, 10, 150, 5);
        featureField = new JSpinner(model);
        featureField.setFont(sansSerifBold);
        topPanel.add(featureField);


        addLabel(topPanel, "Train Size", sansSerifBold);
        modelTrainSize = new SpinnerNumberModel(TRAIN_SIZE, 5, 100, 5);
        trainField = new JSpinner(modelTrainSize);
        trainField.setFont(sansSerifBold);
        topPanel.add(trainField);


        addLabel(topPanel, "Test Size", sansSerifBold);
        modelTestSize = new SpinnerNumberModel(TEST_SIZE, 5, 40, 5);
        testField = new JSpinner(modelTestSize);
        testField.setFont(sansSerifBold);
        topPanel.add(testField);

        addLabel(topPanel, "Reg Pram", sansSerifBold);

        modelRegSize = new SpinnerNumberModel(REG_SIZE, 0.001, 10, 0.05);
        regField = new JSpinner(modelRegSize);
        regField.setFont(sansSerifBold);
        topPanel.add(regField);

        rmse = addLabel(topPanel, "RMSE : ", sansSerifBold);

        mainPanel.add(topPanel, BorderLayout.NORTH);

    }

    private JLabel addLabel(JPanel panel, String text, Font sansSerifBold) {
        JLabel label = new JLabel(text);
        label.setFont(sansSerifBold);
        panel.add(label);
        return label;
    }

    private void updateSelectedPanel(Item item) {
        JLabel jLabel = addLabel(selectedUserInfoPanel, item.getDescription() + " - " + item.getSize() + " - " + item.getPrice() + " $", sansSerifItalic);
        jLabel.setForeground(new Color(0, 0, 205));
    }

    private void updateUserWithSuggestion(List<Row> train) {
        for (Row row : train) {
            int userId = (int) row.apply(0);
            WrappedArray items = (WrappedArray) row.apply(1);
            Row[] rows = (Row[]) items.array();
            for (Row rowItem : rows) {
                User user = userHashMap.get(userId);
                user.addSuggestedItem(itemHashMap.get((Integer) rowItem.get(0)));
            }
        }
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
