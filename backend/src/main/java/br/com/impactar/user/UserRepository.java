package br.com.impactar.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Busca por nome ou e-mail (termo já em minúsculas). */
    @Query("""
            select u from User u
            where lower(u.name) like concat('%', :term, '%')
               or u.email like concat('%', :term, '%')
            """)
    Page<User> search(@Param("term") String term, Pageable pageable);
}

