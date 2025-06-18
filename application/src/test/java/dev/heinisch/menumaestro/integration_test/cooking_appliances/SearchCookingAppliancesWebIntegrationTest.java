package dev.heinisch.menumaestro.integration_test.cooking_appliances;

import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.TestPageableResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.openapitools.model.CookingApplianceDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance3;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance4;
import static org.assertj.core.api.Assertions.assertThat;

class SearchCookingAppliancesWebIntegrationTest extends BaseWebIntegrationTest {

    private CookingApplianceDto appliance1;
    private CookingApplianceDto appliance2;
    private CookingApplianceDto appliance3;
    private CookingApplianceDto appliance4;

    @Override
    protected String getBasePath() {
        return "/cooking-appliances";
    }

    @BeforeEach
    public void setup() {
        appliance1 = cookingApplianceMapper.toCookwareDto(cookingApplianceRepository.save(defaultCookingAppliance1()));
        appliance2 = cookingApplianceMapper.toCookwareDto(cookingApplianceRepository.save(defaultCookingAppliance2()));
        appliance3 = cookingApplianceMapper.toCookwareDto(cookingApplianceRepository.save(defaultCookingAppliance3()));
        appliance4 = cookingApplianceMapper.toCookwareDto(cookingApplianceRepository.save(defaultCookingAppliance4()));
    }

    @Test
    public void whenGetCookingAppliances_withNoQuery_thenOK() {
        var result = getCookingAppliancesSuccessful("");

        assertContainsCookingAppliance(result, appliance1);
        assertContainsCookingAppliance(result, appliance2);
        assertContainsCookingAppliance(result, appliance3);
        assertContainsCookingAppliance(result, appliance4);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    public void whenGetCookingAppliances_withPageSizeOne_thenOK() {
        var result = getCookingAppliancesSuccessful("size=1");

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetCookingAppliances_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = getCookingAppliancesSuccessful("size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetCookingAppliances_withPageNumberTwo_thenOK() {
        var result = getCookingAppliancesSuccessful("page=1");

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetCookingAppliances_withQueryForName_thenOK() {
        var result = getCookingAppliancesSuccessful("name=" + appliance1.getName());

        assertContainsCookingAppliance(result, appliance1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    void assertContainsCookingAppliance(TestPageableResponse<CookingApplianceDto> listDto, CookingApplianceDto recipeDto) {
        assertThat(listDto.getContent()).contains(recipeDto);
    }

    private TestPageableResponse<CookingApplianceDto> getCookingAppliancesSuccessful(String query) {
        return getCookingAppliances(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<CookingApplianceDto>>() {
                });
    }

    private Response getCookingAppliances(String query) {
        return getCookingAppliances(
                query,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response getCookingAppliances(String query, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

}
