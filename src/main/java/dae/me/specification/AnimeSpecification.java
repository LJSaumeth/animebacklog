package dae.me.specification;

import dae.me.entity.Anime;
import dae.me.entity.Anime.AnimeStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AnimeSpecification {

    private AnimeSpecification() {}

    public static Specification<Anime> combine(String search, AnimeStatus status,
                                                Integer minRating, Integer maxRating,
                                                Long categoryId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("animeName")),
                        "%" + search.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
            }
            if (maxRating != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), maxRating));
            }
            if (categoryId != null) {
                Join<Object, Object> categories = root.join("categories");
                predicates.add(cb.equal(categories.get("id"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
