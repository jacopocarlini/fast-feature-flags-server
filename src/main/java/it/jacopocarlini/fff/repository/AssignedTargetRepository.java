package it.jacopocarlini.fff.repository;

import it.jacopocarlini.fff.entity.AssignedTarget;
import it.jacopocarlini.fff.entity.Flag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignedTargetRepository extends MongoRepository<AssignedTarget, String> {
    Optional<AssignedTarget> findFirstByFlagKeyAndTargetKey(String flagKey, String targetKey);

    void deleteAllByFlagKey(String flagKey);
}
