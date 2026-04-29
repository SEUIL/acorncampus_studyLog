package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.CommentDao;
import com.acorncampus_studylog.dto.CommentDto;
import com.acorncampus_studylog.dto.PageDto;

import java.util.List;

/** 댓글/대댓글 비즈니스 로직 (2depth, CRUD) */
public class CommentService {

    private final CommentDao commentDao = new CommentDao();

    /**
     * 게시글의 댓글 목록 조회 (부모-자식 트리 구조로 조립)
     * @return 최상위 댓글 목록 (각 댓글의 replies 필드에 대댓글 포함)
     */
    public List<CommentDto> getCommentsByPost(int postId) {
        // TODO: commentDao.findByPostId(flat list) → parentCommentId 기준으로 트리 조립
        // 힌트: 최상위 댓글 먼저 필터, 대댓글은 부모 댓글의 replies 리스트에 추가
        return null;
    }

    /**
     * 댓글 등록
     * @return 생성된 comment_id
     */
    public int addComment(int postId, int userId, String content) {
        // TODO: content 유효성 검사 → commentDao.insert(postId, userId, null, content)
        return 0;
    }

    /**
     * 대댓글 등록 (2depth 제한 검사 포함)
     * @param parentCommentId 부모 댓글 ID
     * @return 생성된 comment_id, 부모가 이미 대댓글이면 예외 발생
     */
    public int addReply(int postId, int userId, int parentCommentId, String content) {
        // TODO: commentDao.findById(parentCommentId) → parent가 이미 대댓글(parentCommentId != null)이면 예외
        //       → commentDao.insert(postId, userId, parentCommentId, content)
        return 0;
    }

    /**
     * 댓글 수정
     * 작성자 본인만 수정 가능 — Controller에서 권한 확인 후 호출
     */
    public void updateComment(int commentId, String content) {
        // TODO: content 유효성 검사 → commentDao.update
    }

    /**
     * 댓글 소프트 삭제
     * 작성자 본인 또는 관리자 — Controller에서 권한 확인 후 호출
     */
    public void deleteComment(int commentId) {
        // TODO: commentDao.softDelete
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /** 관리자 - 전체 댓글 목록 */
    public List<CommentDto> getCommentListForAdmin(int pageNo) {
        // TODO: commentDao.countAllForAdmin → PageDto → commentDao.findAllForAdmin
        return null;
    }

    /** 관리자 - 전체 댓글 페이지 정보 */
    public PageDto getCommentPageForAdmin(int pageNo) {
        // TODO: new PageDto(pageNo, 10, commentDao.countAllForAdmin())
        return null;
    }

    /** 관리자 - 댓글 강제 삭제 */
    public void forceDeleteComment(int commentId) {
        // TODO: commentDao.hardDelete
    }
}
