package DAO;

import Model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    public PaymentDAO() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "invoice_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "payment_date TEXT NOT NULL, " +
                    "method TEXT NOT NULL, " +
                    "reference TEXT, " +
                    "FOREIGN KEY(invoice_id) REFERENCES invoices(id))");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] PaymentDAO: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public boolean insert(Payment p) {
        String sql = "INSERT INTO payments (invoice_id, amount, payment_date, method, reference) " +
                "VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getInvoiceId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentDate());
            ps.setString(4, p.getMethod());
            ps.setString(5, p.getReference());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] insert payment: " + e.getMessage());
            return false;
        }
    }

    public List<Payment> findByInvoice(int invoiceId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE invoice_id = ? ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Payment.Builder()
                        .id(rs.getInt("id"))
                        .invoiceId(rs.getInt("invoice_id"))
                        .amount(rs.getDouble("amount"))
                        .paymentDate(rs.getString("payment_date"))
                        .method(rs.getString("method"))
                        .reference(rs.getString("reference"))
                        .build());
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] find payments: " + e.getMessage());
        }
        return list;
    }
}