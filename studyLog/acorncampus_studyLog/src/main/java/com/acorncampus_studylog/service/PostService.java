package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.PostDao;
import com.acorncampus_studylog.dao.TagDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.TagDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/** 게시글 관련 비즈니스 로직 (CRUD, 검색, 이미지 업로드) */
public class PostService {

    // 페이지당 게시글 수를 상수로 고정
    // 나중에 수정이 필요하면 이 한 줄만 바꾸면 전체 메서드에 반영됨
    private static final int PAGE_SIZE = 10;

    private final PostDao postDao = new PostDao();
    private final TagDao  tagDao  = new TagDao();

    // ── 단건 조회 ─────────────────────────────────────────────────────────────

    /**
     * 게시글 상세 조회 (조회수 증가 포함)
     * @return PostDto (tagList 포함), 존재하지 않으면 null
     */
    public PostDto getPostDetail(int postId) {
        // 1단계: DB에서 게시글을 조회
        //        postDao.findById()는 deleted_at IS NULL 조건이 포함되어 있어서
        //        소프트 삭제된 글은 자동으로 null이 반환됨
        PostDto post = postDao.findById(postId);

        // 2단계: 게시글이 없으면 null 반환 (Controller에서 404 처리)
        if (post == null) return null;

        // 3단계: 조회수 +1
        //        findById 이후에 실행하는 이유:
        //        존재하지 않는 글 ID로 요청이 와도 조회수가 올라가면 안 되기 때문
        postDao.updateViewCount(postId);

        // 4단계: 해당 게시글에 달린 태그 목록을 별도 조회해서 PostDto에 붙임
        //        tags 정보는 posts 테이블이 아니라 post_tags 테이블에 있어서
        //        JOIN 대신 별도 쿼리로 가져와야 함
        List<TagDto> tags = tagDao.findByPostId(postId);
        post.setTagList(tags);

        return post;
    }

    // ── 목록 조회 (페이지네이션) ───────────────────────────────────────────────

    /**
     * 공개 게시글 목록 조회 (최신순, 페이지네이션)
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> getPostList(int pageNo) {
        // 전체 게시글 수를 먼저 구한 뒤 PageDto를 만들어야 offset을 계산할 수 있음
        int total = postDao.countAll();

        // PageDto 생성: (현재 페이지, 페이지 크기, 전체 개수)
        // PageDto 내부에서 offset = (pageNo - 1) * pageSize 를 계산해줌
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);

        // offset부터 PAGE_SIZE개만큼 잘라서 반환
        return postDao.findAll(page.getOffset(), PAGE_SIZE);
    }

    /** 게시글 목록 페이지 정보 반환 — JSP의 페이지 버튼 렌더링에 사용 */
    public PageDto getPostPage(int pageNo) {
        // PageDto 안에 hasPrev(), hasNext(), getTotalPages() 등이 구현되어 있어서
        // JSP에서 이 객체를 받아 페이지 버튼을 그릴 수 있음
        return new PageDto(pageNo, PAGE_SIZE, postDao.countAll());
    }

    /**
     * 특정 사용자의 게시글 목록 (마이페이지 — 비공개 포함)
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> getPostsByUser(int userId, int pageNo) {
        // userId를 넘기는 이유: 마이페이지는 본인 글만 보여줘야 하므로
        // 공개/비공개 구분 없이 본인 글 전체를 가져오는 DAO 메서드 사용
        int total = postDao.countByUserId(userId);
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);
        return postDao.findByUserId(userId, page.getOffset(), PAGE_SIZE);
    }

    /** 마이페이지 페이지 정보 반환 */
    public PageDto getPostPageByUser(int userId, int pageNo) {
        return new PageDto(pageNo, PAGE_SIZE, postDao.countByUserId(userId));
    }

    /**
     * 태그별 공개 게시글 목록 조회
     * @param tagName 태그명 (예: "Java")
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> getPostsByTag(String tagName, int pageNo) {
        // 태그 이름으로 post_tags → posts를 JOIN해서 필터링하는 DAO 메서드 사용
        int total = postDao.countByTag(tagName);
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);
        return postDao.findByTag(tagName, page.getOffset(), PAGE_SIZE);
    }

    /** 태그별 페이지 정보 반환 */
    public PageDto getPostPageByTag(String tagName, int pageNo) {
        return new PageDto(pageNo, PAGE_SIZE, postDao.countByTag(tagName));
    }

    /**
     * 키워드 검색 (제목 + 본문)
     * @param keyword 검색어
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<PostDto> search(String keyword, int pageNo) {
        // DAO의 search() 내부에서 LOWER(title) LIKE LOWER(?) 로 대소문자 무시 검색
        int total = postDao.countSearch(keyword);
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);
        return postDao.search(keyword, page.getOffset(), PAGE_SIZE);
    }

    /** 검색 결과 페이지 정보 반환 */
    public PageDto getSearchPage(String keyword, int pageNo) {
        return new PageDto(pageNo, PAGE_SIZE, postDao.countSearch(keyword));
    }

    // ── 등록 / 수정 / 삭제 ────────────────────────────────────────────────────

