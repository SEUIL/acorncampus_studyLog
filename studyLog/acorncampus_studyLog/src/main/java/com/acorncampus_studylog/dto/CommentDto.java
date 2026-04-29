package com.acorncampus_studylog.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** 댓글 데이터 전송 객체 (comments 테이블 + 대댓글 목록 포함) */
public class CommentDto {

    // --- DB 저장 필드 ---
    private int commentId;
    private int postId;
    private int userId;
    private Integer parentCommentId;  // NULL 이면 최상위 댓글
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    // --- JOIN / 집계 필드 (DB 저장 X, 조회 시 채움) ---
    private String authorName;        // users.nickname
    private int likeCount;
    private int dislikeCount;
    private String myLikeType;        // 로그인 유저의 좋아요 타입 "L"/"D"/null
    private List<CommentDto> replies = new ArrayList<>();  // 대댓글 (depth 2)

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Integer parentCommentId) { this.parentCommentId = parentCommentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

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

    public String getMyLikeType() { return myLikeType; }
    public void setMyLikeType(String myLikeType) { this.myLikeType = myLikeType; }

    public List<CommentDto> getReplies() { return replies; }
    public void setReplies(List<CommentDto> replies) { this.replies = replies; }
}
