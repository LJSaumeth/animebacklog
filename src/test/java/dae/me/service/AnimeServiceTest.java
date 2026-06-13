package dae.me.service;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.entity.Anime.AnimeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AnimeServiceTest {

    @Autowired
    private AnimeService animeService;

    @Test
    void shouldCreateAndFindAnime() {
        AnimeRequestDto dto = new AnimeRequestDto(
                "Fullmetal Alchemist", 64, "1", AnimeStatus.COMPLETED, null, null);

        AnimeResponseDto created = animeService.saveAnime(dto);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Fullmetal Alchemist");

        AnimeResponseDto found = animeService.findById(created.id());
        assertThat(found.name()).isEqualTo("Fullmetal Alchemist");
    }

    @Test
    void shouldThrowOnDuplicateName() {
        AnimeRequestDto dto = new AnimeRequestDto(
                "One Piece", 1000, "1", AnimeStatus.ONGOING, null, null);
        animeService.saveAnime(dto);

        assertThatThrownBy(() -> animeService.saveAnime(dto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
