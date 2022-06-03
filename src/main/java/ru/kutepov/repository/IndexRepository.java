package ru.kutepov.repository;

import ru.kutepov.model.Index;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
}