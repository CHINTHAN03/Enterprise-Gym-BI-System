package view;

import dao.MemberDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddMemberDialog extends JDialog {
    private JTextField firstNameField, lastNameField, phoneField;
    private JComboBox<String> planCombo, trainerCombo;
    private boolean isRegistered = false;

    public AddMemberDialog(JFrame parent) {
        super(parent, "Register New Gym Member", true);
        setupUI();
    }

    private void setupUI() {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));


        formPanel.add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        formPanel.add(firstNameField);

        formPanel.add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        formPanel.add(lastNameField);

        formPanel.add(new JLabel("Phone Number:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);


        formPanel.add(new JLabel("Membership Plan:"));
        String[] plans = {"None", "Standard Access (ID: 1)", "Quarterly Strength Pro (ID: 2)", "Annual VIP (ID: 3)"};
        planCombo = new JComboBox<>(plans);
        formPanel.add(planCombo);

        formPanel.add(new JLabel("Assign Trainer:"));
        String[] trainers = {"None", "Arjun Kumar (ID: 1)", "Priya Sharma (ID: 2)"};
        trainerCombo = new JComboBox<>(trainers);
        formPanel.add(trainerCombo);

        add(formPanel, BorderLayout.CENTER);


        JButton submitButton = new JButton("Register Member");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setBackground(new Color(85, 255, 85));
        submitButton.setForeground(Color.BLACK);

        submitButton.addActionListener(e -> saveMember());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        bottomPanel.add(submitButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void saveMember() {
        String fName = firstNameField.getText().trim();
        String lName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();


        int planId = planCombo.getSelectedIndex();
        int trainerId = trainerCombo.getSelectedIndex();

        if (fName.isEmpty() || lName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First and Last Name are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MemberDAO dao = new MemberDAO();
        if (dao.registerNewMember(fName, lName, phone, planId, trainerId)) {
            isRegistered = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Database Error: Could not save member.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}