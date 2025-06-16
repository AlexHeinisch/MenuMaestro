package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.image.ImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageRecord, String> {

    List<ImageRecord> findImageRecordsByUploadedBy(String username);

    @Modifying
    @Query(value = """
            DELETE FROM image_record
                        WHERE image_record.created_at <= ?1
                              AND image_record.id IN (SELECT i.id FROM image_record i
                              LEFT JOIN recipe_value rv ON rv.image_id = i.id
                                                        WHERE rv.id IS NULL)

            """, nativeQuery = true)
    int deleteByUploadedBeforeAndNotReferenced(Instant uploadedBefore);
}
