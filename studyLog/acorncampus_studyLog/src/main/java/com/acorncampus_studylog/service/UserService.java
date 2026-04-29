package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.UserDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.UserDetailDto;
import com.acorncampus_studylog.util.BCryptUtil;

import java.util.List;

/** 회원 관련 비즈니스 로직 (로그인, 가입, 프로필, 탈퇴) */
public class UserService {

    private final UserDao userDao = new UserDao();

    /**
     * 로그인 처리
     * @param email 이메일
     * @param rawPassword 입력한 평문 비밀번호
     * @return 로그인 성공 시 UserDetailDto, 실패 시 null
     */
    public UserDetailDto login(String email, String rawPassword) {
        // TODO: userDao.findByEmail → BCryptUtil.check → 정지 계정 확인 → 반환
        return null;
    }

    /**
     * 회원가입
     * @param email 이메일
     * @param nickname 닉네임
     * @param rawPassword 평문 비밀번호 (BCryptUtil.hash 후 저장)
     * @return 생성된 user_id
     */
    public int register(String email, String nickname, String rawPassword) {
        // TODO: 중복 이메일/닉네임 확인 → BCryptUtil.hash → userDao.insert
        return 0;
    }

    /**
     * 이메일 중복 확인 (AJAX용)
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean checkEmailAvailable(String email) {
        // TODO: userDao.findByEmail != null이면 false
        return false;
    }

    /**
     * user_id로 사용자 상세 정보 조회
     * @return UserDetailDto 또는 null
     */
    public UserDetailDto getUserById(int userId) {
        // TODO: userDao.findById
        return null;
    }

    /**
     * 프로필 수정 (닉네임, 자기소개, 아바타 URL)
     * 닉네임 중복 검사 포함
     */
    public void updateProfile(int userId, String nickname, String bio, String avatarUrl) {
        // TODO: 닉네임 중복 확인 → userDao.updateProfile
    }

    /**
     * 비밀번호 변경
     * @param oldPassword 현재 비밀번호 (검증용)
     * @param newPassword 새 비밀번호 (BCrypt 후 저장)
     * @return 현재 비밀번호 일치 시 true, 불일치 시 false
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // TODO: findById → BCryptUtil.check(oldPw) → hash(newPw) → updatePassword
        return false;
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     * @param rawPassword 현재 비밀번호 (본인 확인)
     * @return 비밀번호 일치 시 true
     */
    public boolean deleteAccount(int userId, String rawPassword) {
        // TODO: 비밀번호 확인 → userDao.softDelete
        return false;
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /**
     * 관리자 - 사용자 목록 조회
     * @param keyword 이메일/닉네임 검색어 (null이면 전체)
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<UserDetailDto> getUserList(String keyword, int pageNo) {
        // TODO: countAll → PageDto → findAll(keyword, offset, pageSize)
        return null;
    }

    /** 관리자 - 페이지 정보 반환 */
    public PageDto getUserPage(String keyword, int pageNo) {
        // TODO: countAll → new PageDto(pageNo, 10, totalCount)
        return null;
    }

    /** 관리자 - 계정 정지 */
    public void banUser(int userId) {
        // TODO: userDao.ban(userId)
    }

    /** 관리자 - 계정 정지 해제 */
    public void unbanUser(int userId) {
        // TODO: userDao.unban(userId)
    }

    /** 관리자 - 계정 강제 삭제 */
    public void forceDeleteUser(int userId) {
        // TODO: userDao.hardDelete(userId)
    }
}
