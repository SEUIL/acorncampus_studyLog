package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.CommentDao;
import com.acorncampus_studylog.dto.CommentDto;
import com.acorncampus_studylog.dto.PageDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 댓글/대댓글 비즈니스 로직 (2depth, CRUD) */
public class CommentService {

    private final CommentDao commentDao = new CommentDao();

    /**
     * 게시글의 댓글 목록 조회 (부모-자식 트리 구조로 조립)
     * @return 최상위 댓글 목록 (각 댓글의 replies 필드에 대댓글 포함)
     */
    public List<CommentDto> getCommentsByPost(int postId) {
        // 1. DAO에서 평탄화된(flat) 전체 댓글 목록 가져오기
        List<CommentDto> flatList = commentDao.findByPostId(postId);

        // 최종적으로 반환할 최상위 댓글 리스트
        List<CommentDto> rootComments = new ArrayList<>();

        // 부모 댓글을 빠르게(O(1)) 찾기 위한 Map 구조
        Map<Integer, CommentDto> commentMap = new HashMap<>();

        // 2. 모든 댓글을 Map에 담기 (ID를 키로 사용)
        for (CommentDto comment : flatList) {
            // NullPointerException 방지를 위해 자식 리스트 초기화
            if (comment.getReplies() == null) {
                comment.setReplies(new ArrayList<>());
            }
            commentMap.put(comment.getCommentId(), comment);
        }

        // 3. 리스트를 순회하며 트리 구조 조립
        for (CommentDto comment : flatList) {
            if (comment.getParentCommentId() == null) {
                // 최상위 댓글이면 결과 리스트에 담기
                rootComments.add(comment);
            } else {
                // 대댓글이면 Map에서 부모 댓글을 찾아 replies에 추가하기
                CommentDto parent = commentMap.get(comment.getParentCommentId());
                if (parent != null) {
                    parent.getReplies().add(comment);
                }
            }
        }

        // 4. 조립이 완료된 최상위 댓글 리스트 반환
        return rootComments;
    }

    /**
     * 댓글 등록
     * @return 생성된 comment_id
     */

    public int addComment(int postId, int userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다.");
        }
        // 내부적으로 DAO를 호출할 때 parentId 자리에 강제로 null을 집어넣음
        return commentDao.insert(postId, userId, null, content.trim());
    }

    /**
     * 대댓글 등록 (2depth 제한 검사 포함)
     * @param parentCommentId 부모 댓글 ID
     * @return 생성된 comment_id, 부모가 이미 대댓글이면 예외 발생
     */

    public int addReply(int postId, int userId, int parentCommentId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("답글 내용은 비어있을 수 없습니다.");
        }
        // 전달받은 parentId를 그대로 넘김
        return commentDao.insert(postId, userId, parentCommentId, content.trim());
    }

    /**
     * 댓글 수정
     * 작성자 본인만 수정 가능 — Controller에서 권한 확인 후 호출
     */
    public void updateComment(int commentId, String content) {
        // TODO: content 유효성 검사 → commentDao.update
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다.");
        }
        commentDao.update(commentId, content.trim());

    }

    /**
     * 댓글 소프트 삭제
     * 작성자 본인 또는 관리자 — Controller에서 권한 확인 후 호출
     */
    public void deleteComment(int commentId) {
        // TODO: commentDao.softDelete
        commentDao.softDelete(commentId);
    }

// ── 관리자 전용 ──────────────────────────────────────────────────────────

    /**
     * 관리자 - 전체 댓글 목록
     * @param pageNo 현재 페이지 번호
     */
    public List<CommentDto> getCommentListForAdmin(int pageNo) {
        int limit = 10; // 한 페이지에 보여줄 댓글 수
        int offset = (pageNo - 1) * limit; // 건너뛸 데이터 수 계산

        // DAO의 findAllForAdmin 메서드를 호출하여 페이징된 목록 반환
        return commentDao.findAllForAdmin(offset, limit);
    }

    public List<CommentDto> getCommentListForAdmin(String keyword, int pageNo) {
        int limit = 10;
        int offset = (pageNo - 1) * limit;
        return commentDao.findAllForAdmin(keyword, offset, limit);
    }

    /**
     * 관리자 - 전체 댓글 페이지 정보
     * @param pageNo 현재 페이지 번호
     */
    public PageDto getCommentPageForAdmin(int pageNo) {
        int limit = 10; // 한 페이지에 보여줄 댓글 수

        // DAO의 countAllForAdmin 메서드를 통해 전체 삭제되지 않은 댓글 수를 조회
        int totalCount = commentDao.countAllForAdmin();

        return new PageDto(pageNo, limit, totalCount);
    }

    public PageDto getCommentPageForAdmin(String keyword, int pageNo) {
        int limit = 10;
        return new PageDto(pageNo, limit, commentDao.countAllForAdmin(keyword));
    }

    /**
     * 관리자 - 댓글 강제 삭제
     * @param commentId 삭제할 댓글 ID
     */
    public void forceDeleteComment(int commentId) {
        // DAO의 hardDelete 메서드를 호출하여 DB에서 완전히 삭제 (DELETE 쿼리)
        commentDao.hardDelete(commentId);
    }

    public CommentDto findById(int commentId) {
        return commentDao.findById(commentId);
    }
}
