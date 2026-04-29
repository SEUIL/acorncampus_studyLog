package com.acorncampus_studylog.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** 시리즈 데이터 전송 객체 (series 테이블 + 소속 게시글 목록 포함) */
public class SeriesDto {

    // --- DB 저장 필드 ---
    private int seriesId;
    private int userId;
    private String name;
    private String description;
    private String isPublic;   // "Y" / "N"
    private Timestamp createdAt;

    // --- JOIN / 집계 필드 (DB 저장 X, 조회 시 채움) ---
    private String authorName;  // users.nickname
    private int postCount;
    private List<PostDto> postList = new ArrayList<>();  // 시리즈 상세 조회 시 사용

    public int getSeriesId() { return seriesId; }
    public void setSeriesId(int seriesId) { this.seriesId = seriesId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public List<PostDto> getPostList() { return postList; }
    public void setPostList(List<PostDto> postList) { this.postList = postList; }
}
