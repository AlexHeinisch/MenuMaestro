package dev.heinisch.menumaestro.integration_test.utils;

import org.assertj.core.api.AbstractAssert;
import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpStatus;

public class ErrorResponseAssert extends AbstractAssert<ErrorResponseAssert, ErrorResponse> {

    protected ErrorResponseAssert(ErrorResponse actual) {
        super(actual, ErrorResponseAssert.class);
    }

    public static ErrorResponseAssert assertThat(ErrorResponse actual) {
        return new ErrorResponseAssert(actual);
    }

    public ErrorResponseAssert hasStatus(HttpStatus status) {
        isNotNull();
        if (actual.getStatus() != status.value()) {
            failWithMessage("Expected status to be %d but was %d", status.value(), actual.getStatus());
        }
        return this;
    }

    public ErrorResponseAssert messageContains(String message) {
        isNotNull();
        if (!actual.getMessage().contains(message)) {
            failWithMessage("Message does not contain '%s' (actual message: '%s')", message, actual.getMessage());
        }
        return this;
    }

    public ErrorResponseAssert messageContainsIgnoreCase(String message) {
        isNotNull();
        if (!actual.getMessage().toLowerCase().contains(message.toLowerCase())) {
            failWithMessage("Message does not contain '%s' (actual message: '%s')", message, actual.getMessage());
        }
        return this;
    }

    public ErrorResponseAssert messageEquals(String message) {
        isNotNull();
        if (!actual.getMessage().equals(message)) {
            failWithMessage("Message does not equal '%s' (actual message: '%s')", message, actual.getMessage());
        }
        return this;
    }

    public ErrorResponseAssert detailsContainSubstring(String detailMessage) {
        isNotNull();
        if (actual.getDetails().stream().noneMatch((s) -> s.contains(detailMessage))) {
            failWithMessage("Details do not contain '%s' (actual details: '[%s]')", detailMessage, String.join("; ", actual.getDetails()));
        }
        return this;
    }

    public ErrorResponseAssert detailsHaveSize(int size) {
        isNotNull();
        if (actual.getDetails().size() != size) {
            failWithMessage("Details do not have size %d (actual size: %d)", size, actual.getDetails().size());
        }
        return this;
    }
}
