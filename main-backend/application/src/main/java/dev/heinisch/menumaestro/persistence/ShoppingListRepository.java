package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.shopping_list.ShoppingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    @Query(value = """
           SELECT DISTINCT sl FROM ShoppingList sl
             WHERE (LOWER(sl.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL)
               AND (:isClosed IS NULL OR
                    (sl.isClosed = :isClosed))
               AND (sl.organizationId in :organizationIds)
               AND (:menuId IS NULL OR sl.menuId = :menuId)
             ORDER BY sl.isClosed ASC, sl.name ASC
           """)
    Page<ShoppingList> searchShoppingListsByNameAndShoppingListStatus(String name, Boolean isClosed, Long menuId, Set<Long> organizationIds, Pageable pageable);

    @Query(value = """
           SELECT DISTINCT sl FROM ShoppingList sl
             WHERE (LOWER(sl.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL)
               AND (:isClosed IS NULL OR
                    (sl.isClosed = :isClosed))
               AND (:menuId IS NULL OR sl.menuId = :menuId)
             ORDER BY sl.isClosed ASC, sl.name ASC
           """)
    Page<ShoppingList> searchShoppingListsByNameAndShoppingListStatusAdmin(String name, Boolean isClosed, Long menuId, Pageable pageable);

    @Query("SELECT COUNT(*) FROM ShoppingList  sl WHERE sl.menuId = :menuId AND sl.isClosed = false")
    long existsShoppingListForMenu(Long menuId);


    @Query("SELECT distinct sl from ShoppingList sl WHERE sl.menuId = :menuId")
    List<ShoppingList> getShoppingListByMenuId(Long menuId);
}
