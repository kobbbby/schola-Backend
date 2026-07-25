package com.schola.backend.repository;

import com.schola.backend.entity.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, String> {

    List<Scholarship> findByActiveTrue();

    @Query("SELECT s FROM Scholarship s WHERE s.active = true AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Scholarship> searchByTitle(String query);
}