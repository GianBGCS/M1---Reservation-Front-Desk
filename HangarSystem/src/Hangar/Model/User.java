package Model;

public class User {
    private final String  username;
    private final String  passwordHash;
    private final boolean admin;

    private User(UserBuilder builder) {
        this.username     = builder.username;
        this.passwordHash = builder.passwordHash;
        this.admin        = builder.admin;
    }

    public String  getUsername()     { return username; }
    public String  getPasswordHash() { return passwordHash; }
    public boolean isAdmin()         { return admin; }

    public static class UserBuilder {
        private String  username;
        private String  passwordHash;
        private boolean admin;

        public UserBuilder username(String v)     { this.username = v;     return this; }
        public UserBuilder passwordHash(String v) { this.passwordHash = v; return this; }
        public UserBuilder admin(boolean v)       { this.admin = v;        return this; }
        public User build()                       { return new User(this); }
    }
}