package com.example.auth_service.utils;

import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class for creating JPA Specifications for the User entity.
 * Provides methods to build dynamic query conditions based on user attributes.
 */
public class UserSpecifications {
    private UserSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a Specification to filter users by a keyword.
     * The keyword is matched against the user's full name and email fields (case-insensitive).
     *
     * @param keyword The keyword to search for. If null or empty, no filtering is applied.
     * @return A Specification for filtering users by the given keyword.
     */
    public static Specification<User> hasKeyword(String keyword){
        return (root,query,criteriaBuilder) ->{
            if(keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            };
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
            );
        };
    }

    /**
     * Creates a Specification to filter users by their role.
     *
     * @param role The role to filter by. If null, no filtering is applied.
     * @return A Specification for filtering users by the given role.
     */
    public static Specification<User> hasRole(UserRole role){
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role"), role);
    }

    /**
     * Creates a Specification to filter users by their active status.
     *
     * @param isActive The active status to filter by. If null, no filtering is applied.
     * @return A Specification for filtering users by their active status.
     */
    public static Specification<User> isActive(Boolean isActive){
        return (root, query, cb) ->
                isActive == null  ? null : cb.equal(root.get("isActive"), isActive);
    }
}
