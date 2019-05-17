package se.magnus.microservices.core.product;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;

@RunWith(SpringRunner.class)
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @Before
   	public void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        StepVerifier.create(repository.save(entity))
            .expectNextMatches(createdEntity -> {
                savedEntity = createdEntity;
                return areProductEqual(entity, savedEntity);
            })
            .verifyComplete();
    }


    @Test
   	public void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);

        StepVerifier.create(repository.save(newEntity))
            .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
            .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
            .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
            .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2l).verifyComplete();
    }

    @Test
   	public void update() {
        savedEntity.setName("n2");
        StepVerifier.create(repository.save(savedEntity))
            .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
            .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
            .expectNextMatches(foundEntity ->
                foundEntity.getVersion() == 1 &&
                foundEntity.getName().equals("n2"))
            .verifyComplete();
    }

    @Test
   	public void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
   	public void getByProductId() {

        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
            .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
            .verifyComplete();
    }

    @Test
   	public void duplicateError() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.getId()))
            .expectNextMatches(foundEntity ->
                foundEntity.getVersion() == 1 &&
                foundEntity.getName().equals("n1"))
            .verifyComplete();
    }

    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return
            (expectedEntity.getId().equals(actualEntity.getId())) &&
            (expectedEntity.getVersion() == actualEntity.getVersion()) &&
            (expectedEntity.getProductId() == actualEntity.getProductId()) &&
            (expectedEntity.getName().equals(actualEntity.getName())) &&
            (expectedEntity.getWeight() == actualEntity.getWeight());
    }
}
