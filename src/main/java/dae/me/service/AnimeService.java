package dae.me.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dae.me.entity.Anime;
import dae.me.repository.AnimeRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AnimeService {
	private final AnimeRepository animeRepository;
	
	public AnimeService(AnimeRepository animeRepository) {
		this.animeRepository = animeRepository;
	}
	
	@Transactional
	public Anime saveAnime(Anime anime) {
		return animeRepository.save(anime);
	}
	
	@Transactional
	public List<Anime> saveAllAnime(List<Anime> animes){
		return animeRepository.saveAll(animes);
	}
	
	
	public Anime findAnimeById(Long id) {
		return animeRepository.findById(id).orElse(null);
	}
	
	public List<Anime> findAllAnimeById(Long id){
		return animeRepository.findAll();
	}
	
	@Transactional
	public void deleteAnimeById(Long Id) {
		animeRepository.deleteById(Id);
		animeRepository.flush();
	}
	
	@Transactional
	public void deleteAllAnime() {
		animeRepository.deleteAll();
		animeRepository.flush();
	}
	
	@Transactional
	public Optional<Anime> findAnimeByName(String name){
		return animeRepository.findAnimeByName(name);
	}
	
	@Transactional
	public Optional<Anime> findAnimeByNameContaining(String keyword){
		return animeRepository.findAnimeByNameContaining(keyword);
		
	}
	
	 @Transactional
	    public Anime updateAnime(Long id, Anime updatedAnime) {
	        Anime existing = animeRepository.findById(id)
	            .orElseThrow(() -> new EntityNotFoundException("Anime no encontrado con id " + id));

	        existing.setAnimeName(updatedAnime.getAnimeName());
	        existing.setQuantityEpisodes(updatedAnime.getQuantityEpisodes());
	        existing.setSeasons(updatedAnime.getSeasons());
	        existing.setStatus(updatedAnime.getStatus());
	        existing.setNameImage(updatedAnime.getNameImage());
	        existing.setHiddenNameImage(updatedAnime.getHiddenNameImage());

	        return animeRepository.save(existing);
	    }


}
