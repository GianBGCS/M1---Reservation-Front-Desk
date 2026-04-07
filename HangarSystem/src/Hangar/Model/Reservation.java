package Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reservation {

    public static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String            STATUS_ACTIVE    = "ACTIVE";
    public static final String            STATUS_CANCELLED = "CANCELLED";

    private int       reservationId;
    private String    customerName;
    private String    aircraftTailNumber;
    private String    hangarSlot;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    status;

    private Reservation() {}

    public int       getReservationId()      { return reservationId; }
    public String    getCustomerName()       { return customerName; }
    public String    getAircraftTailNumber() { return aircraftTailNumber; }
    public String    getHangarSlot()         { return hangarSlot; }
    public LocalDate getStartDate()          { return startDate; }
    public LocalDate getEndDate()            { return endDate; }
    public String    getStatus()             { return status; }

    public void setReservationId(int v)         { this.reservationId = v; }
    public void setCustomerName(String v)       { this.customerName = v; }
    public void setAircraftTailNumber(String v) { this.aircraftTailNumber = v; }
    public void setHangarSlot(String v)         { this.hangarSlot = v; }
    public void setStartDate(LocalDate v)       { this.startDate = v; }
    public void setEndDate(LocalDate v)         { this.endDate = v; }
    public void setStatus(String v)             { this.status = v; }

    @Override
    public String toString() {
        return String.format(
                "  ID: %-6d | Customer: %-20s | Aircraft: %-10s | Slot: %-4s | %s to %s | [%s]",
                reservationId, customerName, aircraftTailNumber, hangarSlot,
                startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT), status
        );
    }

    public static class Builder {
        private int       reservationId = 0;
        private String    customerName;
        private String    aircraftTailNumber;
        private String    hangarSlot;
        private LocalDate startDate;
        private LocalDate endDate;
        private String    status = STATUS_ACTIVE;

        public Builder reservationId(int val)         { this.reservationId = val;      return this; }
        public Builder customerName(String val)       { this.customerName = val;       return this; }
        public Builder aircraftTailNumber(String val) { this.aircraftTailNumber = val; return this; }
        public Builder hangarSlot(String val)         { this.hangarSlot = val;         return this; }
        public Builder startDate(LocalDate val)       { this.startDate = val;          return this; }
        public Builder endDate(LocalDate val)         { this.endDate = val;            return this; }
        public Builder status(String val)             { this.status = val;             return this; }

        public Reservation build() {
            Reservation r        = new Reservation();
            r.reservationId      = this.reservationId;
            r.customerName       = this.customerName;
            r.aircraftTailNumber = this.aircraftTailNumber;
            r.hangarSlot         = this.hangarSlot;
            r.startDate          = this.startDate;
            r.endDate            = this.endDate;
            r.status             = this.status;
            return r;
        }
    }
}