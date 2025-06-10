package at.codemaestro.persistence;

import at.codemaestro.domain.ingredient.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    long countByIdIn(Set<Long> ids);

    @Query("""
            SELECT DISTINCT i, levenshtein(i.name, :name) AS levi
                        FROM Ingredient i WHERE
                                    (upper(i.name) LIKE upper('%' || :name || '%')
                                     OR CAST(levenshtein(upper(i.name), upper(:name)) AS INTEGER) <= 1)
                                     AND
                                    (i.status = 'PUBLIC' OR (:username IS NOT NULL AND i.username = :username)) ORDER BY levi
            """)
    Page<Object[]> findIngredientAndOwnRequestedByNameContainingIgnoreCase(String name, String username, Pageable pageable);

    @Query("SELECT DISTINCT i FROM Ingredient i WHERE i.status = 'PUBLIC' OR (:username IS NOT NULL AND i.username = :username)")
    Page<Ingredient> findAllPublicAndOwnRequested(String username,Pageable p);

    @Query("SELECT DISTINCT i FROM Ingredient i WHERE i.status = 'REQUESTED' AND (:username IS NOT NULL AND i.username = :username)")
    List<Ingredient> findOwnRequested(String username);


    Optional<Ingredient> findByName(String name);

    @Query("SELECT DISTINCT i FROM Ingredient i WHERE i.name in :names")
    List<Ingredient> findMultipleByName(List<String> names);

    @Query("SELECT DISTINCT i FROM Ingredient i WHERE i.status = 'REQUESTED'")
    Page<Ingredient> findAllRequested(Pageable p);



    @Modifying
    @Query(value = """

        UPDATE shopping_list_item
        SET ingredient_id = :ingredientIdWhichReplaces
        WHERE ingredient_id = :ingredientIdToReplace;

        UPDATE recipe_ingredient_use
        SET ingredient_id = :ingredientIdWhichReplaces
        WHERE ingredient_id = :ingredientIdToReplace;

        UPDATE stash_entry
        SET ingredient_id = :ingredientIdWhichReplaces
        WHERE ingredient_id = :ingredientIdToReplace;

        DELETE FROM ingredient
        WHERE id = :ingredientIdToReplace;
    """, nativeQuery = true)
    void replaceAllIngredientReferences(Long ingredientIdToReplace, Long ingredientIdWhichReplaces);

    @Query(value = """
    SELECT COUNT(*) = 0
    FROM (
        SELECT shopping_list_id FROM shopping_list_item
        WHERE ingredient_id IN (:ingredientIdToReplace, :ingredientIdWhichReplaces)
        GROUP BY shopping_list_id
        HAVING COUNT(DISTINCT ingredient_id) = 2
        UNION
        SELECT recipe_id FROM recipe_ingredient_use
        WHERE ingredient_id IN (:ingredientIdToReplace, :ingredientIdWhichReplaces)
        GROUP BY recipe_id
        HAVING COUNT(DISTINCT ingredient_id) = 2
        UNION
        SELECT stash_id FROM stash_entry
        WHERE ingredient_id IN (:ingredientIdToReplace, :ingredientIdWhichReplaces)
        GROUP BY stash_id
        HAVING COUNT(DISTINCT ingredient_id) = 2
    ) AS combined
    """, nativeQuery = true)
    boolean checkIfReplacementIsSafe(Long ingredientIdToReplace, Long ingredientIdWhichReplaces);
    @Modifying
    @Query(value = """

    DELETE FROM shopping_list_item
    WHERE ingredient_id = :ingredientId;

    DELETE FROM recipe_ingredient_use
    WHERE ingredient_id = :ingredientId;

    DELETE FROM stash_entry
    WHERE ingredient_id = :ingredientId;

    DELETE FROM ingredient
    WHERE id = :ingredientId;

    """, nativeQuery = true)
    void deleteIngredientAndReferences(Long ingredientId);
}
