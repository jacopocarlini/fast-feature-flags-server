package it.jacopocarlini.fff.repository;

import it.jacopocarlini.fff.entity.Flag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlagRepository extends MongoRepository<Flag, String> {

    Optional<Flag> findFirstByFlagKey(String flagKey);

    void deleteByFlagKey(String flagKey);
}
