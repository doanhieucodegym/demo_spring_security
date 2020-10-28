package com.hivetech.springbootsecurity.repository;

import com.hivetech.springbootsecurity.model.ERole;
import com.hivetech.springbootsecurity.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(ERole role);
}
