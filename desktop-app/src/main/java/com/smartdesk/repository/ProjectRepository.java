package com.smartdesk.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartdesk.model.Company;
import com.smartdesk.model.Project;
import com.smartdesk.model.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    long countByCompany(Company company);
    long countByStatusIn(Collection<ProjectStatus> statuses);
    List<Project> findAllByCompanyOrderByNameAsc(Company company);
}
