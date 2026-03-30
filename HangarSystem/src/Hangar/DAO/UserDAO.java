package Hangar.DAO;

import Hangar.Model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {
    private static final String DB_URL = "jdbc:sqlite:User.db";
    private Connection connection;

    public UserDAO() {
        try {
            this.connection = DriverManager.getConnection(DB_URL);
            createTable();
        } catch (SQLException e) {
            System.err.println("FATAL: Cannot connect to database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user (
                id INTEGER PRIMARY KEY AUTOINCREMENT,        -- user ID, user-provided, NOT auto-increment
                username TEXT NOT NULL,
                passwordHash TEXT NOT NULL,
                admin TEXT NOT NULL
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Table creation failed: " + e.getMessage());
        }
    }

    public User addUser(User user) {
        String sql = """
            INSERT INTO user(
                username, passwordHash, admin
            ) VALUES(?, ?, ?)
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUSERNAME());
            pstmt.setString(2, user.getPASSWORDHASH());
            pstmt.setBoolean(3, user.isADMIN());

            pstmt.executeUpdate();
            return user;
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            return null;
        }
    }

    public List<User> getAllUser() {
        String sql = "SELECT * FROM user";
        List<User> users = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User s = new User.UserBuilder()
                        .username(rs.getString("username"))
                        .passwordHash(rs.getString("passwordHash"))
                        .admin(rs.getBoolean("admin"))
                        .build();
                users.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Read failed: " + e.getMessage());
        }
        return users;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User.UserBuilder()
                            .username(rs.getString("username"))
                            .passwordHash(rs.getString("passwordHash"))
                            .admin(rs.getBoolean("admin"))
                            .build();
                }
            }
        } catch (SQLException e) {
            System.out.println("Read by ID failed: " + e.getMessage());
        }
        return null;
    }

    public void clearAllUser() {
        String sql = "DELETE FROM user";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("All user deleted.");
        } catch (SQLException e) {
            System.out.println("Clear failed: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Close failed: " + e.getMessage());
        }
    }
    public void printDatabasePath() {
        try {
            String url = DriverManager.getConnection(DB_URL).getMetaData().getURL();
            System.out.println("🔍 Database URL: " + url);
        } catch (SQLException e) {
            System.out.println("Could not get database path: " + e.getMessage());
        }
    }

    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM user WHERE admin = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Count admins failed: " + e.getMessage());
        }
        return 0;
    }
}