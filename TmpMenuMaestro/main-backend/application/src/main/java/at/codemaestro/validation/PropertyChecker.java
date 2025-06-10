package at.codemaestro.validation;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static at.codemaestro.validation.UserConstraints.EMAIL_PATTERN;

public class PropertyChecker {

    List<String> collectedErrorMessages = new ArrayList<>();

    public static PropertyChecker begin() {
        return new PropertyChecker();
    }

    public PropertyChecker append(PropertyChecker other) {
        other.finalize(lst -> this.collectedErrorMessages.addAll(lst));
        return this;
    }

    public void finalize(Consumer<List<String>> consumer) {
        if (!collectedErrorMessages.isEmpty()) {
            consumer.accept(collectedErrorMessages);
        }
    }

    public PropertyChecker.StringPropertyChecker checkThat(String property, String propertyName) {
        return new StringPropertyChecker(property, propertyName);
    }

    public PropertyChecker.LongPropertyChecker checkThat(Long property, String propertyName) {
        return new LongPropertyChecker(property, propertyName);
    }

    public PropertyChecker.ObjectPropertyChecker checkThat(Object property, String propertyName) {
        return new ObjectPropertyChecker(property, propertyName);
    }

    public PropertyChecker.IntegerPropertyChecker checkThat(Integer property, String propertyName) {
        return new IntegerPropertyChecker(property, propertyName);
    }

    public PropertyChecker.FloatPropertyChecker checkThat(Float property, String propertyName) {
        return new FloatPropertyChecker(property, propertyName);
    }

    public PropertyChecker.DoublePropertyChecker checkThat(Double property, String propertyName) {
        return new DoublePropertyChecker(property, propertyName);
    }

    public <T> PropertyChecker.ListPropertyChecker<T> checkThat(List<T> property, String propertyName) {
        return new ListPropertyChecker<>(property, propertyName);
    }

    public <T extends Enum<T>> PropertyChecker.EnumPropertyChecker<T> checkThat(Enum<T> property, String propertyName) {
        return new EnumPropertyChecker<>(property, propertyName);
    }

    private <T> void check(T input, Predicate<T> predicate, String message) {
        if (!predicate.test(input)) {
            PropertyChecker.this.collectedErrorMessages.add(message);
        }
    }

    @AllArgsConstructor
    public class ObjectPropertyChecker {
        private final Object property;
        private final String propertyName;

        public PropertyChecker.ObjectPropertyChecker notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }
    }

    public class EnumPropertyChecker<T extends Enum<T>> {
        private final Enum<T> property;
        private final String propertyName;

        public EnumPropertyChecker(Enum<T> property, String propertyName) {
            this.property = property;
            this.propertyName = propertyName;
        }

        public PropertyChecker.EnumPropertyChecker<T> notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.EnumPropertyChecker<T> isNotIn(List<Enum<T>> blacklistedValues) {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || !blacklistedValues.contains(x),
                String.format("%s has value that is blacklisted (blacklisted-values=[%s])", this.propertyName, String.join(", ", blacklistedValues.stream().map(Enum::name).toList()))
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }
    }

    @AllArgsConstructor
    public class StringPropertyChecker {
        private final String property;
        private final String propertyName;

        public PropertyChecker.StringPropertyChecker notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        /**
         * Checks if a string is not blank.
         * This check is only applied if the string is NOT null.
         * To check for notNull, use the notNull check.
         *
         * @return self
         */
        public PropertyChecker.StringPropertyChecker notBlank() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || !x.isBlank(),
                String.format("%s cannot be blank", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.StringPropertyChecker length(int length) {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x.length() == length,
                String.format("%s has wrong length (expected: %d)", this.propertyName, length)
            );
            return this;
        }

        public PropertyChecker.StringPropertyChecker maxLength(int maxLength) {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x.length() <= maxLength,
                String.format("%s is too long (max-length: %d)", this.propertyName, maxLength)
            );
            return this;
        }

        public PropertyChecker.StringPropertyChecker minLength(int minLength) {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x.length() >= minLength,
                String.format("%s is too short (min-length: %d)", this.propertyName, minLength)
            );
            return this;
        }

        public PropertyChecker.StringPropertyChecker isEmail() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || EMAIL_PATTERN.matcher(x).matches(),
                String.format("%s is not an email", this.propertyName)
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }
    }

    @AllArgsConstructor
    public class LongPropertyChecker {
        private final Long property;
        private final String propertyName;

        public PropertyChecker.LongPropertyChecker notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.LongPropertyChecker notNegative() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x >= 0,
                String.format("%s is negative", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.LongPropertyChecker negative() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x < 0,
                String.format("%s is not negative", this.propertyName)
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }

    }

    @AllArgsConstructor
    public class IntegerPropertyChecker {
        private final Integer property;
        private final String propertyName;

        public PropertyChecker.IntegerPropertyChecker notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.IntegerPropertyChecker positive() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x > 0,
                String.format("%s is not positive", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.IntegerPropertyChecker negative() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x < 0,
                String.format("%s is not negative", this.propertyName)
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }

    }

    @AllArgsConstructor
    public class FloatPropertyChecker {
        private final Float property;
        private final String propertyName;

        public PropertyChecker.FloatPropertyChecker notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.FloatPropertyChecker positive() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x > 0,
                String.format("%s is not positive", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.FloatPropertyChecker notNegative() {
            PropertyChecker.this.check(
                    this.property,
                    (x) -> x == null || x >= 0,
                    String.format("%s is not positive nor 0", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.FloatPropertyChecker negative() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || x < 0,
                String.format("%s is not negative", this.propertyName)
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }

    }

    @AllArgsConstructor
    public class DoublePropertyChecker {
        private final Double property;
        private final String propertyName;

        public PropertyChecker.DoublePropertyChecker notNull() {
            PropertyChecker.this.check(
                    this.property,
                    Objects::nonNull,
                    String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.DoublePropertyChecker positive() {
            PropertyChecker.this.check(
                    this.property,
                    (x) -> x == null || x > 0,
                    String.format("%s is not positive", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.DoublePropertyChecker negative() {
            PropertyChecker.this.check(
                    this.property,
                    (x) -> x == null || x < 0,
                    String.format("%s is not negative", this.propertyName)
            );
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }

    }

    @AllArgsConstructor
    public class ListPropertyChecker<T> {
        private final List<T> property;
        private final String propertyName;

        public PropertyChecker.ListPropertyChecker<T> notNull() {
            PropertyChecker.this.check(
                this.property,
                Objects::nonNull,
                String.format("%s is missing", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.ListPropertyChecker<T> notEmpty() {
            PropertyChecker.this.check(
                this.property,
                (x) -> x == null || !x.isEmpty(),
                String.format("%s is empty", this.propertyName)
            );
            return this;
        }

        public PropertyChecker.ListPropertyChecker<T> forEach(BiConsumer<PropertyChecker, T> checkFunction) {
            if (this.property == null) {
                return this;
            }
            this.property.forEach(element -> checkFunction.accept(PropertyChecker.this, element));
            return this;
        }

        public PropertyChecker done() {
            return PropertyChecker.this;
        }
    }

}
