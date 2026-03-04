package com.hieu.ms.feature.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
interface RoleRepository extends JpaRepository<Role, String>, QuerydslPredicateExecutor<Role> {}
