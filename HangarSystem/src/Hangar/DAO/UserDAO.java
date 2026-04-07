package DAO;

import Model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String DB_URL = "jdbc:sqlite:User.db";

    public UserDAO() {
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    username     TEXT    NOT NULL UNIQUE,
                    passwordHash TEXT    NOT NULL,
                    admin        INTEGER NOT NULL DEFAULT 0
                )
            """);
        } catch (SQLException e) {
            System.err.println("FATAL: UserDAO setup failed: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User.UserBuilder()
                .username(rs.getString("username"))
                .passwordHash(rs.getString("passwordHash"))
                .admin(rs.getInt("admin") == 1)
                .build();
    }

    public User addUser(User user) {
        String sql = "INSERT INTO user (username, passwordHash, admin) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setInt   (3, user.isAdmin() ? 1 : 0);
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] addUser: " + e.getMessage());
            return null;
        }
    }

    public List<User> getAllUser() {
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery("SELECT * FROM user")) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] getAllUser: " + e.getMessage());
        }
        return users;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] getUserByUsername: " + e.getMessage());
        }
        return null;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM user WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteByUsername(String username) {
        String sql = "DELETE FROM user WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] deleteByUsername: " + e.getMessage());
            return false;
        }
    }

    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM user WHERE admin = 1";
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] countAdmins: " + e.getMessage());
        }
        return 0;
    }

    public void clearAllUser() {
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM user");
            System.out.println("All users deleted.");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] clearAllUser: " + e.getMessage());
        }
    }
}