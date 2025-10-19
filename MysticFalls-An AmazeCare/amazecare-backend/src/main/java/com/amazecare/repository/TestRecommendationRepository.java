package com.amazecare.repository;

import com.amazecare.entity.TestRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRecommendationRepository extends JpaRepository<TestRecommendation, Long> {
}
