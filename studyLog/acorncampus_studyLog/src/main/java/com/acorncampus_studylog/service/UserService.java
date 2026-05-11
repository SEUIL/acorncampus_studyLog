package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.UserDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.UserDetailDto;
import com.acorncampus_studylog.util.BCryptUtil;

import java.util.List;
import java.util.Map;

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

        // 이메일 조회
        UserDetailDto user = userDao.findByEmail(email);

        // 이메일 조회 x, 사용자 없으면 로그인 실패
        if(user == null){
            return null;
        }

        // 이메일 조회 o, 비밀번호 검증
        boolean ok = BCryptUtil.check(rawPassword, user.getPassword());

        if (!ok){
            return null;
        }

        // 정지 계정 확인
        if ("Y".equals(user.getIsBanned())) {
            return null;
        }

        // 로그인 성공
        return user;
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

        // 중복 이메일 확인
        if (userDao.findByEmail(email) != null ){
            return 0;

        }

        // 중복 닉네임 확인
        if (userDao.findByNickname(nickname) != null){
            return 0;
        }

        // 비밀번호 저장 전 해시
        String hashed = BCryptUtil.hash(rawPassword);

        // 아이디 생성
        return userDao.insert(email, nickname, hashed);
    }


    /**
     * 이메일 중복 확인 (AJAX용)
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean checkEmailAvailable(String email) {
        // TODO: userDao.findByEmail != null이면 false
        if (userDao.findByEmail(email) == null){
            return true;
        }else {
            return false;
        }
    }

    /**
     * user_id로 사용자 상세 정보 조회
     * @return UserDetailDto 또는 null
     */
    public UserDetailDto getUserById(int userId) {
        // TODO: userDao.findById
        UserDetailDto user = userDao.findById(userId);

        if (user != null){
            return user;
        }

        return null;
    }


    /**
     * 프로필 수정 (닉네임, 자기소개, 아바타 URL)
     * 닉네임 중복 검사 포함
     */
    public void updateProfile(int userId, String nickname, String bio, String avatarUrl) {
        // TODO: 닉네임 중복 확인 → userDao.updateProfile

        // 닉네임 중복 확인
        UserDetailDto existUSer = userDao.findByNickname(nickname);

        // 중복일 경우 사용불가
        if (existUSer != null && existUSer.getUserId() != userId) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        // 중복없을 경우 수정 업데이트
        userDao.updateProfile(userId, nickname,bio, avatarUrl);
        
    }


    /**
     * 비밀번호 변경
     * @param oldPassword 현재 비밀번호 (검증용)
     * @param newPassword 새 비밀번호 (BCrypt 후 저장)
     * @return 현재 비밀번호 일치 시 true, 불일치 시 false
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // TODO: findById → BCryptUtil.check(oldPw) → hash(newPw) → updatePassword

        // 사용자 조회
        UserDetailDto user = userDao.findById(userId);

        // 사용자 없으면 실패
        if ( user == null){
            return false;
        }

        // 현재 비밀번호 확인
        boolean ok = BCryptUtil.check(oldPassword, user.getPassword());

        // 현재 비밀번호 불일치 시 false
        if (!ok){
            return false;
        }

        // 새 비밀번호
        String hashed = BCryptUtil.hash(newPassword);

        userDao.updatePassword(userId, hashed);
        return true;
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     * @param rawPassword 현재 비밀번호 (본인 확인)
     * @return 비밀번호 일치 시 true
     */
    public boolean deleteAccount(int userId, String rawPassword) {
        // TODO: 비밀번호 확인 → userDao.softDelete

        // 사용자 조회
        UserDetailDto user = userDao.findById(userId);

        // 비밀번호 확인
        boolean ok = BCryptUtil.check(rawPassword, user.getPassword());

        // 틀릴 경우 실패
        if (!ok){
            return false;
        }

        // 소프트 삭제
        userDao.softDelete(userId);
        return true;
    }


    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /**
     * 관리자 - 사용자 목록 조회
     * @param keyword 이메일/닉네임 검색어 (null이면 전체)
     * @param pageNo 현재 페이지 (1-based)
     */
    public List<UserDetailDto> getUserList(String keyword, int pageNo) {
        return getUserList(keyword, null, pageNo);
    }

    /**
     * 관리자 - 사용자 목록 조회
     * 검색어와 상태 필터를 함께 적용해서 회원관리 화면의 select와 실제 목록을 맞춘다.
     */
    public List<UserDetailDto> getUserList(String keyword, String status, int pageNo) {
        int pageSize = 10;
        PageDto pageDto = getUserPage(keyword, status, pageNo);
        return userDao.findAll(keyword, normalizeAdminStatus(status), pageDto.getOffset(), pageSize);
    }

    /** 관리자 - 페이지 정보 반환 */
    public PageDto getUserPage(String keyword, int pageNo) {
        return getUserPage(keyword, null, pageNo);
    }

    /** 관리자 - 검색어와 상태 필터가 반영된 페이지 정보 반환 */
    public PageDto getUserPage(String keyword, String status, int pageNo) {
        int totalCount = userDao.countAll(keyword, normalizeAdminStatus(status));
        return new PageDto(pageNo, 10, totalCount);
    }

    /** 관리자 - 상태별 회원 수 통계 */
    public int getUserCountByStatus(String status) {
        return userDao.countByStatus(normalizeAdminStatus(status));
    }

    /** 관리자 - 계정 정지 */
    /** Admin dashboard - today registered user count. */
    public int getTodayUserCount() {
        return userDao.countTodayCreated();
    }

    /** Admin dashboard - daily registered user counts for the last 7 days. */
    public List<Map<String, Object>> getRecentUserStats() {
        List<Map<String, Object>> stats = userDao.countDailyCreatedLast7Days();
        int maxCount = 1;
        for (Map<String, Object> item : stats) {
            maxCount = Math.max(maxCount, ((Number) item.get("count")).intValue());
        }
        for (Map<String, Object> item : stats) {
            int count = ((Number) item.get("count")).intValue();
            item.put("percent", count == 0 ? 0 : Math.max(8, Math.round(count * 100f / maxCount)));
        }
        return stats;
    }

    public void banUser(int userId) {
        // TODO: userDao.ban(userId)

        // 사용자 조회
        UserDetailDto user = userDao.findById(userId);

        // 사용자가 없으면 종료
        if ( user == null){
            return;
        }

        // 계정 정지
        userDao.ban(userId);

    }

    /** 관리자 - 계정 정지 해제 */
    public void unbanUser(int userId) {
        // TODO: userDao.unban(userId)

        // 사용자 조회
        UserDetailDto user = userDao.findById(userId);

        // 사용자 없으면 종료
        if ( user == null){
            return;
        }

        // 계정 정지 해제
        userDao.unban(userId);

    }

    /** 관리자 - 계정 강제 삭제 */
    public void deleteUserByAdmin(int userId) {
        // 관리자 삭제도 일반 탈퇴와 같은 soft delete로 통일한다.
        // users를 hard delete하면 posts/comments/reports/likes의 FK 참조 때문에 실패할 수 있다.
        UserDetailDto user = userDao.findById(userId);
        if (user == null) {
            return;
        }
        userDao.softDelete(userId);
    }

    public void forceDeleteUser(int userId) {
        // TODO: userDao.hardDelete(userId)

        // 사용자 조회
        UserDetailDto user = userDao.findById(userId);

        // 사용자 없으면 종료
        if ( user == null){
            return;
        }

        // 계정 강제 삭제
        userDao.hardDelete(userId);

    }

    private String normalizeAdminStatus(String status) {
        if ("active".equals(status) || "banned".equals(status) || "deleted".equals(status)) {
            return status;
        }
        return null;
    }
}
