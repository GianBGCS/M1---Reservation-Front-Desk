package Model;

public class Payment {
    private int id;
    private int invoiceId;
    private double amount;
    private String paymentDate;
    private String method;
    private String reference;

    private Payment() {}

    public int getId() { return id; }
    public int getInvoiceId() { return invoiceId; }
    public double getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getMethod() { return method; }
    public String getReference() { return reference; }

    public static class Builder {
        private int id;
        private int invoiceId;
        private double amount;
        private String paymentDate;
        private String method;
        private String reference;

        public Builder id(int v) { this.id = v; return this; }
        public Builder invoiceId(int v) { this.invoiceId = v; return this; }
        public Builder amount(double v) { this.amount = v; return this; }
        public Builder paymentDate(String v) { this.paymentDate = v; return this; }
        public Builder method(String v) { this.method = v; return this; }
        public Builder reference(String v) { this.reference = v; return this; }

        public Payment build() {
            Payment p = new Payment();
            p.id = id;
            p.invoiceId = invoiceId;
            p.amount = amount;
            p.paymentDate = paymentDate;
            p.method = method;
            p.reference = reference;
            return p;
        }
    }
}