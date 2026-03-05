package dao;

import core.DatabaseConnection;
import model.Member;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();

        String query = "SELECT m.member_id, m.first_name, m.last_name, m.phone_number, " +
                "p.plan_name, t.first_name AS trainer_first, t.last_name AS trainer_last, m.is_checked_in, " +
                "(SELECT MAX(check_in_time) FROM Check_Ins c WHERE c.member_id = m.member_id) AS last_check_in " +
                "FROM Members m " +
                "LEFT JOIN Membership_Plans p ON m.plan_id = p.plan_id " +
                "LEFT JOIN Trainers t ON m.assigned_trainer_id = t.trainer_id " +
                "ORDER BY m.member_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String trainerName = rs.getString("trainer_first") != null ? rs.getString("trainer_first") + " " + rs.getString("trainer_last") : "Unassigned";
                String planName = rs.getString("plan_name") != null ? rs.getString("plan_name") : "No Active Plan";
                String lastCheckIn = rs.getTimestamp("last_check_in") != null ? rs.getTimestamp("last_check_in").toString() : "Never Checked In";

                members.add(new Member(
                        rs.getInt("member_id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("phone_number"), planName, trainerName, rs.getBoolean("is_checked_in"), lastCheckIn
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return members;
    }

    public boolean registerNewMember(String firstName, String lastName, String phone, int planId, int trainerId) {
        String insertMember = "INSERT INTO Members (first_name, last_name, phone_number, plan_id, assigned_trainer_id, is_active, is_checked_in) VALUES (?, ?, ?, ?, ?, TRUE, FALSE)";
        String insertPayment = "INSERT INTO Payments (member_id, amount) VALUES (?, (SELECT price FROM Membership_Plans WHERE plan_id = ?))";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int newMemberId = -1;
            try (java.sql.PreparedStatement pstmt1 = conn.prepareStatement(insertMember, Statement.RETURN_GENERATED_KEYS)) {
                pstmt1.setString(1, firstName);
                pstmt1.setString(2, lastName);
                pstmt1.setString(3, phone);
                if (planId > 0) pstmt1.setInt(4, planId); else pstmt1.setNull(4, java.sql.Types.INTEGER);
                if (trainerId > 0) pstmt1.setInt(5, trainerId); else pstmt1.setNull(5, java.sql.Types.INTEGER);
                pstmt1.executeUpdate();
                ResultSet rs = pstmt1.getGeneratedKeys();
                if (rs.next()) newMemberId = rs.getInt(1);
            }

            if (newMemberId != -1 && planId > 0) {
                try (java.sql.PreparedStatement pstmt2 = conn.prepareStatement(insertPayment)) {
                    pstmt2.setInt(1, newMemberId);
                    pstmt2.setInt(2, planId);
                    pstmt2.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    public boolean logCheckIn(int memberId) {
        String logQuery = "INSERT INTO Check_Ins (member_id) VALUES (?)";
        String statusQuery = "UPDATE Members SET is_checked_in = TRUE WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt1 = conn.prepareStatement(logQuery);
             java.sql.PreparedStatement pstmt2 = conn.prepareStatement(statusQuery)) {

            pstmt1.setInt(1, memberId);
            pstmt1.executeUpdate();

            pstmt2.setInt(1, memberId);
            return pstmt2.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }


    public boolean checkOutMember(int memberId) {
        String query = "UPDATE Members SET is_checked_in = FALSE WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteMember(int memberId) {
        String query = "DELETE FROM Members WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public java.util.Map<String, Integer> getDashboardMetrics() {
        java.util.Map<String, Integer> metrics = new java.util.HashMap<>();
        metrics.put("total", 0); metrics.put("present", 0); metrics.put("today_checkins", 0);
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rsTotal = stmt.executeQuery("SELECT COUNT(*) FROM Members");
            if (rsTotal.next()) metrics.put("total", rsTotal.getInt(1));


            ResultSet rsPresent = stmt.executeQuery("SELECT COUNT(*) FROM Members WHERE is_checked_in = TRUE");
            if (rsPresent.next()) metrics.put("present", rsPresent.getInt(1));

            ResultSet rsCheckins = stmt.executeQuery("SELECT COUNT(DISTINCT member_id) FROM Check_Ins WHERE DATE(check_in_time) = CURRENT_DATE");
            if (rsCheckins.next()) metrics.put("today_checkins", rsCheckins.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return metrics;
    }

    public double getTotalRevenue() {
        String query = "SELECT SUM(amount) FROM Payments";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public java.util.Map<String, Double> getRevenueByPlan() {
        java.util.Map<String, Double> revenueData = new java.util.HashMap<>();
        String query = "SELECT p.plan_name, SUM(py.amount) AS total_revenue FROM Payments py JOIN Members m ON py.member_id = m.member_id JOIN Membership_Plans p ON m.plan_id = p.plan_id GROUP BY p.plan_name";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) revenueData.put(rs.getString("plan_name"), rs.getDouble("total_revenue"));
        } catch (SQLException e) { e.printStackTrace(); }
        return revenueData;
    }
}