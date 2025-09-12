package dae.me.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString(exclude = "hiddenNameImage")
@Builder
@Entity
@Table(name = "Animes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anime {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	
	@Column(name = "name", nullable = false, unique = true)
	private String animeName;
	
	@Column(name = "episodes", nullable = false)
	private Integer quantityEpisodes;
	
	@Column(name="seasons", nullable = false)
	private String seasons;
	
	public enum AnimeStatus {
	    ONGOING, COMPLETED, HIATUS
	}

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private AnimeStatus status;

	
	@Column(name = "image", nullable = false)
	private String nameImage;
	
	@Column(name = "hidden_image", nullable = false)
	private String hiddenNameImage;

}
