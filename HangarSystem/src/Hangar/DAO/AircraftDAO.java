package Hangar.DAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AircraftDAO {
    private static final String DB_URL = "jdbc:sqlite:hangar_system.db";
    private Connection connection;

    public AircraftDAO() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            if (this.connection == null) {
                throw new SQLException("Connection instance is null!");
            }
            createTable();
        } catch (Exception e) {
            System.err.println("[FATAL] Could not connect to database!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS aircrafts (id INTEGER PRIMARY KEY, name TEXT, phone TEXT, email TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isNameDuplicate(String name) {
        String sql = "SELECT 1 FROM aircrafts WHERE name = ? COLLATE NOCASE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean isPhoneDuplicate(String phone) {
        String sql = "SELECT 1 FROM aircrafts WHERE phone = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean isEmailDuplicate(String email) {
        String sql = "SELECT 1 FROM aircrafts WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public void saveAircraft(Aircraft a) {
        String sql = "INSERT INTO aircrafts(id, name, phone, email) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, a.getId());
            pstmt.setString(2, a.getName());
            pstmt.setString(3, a.getPhone());
            pstmt.setString(4, a.getEmail());
            pstmt.executeUpdate();
            System.out.println("\n[SUCCESS] New ID: " + a.getId());
        } catch (SQLException e) { System.out.println("Save failed: " + e.getMessage()); }
    }

    public void searchAircraftById(int id) {
        String sql = "SELECT * FROM aircrafts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("No aircraft found with ID: " + id);
                return;
            }
            System.out.printf("ID: %d | Name: %s | Phone: %s | Email: %s%n",
                    rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getString("email"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Aircraft> getAllAircrafts() {
        List<Aircraft> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM aircrafts")) {
            while (rs.next()) {
                list.add(new Aircraft.Builder().setId(rs.getInt("id")).setName(rs.getString("name"))
                        .setPhone(rs.getString("phone")).setEmail(rs.getString("email")).build());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteOldRecord(int id) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM aircrafts WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public void close() { try { if (connection != null) connection.close(); } catch (SQLException e) {} }
}