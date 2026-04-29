package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.PostDao;
import com.acorncampus_studylog.dao.TagDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.TagDto;

import java.util.List;

/** 태그 비즈니스 로직 (클라우드 조회, 태그별 게시글) */
public class TagService {

    private final TagDao  tagDao  = new TagDao();
    private final PostDao postDao = new PostDao();

    /**
     * 태그 클라우드용 전체 태그 목록 (사용 빈도 내림차순)
     * postCount = 0인 태그는 제외할지 여부는 TODO에서 결정
     */
    public List<TagDto> getTagCloud() {
        // TODO: tagDao.findAll() — 필요 시 postCount > 0 필터링
        return null;
    }

    /**
     * 태그명으로 공개 게시글 목록 조회
     * @param tagName 태그명
     * @param pageNo 현재 페이지
     */
    public List<PostDto> getTaggedPosts(String tagName, int pageNo) {
        // TODO: postDao.countByTag → PageDto → postDao.findByTag
        return null;
    }

    /** 태그별 게시글 페이지 정보 */
    public PageDto getTaggedPostPage(String tagName, int pageNo) {
        // TODO: new PageDto(pageNo, 10, postDao.countByTag(tagName))
        return null;
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /** 관리자 - 전체 태그 목록 (관리 화면용, 사용 빈도 포함) */
    public List<TagDto> getAllTagsForAdmin() {
        // TODO: tagDao.findAll()
        return null;
    }

    /**
     * 관리자 - 태그 삭제
     * 삭제 시 연결된 post_tags도 CASCADE 삭제 (SQL 스키마 설정)
     */
    public void deleteTag(int tagId) {
        // TODO: tagDao.deleteById
    }
}
