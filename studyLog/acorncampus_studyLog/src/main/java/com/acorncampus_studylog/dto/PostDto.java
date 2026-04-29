package com.acorncampus_studylog.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** 게시글 데이터 전송 객체 (posts 테이블 + 조회용 집계 필드 포함) */
public class PostDto {

    // --- DB 저장 필드 ---
    private int postId;
    private int userId;
    private Integer seriesId;      // NULL 허용 (시리즈 없으면 null)
    private String title;
    private String content;        // CLOB
    private String thumbnailUrl;
    private String isPublic;       // "Y" / "N"
    private int viewCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    // --- JOIN / 집계 필드 (DB 저장 X, 조회 시 채움) ---
    private String authorName;     // users.nickname
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private String seriesName;     // series.name
    private List<TagDto> tagList = new ArrayList<>();

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getSeriesId() { return seriesId; }
    public void setSeriesId(Integer seriesId) { this.seriesId = seriesId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public List<TagDto> getTagList() { return tagList; }
    public void setTagList(List<TagDto> tagList) { this.tagList = tagList; }
}
