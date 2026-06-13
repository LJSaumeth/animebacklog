package dae.me.repository;

import dae.me.entity.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long>, JpaSpecificationExecutor<Anime> {

    Optional<Anime> findByAnimeName(String animeName);

    Optional<Anime> findByAnimeNameContainingIgnoreCase(String keyword);
}
