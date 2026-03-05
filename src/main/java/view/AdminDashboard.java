package view;

import core.CSVExporter;
import dao.MemberDAO;
import model.Member;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private MemberDAO memberDAO;
    private JLabel totalMembersLabel;
    private JLabel presentMembersLabel;
    private JLabel todayCheckinsLabel;
    private JLabel totalRevenueLabel;

    private DefaultCategoryDataset revenueDataset;

    public AdminDashboard() {
        memberDAO = new MemberDAO();
        setupUI();
        loadMemberData();
    }

    private void setupUI() {
        setTitle("Enterprise Gym BI Dashboard");
        setSize(1450, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainContainer);

        // Header & Controls
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Member Database & Financial Analytics");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(110, 190, 255));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // Export Button
        JButton exportBtn = createStyledButton("Export Roster", new Color(41, 105, 190));
        exportBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("Active_Gym_Roster.csv"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
                if (CSVExporter.exportTableToCSV(memberTable.getModel(), file)) {
                    JOptionPane.showMessageDialog(this, "Exported successfully!");
                }
            }
        });

        // Check-Out Button
        JButton checkOutBtn = createStyledButton("Check-Out", new Color(108, 117, 125)); // Slate Gray
        checkOutBtn.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a member first."); return; }
            if (memberDAO.checkOutMember((int) tableModel.getValueAt(row, 0))) loadMemberData();
        });

        // Check-In Button
        JButton checkInBtn = createStyledButton("Check-In", new Color(224, 142, 11)); // Orange
        checkInBtn.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a member first."); return; }
            if (memberDAO.logCheckIn((int) tableModel.getValueAt(row, 0))) loadMemberData();
        });

        // Remove User Button
        JButton removeBtn = createStyledButton("Remove", new Color(211, 47, 47)); // Crimson Red
        removeBtn.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a member to remove."); return; }

            String name = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete " + name + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (memberDAO.deleteMember((int) tableModel.getValueAt(row, 0))) loadMemberData();
            }
        });

        // Add Member Button
        JButton addMemberBtn = createStyledButton("+ Add Member", new Color(43, 171, 96)); // Green
        addMemberBtn.addActionListener(e -> {
            AddMemberDialog dialog = new AddMemberDialog(this);
            dialog.setVisible(true);
            if (dialog.isRegistered()) loadMemberData();
        });

        actionPanel.add(exportBtn);
        actionPanel.add(checkOutBtn);
        actionPanel.add(checkInBtn);
        actionPanel.add(removeBtn);
        actionPanel.add(addMemberBtn);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        // Analytics HUD
        JPanel hudPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        hudPanel.setBorder(new EmptyBorder(15, 0, 20, 0));

        totalMembersLabel = createStatCard("Total Members", "0", new Color(110, 190, 255));
        presentMembersLabel = createStatCard("Present Now", "0", new Color(85, 255, 85)); // Updated
        todayCheckinsLabel = createStatCard("Check-ins Today", "0", new Color(255, 170, 0));
        totalRevenueLabel = createStatCard("Total Revenue", "₹0", new Color(200, 100, 255));

        hudPanel.add(totalMembersLabel);
        hudPanel.add(presentMembersLabel);
        hudPanel.add(todayCheckinsLabel);
        hudPanel.add(totalRevenueLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(hudPanel, BorderLayout.CENTER);
        mainContainer.add(topContainer, BorderLayout.NORTH);

        // Data Table
        String[] columns = {"ID", "First Name", "Last Name", "Phone", "Plan", "Trainer", "Status", "Last Check-In"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        memberTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isCellSelected(row, column)) {
                    c.setBackground(row % 2 == 0 ? new Color(43, 45, 48) : new Color(33, 35, 38));
                }
                return c;
            }
        };
        memberTable.setRowHeight(35);
        memberTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        memberTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        memberTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        memberTable.setShowGrid(false);

        memberTable.getColumnModel().getColumn(0).setMaxWidth(50);
        memberTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        memberTable.getColumnModel().getColumn(6).setPreferredWidth(110); // Made slightly wider for "CHECKED OUT"
        memberTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        //  Status Column Renderer for Checked In / Checked Out
        memberTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("CHECKED IN".equals(value)) {
                    label.setForeground(new Color(85, 255, 85)); // Neon Green
                } else {
                    label.setForeground(new Color(150, 150, 150)); // Dim Gray
                }
                return label;
            }
        });

        JScrollPane tableScroll = new JScrollPane(memberTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65), 1));

        // Financial Chart Panel
        revenueDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart("Revenue by Membership Plan", "", "Revenue (₹)", revenueDataset, PlotOrientation.VERTICAL, false, true, false);
        applyDarkThemeToChart(barChart);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(500, 400));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(new Color(43, 45, 48));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, chartPanel);
        splitPane.setDividerLocation(850);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        mainContainer.add(splitPane, BorderLayout.CENTER);
    }

    // Helper to keep button styling clean
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    private void applyDarkThemeToChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(43, 45, 48));
        chart.getTitle().setPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(33, 35, 38));
        plot.setDomainGridlinePaint(new Color(70, 73, 75));
        plot.setRangeGridlinePaint(new Color(70, 73, 75));
        plot.setOutlineVisible(false);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
        domainAxis.setLabelPaint(Color.WHITE);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
        rangeAxis.setLabelPaint(Color.WHITE);
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(200, 100, 255));
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.2);
    }

    private JLabel createStatCard(String title, String value, Color color) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        JLabel label = new JLabel("<html><div style='text-align: center; padding: 15px;'>" +
                "<span style='font-size: 15px; color: #A0A0A0;'>" + title + "</span><br><br>" +
                "<span style='font-size: 32px; font-weight: bold; color: " + hex + ";'>" + value + "</span>" +
                "</div></html>");
        label.setOpaque(true);
        label.setBackground(new Color(33, 35, 38));
        label.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65), 1));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void loadMemberData() {
        tableModel.setRowCount(0);

        // Update HUD
        Map<String, Integer> metrics = memberDAO.getDashboardMetrics();
        double totalRev = memberDAO.getTotalRevenue();

        totalMembersLabel.setText(createStatCard("Total Members", String.valueOf(metrics.get("total")), new Color(110, 190, 255)).getText());
        presentMembersLabel.setText(createStatCard("Present Now", String.valueOf(metrics.get("present")), new Color(85, 255, 85)).getText());
        todayCheckinsLabel.setText(createStatCard("Check-ins Today", String.valueOf(metrics.get("today_checkins")), new Color(255, 170, 0)).getText());
        totalRevenueLabel.setText(createStatCard("Total Revenue", "₹" + String.format("%.2f", totalRev), new Color(200, 100, 255)).getText());

        // Update the Chart
        revenueDataset.clear();
        Map<String, Double> planRevenue = memberDAO.getRevenueByPlan();
        for (Map.Entry<String, Double> entry : planRevenue.entrySet()) {
            revenueDataset.addValue(entry.getValue(), "Revenue", entry.getKey());
        }

        // Update Table Data
        List<Member> members = memberDAO.getAllMembers();
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                    m.getMemberId(), m.getFirstName(), m.getLastName(), m.getPhone(),
                    m.getPlanName(), m.getTrainerName(), m.isCheckedIn() ? "CHECKED IN" : "CHECKED OUT", m.getLastCheckIn()
            });
        }
    }
}