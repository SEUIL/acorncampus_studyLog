package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.PostDao;
import com.acorncampus_studylog.dao.TagDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.PostDto;

import java.util.List;

/** 게시글 관련 비즈니스 로직 (CRUD, 검색, 이미지 업로드) */
public class PostService {

    private final PostDao postDao = new PostDao();
    private final TagDao  tagDao  = new TagDao();

    /**
     * 게시글 상세 조회 (조회수 증가 포함)
     * @return PostDto (tagList 포함), 존재하지 않으면 null
     */
    public PostDto getPostDetail(int postId) {
        // TODO: postDao.findById → updateViewCount → tagDao.findByPostId → tagList 세팅
        return null;
    }

    /**
     * 공개 게시글 목록 조회 (페이지네이션)
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> getPostList(int pageNo) {
        // TODO: postDao.countAll → PageDto → postDao.findAll(offset, pageSize)
        return null;
    }

    /** 게시글 목록 페이지 정보 반환 */
    public PageDto getPostPage(int pageNo) {
        // TODO: new PageDto(pageNo, 10, postDao.countAll())
        return null;
    }

    /**
     * 특정 사용자의 게시글 목록 (마이페이지)
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> getPostsByUser(int userId, int pageNo) {
        // TODO: postDao.countByUserId → PageDto → postDao.findByUserId
        return null;
    }

    /** 마이페이지 페이지 정보 반환 */
    public PageDto getPostPageByUser(int userId, int pageNo) {
        // TODO: new PageDto(pageNo, 10, postDao.countByUserId(userId))
        return null;
    }

    /**
     * 태그별 게시글 목록
     * @param tagName 태그명
     * @param pageNo 현재 페이지
     */
    public List<PostDto> getPostsByTag(String tagName, int pageNo) {
        // TODO: postDao.countByTag → PageDto → postDao.findByTag
        return null;
    }

    /** 태그별 페이지 정보 반환 */
    public PageDto getPostPageByTag(String tagName, int pageNo) {
        // TODO: new PageDto(pageNo, 10, postDao.countByTag(tagName))
        return null;
    }

    /**
     * 키워드 검색 (제목 + 본문)
     * @param keyword 검색어
     * @param pageNo 현재 페이지
     */
    public List<PostDto> search(String keyword, int pageNo) {
        // TODO: postDao.countSearch → PageDto → postDao.search
        return null;
    }

    /** 검색 결과 페이지 정보 반환 */
    public PageDto getSearchPage(String keyword, int pageNo) {
        // TODO: new PageDto(pageNo, 10, postDao.countSearch(keyword))
        return null;
    }

    /**
     * 게시글 등록
     * @param tagNames 태그 이름 목록 (최대 5개, 서비스에서 검증)
     * @return 생성된 post_id
     */
    public int createPost(int userId, Integer seriesId, String title, String content,
                          String thumbnailUrl, String isPublic, List<String> tagNames) {
        // TODO: tagNames 5개 초과 시 예외 → postDao.insert → tagDao.replacePostTags → post_id 반환
        return 0;
    }

    /**
     * 게시글 수정
     * 작성자 본인 또는 관리자만 수정 가능 — Controller에서 권한 확인 후 호출
     */
    public void updatePost(int postId, Integer seriesId, String title, String content,
                           String thumbnailUrl, String isPublic, List<String> tagNames) {
        // TODO: postDao.update → tagDao.replacePostTags
    }

    /**
     * 게시글 소프트 삭제
     * 작성자 본인 또는 관리자만 삭제 가능 — Controller에서 권한 확인 후 호출
     */
    public void deletePost(int postId) {
        // TODO: postDao.softDelete
    }

    /**
     * 이미지 업로드 (Toast UI Editor 드래그앤드롭용)
     * @param uploadDir 서버 실제 업로드 경로 (ServletContext.getRealPath)
     * @param originalFileName 원본 파일명
     * @param fileData 파일 바이트 배열
     * @return 브라우저에서 접근 가능한 URL 경로 (/resources/upload/xxx.jpg)
     */
    public String saveUploadedImage(String uploadDir, String originalFileName, byte[] fileData) {
        // TODO: UUID 파일명 생성 → 파일 저장 → URL 경로 반환
        return null;
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /** 관리자 - 전체 게시글 목록 (비공개 포함) */
    public List<PostDto> getPostListForAdmin(int pageNo) {
        // TODO: postDao.countAllForAdmin → PageDto → postDao.findAllForAdmin
        return null;
    }

    /** 관리자 - 전체 게시글 페이지 정보 */
    public PageDto getPostPageForAdmin(int pageNo) {
        // TODO: new PageDto(pageNo, 10, postDao.countAllForAdmin())
        return null;
    }

    /** 관리자 - 게시글 강제 삭제 (하드 삭제) */
    public void forceDeletePost(int postId) {
        // TODO: postDao.hardDelete
    }
}
