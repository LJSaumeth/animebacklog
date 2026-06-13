package dae.me.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(exclude = "categories")
@ToString(exclude = {"imageUrl", "categories"})
@Builder
@Entity
@Table(name = "Animes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String animeName;

    @Column(name = "episodes", nullable = false)
    private Integer quantityEpisodes;

    @Column(name = "seasons", nullable = false)
    private String seasons;

    public enum AnimeStatus {
        ONGOING, COMPLETED, HIATUS
    }

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AnimeStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "mal_id")
    private Long malId;

    @Column(name = "rating")
    private Integer rating;

    @ManyToMany
    @JoinTable(
            name = "anime_categories",
            joinColumns = @JoinColumn(name = "anime_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();
}
