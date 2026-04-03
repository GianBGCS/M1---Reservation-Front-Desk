package DAO;

import Model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    public ReservationDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customer_name TEXT NOT NULL, " +
                    "aircraft_tail_number TEXT NOT NULL, " +
                    "hangar_slot TEXT NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "end_date TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE');");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Reservation findByTailNumber(String tailNumber) {
        String sql = "SELECT * FROM reservations WHERE aircraft_tail_number = ? AND status = 'ACTIVE'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tailNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Reservation.Builder()
                        .reservationId(rs.getInt("id"))
                        .customerName(rs.getString("customer_name"))
                        .aircraftTailNumber(rs.getString("aircraft_tail_number"))
                        .hangarSlot(rs.getString("hangar_slot"))
                        .startDate(LocalDate.parse(rs.getString("start_date"), Reservation.DATE_FORMAT))
                        .endDate(LocalDate.parse(rs.getString("end_date"), Reservation.DATE_FORMAT))
                        .status(rs.getString("status")).build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean hasOverlap(String slot, LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE hangar_slot = ? AND status = 'ACTIVE' " +
                "AND NOT (end_date < ? OR start_date > ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot);
            ps.setString(2, start.format(Reservation.DATE_FORMAT));
            ps.setString(3, end.format(Reservation.DATE_FORMAT));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean save(Reservation r) {
        String sql = "INSERT INTO reservations (customer_name, aircraft_tail_number, hangar_slot, start_date, end_date, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCustomerName());
            ps.setString(2, r.getAircraftTailNumber());
            ps.setString(3, r.getHangarSlot());
            ps.setString(4, r.getStartDate().format(Reservation.DATE_FORMAT));
            ps.setString(5, r.getEndDate().format(Reservation.DATE_FORMAT));
            ps.setString(6, r.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reservations")) {
            while (rs.next()) {
                list.add(new Reservation.Builder()
                        .reservationId(rs.getInt("id"))
                        .customerName(rs.getString("customer_name"))
                        .aircraftTailNumber(rs.getString("aircraft_tail_number"))
                        .hangarSlot(rs.getString("hangar_slot"))
                        .startDate(LocalDate.parse(rs.getString("start_date"), Reservation.DATE_FORMAT))
                        .endDate(LocalDate.parse(rs.getString("end_date"), Reservation.DATE_FORMAT))
                        .status(rs.getString("status")).build());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}