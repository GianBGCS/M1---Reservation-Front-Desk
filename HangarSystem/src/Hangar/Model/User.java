package Hangar.Model;

public class User {
    private final String USERNAME;
    private final String PASSWORDHASH;
    private final boolean ADMIN;

    private User(UserBuilder builder){
        this.USERNAME = builder.username;
        this.PASSWORDHASH = builder.passwordHash;
        this.ADMIN = builder.admin;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public String getPASSWORDHASH() {
        return PASSWORDHASH;
    }

    public boolean isADMIN() {
        return ADMIN;
    }

    public static class UserBuilder{
        private String username;
        private String passwordHash;
        private boolean admin;

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash){
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder admin(boolean admin){
            this.admin = admin;
            return this;
        }

        public User build(){
            return new User(this);
        }
    }
}
