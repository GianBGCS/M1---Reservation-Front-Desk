public class Customer {
    private final int id;
    private final String name;
    private final String phone;
    private final String email;

    private Customer(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.phone = builder.phone;
        this.email = builder.email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public static class Builder {
        private int id;
        private String name;
        private String phone;
        private String email;

        public Builder setId(int id) { this.id = id; return this; }
        public Builder setName(String name) { this.name = name; return this; }
        public Builder setPhone(String phone) { this.phone = phone; return this; }
        public Builder setEmail(String email) { this.email = email; return this; }
        public Customer build() { return new Customer(this); }
    }
}