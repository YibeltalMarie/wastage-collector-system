package com.wastecollector.api.model.entity;

import com.wastecollector.api.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to the 'users' table in the database.
 *
 * Lombok annotations used here:
 *   @Getter          → generates getUser(), getRole(), etc. for every field
 *   @Setter          → generates setFullName(), setRole(), etc.
 *   @NoArgsConstructor → generates User() with no arguments (required by JPA)
 *   @AllArgsConstructor → generates User(id, fullName, ...) with all fields
 *   @Builder         → enables: User.builder().fullName("Abebe").role(Role.CITIZEN).build()
 *
 * Why @NoArgsConstructor is required by JPA:
 * JPA (Hibernate) creates entity objects by calling the no-argument constructor
 * first, then setting each field. Without it, Hibernate cannot instantiate
 * the class and throws an exception.
 */
@Entity
@Table(name = "users")          // maps this class to the 'users' table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * @Id marks this as the primary key.
     * @GeneratedValue with UUID strategy tells JPA to generate a UUID automatically.
     * We do not set this manually — the database or JPA generates it.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @Column maps this field to the 'full_name' column.
     * nullable = false → Hibernate validates this is not null before saving.
     * length = 100     → Hibernate validates max length before saving.
     *
     * These match exactly the constraints in V1__create_users_table.sql.
     * If they conflict, Hibernate will complain on startup (good — catches mistakes).
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 150, unique = true)
    private String email;

    /**
     * Stores the BCrypt hash of the password.
     * The raw password is NEVER stored anywhere — only the hash.
     * BCrypt hash always starts with "$2a$" and is always 60 chars.
     * length = 255 gives plenty of room.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * @Enumerated(EnumType.STRING) stores the enum as a string in the database.
     * Without this, JPA stores the enum as an integer (0, 1, 2) — terrible for
     * readability and dangerous if enum order ever changes.
     * With STRING: the database contains "CITIZEN", "COLLECTOR", "ADMIN" — clear.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "sub_city", length = 100)
    private String subCity;

    @Column(length = 50)
    private String kebele;

    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * is_active = false means the account is deactivated.
     * The user cannot log in. Their data is preserved.
     * This is called a "soft delete" — we never delete user records.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default              // tells Lombok Builder to use this default value
    private boolean isActive = true;

    /**
     * @CreationTimestamp: Hibernate sets this automatically when the record
     * is first inserted. We never set it manually.
     * updatable = false: Hibernate never changes this after the first insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * @UpdateTimestamp: Hibernate updates this automatically every time
     * the record is saved/updated. We never set it manually.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
