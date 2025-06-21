package com.tomato.tomato.api.repository;

import com.tomato.tomato.api.model.Herbicide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HerbicideRepository extends JpaRepository<Herbicide, Long> {
    Optional<Herbicide> findByName(String name);
}
