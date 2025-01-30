package remindme.Entities;

import java.util.Locale;

public class User {
    public final String name;
    public final String surname;
    public final String email; // nullable
    public final String language;

    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.language = Locale.getDefault().getDisplayName();
    }

    public String getUserCompleteName() {
        return name + " " + surname; 
    } 
    
    @Override
    public String toString() {
        return name + " " + surname + ", " + email + ", " + language; 
    } 

    public static User getDefaultUser() {
        return new User("Unregistered", "User", "");
    }
}
