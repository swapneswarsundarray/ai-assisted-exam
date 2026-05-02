package com.aentic.exam.repository;

import com.aentic.exam.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByReportCode(String reportCode);
    Optional<Report> findByExamId(Long examId);
}
