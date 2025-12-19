package remindme.Entities;

import java.util.Locale;

public record User (
    String name,
    String surname,
    String email,
    String language
) {
    public User(String name, String surname, String email) {
        this(name, surname, email, Locale.getDefault().getDisplayName());
    }

    public String getUserCompleteName() {
        return name + " " + surname;
    }

    // factory method
    public static User getDefaultUser() {
        return new User("Unregistered", "User", "");
    }
}
