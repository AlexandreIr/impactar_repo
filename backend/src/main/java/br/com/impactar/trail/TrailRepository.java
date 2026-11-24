package br.com.impactar.trail;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrailRepository extends JpaRepository<Trail, UUID> {

    List<Trail> findAllByOrderByUpdatedAtDesc();

    List<Trail> findByPublishedTrueOrderByTitleAsc();
}
