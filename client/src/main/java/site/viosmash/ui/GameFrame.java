package site.viosmash.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class GameFrame extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private DefaultListModel<String> invitedUsersModel;
    private String currentUser;

    public GameFrame(String currentUser, java.util.List<String> allUsers) {
        this.currentUser = currentUser;
        setTitle("Prepare Start Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Table model with Invite button
        tableModel = new DefaultTableModel(new Object[]{"Username", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        userTable = new JTable(tableModel);
        userTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Add users to table
        for (String user : allUsers) {
            if (!user.equals(currentUser)) {
                tableModel.addRow(new Object[]{user, "Invite"});
            }
        }

        // List of invited users
        invitedUsersModel = new DefaultListModel<>();
        invitedUsersModel.addElement(currentUser + " (You)");
        JList<String> invitedUsersList = new JList<>(invitedUsersModel);

        // Start game button
        JButton startGameButton = new JButton("Start Game");
        startGameButton.addActionListener(e -> {
            java.util.List<String> players = Collections.list(invitedUsersModel.elements());
            new PlayGameScreen(players).setVisible(true);
            dispose();
        });

        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Invited Users:"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(invitedUsersList), BorderLayout.CENTER);
        rightPanel.add(startGameButton, BorderLayout.SOUTH);

        getContentPane().setLayout(new GridLayout(1, 2));
        getContentPane().add(leftPanel);
        getContentPane().add(rightPanel);
    }

    // Renderer for Invite button
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "Invite" : value.toString());
            return this;
        }
    }

    // Editor for Invite button
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String username;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            username = (String) table.getValueAt(row, 0);
            button.setText("Invite");
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                if (!invitedUsersModel.contains(username)) {
                    invitedUsersModel.addElement(username);
                } else {
                    JOptionPane.showMessageDialog(button, username + " is already invited!");
                }
            }
            clicked = false;
            return "Invite";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    // ================= Play Game Screen =================
    static class PlayGameScreen extends JFrame {
        private JTable scoreTable;
        private DefaultTableModel scoreModel;
        private JPanel colorPanel;
        private java.util.List<String> players;
        private Random random = new Random();
        private java.util.List<String> currentColors;
        private JLabel roundLabel;
        private int round = 1;

        public PlayGameScreen(java.util.List<String> players) {
            this.players = players;
            setTitle("Play Game");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(700, 500);
            setLocationRelativeTo(null);

            // Scoreboard
            scoreModel = new DefaultTableModel(new Object[]{"Player", "Score"}, 0);
            for (String player : players) {
                scoreModel.addRow(new Object[]{player, 0});
            }
            scoreTable = new JTable(scoreModel);

            // Color display panel (show multiple colors + round label)
            colorPanel = new JPanel();
            colorPanel.setLayout(new BorderLayout());

            JPanel colorsGrid = new JPanel(new GridLayout(1, 3, 10, 10));
            colorPanel.add(colorsGrid, BorderLayout.CENTER);

            roundLabel = new JLabel("Round: 1", SwingConstants.CENTER);
            roundLabel.setFont(new Font("Arial", Font.BOLD, 16));
            colorPanel.add(roundLabel, BorderLayout.SOUTH);

            showRandomColors(colorsGrid);

            // Button to choose colors (open dialog)
            JButton chooseBtn = new JButton("Choose Colors");
            chooseBtn.addActionListener(e -> openColorChoiceDialog(colorsGrid));

            // Layout
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.add(new JScrollPane(scoreTable), BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.add(colorPanel, BorderLayout.CENTER);
            rightPanel.add(chooseBtn, BorderLayout.SOUTH);

            getContentPane().setLayout(new GridLayout(1, 2));
            getContentPane().add(leftPanel);
            getContentPane().add(rightPanel);
        }

        private void showRandomColors(JPanel colorsGrid) {
            colorsGrid.removeAll();
            currentColors = new ArrayList<>();

            String[] colors = {"Red", "Green", "Blue"};
            int numColors = 3; // can be increased in future

            Set<Integer> chosen = new HashSet<>();
            while (chosen.size() < numColors) {
                chosen.add(random.nextInt(colors.length));
            }

            for (int idx : chosen) {
                String color = colors[idx];
                currentColors.add(color);
                JPanel cPanel = new JPanel();
                switch (color) {
                    case "Red": cPanel.setBackground(Color.RED); break;
                    case "Green": cPanel.setBackground(Color.GREEN); break;
                    case "Blue": cPanel.setBackground(Color.BLUE); break;
                }
                colorsGrid.add(cPanel);
            }

            colorsGrid.revalidate();
            colorsGrid.repaint();
        }

        private void openColorChoiceDialog(JPanel colorsGrid) {
            JDialog dialog = new JDialog(this, "Choose 3 Colors", true);
            dialog.setSize(300, 200);
            dialog.setLocationRelativeTo(this);

            String[] colorOptions = {"Red", "Green", "Blue"};
            JList<String> list = new JList<>(colorOptions);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JButton confirm = new JButton("Confirm");
            confirm.addActionListener(e -> {
                java.util.List<String> chosen = list.getSelectedValuesList();
                if (chosen.size() != 3) {
                    JOptionPane.showMessageDialog(dialog, "Please choose exactly 3 colors!");
                } else {
                    if (new HashSet<>(chosen).equals(new HashSet<>(currentColors))) {
                        int currentScore = (int) scoreModel.getValueAt(0, 1);
                        scoreModel.setValueAt(currentScore + 1, 0, 1);
                        JOptionPane.showMessageDialog(this, "Correct! +1 point");
                    } else {
                        JOptionPane.showMessageDialog(this, "Wrong! Colors were: " + currentColors);
                    }
                    round++;
                    roundLabel.setText("Round: " + round);
                    showRandomColors(colorsGrid);
                    dialog.dispose();
                }
            });

            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(list), BorderLayout.CENTER);
            dialog.add(confirm, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            java.util.List<String> users = Arrays.asList("Alice", "Bob", "Charlie", "David");
            new GameFrame("Alice", users).setVisible(true);
        });
    }
}