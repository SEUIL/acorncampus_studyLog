package com.acorncampus_studylog.dto;

/** 태그 데이터 전송 객체 (tags 테이블 + 사용 빈도 포함) */
public class TagDto {

    private int tagId;
    private String name;
    private int postCount;  // 해당 태그를 사용 중인 공개 게시글 수

    public TagDto() {}

    public TagDto(int tagId, String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }
}
