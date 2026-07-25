package com.schola.backend.repository;

import com.schola.backend.entity.SavedScholarship;
import com.schola.backend.entity.User;
import com.schola.backend.entity.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedScholarshipRepository extends JpaRepository<SavedScholarship, String> {
    List<SavedScholarship> findByUser(User user);
    Optional<SavedScholarship> findByUserAndScholarship(User user, Scholarship scholarship);
    boolean existsByUserAndScholarship(User user, Scholarship scholarship);
}