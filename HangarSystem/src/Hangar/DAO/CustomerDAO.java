package DAO;

import Model.Customer;
import java.sql.*;

public class CustomerDAO {
    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    public CustomerDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT NOT NULL UNIQUE, " +
                    "email TEXT NOT NULL UNIQUE);");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean saveCustomer(Customer c) {
        String sql = "INSERT INTO customers (id, name, phone, email) VALUES (?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public Customer findById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer.Builder()
                        .setId(rs.getInt("id"))
                        .setName(rs.getString("name"))
                        .setPhone(rs.getString("phone"))
                        .setEmail(rs.getString("email")).build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // === NEW: Find customer by phone ===
    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer.Builder()
                        .setId(rs.getInt("id"))
                        .setName(rs.getString("name"))
                        .setPhone(rs.getString("phone"))
                        .setEmail(rs.getString("email")).build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // === NEW: Find customer by email ===
    public Customer findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer.Builder()
                        .setId(rs.getInt("id"))
                        .setName(rs.getString("name"))
                        .setPhone(rs.getString("phone"))
                        .setEmail(rs.getString("email")).build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean isNameDuplicate(String name) {
        return checkExists("SELECT 1 FROM customers WHERE LOWER(name) = LOWER(?)", name);
    }

    public boolean isPhoneDuplicate(String phone) {
        return checkExists("SELECT 1 FROM customers WHERE phone = ?", phone);
    }

    public boolean isEmailDuplicate(String email) {
        return checkExists("SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?)", email);
    }

    private boolean checkExists(String sql, String val) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, val);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }
}