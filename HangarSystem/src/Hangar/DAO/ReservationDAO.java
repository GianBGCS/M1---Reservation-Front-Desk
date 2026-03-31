package DAO;

import Model.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS reservations (" +
                    "    id                   INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "    customer_name        TEXT    NOT NULL, " +
                    "    aircraft_tail_number TEXT    NOT NULL, " +
                    "    hangar_slot          TEXT    NOT NULL, " +
                    "    start_date           TEXT    NOT NULL, " +
                    "    end_date             TEXT    NOT NULL, " +
                    "    status               TEXT    NOT NULL DEFAULT 'ACTIVE'" +
                    ");";

    private static final String SQL_INSERT =
            "INSERT INTO reservations " +
                    "(customer_name, aircraft_tail_number, hangar_slot, start_date, end_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM reservations ORDER BY id";

    private static final String SQL_FIND_BY_CUSTOMER =
            "SELECT * FROM reservations WHERE LOWER(customer_name) = LOWER(?)";

    private static final String SQL_FIND_BY_AIRCRAFT =
            "SELECT * FROM reservations WHERE LOWER(aircraft_tail_number) = LOWER(?)";

    private static final String SQL_HAS_OVERLAP =
            "SELECT COUNT(*) FROM reservations " +
                    "WHERE hangar_slot = ? AND status = 'ACTIVE' AND id != ? " +
                    "AND start_date <= ? AND end_date >= ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE reservations SET status = ? WHERE id = ?";

    public ReservationDAO() {
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement()) {
            stmt.execute(SQL_CREATE_TABLE);
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] Setup failed: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public Reservation insert(Reservation reservation) {
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, reservation.getCustomerName());
            ps.setString(2, reservation.getAircraftTailNumber());
            ps.setString(3, reservation.getHangarSlot());
            ps.setString(4, reservation.getStartDate().format(Reservation.DATE_FORMAT));
            ps.setString(5, reservation.getEndDate().format(Reservation.DATE_FORMAT));
            ps.setString(6, reservation.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reservation.setReservationId(keys.getInt(1));
                    return reservation;
                }
            }

        } catch (SQLException e) {
            System.err.println("  [DB ERROR] insert: " + e.getMessage());
        }
        return null;
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findAll: " + e.getMessage());
        }
        return list;
    }
    public List<Reservation> findByCustomer(String customerName) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CUSTOMER)) {

            ps.setString(1, customerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findByCustomer: " + e.getMessage());
        }
        return list;
    }
    public List<Reservation> findByAircraft(String tailNumber) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_AIRCRAFT)) {

            ps.setString(1, tailNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findByAircraft: " + e.getMessage());
        }
        return list;
    }
    public boolean hasOverlap(String hangarSlot, LocalDate start, LocalDate end, int excludeId) {
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_HAS_OVERLAP)) {

            ps.setString(1, hangarSlot);
            ps.setInt   (2, excludeId);
            ps.setString(3, end.format(Reservation.DATE_FORMAT));
            ps.setString(4, start.format(Reservation.DATE_FORMAT));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("  [DB ERROR] hasOverlap: " + e.getMessage());
        }
        return false;
    }
    public boolean updateStatus(int id, String newStatus) {
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            ps.setString(1, newStatus);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("  [DB ERROR] updateStatus: " + e.getMessage());
        }
        return false;
    }
    private Reservation mapRow(ResultSet rs) throws SQLException {
        return new Reservation.Builder()
                .reservationId(rs.getInt("id"))
                .customerName(rs.getString("customer_name"))
                .aircraftTailNumber(rs.getString("aircraft_tail_number"))
                .hangarSlot(rs.getString("hangar_slot"))
                .startDate(LocalDate.parse(rs.getString("start_date"), Reservation.DATE_FORMAT))
                .endDate(LocalDate.parse(rs.getString("end_date"),     Reservation.DATE_FORMAT))
                .status(rs.getString("status"))
                .build();
    }

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM reservations WHERE id = ?";

    private static final String SQL_UPDATE =
            "UPDATE reservations " +
                    "SET aircraft_tail_number=?, hangar_slot=?, start_date=?, end_date=? " +
                    "WHERE id=?";

    public Reservation findById(int id) {
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findById: " + e.getMessage());
        }
        return null;
    }
}