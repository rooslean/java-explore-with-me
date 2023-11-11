package ru.practicum.ewm.compilation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long>, QuerydslPredicateExecutor<Compilation> {
}
