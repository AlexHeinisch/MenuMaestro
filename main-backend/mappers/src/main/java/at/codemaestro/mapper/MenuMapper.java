package at.codemaestro.mapper;

import at.codemaestro.domain.menu.Meal;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.menu.MenuItem;
import at.codemaestro.domain.menu.Snapshot;
import at.codemaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.MealInMenuDto;
import org.openapitools.model.MenuCreateDto;
import org.openapitools.model.MenuDetailDto;
import org.openapitools.model.MenuSummaryDto;
import org.openapitools.model.MenuSummaryListPaginatedDto;
import org.openapitools.model.OrganizationSummaryDto;
import org.openapitools.model.SnapshotInMenuDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MenuMapper extends BasePageableMapper<MenuSummaryListPaginatedDto, MenuSummaryDto> {

    @Mapping(target = "items", expression = "java(java.util.Collections.emptyList())")
    Menu toMenu(MenuCreateDto menu);

    @Mapping(target = "organization.id", source = "organizationId")
    MenuSummaryDto toMenuSummaryDto(Menu menu);

    @Mapping(target = "meals", source = "items")
    @Mapping(target = "snapshots", source = "items")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "stashId", source = "stash.id")
    MenuDetailDto toMenuDetailDto(Menu menu);

    default List<MealInMenuDto> toMealInMenuDtoList(Set<MenuItem> menuItems) {
        return menuItems.stream()
                .sorted(Comparator.comparing(MenuItem::getPosition))
                .filter(menuItem -> menuItem instanceof Meal)
                .map(Meal.class::cast)
                .map(this::toMealInMenuDto)
                .toList();
    }

    default List<SnapshotInMenuDto> toSnapshotInMenuDtoList(Set<MenuItem> menuItems) {
        return menuItems.stream()
                .sorted(Comparator.comparing(MenuItem::getPosition))
                .filter(menuItem -> menuItem instanceof Snapshot)
                .map(Snapshot.class::cast)
                .map(this::toSnapshotInMenuDto)
                .toList();
    }

    MealInMenuDto toMealInMenuDto(Meal meal);

    @Mapping(target = "numberOfMealsIncluded", source = "metadata.numberOfMealsIncluded")
    @Mapping(target = "numberOfTotalIngredients", source = "metadata.numberOfTotalIngredients")
    @Mapping(target = "numberOfAvailableIngredients", source = "metadata.numberOfAvailableIngredients")
    SnapshotInMenuDto toSnapshotInMenuDto(Snapshot snapshot);

    default void addOrganizationSummaryDto(Collection<MenuSummaryDto> menuSummaries, Map<Long, OrganizationSummaryDto> organizationsById) {
        for (MenuSummaryDto menuSummaryDto : menuSummaries) {
            menuSummaryDto.setOrganization(organizationsById.get(menuSummaryDto.getOrganization().getId()));
        }
    }
}
