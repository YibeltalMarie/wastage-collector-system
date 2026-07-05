package com.wastecollector.api.repository;

import com.wastecollector.api.model.entity.User;
import com.wastecollector.api.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the User entity.
 *
 * JpaRepository<User, UUID> means:
 *   - User    → the entity type this repository manages
 *   - UUID    → the type of the primary key (id field)
 *
 * By extending JpaRepository, you automatically get:
 *   save(user)           → INSERT or UPDATE
 *   findById(id)         → SELECT WHERE id = ?
 *   findAll()            → SELECT all rows
 *   delete(user)         → DELETE
 *   count()              → SELECT COUNT(*)
 *   existsById(id)       → SELECT EXISTS(...)
 *   ... and many more
 *
 * Spring Data JPA reads method names and generates SQL automatically.
 * Method name rules:
 *   findBy{Field}        → SELECT WHERE field = ?
 *   existsBy{Field}      → SELECT EXISTS WHERE field = ?
 *   countBy{Field}       → SELECT COUNT WHERE field = ?
 *   findBy{Field}And{Field} → SELECT WHERE field1 = ? AND field2 = ?
 *
 * @Repository marks this as a Spring bean so it can be injected
 * with @Autowired or constructor injection.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Generated SQL:
     *   SELECT * FROM users WHERE phone_number = ? LIMIT 1
     *
     * Returns Optional<User> — never returns null.
     * Optional forces the caller to handle the "not found" case explicitly.
     * This prevents NullPointerExceptions.
     *
     * Usage: userRepository.findByPhoneNumber("0911234567")
     *          .orElseThrow(() -> new ResourceNotFoundException(...))
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Generated SQL:
     *   SELECT EXISTS(SELECT 1 FROM users WHERE phone_number = ?)
     *
     * Used during registration to check if the phone number is already taken
     * before attempting to insert a duplicate (which would throw a DB error).
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Generated SQL:
     *   SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     */
    boolean existsByEmail(String email);

    /**
     * Generated SQL:
     *   SELECT COUNT(*) FROM users WHERE role = ?
     *
     * Used by admin dashboard statistics.
     */
    long countByRole(Role role);
}
