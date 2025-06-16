package dev.heinisch.menumaestro.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "initial-organizations")
public class InitialOrganizationProperties {

    private List<@Valid SingleOrganization> organizations;

    @Getter
    @Setter
    @Validated
    public static class SingleOrganization {
        @NotBlank
        private String name, description;
        @NotNull
        private List<@Valid MemberInOrganization> members;
    }

    @Getter
    @Setter
    @Validated
    public static class MemberInOrganization {
        @NotBlank
        String username;
        @NotNull @Pattern(regexp = "^(OWNER,ADMIN,PLANNER,SHOPPER,MEMBER,INVITED)$")
        String role;
    }
}
