package com.oldgamer.bot.model;

import java.util.List;

public class Game {
    private long id;
    private String name;
    private String platform;
    private String genre;
    private String description;
    private String imageUrl;
    private List<String> platforms;
    private int rating;

    public Game() {}

    public Game(long id, String name, String platform, String description) {
        this.id = id;
        this.name = name;
        this.platform = platform;
        this.description = description;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