    /**
     * 게시글 등록
     * @param tagNames 태그 이름 목록 (최대 5개, 여기서 검증)
     * @return 생성된 post_id
     * @throws IllegalArgumentException 태그 5개 초과 시
     */
    public int createPost(int userId, Integer seriesId, String title, String content,
                          String thumbnailUrl, String isPublic, List<String> tagNames) {
        // 태그 개수 제한은 DB 레벨이 아니라 Service 레벨에서 처리함
        // DB에는 개수 제약이 없기 때문에 비즈니스 규칙으로 여기서 막아야 함
        if (tagNames != null && tagNames.size() > 5) {
            throw new IllegalArgumentException("태그는 최대 5개까지만 등록할 수 있습니다.");
        }

        // 게시글 먼저 저장 → post_id를 받아야 태그 연결이 가능하기 때문에 순서가 중요함
        int postId = postDao.insert(userId, seriesId, title, content, thumbnailUrl, isPublic);

        // 태그가 있을 때만 replacePostTags 호출
        // replacePostTags 내부에서 트랜잭션 처리 (기존 태그 삭제 → 새 태그 삽입)
        if (tagNames != null && !tagNames.isEmpty()) {
            tagDao.replacePostTags(postId, tagNames);
        }

        return postId;
    }

    /**
     * 게시글 수정
     * 작성자 본인 또는 관리자만 수정 가능 — 권한 확인은 Controller에서 처리하고 여기서는 수정만 담당
     * @throws IllegalArgumentException 태그 5개 초과 시
     */
    public void updatePost(int postId, Integer seriesId, String title, String content,
                           String thumbnailUrl, String isPublic, List<String> tagNames) {
        if (tagNames != null && tagNames.size() > 5) {
            throw new IllegalArgumentException("태그는 최대 5개까지만 등록할 수 있습니다.");
        }

        // 게시글 내용 수정
        postDao.update(postId, seriesId, title, content, thumbnailUrl, isPublic);

        // tagNames가 null이면 "태그 변경 없음" → replacePostTags 스킵
        // tagNames가 빈 리스트면 "모든 태그 제거" → replacePostTags 호출 (기존 태그 전부 삭제)
        // 이 두 경우를 구분하기 위해 null 체크가 필요함
        if (tagNames != null) {
            tagDao.replacePostTags(postId, tagNames);
        }
    }

    /**
     * 게시글 소프트 삭제
     * 실제로 DB 행을 지우지 않고 deleted_at 컬럼에 현재 시각을 설정함
     * 이후 모든 조회 쿼리는 WHERE deleted_at IS NULL 조건으로 자동 필터링됨
     * 권한 확인은 Controller에서 처리
     */
    public void deletePost(int postId) {
        postDao.softDelete(postId);
    }

    // ── 이미지 업로드 ──────────────────────────────────────────────────────────

    /**
     * 이미지 업로드 (Toast UI Editor 드래그앤드롭용)
     * @param uploadDir 서버 실제 업로드 경로 (ServletContext.getRealPath("/resources/upload"))
     * @param originalFileName 원본 파일명 (확장자 추출용)
     * @param fileData 파일 바이트 배열
     * @return 브라우저에서 접근 가능한 URL 경로 (/resources/upload/xxx.jpg)
     * @throws RuntimeException 파일 저장 실패 시
     */
    public String saveUploadedImage(String uploadDir, String originalFileName, byte[] fileData) {
        // 원본 파일명에서 확장자만 추출 (.jpg, .png 등)
        // 파일명 자체는 UUID로 교체해야 하지만 확장자는 브라우저 호환을 위해 유지
        String ext = "";
        int dotIdx = originalFileName.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = originalFileName.substring(dotIdx).toLowerCase();
        }

        // UUID로 파일명 생성: 서로 다른 사용자가 동일한 파일명을 올려도 덮어쓰기가 발생하지 않음
        // UUID에서 하이픈(-) 제거 → 파일명에서 하이픈이 들어가도 문제없지만 깔끔하게 처리
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 업로드 디렉터리가 없으면 자동 생성
        // 서버를 처음 배포하거나 디렉터리가 삭제된 경우에 대비
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일을 서버 디스크에 저장
        // try-with-resources 사용으로 스트림 자동 close 보장
        File dest = new File(dir, savedName);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(fileData);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + savedName, e);
        }

        // 브라우저가 접근할 수 있는 상대 URL 반환
        // 실제 디스크 경로(C:\...) 가 아닌 웹 경로(/resources/upload/...)를 반환해야 함
        // Controller에서 contextPath를 앞에 붙여서 완전한 URL을 만들어줌
        return "/resources/upload/" + savedName;
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /** 관리자 - 전체 게시글 목록 (비공개 포함, 페이지네이션) */
    public List<PostDto> getPostListForAdmin(int pageNo) {
        // countAllForAdmin()은 is_public 조건 없이 전체 카운트
        // findAllForAdmin()도 is_public 조건 없이 전체 조회
        int total = postDao.countAllForAdmin();
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);
        return postDao.findAllForAdmin(page.getOffset(), PAGE_SIZE);
    }

    public List<PostDto> getPostListForAdmin(String keyword, String status, int pageNo) {
        // 관리자 게시글 목록의 검색어/공개상태 조건을 같은 페이지 계산에 반영한다.
        int total = postDao.countAllForAdmin(keyword, status);
        PageDto page = new PageDto(pageNo, PAGE_SIZE, total);
        return postDao.findAllForAdmin(keyword, status, page.getOffset(), PAGE_SIZE);
    }

    /** 관리자 - 전체 게시글 페이지 정보 */
    public PageDto getPostPageForAdmin(int pageNo) {
        return new PageDto(pageNo, PAGE_SIZE, postDao.countAllForAdmin());
    }

    public PageDto getPostPageForAdmin(String keyword, String status, int pageNo) {
        return new PageDto(pageNo, PAGE_SIZE, postDao.countAllForAdmin(keyword, status));
    }

    /** 관리자 - 게시글 강제 삭제 (하드 삭제 — 실제 DB 행 삭제) */
    public void forceDeletePost(int postId) {
        // 일반 삭제는 softDelete (deleted_at 설정), 관리자 강제 삭제는 hardDelete (DELETE FROM)
        postDao.hardDelete(postId);
    }
}
