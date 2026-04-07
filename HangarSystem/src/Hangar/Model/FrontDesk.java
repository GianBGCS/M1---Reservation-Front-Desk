package Model;


public class FrontDesk {

    private final int    reservationId;
    private final String aircraftTailNumber;
    private final String customerName;
    private final String hangarSlot;
    private final String startDate;
    private final String endDate;
    private final String status;
    private final String aircraftModel;
    private final String wingspan;
    private final String length;
    private final String checkInTime;
    private final String estimatedDeparture;

    private FrontDesk(Builder builder) {
        this.reservationId      = builder.reservationId;
        this.aircraftTailNumber = builder.aircraftTailNumber;
        this.customerName       = builder.customerName;
        this.hangarSlot         = builder.hangarSlot;
        this.startDate          = builder.startDate;
        this.endDate            = builder.endDate;
        this.status             = builder.status;
        this.aircraftModel      = builder.aircraftModel;
        this.wingspan           = builder.wingspan;
        this.length             = builder.length;
        this.checkInTime        = builder.checkInTime;
        this.estimatedDeparture = builder.estimatedDeparture;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getReservationId()      { return reservationId; }
    public String getAircraftTailNumber() { return aircraftTailNumber; }
    public String getCustomerName()       { return customerName; }
    public String getHangarSlot()         { return hangarSlot; }
    public String getStartDate()          { return startDate; }
    public String getEndDate()            { return endDate; }
    public String getStatus()             { return status; }
    public String getAircraftModel()      { return aircraftModel; }
    public String getWingspan()           { return wingspan; }
    public String getLength()             { return length; }
    public String getCheckInTime()        { return checkInTime; }
    public String getEstimatedDeparture() { return estimatedDeparture; }

    // ════════════════════════════════════════════════════════════════════════
    // BUILDER
    // ════════════════════════════════════════════════════════════════════════

    public static class Builder {

        private int    reservationId      = 0;
        private String aircraftTailNumber = "";
        private String customerName       = "";
        private String hangarSlot         = "";
        private String startDate          = "";
        private String endDate            = "";
        private String status             = "";
        private String aircraftModel      = "";
        private String wingspan           = "";
        private String length             = "";
        private String checkInTime        = "";
        private String estimatedDeparture = "";

        public Builder id(int v)              { this.reservationId = v;      return this; }
        public Builder tail(String v)         { this.aircraftTailNumber = v; return this; }
        public Builder name(String v)         { this.customerName = v;       return this; }
        public Builder slot(String v)         { this.hangarSlot = v;         return this; }
        public Builder start(String v)        { this.startDate = v;          return this; }
        public Builder end(String v)          { this.endDate = v;            return this; }
        public Builder status(String v)       { this.status = v;             return this; }
        public Builder aircraftModel(String v){ this.aircraftModel = v;      return this; }
        public Builder wingspan(String v)     { this.wingspan = v;           return this; }
        public Builder length(String v)       { this.length = v;             return this; }
        public Builder checkInTime(String v)  { this.checkInTime = v;        return this; }
        public Builder estimatedDep(String v) { this.estimatedDeparture = v; return this; }

        public FrontDesk build() { return new FrontDesk(this); }
    }
}