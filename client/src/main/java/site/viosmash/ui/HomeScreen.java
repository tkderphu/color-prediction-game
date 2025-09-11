package site.viosmash.ui;

import site.viosmash.network.ClientConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class HomeScreen extends JFrame {
    private String currentUser;
    private ClientConnection connection;
    public HomeScreen(String currentUser, java.util.List<String> allUsers) {
        this.currentUser = currentUser;
        setTitle("Home Screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JButton btnUsers = new JButton("Display Users");
        JButton btnPlay = new JButton("Play Game");
        JButton btnHistory = new JButton("History Play Game");
        JButton btnLogout = new JButton("Logout");

        btnUsers.addActionListener(e -> showUsers(allUsers));
        btnPlay.addActionListener(e -> {
            new PrepareGameScreen(currentUser, allUsers).setVisible(true);
            dispose();
        });
        btnHistory.addActionListener(e -> showHistory());


        JLabel lblUser = new JLabel("Logged in as: " + currentUser);
        lblUser.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(btnUsers);
        buttonPanel.add(btnPlay);
        buttonPanel.add(btnHistory);
        buttonPanel.add(btnLogout);
        setLayout(new BorderLayout());
        add(lblUser, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private void showUsers(java.util.List<String> allUsers) {
        JDialog dialog = new JDialog(this, "All Users", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Username"}, 0);
        for (String user : allUsers) {
            model.addRow(new Object[]{user});
        }
        JTable table = new JTable(model);
        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }

    private void showHistory() {
        JDialog dialog = new JDialog(this, "Play History - " + currentUser, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        // Dummy data for now
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Round", "Result", "Score"}, 0);
        model.addRow(new Object[]{1, "Win", 5});
        model.addRow(new Object[]{2, "Lose", 3});
        model.addRow(new Object[]{3, "Win", 6});

        JTable table = new JTable(model);
        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            java.util.List<String> users = Arrays.asList("Alice", "Bob", "Charlie", "David");
            new HomeScreen("Alice", users).setVisible(true);
        });
    }
}

// ================= Prepare Game Screen =================
class PrepareGameScreen extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private DefaultListModel<String> invitedUsersModel;
    private String currentUser;
    private java.util.List<String> allUsers;

    public PrepareGameScreen(String currentUser, java.util.List<String> allUsers) {
        this.currentUser = currentUser;
        this.allUsers = allUsers;

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
            new GameFrame.PlayGameScreen(players).setVisible(true);
            dispose();
        });

        // Close button (return to Home)
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            new HomeScreen(currentUser, allUsers).setVisible(true);
            dispose();
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonsPanel.add(startGameButton);
        buttonsPanel.add(closeButton);

        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Invited Users:"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(invitedUsersList), BorderLayout.CENTER);
        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);

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
}
