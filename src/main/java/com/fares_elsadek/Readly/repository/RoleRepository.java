package com.fares_elsadek.Readly.repository;

import com.fares_elsadek.Readly.entity.Role;
import com.fares_elsadek.Readly.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {
    Optional<Role> findByName(RoleType name);
}
