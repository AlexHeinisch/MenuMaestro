package dev.heinisch.menumaestro.integration_test.utils.test_constants;

import dev.heinisch.menumaestro.domain.menu.Menu;
import org.openapitools.model.MenuCreateDto;
import org.openapitools.model.SnapshotCreateDto;

import java.util.Collections;

public class DefaultMenuTestData {

    public static final String DEFAULT_MENU_NAME_1 = "Test Menu 1: Alpha";
    public static final String DEFAULT_MENU_DESCRIPTION_1 = "Test Menu Alpha Description Lorem 1 Ispum Lorem Ipsum";
    public static final Integer DEFAULT_MENU_NUMBER_OF_PEOPLE_1 = 10;

    public static final String DEFAULT_MENU_NAME_2 = "Test Menu 2: Beta";
    public static final String DEFAULT_MENU_DESCRIPTION_2 = "Test Menu Beta Description 2 Lorem Ispum Lorem Ipsum";
    public static final Integer DEFAULT_MENU_NUMBER_OF_PEOPLE_2 = 20;

    public static final String DEFAULT_MENU_NAME_3 = "Test Menu 3: Gamma";
    public static final String DEFAULT_MENU_DESCRIPTION_3 = "Test Menu Gamma Description Lorem 3 Ispum Lorem Ipsum";
    public static final Integer DEFAULT_MENU_NUMBER_OF_PEOPLE_3 = 30;

    public static final String DEFAULT_SNAPSHOT_NAME_1 = "Snapshot 1";
    public static final String DEFAULT_SNAPSHOT_NAME_2 = "Snapshot 2";

    public static Menu defaultMenu1(long organizationId) {
        return Menu.builder()
                .organizationId(organizationId)
                .name(DEFAULT_MENU_NAME_1)
                .description(DEFAULT_MENU_DESCRIPTION_1)
                .numberOfPeople(DEFAULT_MENU_NUMBER_OF_PEOPLE_1)
                .items(Collections.emptyList())
                .build();
    }

    public static Menu defaultMenu2(long organizationId) {
        return Menu.builder()
                .organizationId(organizationId)
                .name(DEFAULT_MENU_NAME_2)
                .description(DEFAULT_MENU_DESCRIPTION_2)
                .numberOfPeople(DEFAULT_MENU_NUMBER_OF_PEOPLE_2)
                .items(Collections.emptyList())
                .build();
    }

    public static Menu defaultMenu3(long organizationId) {
        return Menu.builder()
                .organizationId(organizationId)
                .name(DEFAULT_MENU_NAME_3)
                .description(DEFAULT_MENU_DESCRIPTION_3)
                .numberOfPeople(DEFAULT_MENU_NUMBER_OF_PEOPLE_3)
                .items(Collections.emptyList())
                .build();
    }

    public static MenuCreateDto defaultMenuCreateDto1() {
        return new MenuCreateDto()
                .name(DEFAULT_MENU_NAME_1)
                .description(DEFAULT_MENU_DESCRIPTION_1)
                .numberOfPeople(DEFAULT_MENU_NUMBER_OF_PEOPLE_1);
    }

    public static MenuCreateDto defaultMenuCreateDto2() {
        return new MenuCreateDto()
                .name(DEFAULT_MENU_NAME_2)
                .description(DEFAULT_MENU_DESCRIPTION_2)
                .numberOfPeople(DEFAULT_MENU_NUMBER_OF_PEOPLE_2);
    }

    public static MenuCreateDto defaultMenuCreateDto3() {
        return new MenuCreateDto()
                .name(DEFAULT_MENU_NAME_3)
                .description(DEFAULT_MENU_DESCRIPTION_3)
                .numberOfPeople(DEFAULT_MENU_NUMBER_OF_PEOPLE_3);
    }

    public static SnapshotCreateDto defaultSnapshotCreateDto1() {
        return new SnapshotCreateDto().name(DEFAULT_SNAPSHOT_NAME_1);
    }

    public static SnapshotCreateDto defaultSnapshotCreateDto2() {
        return new SnapshotCreateDto().name(DEFAULT_SNAPSHOT_NAME_2);
    }
}
