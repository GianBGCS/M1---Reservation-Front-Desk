package DAO;

import java.sql.*;

public class HangarPricingDAO {
    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    public HangarPricingDAO() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS hangar_pricing (" +
                    "category TEXT PRIMARY KEY, " +
                    "daily_rate REAL NOT NULL)");

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM hangar_pricing");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO hangar_pricing VALUES ('SMALL', 500)");
                stmt.execute("INSERT INTO hangar_pricing VALUES ('MEDIUM', 1000)");
                stmt.execute("INSERT INTO hangar_pricing VALUES ('LARGE', 2000)");
                System.out.println("[DB] Default hangar pricing inserted.");
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] HangarPricingDAO setup: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public double getDailyRate(String category) {
        String sql = "SELECT daily_rate FROM hangar_pricing WHERE category = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("daily_rate");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] getDailyRate: " + e.getMessage());
        }
        return 500;
    }
}