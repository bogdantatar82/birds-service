package com.hunus.birdsservice.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hunus.birdsservice.persistence.data.Birds;

@Repository
public interface BirdsDAO extends JpaRepository<Birds, UUID> {

    Optional<Birds> findByNameAndColor(String name, String color);
}
