package com.closer.repository;

import com.closer.model.Contact;
import com.closer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByOwnerAndIsActiveTrue(User owner);

    @Query("SELECT c FROM Contact c WHERE c.owner = :owner AND c.isActive = true " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (c.owner = :owner AND c.isActive = true AND LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Contact> searchByOwnerAndQuery(@Param("owner") User owner, @Param("query") String query);

    long countByOwnerAndIsActiveTrue(User owner);

    Optional<Contact> findByIdAndOwner(UUID id, User owner);

    @Query("SELECT c FROM Contact c WHERE c.isActive = true AND c.lastInteractionDate < :cutoff")
    List<Contact> findInactiveContactsBefore(@Param("cutoff") LocalDateTime cutoff);
}
