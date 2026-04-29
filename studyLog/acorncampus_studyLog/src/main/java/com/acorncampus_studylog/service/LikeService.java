package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.LikeDao;

import java.util.HashMap;
import java.util.Map;

/** 좋아요/싫어요 비즈니스 로직 — AJAX 요청 처리, JSON 응답용 Map 반환 */
public class LikeService {

    private final LikeDao likeDao = new LikeDao();

    /**
     * 게시글 좋아요/싫어요 토글
     * 규칙: 동일 타입 재클릭 → 취소 / 다른 타입 클릭 → 변경 / 없으면 → 등록
     *
     * @param postId   게시글 ID
     * @param userId   로그인 사용자 ID
     * @param likeType "L"(좋아요) 또는 "D"(싫어요)
     * @return {"status":"ok", "likeCount":N, "dislikeCount":N, "myLike":"L"/"D"/null}
     */
    public Map<String, Object> togglePostLike(int postId, int userId, String likeType) {
        // TODO:
        // 1. likeDao.findPostLike → 현재 상태 확인
        // 2. 동일 타입이면 deletePostLike (취소), 다른 타입이면 updatePostLike, null이면 insertPostLike
        // 3. likeDao.countPostLikes → 최신 카운트 조회
        // 4. Map에 status/likeCount/dislikeCount/myLike 담아 반환
        return new HashMap<>();
    }

    /**
     * 댓글 좋아요/싫어요 토글
     *
     * @param commentId 댓글 ID
     * @param userId    로그인 사용자 ID
     * @param likeType  "L" 또는 "D"
     * @return {"status":"ok", "likeCount":N, "dislikeCount":N, "myLike":"L"/"D"/null}
     */
    public Map<String, Object> toggleCommentLike(int commentId, int userId, String likeType) {
        // TODO: togglePostLike와 동일 흐름, commentDao 메서드 사용
        return new HashMap<>();
    }

    /**
     * 게시글 좋아요/싫어요 수 조회 (상세 페이지 초기 렌더링용)
     * @return int[0] = likeCount, int[1] = dislikeCount
     */
    public int[] getPostLikeCounts(int postId) {
        // TODO: likeDao.countPostLikes
        return new int[]{0, 0};
    }

    /**
     * 댓글 좋아요/싫어요 수 조회
     * @return int[0] = likeCount, int[1] = dislikeCount
     */
    public int[] getCommentLikeCounts(int commentId) {
        // TODO: likeDao.countCommentLikes
        return new int[]{0, 0};
    }
}
