package com.trungha.identity_service.repository;

import com.trungha.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // tạo cái bean userRepository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username); // check username đã tồn tại chưa
}

