package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.PostDao;
import com.acorncampus_studylog.dao.TagDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.TagDto;

import java.util.List;

/** 태그 비즈니스 로직 (클라우드 조회, 태그별 게시글) */
public class TagService {

    private static final int PAGE_SIZE = 10;

    private final TagDao  tagDao  = new TagDao();
    // 태그별 게시글 조회는 PostDao에 이미 구현되어 있어서 PostDao를 직접 사용
    private final PostDao postDao = new PostDao();

    /**
     * 태그 클라우드용 전체 태그 목록 (사용 빈도 내림차순)
     * postCount = 0인 태그도 포함 — 관리자 화면과 동일한 데이터 소스 사용
     */
    public List<TagDto> getTagCloud() {
        // TagDao.findAll()은 각 태그의 post_count(공개 게시글에서 사용된 횟수)를 함께 조회
        // post_count 내림차순 → name 오름차순으로 정렬되어 반환됨 (SQL에서 처리)
        return tagDao.findAll();
    }

    /**
     * 태그명으로 공개 게시글 목록 조회
     * @param tagName 태그명
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> getTaggedPosts(String tagName, int pageNo) {
        // TagService가 PostDao를 직접 호출하는 이유:
        // 태그별 게시글 조회 로직은 PostDao에 이미 구현되어 있고
        // PostService를 거치면 의존 관계가 복잡해지므로 여기서 직접 호출
        int total = postDao.countByTag(tagName);
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);
        return postDao.findByTag(tagName, page.getOffset(), PAGE_SIZE);
    }

    /** 태그별 게시글 페이지 정보 */
    public PageDto getTaggedPostPage(String tagName, int pageNo) {
        return new PageDto(pageNo, PAGE_SIZE, postDao.countByTag(tagName));
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /**
     * 관리자 - 전체 태그 목록 (관리 화면용, 사용 빈도 포함)
     * getTagCloud()와 동일한 데이터지만 의미상 분리해둠
     * 나중에 관리자용 필터나 정렬 조건을 추가할 때 독립적으로 수정 가능
     */
    public List<TagDto> getAllTagsForAdmin() {
        return tagDao.findAll();
    }

    /**
     * 관리자 - 태그 삭제
     * post_tags 테이블의 관련 행은 schema.sql에 CASCADE DELETE가 설정되어 있어
     * 태그를 지우면 post_tags의 연결 행도 자동으로 같이 삭제됨
     */
    public void deleteTag(int tagId) {
        tagDao.deleteById(tagId);
    }
}
