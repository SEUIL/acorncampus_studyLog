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
        String currentLike = likeDao.findPostLike(postId, userId);
        String finalLikeStatus = likeType;

        // 2. 상태에 따른 분기 처리
        if (currentLike == null) {
            // 반응이 없었다면 새로 등록[cite: 4]
            likeDao.insertPostLike(postId, userId, likeType);
        } else if (currentLike.equals(likeType)) {
            // 동일한 버튼을 다시 눌렀다면 취소[cite: 4]
            likeDao.deletePostLike(postId, userId);
            finalLikeStatus = null; // 프론트엔드 아이콘 비활성화를 위해 null 처리
        } else {
            // 다른 버튼을 눌렀다면 타입 변경 (좋아요 ↔ 싫어요)[cite: 4]
            likeDao.updatePostLike(postId, userId, likeType);
        }

        // 3. 최신 카운트 조회[cite: 4]
        int[] counts = likeDao.countPostLikes(postId);

        // 4. 결과 Map 조립 (컨트롤러에서 바로 JSON으로 변환 가능하도록)
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("likeCount", counts[0]);
        result.put("dislikeCount", counts[1]);
        result.put("myLike", finalLikeStatus);

        return result;
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
        String currentLike = likeDao.findCommentLike(commentId, userId);
        String finalLikeStatus = likeType;

        // 2. 상태에 따른 분기 처리
        if (currentLike == null) {
            // 반응이 없었다면 새로 등록[cite: 4]
            likeDao.insertCommentLike(commentId, userId, likeType);
        } else if (currentLike.equals(likeType)) {
            // 동일한 버튼을 다시 눌렀다면 취소[cite: 4]
            likeDao.deleteCommentLike(commentId, userId);
            finalLikeStatus = null;
        } else {
            // 다른 버튼을 눌렀다면 타입 변경[cite: 4]
            likeDao.updateCommentLike(commentId, userId, likeType);
        }

        // 3. 최신 카운트 조회[cite: 4]
        int[] counts = likeDao.countCommentLikes(commentId);

        // 4. 결과 Map 조립
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("likeCount", counts[0]);
        result.put("dislikeCount", counts[1]);
        result.put("myLike", finalLikeStatus);

        return result;
    }

    /**
     * 게시글 좋아요/싫어요 수 조회 (상세 페이지 초기 렌더링용)
     * @return int[0] = likeCount, int[1] = dislikeCount
     */
    public int[] getPostLikeCounts(int postId) {
        // DAO에 요청하여 배열 [likeCount, dislikeCount] 반환
        return likeDao.countPostLikes(postId);
    }

    /**
     * 댓글 좋아요/싫어요 수 조회
     * @return int[0] = likeCount, int[1] = dislikeCount
     */
    public int[] getCommentLikeCounts(int commentId) {
        // DAO에 요청하여 배열 [likeCount, dislikeCount] 반환
        return likeDao.countCommentLikes(commentId);
    }

    /**
     * 특정 사용자의 게시글 좋아요/싫어요 상태 조회 (상세 페이지 초기 렌더링용)
     * @param postId 게시글 ID
     * @param userId 로그인 사용자 ID
     * @return "L"(좋아요), "D"(싫어요), null(반응 없음)
     */
    public String getPostLikeStatus(int postId, int userId) {
        // DAO에 요청하여 현재 사용자의 좋아요 상태를 반환[cite: 4]
        return likeDao.findPostLike(postId, userId);
    }

    /**
     * 특정 사용자의 댓글 좋아요/싫어요 상태 조회 (초기 렌더링용)
     * @param commentId 댓글 ID
     * @param userId 로그인 사용자 ID
     * @return "L"(좋아요), "D"(싫어요), null(반응 없음)
     */
    public String getCommentLikeStatus(int commentId, int userId) {
        // DAO에 요청하여 현재 사용자의 댓글 좋아요 상태를 반환[cite: 4]
        return likeDao.findCommentLike(commentId, userId);
    }
}
