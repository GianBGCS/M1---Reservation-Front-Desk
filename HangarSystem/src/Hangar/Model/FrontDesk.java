package Model;

public class FrontDesk {
    private final int reservationId;
    private final String aircraftTailNumber;
    private final String customerName;
    private final String hangarSlot;
    private final String startDate;
    private final String endDate;
    private final String status;

    private FrontDesk(Builder builder) {
        this.reservationId = builder.reservationId;
        this.aircraftTailNumber = builder.aircraftTailNumber;
        this.customerName = builder.customerName;
        this.hangarSlot = builder.hangarSlot;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.status = builder.status;
    }

    public int getReservationId() { return reservationId; }
    public String getAircraftTailNumber() { return aircraftTailNumber; }
    public String getCustomerName() { return customerName; }
    public String getHangarSlot() { return hangarSlot; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }

    public static class Builder {
        private int reservationId;
        private String aircraftTailNumber;
        private String customerName;
        private String hangarSlot;
        private String startDate;
        private String endDate;
        private String status;

        public Builder id(int id) { this.reservationId = id; return this; }
        public Builder tail(String tail) { this.aircraftTailNumber = tail; return this; }
        public Builder name(String name) { this.customerName = name; return this; }
        public Builder slot(String slot) { this.hangarSlot = slot; return this; }
        public Builder start(String start) { this.startDate = start; return this; }
        public Builder end(String end) { this.endDate = end; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public FrontDesk build() { return new FrontDesk(this); }
    }
}