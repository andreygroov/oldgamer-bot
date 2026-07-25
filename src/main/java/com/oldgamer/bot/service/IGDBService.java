package com.oldgamer.bot.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.oldgamer.bot.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class IGDBService {
    private static final Logger logger = LoggerFactory.getLogger(IGDBService.class);

    @Value("${rawg.api.key}")
    private String rawgApiKey;

    @Value("${giantbomb.api.key}")
    private String giantBombApiKey;

    private static final String RAWG_API_URL = "https://api.rawg.io/api/games";
    private static final String GIANTBOMB_API_URL = "https://www.giantbomb.com/api/games";
    private final Gson gson = new Gson();

    public Game searchGame(String gameName, String platform) {
        try {
            logger.debug("Searching for game: {} on platform: {}", gameName, platform);

            // Сначала пытаемся RAWG (лучше поддержка)
            Game game = searchRAWG(gameName, platform);
            if (game != null) {
                logger.debug("Found game in RAWG: {}", game.getName());
                return game;
            }

            // Если не нашли в RAWG, пытаемся Giant Bomb
            game = searchGiantBomb(gameName, platform);
            if (game != null) {
                logger.debug("Found game in Giant Bomb: {}", game.getName());
                return game;
            }

            // Если совсем ничего не нашли - генерируем мок
            return generateMockGame(gameName, platform);

        } catch (Exception e) {
            logger.error("Failed to search games", e);
            return generateMockGame(gameName, platform);
        }
    }

    private Game searchRAWG(String gameName, String platform) {
        try {
            String platformId = getRAWGPlatformId(platform);
            String encodedName = URLEncoder.encode(gameName, "UTF-8");
            String urlString = String.format("%s?search=%s&platforms=%s&key=%s",
                    RAWG_API_URL, encodedName, platformId, rawgApiKey);

            String response = makeHttpRequest(urlString);
            if (response == null) return null;

            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonArray results = json.getAsJsonArray("results");

            if (results != null && results.size() > 0) {
                JsonObject gameData = results.get(0).getAsJsonObject();
                return parseRAWGGame(gameData, platform);
            }
        } catch (Exception e) {
            logger.warn("RAWG search failed for: {}", gameName, e);
        }
        return null;
    }

    private Game searchGiantBomb(String gameName, String platform) {
        try {
            String encodedName = URLEncoder.encode(gameName, "UTF-8");
            String urlString = String.format("%s?api_key=%s&format=json&query=%s",
                    GIANTBOMB_API_URL, giantBombApiKey, encodedName);

            String response = makeHttpRequest(urlString);
            if (response == null) return null;

            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonArray results = json.getAsJsonArray("results");

            if (results != null && results.size() > 0) {
                JsonObject gameData = results.get(0).getAsJsonObject();
                return parseGiantBombGame(gameData, platform);
            }
        } catch (Exception e) {
            logger.warn("Giant Bomb search failed for: {}", gameName, e);
        }
        return null;
    }

    private Game parseRAWGGame(JsonObject gameData, String platform) {
        Game game = new Game();

        if (gameData.has("id")) {
            game.setId(gameData.get("id").getAsLong());
        }

        if (gameData.has("name")) {
            game.setName(gameData.get("name").getAsString());
        }

        if (gameData.has("description")) {
            String desc = gameData.get("description").getAsString();
            if (desc != null && !desc.isEmpty()) {
                // Убираем HTML теги
                desc = desc.replaceAll("<[^>]*>", "");
                game.setDescription(desc.substring(0, Math.min(300, desc.length())));
            }
        }

        if (gameData.has("genres")) {
            JsonArray genres = gameData.getAsJsonArray("genres");
            if (genres.size() > 0) {
                game.setGenre(genres.get(0).getAsJsonObject().get("name").getAsString());
            }
        }

        if (gameData.has("rating")) {
            game.setRating((int) (gameData.get("rating").getAsDouble() * 10));
        }

        if (gameData.has("background_image")) {
            game.setImageUrl(gameData.get("background_image").getAsString());
        }

        game.setPlatform(normalizePlatform(platform));
        return game;
    }

    private Game parseGiantBombGame(JsonObject gameData, String platform) {
        Game game = new Game();

        if (gameData.has("id")) {
            game.setId(gameData.get("id").getAsLong());
        }

        if (gameData.has("name")) {
            game.setName(gameData.get("name").getAsString());
        }

        if (gameData.has("description")) {
            String desc = gameData.get("description").getAsString();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.replaceAll("<[^>]*>", "");
                game.setDescription(desc.substring(0, Math.min(300, desc.length())));
            }
        }

        if (gameData.has("genres")) {
            JsonArray genres = gameData.getAsJsonArray("genres");
            if (genres.size() > 0) {
                game.setGenre(genres.get(0).getAsJsonObject().get("name").getAsString());
            }
        }

        if (gameData.has("image")) {
            JsonObject image = gameData.getAsJsonObject("image");
            if (image.has("small_url")) {
                game.setImageUrl(image.get("small_url").getAsString());
            }
        }

        game.setPlatform(normalizePlatform(platform));
        return game;
    }

    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        if (conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();
            return content.toString();
        }
        return null;
    }

    private String getRAWGPlatformId(String platform) {
        return switch (platform.toLowerCase()) {
            case "ps5", "ps" -> "187";
            case "xbox", "xbox series x" -> "186";
            case "switch", "nintendo switch" -> "7";
            case "pc" -> "4";
            default -> "";
        };
    }

    private String normalizePlatform(String platform) {
        return platform.toUpperCase().contains("PS") ? "PS5" :
               platform.toUpperCase().contains("XBOX") ? "Xbox Series X" :
               platform.toUpperCase().contains("SWITCH") ? "Nintendo Switch" :
               platform.toUpperCase().contains("PC") ? "PC" : platform;
    }

    private Game generateMockGame(String gameName, String platform) {
        Game game = new Game();
        game.setName(gameName);
        game.setPlatform(normalizePlatform(platform));
        game.setDescription(String.format("%s - захватывающая игра, которая перевернёт твой геймерский опыт. " +
                "Увлекательный сюжет, красивая графика, и бесконечные часы веселья.", gameName));
        game.setGenre(generateGenre(gameName));
        game.setRating((int) (Math.random() * 50 + 60));
        game.setImageUrl("https://via.placeholder.com/300x400?text=" + gameName.replace(" ", "+"));
        return game;
    }

    private String generateGenre(String gameName) {
        String lower = gameName.toLowerCase();
        if (lower.contains("zelda") || lower.contains("quest")) return "Adventure";
        if (lower.contains("fight") || lower.contains("street")) return "Fighting";
        if (lower.contains("fifa") || lower.contains("madden") || lower.contains("nba")) return "Sports";
        if (lower.contains("2k") || lower.contains("call")) return "Shooter";
        return "Action RPG";
    }
}
