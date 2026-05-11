package com.acorncampus_studylog.dto;

/** 사용자 정보 DTO. 로그인 후 세션(loginUser)에 저장됨 */
public class UserDto {

    private int    userId;     // PK
    private String username;   // 닉네임
    private String email;      // 로그인 ID
    private String role;       // 권한: "USER" | "ADMIN"
    private String isBanned;   // 정지 여부: "Y" | "N"
    private String avatarUrl;  // 프로필 사진 경로
    private String bio;        // 한 줄 소개 (프로필 수정에서 설정)

    public int getUserId()                       { return userId; }
    public void setUserId(int userId)            { this.userId = userId; }

    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getRole()                      { return role; }
    public void setRole(String role)             { this.role = role; }

    public String getIsBanned()                  { return isBanned; }
    public void setIsBanned(String isBanned)     { this.isBanned = isBanned; }

    public String getAvatarUrl()                 { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }

    public String getBio()                       { return bio; }
    public void setBio(String bio)               { this.bio = bio; }
}
