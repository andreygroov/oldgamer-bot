package com.oldgamer.bot.service;

import com.oldgamer.bot.model.GeneratedPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private final DataSource dataSource;

    public DatabaseService(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeTables();
    }

    private void initializeTables() {
        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();

            // Posts table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS generated_posts (
                    id SERIAL PRIMARY KEY,
                    game_name VARCHAR(255) NOT NULL,
                    platform VARCHAR(50) NOT NULL,
                    game_condition VARCHAR(50) NOT NULL,
                    avito_description TEXT,
                    telegram_post TEXT,
                    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    scheduled_at TIMESTAMP,
                    published_at TIMESTAMP
                )
            """);

            // Games cache table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game_cache (
                    id SERIAL PRIMARY KEY,
                    game_name VARCHAR(255) NOT NULL UNIQUE,
                    platform VARCHAR(50) NOT NULL,
                    description TEXT,
                    genre VARCHAR(100),
                    rating INT,
                    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.close();
            logger.info("Database tables initialized");
        } catch (SQLException e) {
            logger.error("Failed to initialize tables", e);
        }
    }

    public GeneratedPost savePost(GeneratedPost post) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = """
                INSERT INTO generated_posts
                (game_name, platform, game_condition, avito_description, telegram_post, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, post.getGameName());
                pstmt.setString(2, post.getPlatform());
                pstmt.setString(3, post.getCondition());
                pstmt.setString(4, post.getAvitoDescription());
                pstmt.setString(5, post.getTelegramPost());
                pstmt.setString(6, post.getStatus().name());
                pstmt.setTimestamp(7, Timestamp.valueOf(post.getCreatedAt()));

                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        post.setId(keys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to save post", e);
        }
        return post;
    }

    public GeneratedPost getPostById(long id) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM generated_posts WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToPost(rs);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get post", e);
        }
        return null;
    }

    public List<GeneratedPost> getDraftPosts() {
        List<GeneratedPost> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM generated_posts WHERE status = 'DRAFT' ORDER BY created_at DESC";
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        posts.add(mapResultSetToPost(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get draft posts", e);
        }
        return posts;
    }

    public void updatePostStatus(long postId, GeneratedPost.PostStatus status) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE generated_posts SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status.name());
                pstmt.setLong(2, postId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to update post status", e);
        }
    }

    public void schedulePost(long postId, LocalDateTime scheduledTime) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE generated_posts SET status = 'SCHEDULED', scheduled_at = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(scheduledTime));
                pstmt.setLong(2, postId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to schedule post", e);
        }
    }

    private GeneratedPost mapResultSetToPost(ResultSet rs) throws SQLException {
        GeneratedPost post = new GeneratedPost();
        post.setId(rs.getLong("id"));
        post.setGameName(rs.getString("game_name"));
        post.setPlatform(rs.getString("platform"));
        post.setCondition(rs.getString("game_condition"));
        post.setAvitoDescription(rs.getString("avito_description"));
        post.setTelegramPost(rs.getString("telegram_post"));
        post.setStatus(GeneratedPost.PostStatus.valueOf(rs.getString("status")));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp scheduledAt = rs.getTimestamp("scheduled_at");
        if (scheduledAt != null) {
            post.setScheduledAt(scheduledAt.toLocalDateTime());
        }

        return post;
    }
}
