package com.prtec.tasks.adapter.out.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prtec.tasks.domain.model.entity.UserDetails;

@Repository
public interface IUserDetailsRepository extends JpaRepository<UserDetails, Long> {
	Optional<UserDetails> findByUsername(String username);
}
