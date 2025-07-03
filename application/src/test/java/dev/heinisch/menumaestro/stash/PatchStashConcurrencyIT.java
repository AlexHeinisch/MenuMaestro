package dev.heinisch.menumaestro.stash;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientComputationService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUnitConversionService;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.exceptions.VersionMatchFailedException;
import dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData;
import dev.heinisch.menumaestro.mapper.IngredientMapper;
import dev.heinisch.menumaestro.mapper.StashMapper;
import dev.heinisch.menumaestro.persistence.EntityLockingRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import dev.heinisch.menumaestro.service.StashService;
import dev.heinisch.menumaestro.utils.DatabaseCleanerExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the soundness of our locking scheme, also important to detect hibernate regressions.
 * It is a Jpa/service test to minimize time jitter as we set up two concurrent updates.
 */
@ActiveProfiles({"postgres-test", "datagen-off"})
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {StashService.class, EntityLockingRepository.class, StashMapper.class, IngredientMapper.class, IngredientUnitConversionService.class, IngredientComputationService.class}))
@ExtendWith(DatabaseCleanerExtension.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PatchStashConcurrencyIT {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    PlatformTransactionManager txManager;
    TransactionTemplate txTemplate;

    Account account;
    Organization organization;
    Menu menu;
    Long stashId;
    Long ingredient1Id;
    @Autowired
    private StashService stashService;


    @BeforeEach
    void setup() {
        txTemplate = new TransactionTemplate(txManager);
        organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        stashId = organization.getStash().getId();
        ingredient1Id = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient1()).getId();
    }

    @AfterEach
    void teardown() {
        organizationRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    public void multipleConcurrentPatchUpdates_editConflictDetected() throws InterruptedException {
        var patch = List.of(new IngredientUseCreateEditDto().id(ingredient1Id)
                .amount(10f)
                .unit(IngredientUnitDto.KILOGRAMS));
        int nThreads = 50;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(nThreads);
        List<Thread> threads = new ArrayList<>();
        AtomicInteger nSucceeded = new AtomicInteger(0);
        AtomicInteger nPreconditionFail = new AtomicInteger(0);
        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(() -> {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                try {
                    stashService.updateStash(stashId, patch, 0L);
                    nSucceeded.incrementAndGet();
                } catch (VersionMatchFailedException e) {
                    nPreconditionFail.incrementAndGet();
                } catch (RuntimeException e) {
                    Assertions.fail(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Assertions.assertEquals(1, nSucceeded.get());
        Assertions.assertEquals(nThreads - 1, nPreconditionFail.get());
    }
}
