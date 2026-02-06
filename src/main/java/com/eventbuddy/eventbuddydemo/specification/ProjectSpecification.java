package com.eventbuddy.eventbuddydemo.specification;

import com.eventbuddy.eventbuddydemo.dto.project.ProjectFilterDto;
import com.eventbuddy.eventbuddydemo.model.Project;
import com.eventbuddy.eventbuddydemo.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {

    public static Specification<Project> withFilters(User owner, ProjectFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("owner"), owner));

            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase().trim() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        searchPattern
                ));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getDeadlineFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("deadline"),
                        filter.getDeadlineFrom()
                ));
            }

            if (filter.getDeadlineTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("deadline"),
                        filter.getDeadlineTo()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
