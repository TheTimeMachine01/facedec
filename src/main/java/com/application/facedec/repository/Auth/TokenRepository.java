package com.application.facedec.repository.Auth;


import com.application.facedec.entity.Auth.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUserId(Long userId);
}
