package Model;

public class Invoice {
    private int id;
    private int reservationId;
    private String customerName;
    private String aircraftTail;
    private String hangarSlot;
    private String startDate;
    private String endDate;
    private int days;
    private double dailyRate;
    private double totalAmount;
    private double depositPaid;
    private double additionalPaid;
    private double balance;
    private String status;        // PENDING, PARTIAL, PAID

    private Invoice() {}

    public int getId() { return id; }
    public int getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public String getAircraftTail() { return aircraftTail; }
    public String getHangarSlot() { return hangarSlot; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getDays() { return days; }
    public double getDailyRate() { return dailyRate; }
    public double getTotalAmount() { return totalAmount; }
    public double getDepositPaid() { return depositPaid; }
    public double getAdditionalPaid() { return additionalPaid; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }

    public static class Builder {
        private int id;
        private int reservationId;
        private String customerName;
        private String aircraftTail;
        private String hangarSlot;
        private String startDate;
        private String endDate;
        private int days;
        private double dailyRate;
        private double totalAmount;
        private double depositPaid;
        private double additionalPaid;
        private double balance;
        private String status = "PENDING";

        public Builder id(int id) { this.id = id; return this; }
        public Builder reservationId(int id) { this.reservationId = id; return this; }
        public Builder customerName(String v) { this.customerName = v; return this; }
        public Builder aircraftTail(String v) { this.aircraftTail = v; return this; }
        public Builder hangarSlot(String v) { this.hangarSlot = v; return this; }
        public Builder startDate(String v) { this.startDate = v; return this; }
        public Builder endDate(String v) { this.endDate = v; return this; }
        public Builder days(int v) { this.days = v; return this; }
        public Builder dailyRate(double v) { this.dailyRate = v; return this; }
        public Builder totalAmount(double v) { this.totalAmount = v; return this; }
        public Builder depositPaid(double v) { this.depositPaid = v; return this; }
        public Builder additionalPaid(double v) { this.additionalPaid = v; return this; }
        public Builder balance(double v) { this.balance = v; return this; }
        public Builder status(String v) { this.status = v; return this; }

        public Invoice build() {
            Invoice inv = new Invoice();
            inv.id = id;
            inv.reservationId = reservationId;
            inv.customerName = customerName;
            inv.aircraftTail = aircraftTail;
            inv.hangarSlot = hangarSlot;
            inv.startDate = startDate;
            inv.endDate = endDate;
            inv.days = days;
            inv.dailyRate = dailyRate;
            inv.totalAmount = totalAmount;
            inv.depositPaid = depositPaid;
            inv.additionalPaid = additionalPaid;
            inv.balance = balance;
            inv.status = status;
            return inv;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "INV-%04d | Customer: %-15s | Slot: %-4s | Days: %2d | Total: %8.2f | Paid: %8.2f | Balance: %8.2f | %s",
                id, customerName, hangarSlot, days, totalAmount,
                depositPaid + additionalPaid, balance, status);
    }
}