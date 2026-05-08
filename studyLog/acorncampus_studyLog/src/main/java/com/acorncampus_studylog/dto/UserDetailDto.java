package com.acorncampus_studylog.dto;

import java.sql.Timestamp;

/** DB에서 조회한 사용자 전체 정보 (세션용 UserDto와 별개로 사용) */
public class UserDetailDto {

    private int userId;
    private String email;
    private String nickname;
    private String password;   // BCrypt 해시 (로그인 검증용)
    private String bio;
    private String avatarUrl;
    private String role;       // "USER" / "ADMIN"
    private String isBanned;   // "Y" / "N"
    private Timestamp createdAt;
    private Timestamp deletedAt;

    /** UserDetailDto → 세션 저장용 UserDto 변환 */
    public UserDto toSessionDto() {
        UserDto dto = new UserDto();
        dto.setUserId(userId);
        dto.setUsername(nickname);
        dto.setEmail(email);
        dto.setRole(role);
        dto.setIsBanned(isBanned);
        dto.setAvatarUrl(avatarUrl);
        dto.setBio(bio);   // 한 줄 소개 — 사이드바·대시보드 표시용
        return dto;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getIsBanned() { return isBanned; }
    public void setIsBanned(String isBanned) { this.isBanned = isBanned; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }
}
