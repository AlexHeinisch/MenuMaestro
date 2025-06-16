package dev.heinisch.menumaestro.validation;

import java.util.regex.Pattern;

public class UserConstraints {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+$", Pattern.CASE_INSENSITIVE);

    public static PropertyChecker validUsername(String username, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(username, propertyName)
            .notNull().notBlank().maxLength(50).done();
    }

    public static PropertyChecker validUsername(String username) {
        return validUsername(username, "username");
    }

    public static PropertyChecker validEmail(String email) {
        return PropertyChecker.begin()
            .checkThat(email, "email")
            .notNull().notBlank().isEmail().maxLength(50).done();
    }

    public static PropertyChecker validEmailNullable(String email) {
        return PropertyChecker.begin()
            .checkThat(email, "email")
            .notBlank().isEmail().maxLength(50).done();
    }

    public static PropertyChecker validFirstNameNullable(String firstName) {
        return PropertyChecker.begin()
            .checkThat(firstName, "first name")
            .notBlank().maxLength(50).done();
    }

    public static PropertyChecker validFirstName(String firstName) {
        return PropertyChecker.begin()
            .checkThat(firstName, "first name")
            .notNull().notBlank().maxLength(50).done();
    }

    public static PropertyChecker validLastNameNullable(String lastName) {
        return PropertyChecker.begin()
            .checkThat(lastName, "last name")
            .notBlank().maxLength(50).done();
    }

    public static PropertyChecker validLastName(String lastName) {
        return PropertyChecker.begin()
            .checkThat(lastName, "last name")
            .notNull().notBlank().maxLength(50).done();
    }

    public static PropertyChecker validPasswordNullable(String password) {
        return PropertyChecker.begin()
            .checkThat(password, "password")
            .notBlank().minLength(6).maxLength(50).done();
    }

    public static PropertyChecker validPassword(String password) {
        return PropertyChecker.begin()
            .checkThat(password, "password")
            .notNull().notBlank().minLength(6).maxLength(50).done();
    }


}
