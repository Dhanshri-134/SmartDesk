package com.smartdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartdesk.model.Company;
import com.smartdesk.model.ReviewFeedback;

public interface ReviewFeedbackRepository extends JpaRepository<ReviewFeedback, Long> {
    List<ReviewFeedback> findAllByOrderByCreatedAtDesc();
    List<ReviewFeedback> findAllByCompanyOrderByCreatedAtDesc(Company company);
}
