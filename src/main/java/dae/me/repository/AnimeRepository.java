package dae.me.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dae.me.entity.Anime;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {
	Optional<Anime> findAnimeByName(String name); 
	Optional<Anime> findAnimeByNameContaining(String keyword);
}
