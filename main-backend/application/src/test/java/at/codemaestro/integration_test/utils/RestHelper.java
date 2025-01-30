package at.codemaestro.integration_test.utils;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

public class RestHelper {

    public static void basicAuthTests(Function<RequestSpecification, Response> func) {
        // missing header
        var errorResponse1 = func.apply(RestAssured.given().headers(new Headers()))
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract()
                .as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse1)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");

        // invalid token
        var errorResponse2 = func.apply(RestAssured.given().headers(new Headers(new Header("Authorization", "Bearer LOL"))))
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract()
                .as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse2)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token");
    }

    @RequiredArgsConstructor
    private static class BaseRestHelper {
        protected final Header validAuthHeader;
        protected final Method httpMethod;
        protected final String uriPattern;
        protected final HttpStatus successStatus;
    }

    public static class BlankRestHelper<R> extends BaseRestHelper {

        private final Class<R> returnType;

        public BlankRestHelper(Class<R> returnType, Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
            this.returnType = returnType;
        }

        public ErrorResponse requestFails(HttpStatus expected, Headers headers) {
            return request(headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(HttpStatus expected) {
            return request()
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public R requestSuccessful() {
            return request()
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public R requestSuccessful(Headers headers) {
            return request(headers)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public Response request() {
            return request(new Headers(validAuthHeader));
        }

        public Response request(Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .request(httpMethod, uriPattern);
        }

        public void basicAuthTests() {
            RestHelper.basicAuthTests(rf -> rf.request(httpMethod, uriPattern));
        }
    }

    public static class BlankWithoutReturnRestHelper extends BaseRestHelper {

        public BlankWithoutReturnRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(HttpStatus expected, Headers headers) {
            return request(headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(HttpStatus expected) {
            return request()
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public void requestSuccessful() {
            request()
                    .then()
                    .statusCode(successStatus.value());
        }

        public void requestSuccessful(Headers headers) {
            request(headers)
                    .then()
                    .statusCode(successStatus.value());
        }

        public Response request() {
            return request(new Headers(validAuthHeader));
        }

        public Response request(Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .request(httpMethod, uriPattern);
        }

        public void basicAuthTests() {
            RestHelper.basicAuthTests(rf -> rf.request(httpMethod, uriPattern));
        }
    }

    public static class PathRestHelper<R, P> extends BaseRestHelper {

        private final Class<R> returnType;

        public PathRestHelper(Class<R> returnType, Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
            this.returnType = returnType;
        }

        public ErrorResponse requestFails(P param, HttpStatus expected, Headers headers) {
            return request(param, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param, HttpStatus expected) {
            return request(param)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public R requestSuccessful(P param) {
            return request(param)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public R requestSuccessful(P param, Headers headers) {
            return request(param, headers)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public Response request(P param) {
            return request(param, new Headers(validAuthHeader));
        }

        public Response request(P param, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .request(httpMethod, uriPattern, param);
        }

        public void basicAuthTests(P validParam) {
            RestHelper.basicAuthTests(rf -> rf.request(httpMethod, uriPattern, validParam));
        }
    }

    public static class DualPathRestHelper<R, P, Q> extends BaseRestHelper {

        private final Class<R> returnType;

        public DualPathRestHelper(Class<R> returnType, Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
            this.returnType = returnType;
        }

        public ErrorResponse requestFails(P param1, Q param2, HttpStatus expected, Headers headers) {
            return request(param1, param2, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param1, Q param2, HttpStatus expected) {
            return request(param1, param2)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public R requestSuccessful(P param1, Q param2) {
            return request(param1, param2)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public R requestSuccessful(P param1, Q param2, Headers headers) {
            return request(param1, param2, headers)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public Response request(P param, Q param2) {
            return request(param, param2, new Headers(validAuthHeader));
        }

        public Response request(P param, Q param2, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .request(httpMethod, uriPattern, param, param2);
        }

        public void basicAuthTests(P validParam, Q validParam2) {
            RestHelper.basicAuthTests(rf -> rf.request(httpMethod, uriPattern, validParam, validParam2));
        }
    }

    public static class DualPathWithoutReturnRestHelper<P, Q> extends BaseRestHelper {

        public DualPathWithoutReturnRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(P param1, Q param2, HttpStatus expected, Headers headers) {
            return request(param1, param2, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param1, Q param2, HttpStatus expected) {
            return request(param1, param2)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public void requestSuccessful(P param1, Q param2) {
            request(param1, param2)
                    .then()
                    .statusCode(successStatus.value());
        }

        public void requestSuccessful(P param1, Q param2, Headers headers) {
            request(param1, param2, headers)
                    .then()
                    .statusCode(successStatus.value());
        }

        public Response request(P param, Q param2) {
            return request(param, param2, new Headers(validAuthHeader));
        }

        public Response request(P param, Q param2, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .request(httpMethod, uriPattern, param, param2);
        }

        public void basicAuthTests(P validParam, Q validParam2) {
            RestHelper.basicAuthTests(rf -> rf.request(httpMethod, uriPattern, validParam, validParam2));
        }
    }

    public static class PathWithoutReturnRestHelper<P> extends BaseRestHelper {

        public PathWithoutReturnRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(P param, HttpStatus expected, Headers headers) {
            return request(param, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param, HttpStatus expected) {
            return request(param)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public void requestSuccessful(P param) {
            request(param)
                    .then()
                    .statusCode(successStatus.value());
        }

        public void requestSuccessful(P param, Headers headers) {
            request(param, headers)
                    .then()
                    .statusCode(successStatus.value());
        }

        public Response request(P param) {
            return request(param, new Headers(validAuthHeader));
        }

        public Response request(P param, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .request(httpMethod, uriPattern, param);
        }

        public void basicAuthTests(P validParam) {
            RestHelper.basicAuthTests(rf -> rf.request(httpMethod, uriPattern, validParam));
        }
    }

    public static class PathAndBodyRestHelper<R, P, B> extends BaseRestHelper {

        private final Class<R> returnType;

        public PathAndBodyRestHelper(Class<R> returnType, Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
            this.returnType = returnType;
        }

        public ErrorResponse requestFails(P param, B body, HttpStatus expected, Headers headers) {
            return request(param, body, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param, B body, HttpStatus expected) {
            return request(param, body)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public R requestSuccessful(P param, B body) {
            return request(param, body)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public R requestSuccessful(P param, B body, Headers headers) {
            return request(param, body, headers)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public Response request(P param, B body) {
            return request(param, body, new Headers(validAuthHeader));
        }

        public Response request(P param, B body, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .contentType("application/json")
                    .body(body)
                    .request(httpMethod, uriPattern, param);
        }

        public void basicAuthTests(P validParam, B validBody) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").body(validBody).request(httpMethod, uriPattern, validParam));
        }
    }

    public static class DualPathAndBodyRestHelper<R, P, Q, B> extends BaseRestHelper {

        private final Class<R> returnType;

        public DualPathAndBodyRestHelper(Class<R> returnType, Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
            this.returnType = returnType;
        }

        public ErrorResponse requestFails(P param1, Q param2, B body, Headers headers) {
            return request(param1, param2, body, headers)
                    .then()
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param1, Q param2, B body) {
            return request(param1, param2, body)
                    .then()
                    .extract()
                    .as(ErrorResponse.class);
        }

        public R requestSuccessful(P param1, Q param2, B body) {
            return request(param1, param2, body)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public R requestSuccessful(P param1, Q param2, B body, Headers headers) {
            return request(param1, param2, body, headers)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public Response request(P param, Q param2, B body) {
            return request(param, param2, body, new Headers(validAuthHeader));
        }

        public Response request(P param, Q param2, B body, Headers headers) {
            return RestAssured.given()
                    .contentType("application/json")
                    .body(body)
                    .headers(headers)
                    .request(httpMethod, uriPattern, param, param2);
        }

        public void basicAuthTests(P validParam, Q validParam2, B body) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").body(body).request(httpMethod, uriPattern, validParam, validParam2));
        }
    }

    public static class PathAndBodyWithoutReturnRestHelper<P, B> extends BaseRestHelper {

        public PathAndBodyWithoutReturnRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(P param, B body, HttpStatus expected, Headers headers) {
            return request(param, body, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param, B body, HttpStatus expected) {
            return request(param, body)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public void requestSuccessful(P param, B body) {
            request(param, body)
                    .then()
                    .statusCode(successStatus.value());
        }

        public void requestSuccessful(P param, B body, Headers headers) {
            request(param, body, headers)
                    .then()
                    .statusCode(successStatus.value());
        }

        public Response request(P param, B body) {
            return request(param, body, new Headers(validAuthHeader));
        }

        public Response request(P param, B body, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .contentType("application/json")
                    .body(body)
                    .request(httpMethod, uriPattern, param);
        }

        public void basicAuthTests(P validParam, B validBody) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").body(validBody).request(httpMethod, uriPattern, validParam));
        }
    }

    public static class DualPathAndBodyWithoutReturnRestHelper<P, Q, B> extends BaseRestHelper {

        public DualPathAndBodyWithoutReturnRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(P param1, Q param2, B body, HttpStatus expected, Headers headers) {
            return request(param1, param2, body, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param1, Q param2, B body, HttpStatus expected) {
            return request(param1, param2, body)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public void requestSuccessful(P param1, Q param2, B body) {
            request(param1, param2, body)
                    .then()
                    .statusCode(successStatus.value());
        }

        public void requestSuccessful(P param1, Q param2, B body, Headers headers) {
            request(param1, param2, body, headers)
                    .then()
                    .statusCode(successStatus.value());
        }

        public Response request(P param, Q param2, B body) {
            return request(param, param2, body, new Headers(validAuthHeader));
        }

        public Response request(P param, Q param2, B body, Headers headers) {
            return RestAssured.given()
                    .contentType("application/json")
                    .body(body)
                    .headers(headers)
                    .request(httpMethod, uriPattern, param, param2);
        }

        public void basicAuthTests(P validParam, Q validParam2, B body) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").body(body).request(httpMethod, uriPattern, validParam, validParam2));
        }
    }

    public static class BodyRestHelper<R, B> extends BaseRestHelper {

        private final Class<R> returnType;

        public BodyRestHelper(Class<R> returnType, Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
            this.returnType = returnType;
        }

        public ErrorResponse requestFails(B body, HttpStatus expected, Headers headers) {
            return request(body, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(B body, HttpStatus expected) {
            return request(body)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public R requestSuccessful(B body) {
            return request(body)
                    .then()
                    .statusCode(successStatus.value())
                    .contentType("application/json")
                    .extract()
                    .as(returnType);
        }

        public R requestSuccessful(B body, Headers headers) {
            return request(body, headers)
                    .then()
                    .statusCode(successStatus.value())
                    .extract()
                    .as(returnType);
        }

        public Response request(B body) {
            return request(body, new Headers(validAuthHeader));
        }

        public Response request(B body, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .contentType("application/json")
                    .body(body)
                    .request(httpMethod, uriPattern);
        }

        public void basicAuthTests(B validBody) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").body(validBody).request(httpMethod, uriPattern));
        }
    }

    public static class BodyWithoutReturnRestHelper<B> extends BaseRestHelper {

        public BodyWithoutReturnRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(B body, HttpStatus expected, Headers headers) {
            return request(body, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(B body, HttpStatus expected) {
            return request(body)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public void requestSuccessful(B body) {
            request(body)
                    .then()
                    .statusCode(successStatus.value());
        }

        public void requestSuccessful(B body, Headers headers) {
            request(body, headers)
                    .then()
                    .statusCode(successStatus.value());
        }

        public Response request(B body) {
            return request(body, new Headers(validAuthHeader));
        }

        public Response request(B body, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .contentType("application/json")
                    .body(body)
                    .request(httpMethod, uriPattern);
        }

        public void basicAuthTests(B validBody) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").body(validBody).request(httpMethod, uriPattern));
        }

    }

    public static class QueryRestHelper extends BaseRestHelper {

        public QueryRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(String query, HttpStatus expected, Headers headers) {
            return request(query, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(String query, HttpStatus expected) {
            return request(query)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public Response request(String query) {
            return request(query, new Headers(validAuthHeader));
        }

        public Response request(String query, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .contentType("application/json")
                    .request(httpMethod, uriPattern + (StringUtils.isBlank(query) ? "" : "?" + query));
        }

        public void basicAuthTests(String query) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").request(httpMethod, uriPattern + (StringUtils.isBlank(query) ? "" : "?" + query)));
        }
    }

    public static class QueryWithPathRestHelper<P> extends BaseRestHelper {

        public QueryWithPathRestHelper(Header validAuthHeader, Method httpMethod, String uriPattern, HttpStatus successStatus) {
            super(validAuthHeader, httpMethod, uriPattern, successStatus);
        }

        public ErrorResponse requestFails(P param1, String query, HttpStatus expected, Headers headers) {
            return request(param1, query, headers)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public ErrorResponse requestFails(P param1, String query, HttpStatus expected) {
            return request(param1, query)
                    .then()
                    .statusCode(expected.value())
                    .extract()
                    .as(ErrorResponse.class);
        }

        public Response request(P param1, String query) {
            return request(param1, query, new Headers(validAuthHeader));
        }

        public Response request(P param1, String query, Headers headers) {
            return RestAssured.given()
                    .headers(headers)
                    .contentType("application/json")
                    .request(httpMethod, uriPattern + (StringUtils.isBlank(query) ? "" : "?" + query), param1);
        }

        public void basicAuthTests(P param1, String query) {
            RestHelper.basicAuthTests(rf -> rf.contentType("application/json").request(httpMethod, uriPattern + (StringUtils.isBlank(query) ? "" : "?" + query), param1));
        }
    }

}
