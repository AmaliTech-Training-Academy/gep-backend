package com.example.auth_service.utils;

import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    private UserSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<User> hasRole(UserRole role){
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<User> isActive(Boolean isActive){
        return (root, query, cb) ->
                isActive == null  ? null : cb.equal(root.get("isActive"), isActive);
    }
}
