package ru.kutepov.repository;

import ru.kutepov.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {
    String getSelectorById(Integer id);
}
