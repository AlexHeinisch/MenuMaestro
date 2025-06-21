package dev.heinisch.menumaestro;

import dev.heinisch.menumaestro.validation.PropertyChecker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class PropertyCheckerTest {

    @Test
    void whenCheckString_allValid_noErrorMessages() {
        PropertyChecker.begin()
                .checkThat("test@example.com", "X")
                .notBlank()
                .isEmail()
                .length(16)
                .maxLength(17)
                .minLength(15)
                .done()
                .finalize((lst) -> {
                    Assertions.fail();
                });
    }

    @Test
    void whenCheckString_andBlank_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat("", "X")
                .notBlank()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactly("X cannot be blank"));
    }

    @Test
    void whenCheckString_andTooShortNoEmail_someErrorMessage() {
        PropertyChecker.begin()
                .checkThat("TTT", "X")
                .notBlank()
                .isEmail()
                .minLength(4)
                .maxLength(2)
                .length(5)
                .done()
                .finalize(
                        (lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder(
                                "X is not an email",
                                "X is too short (min-length: 4)",
                                "X is too long (max-length: 2)",
                                "X has wrong length (expected: 5)"
                        ));
    }

    @Test
    void whenCheckString_andNullWithNoBlankCheck_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat((String) null, "X")
                .isEmail()
                .minLength(4)
                .maxLength(2)
                .length(5)
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckLong_allValid_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat(3L, "X")
                .notNull()
                .notNegative()
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckPositiveLong_withNegativeCheck_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat(3L, "X")
                .notNull()
                .notNegative()
                .negative()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is not negative"));
    }

    @Test
    void whenCheckNegativeLong_withPositiveCheck_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat(-3L, "X")
                .notNull()
                .notNegative()
                .negative()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is negative"));
    }

    @Test
    void whenCheckLong_withNullLong_andNoNullCheck_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat((Long) null, "X")
                .notNegative()
                .negative()
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckInt_allValid_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat(3, "X")
                .notNull()
                .positive()
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckPositiveInt_withNegativeCheck_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat(3, "X")
                .notNull()
                .positive()
                .negative()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is not negative"));
    }

    @Test
    void whenCheckNegativeInt_withPositiveCheck_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat(-3, "X")
                .notNull()
                .positive()
                .negative()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is not positive"));
    }

    @Test
    void whenCheckInt_withNullLong_andNoNullCheck_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat((Integer) null, "X")
                .positive()
                .negative()
                .done()
                .finalize((lst) -> Assertions.fail());
    }


    @Test
    void whenCheckFloat_allValid_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat(3.0F, "X")
                .notNull()
                .positive()
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckPositiveFloat_withNegativeCheck_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat(3.0F, "X")
                .notNull()
                .positive()
                .negative()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is not negative"));
    }

    @Test
    void whenCheckNegativeFloat_withPositiveCheck_oneErrorMessage() {
        PropertyChecker.begin()
                .checkThat(-3F, "X")
                .notNull()
                .positive()
                .negative()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is not positive"));
    }

    @Test
    void whenCheckFloat_withNullLong_andNoNullCheck_noErrorMessage() {
        PropertyChecker.begin()
                .checkThat((Float) null, "X")
                .positive()
                .negative()
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckIntList_withEmptyList_andEmptyCheck_oneErrorMessage() {
        List<Integer> tmp = List.of();
        PropertyChecker.begin()
                .checkThat(tmp, "X")
                .notNull()
                .notEmpty()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is empty"));
    }

    @Test
    void whenCheckIntList_withValidList_noErrorMessages() {
        List<Integer> tmp = List.of(1, 2);
        PropertyChecker.begin()
                .checkThat(tmp, "X")
                .notNull()
                .notEmpty()
                .done()
                .finalize((lst) -> Assertions.fail());
    }

    @Test
    void whenCheckIntList_withNullList_andNullCheck_oneErrorMessage() {
        List<Integer> tmp = null;
        PropertyChecker.begin()
                .checkThat(tmp, "X")
                .notNull()
                .notEmpty()
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("X is missing"));
    }

    @Test
    void whenCheckIntListNested_withTwoElementList_andNegativeIntCheck_oneErrorMessage() {
        List<Integer> tmp = List.of(1, -1);
        PropertyChecker.begin()
                .checkThat(tmp, "X")
                .notNull()
                .notEmpty()
                .forEach((c, i) -> c.checkThat(i, "Y").positive().done())
                .done()
                .finalize((lst) -> Assertions.assertThat(lst).containsExactlyInAnyOrder("Y is not positive"));
    }
}
