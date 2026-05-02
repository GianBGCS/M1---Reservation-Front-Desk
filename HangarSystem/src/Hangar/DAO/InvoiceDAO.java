package DAO;

import Model.Invoice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    public InvoiceDAO() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS invoices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "reservation_id INTEGER, " +
                    "customer_name TEXT NOT NULL, " +
                    "aircraft_tail TEXT NOT NULL, " +
                    "hangar_slot TEXT NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "end_date TEXT NOT NULL, " +
                    "days INTEGER NOT NULL, " +
                    "daily_rate REAL NOT NULL, " +
                    "total_amount REAL NOT NULL, " +
                    "deposit_paid REAL DEFAULT 0, " +
                    "additional_paid REAL DEFAULT 0, " +
                    "balance REAL NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'PENDING')");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] InvoiceDAO: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public int insert(Invoice inv) {
        String sql = "INSERT INTO invoices (reservation_id, customer_name, aircraft_tail, " +
                "hangar_slot, start_date, end_date, days, daily_rate, total_amount, " +
                "deposit_paid, additional_paid, balance, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getReservationId());
            ps.setString(2, inv.getCustomerName());
            ps.setString(3, inv.getAircraftTail());
            ps.setString(4, inv.getHangarSlot());
            ps.setString(5, inv.getStartDate());
            ps.setString(6, inv.getEndDate());
            ps.setInt(7, inv.getDays());
            ps.setDouble(8, inv.getDailyRate());
            ps.setDouble(9, inv.getTotalAmount());
            ps.setDouble(10, inv.getDepositPaid());
            ps.setDouble(11, inv.getAdditionalPaid());
            ps.setDouble(12, inv.getBalance());
            ps.setString(13, inv.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] insert invoice: " + e.getMessage());
        }
        return -1;
    }

    public boolean update(Invoice inv) {
        String sql = "UPDATE invoices SET deposit_paid=?, additional_paid=?, balance=?, status=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, inv.getDepositPaid());
            ps.setDouble(2, inv.getAdditionalPaid());
            ps.setDouble(3, inv.getBalance());
            ps.setString(4, inv.getStatus());
            ps.setInt(5, inv.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] update invoice: " + e.getMessage());
            return false;
        }
    }

    public Invoice findById(int id) {
        String sql = "SELECT * FROM invoices WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findById invoice: " + e.getMessage());
        }
        return null;
    }

    public Invoice findByReservationId(int reservationId) {
        String sql = "SELECT * FROM invoices WHERE reservation_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findByReservationId: " + e.getMessage());
        }
        return null;
    }

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM invoices ORDER BY id DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findAll invoices: " + e.getMessage());
        }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM invoices WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        return new Invoice.Builder()
                .id(rs.getInt("id"))
                .reservationId(rs.getInt("reservation_id"))
                .customerName(rs.getString("customer_name"))
                .aircraftTail(rs.getString("aircraft_tail"))
                .hangarSlot(rs.getString("hangar_slot"))
                .startDate(rs.getString("start_date"))
                .endDate(rs.getString("end_date"))
                .days(rs.getInt("days"))
                .dailyRate(rs.getDouble("daily_rate"))
                .totalAmount(rs.getDouble("total_amount"))
                .depositPaid(rs.getDouble("deposit_paid"))
                .additionalPaid(rs.getDouble("additional_paid"))
                .balance(rs.getDouble("balance"))
                .status(rs.getString("status"))
                .build();
    }
}