package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.PasswordResetDao;
import com.acorncampus_studylog.dao.UserDao;
import com.acorncampus_studylog.dto.PasswordResetTokenDto;
import com.acorncampus_studylog.dto.UserDetailDto;
import com.acorncampus_studylog.util.BCryptUtil;
import com.acorncampus_studylog.util.MailUtil;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 비즈니스 로직
 *
 * <p>구현 순서:
 * <ol>
 *   <li>{@link #requestReset(String)} — 이메일로 재설정 링크 발송</li>
 *   <li>{@link #validateToken(String)} — 토큰 유효성 확인</li>
 *   <li>{@link #resetPassword(String, String)} — 새 비밀번호로 변경</li>
 * </ol>
 */
public class PasswordResetService {

    private final UserDao userDao             = new UserDao();
    private final PasswordResetDao resetDao   = new PasswordResetDao();
    private static final int TOKEN_EXPIRE_MIN = 30;

    /**
     * [1단계] 비밀번호 재설정 이메일 발송
     *
     * <p>이메일 존재 여부와 관계없이 항상 동일한 응답을 반환해
     * 이메일 열거 공격(email enumeration attack)을 방지한다.
     *
     * <p>구현 힌트:
     * <ol>
     *   <li>userDao.findByEmail(email) 로 사용자 조회</li>
     *   <li>사용자가 없으면 조용히 반환 (예외 던지지 말 것)</li>
     *   <li>사용자가 있으면 generateToken() 호출로 토큰 생성</li>
     *   <li>만료 시각: LocalDateTime.now().plusMinutes(TOKEN_EXPIRE_MIN)</li>
     *   <li>resetDao.insertToken(userId, token, expiresAt) 으로 DB 저장</li>
     *   <li>MailUtil.sendPasswordResetEmail(email, token) 으로 이메일 발송</li>
     * </ol>
     *
     * @param email 비밀번호를 잊어버린 계정의 이메일
     */
    public void requestReset(String email) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("requestReset 미구현");
    }

    /**
     * [2단계] 토큰 유효성 검증
     *
     * <p>구현 힌트:
     * <ol>
     *   <li>resetDao.findValidToken(token) 으로 DB 조회</li>
     *   <li>null 이면 null 반환 (만료 또는 이미 사용된 토큰)</li>
     *   <li>null 이 아니면 PasswordResetTokenDto 그대로 반환</li>
     * </ol>
     *
     * @param token URL 파라미터로 받은 토큰 값
     * @return 유효한 토큰 정보, 유효하지 않으면 null
     */
    public PasswordResetTokenDto validateToken(String token) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("validateToken 미구현");
    }

    /**
     * [3단계] 비밀번호 재설정
     *
     * <p>구현 힌트:
     * <ol>
     *   <li>validateToken(token) 으로 유효성 재확인 → null 이면 false 반환</li>
     *   <li>BCryptUtil.hash(newPassword) 로 새 비밀번호 해싱</li>
     *   <li>userDao.updatePassword(userId, hashedPassword) 로 DB 업데이트</li>
     *   <li>resetDao.markUsed(token) 으로 토큰 무효화 (재사용 방지)</li>
     *   <li>성공하면 true 반환</li>
     * </ol>
     *
     * @param token       재설정 링크에 포함된 토큰
     * @param newPassword 사용자가 입력한 새 비밀번호 (평문)
     * @return 성공 여부
     */
    public boolean resetPassword(String token, String newPassword) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("resetPassword 미구현");
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    /** SecureRandom으로 64자리 hex 토큰 생성 */
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 32 bytes = 64자리 hex
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** LocalDateTime → java.sql.Timestamp 변환 */
    private Timestamp toTimestamp(LocalDateTime ldt) {
        return Timestamp.valueOf(ldt);
    }
}
