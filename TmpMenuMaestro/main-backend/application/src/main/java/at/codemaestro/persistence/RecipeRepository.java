package at.codemaestro.persistence;

import at.codemaestro.domain.recipe.Recipe;
import at.codemaestro.domain.recipe.RecipeVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByRecipeValue_Name(String name);

    @Query(value = """
            SELECT r FROM Recipe r
            JOIN r.recipeValue rv
            WHERE rv.author LIKE :author
            """)
    Optional<Recipe> findByAuthor(String author);

    @Query(value = """
            SELECT DISTINCT r FROM Recipe r
            JOIN r.recipeValue rv
            WHERE (LOWER(rv.name) like lower(concat('%', :name, '%')) or :name is null)
            AND (LOWER(rv.description) like lower(concat('%', :description, '%')) or :description is null)
            AND (LOWER(rv.author) like lower(concat('%', :author, '%')) or :author is null)
            AND (:visibility is null or r.visibility = :visibility)
            AND ((:cookingApplianceIds) IS NULL OR
                (NOT EXISTS(SELECT ca FROM rv.cookingAppliances ca WHERE ca.id.cookingApplianceId NOT IN (:cookingApplianceIds))
                 AND EXISTS (SELECT ca FROM rv.cookingAppliances ca)))
            AND ((:ingredientIds) IS NULL
                OR EXISTS(SELECT i FROM rv.ingredients i WHERE i.id.ingredientId IN (:ingredientIds)))
            AND (
                r.visibility = 'PUBLIC'
                OR rv.author = :username
                OR :isAdmin = true
                OR (r.visibility = 'ORGANIZATION' AND EXISTS (
                    SELECT 1 FROM OrganizationAccountRelation oar
                    WHERE oar.account.username = rv.author
                    AND oar.organization.id IN (:organizations)
                ))
            )
            """)
    Page<Recipe> findByMultipleValues(@Param("name") String name,
                                      @Param("description") String description,
                                      @Param("author") String author,
                                      @Param("ingredientIds") Set<Long> ingredientIds,
                                      @Param("cookingApplianceIds") Set<Long> cookingApplianceIds,
                                      @Param("visibility") RecipeVisibility visibility,
                                      @Param("username") String username,
                                      @Param("organizations") Set<Long> organizations,
                                      @Param("isAdmin") boolean isAdmin,
                                      Pageable pageable);
}
