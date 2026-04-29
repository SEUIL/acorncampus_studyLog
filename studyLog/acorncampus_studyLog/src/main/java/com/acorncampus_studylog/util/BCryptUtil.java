package com.acorncampus_studylog.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 비밀번호 단방향 암호화 유틸.
 * 회원가입 시 hash(), 로그인 시 check() 사용.
 */
public class BCryptUtil {

    /** 평문 비밀번호를 BCrypt 해시로 변환. 반환값을 DB에 저장 */
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /** 입력 비밀번호와 DB 해시값 일치 여부 반환 */
    public static boolean check(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
