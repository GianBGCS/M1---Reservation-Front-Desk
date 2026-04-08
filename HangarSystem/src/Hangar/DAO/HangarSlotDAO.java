package DAO;

import Model.HangarSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HangarSlotDAO {

    private static final String DB_URL = "jdbc:sqlite:aviation_hangar.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS hangar_slots (" +
                    "    id           INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "    hangar_name  TEXT    NOT NULL, " +
                    "    slot_code    TEXT    NOT NULL UNIQUE, " +
                    "    max_wingspan REAL    NOT NULL, " +
                    "    max_length   REAL    NOT NULL, " +
                    "    category     TEXT    NOT NULL, " +
                    "    status       TEXT    NOT NULL DEFAULT 'AVAILABLE'" +
                    ");";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM hangar_slots ORDER BY hangar_name, slot_code";

    private static final String SQL_FIND_BY_HANGAR =
            "SELECT * FROM hangar_slots WHERE LOWER(hangar_name) = LOWER(?) " +
                    "ORDER BY slot_code";

    private static final String SQL_FIND_BY_SLOT_CODE =
            "SELECT * FROM hangar_slots WHERE UPPER(slot_code) = UPPER(?)";

    private static final String SQL_FIND_AVAILABLE =
            "SELECT * FROM hangar_slots WHERE status = 'AVAILABLE' " +
                    "ORDER BY hangar_name, slot_code";

    private static final String SQL_FIND_BY_HANGAR_AND_STATUS =
            "SELECT * FROM hangar_slots WHERE LOWER(hangar_name) = LOWER(?) " +
                    "AND status = ? ORDER BY slot_code";

    public HangarSlotDAO() {
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement()) {
            stmt.execute(SQL_CREATE_TABLE);
            if (findAll().isEmpty()) {
                insertDefaultSlots();
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] HangarSlotDAO setup failed: " + e.getMessage());
        }
    }

    private void insertDefaultSlots() {
        String[][] defaults = {
                {"Hangar A", "A1", "20.0", "15.0", "SMALL"},
                {"Hangar A", "A2", "20.0", "15.0", "SMALL"},
                {"Hangar A", "A3", "20.0", "15.0", "SMALL"},
                {"Hangar B", "B1", "36.0", "30.0", "MEDIUM"},
                {"Hangar B", "B2", "36.0", "30.0", "MEDIUM"},
                {"Hangar B", "B3", "36.0", "30.0", "MEDIUM"},
                {"Hangar C", "C1", "65.0", "55.0", "LARGE"},
                {"Hangar C", "C2", "65.0", "55.0", "LARGE"}
        };
        String sql = "INSERT INTO hangar_slots (hangar_name, slot_code, max_wingspan, max_length, category, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] d : defaults) {
                ps.setString(1, d[0]);
                ps.setString(2, d[1]);
                ps.setDouble(3, Double.parseDouble(d[2]));
                ps.setDouble(4, Double.parseDouble(d[3]));
                ps.setString(5, d[4]);
                ps.setString(6, "AVAILABLE");
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[DB] Default hangar slots inserted.");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] Inserting default slots: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public List<HangarSlot> findAll() {
        List<HangarSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findAll: " + e.getMessage());
        }
        return list;
    }

    public List<HangarSlot> findByHangar(String hangarName) {
        List<HangarSlot> list = new ArrayList<>();
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_HANGAR)) {
            ps.setString(1, hangarName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findByHangar: " + e.getMessage());
        }
        return list;
    }

    public HangarSlot findBySlotCode(String slotCode) {
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SLOT_CODE)) {
            ps.setString(1, slotCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findBySlotCode: " + e.getMessage());
        }
        return null;
    }

    public List<HangarSlot> findAllAvailable() {
        List<HangarSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(SQL_FIND_AVAILABLE)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findAllAvailable: " + e.getMessage());
        }
        return list;
    }

    public List<HangarSlot> findByHangarAndStatus(String hangarName, String status) {
        List<HangarSlot> list = new ArrayList<>();
        try (Connection conn      = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_HANGAR_AND_STATUS)) {
            ps.setString(1, hangarName);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] findByHangarAndStatus: " + e.getMessage());
        }
        return list;
    }

    // === NEW: Update slot status by slot code ===
    public boolean updateStatusBySlotCode(String slotCode, String status) {
        String sql = "UPDATE hangar_slots SET status = ? WHERE UPPER(slot_code) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, slotCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] updateStatusBySlotCode: " + e.getMessage());
            return false;
        }
    }

    private HangarSlot mapRow(ResultSet rs) throws SQLException {
        return new HangarSlot.Builder()
                .slotId(rs.getInt("id"))
                .hangarName(rs.getString("hangar_name"))
                .slotCode(rs.getString("slot_code"))
                .maxWingspan(rs.getDouble("max_wingspan"))
                .maxLength(rs.getDouble("max_length"))
                .category(rs.getString("category"))
                .status(rs.getString("status"))
                .build();
    }
}