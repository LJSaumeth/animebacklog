package dae.me.javafx.controller;

import org.springframework.stereotype.Component;

@Component
public class JikanDataHolder {

    private String name;
    private Integer episodes;
    private String imageUrl;
    private Long malId;

    public void setData(String name, Integer episodes, String imageUrl, Long malId) {
        this.name = name;
        this.episodes = episodes;
        this.imageUrl = imageUrl;
        this.malId = malId;
    }

    public String getName() { return name; }
    public Integer getEpisodes() { return episodes; }
    public String getImageUrl() { return imageUrl; }
    public Long getMalId() { return malId; }

    public boolean hasData() { return name != null; }

    public void consume(AnimeFormController form) {
        if (name != null) {
            form.setJikanData(name, episodes, imageUrl, malId);
        }
        clear();
    }

    private void clear() {
        name = null;
        episodes = null;
        imageUrl = null;
        malId = null;
    }
}
