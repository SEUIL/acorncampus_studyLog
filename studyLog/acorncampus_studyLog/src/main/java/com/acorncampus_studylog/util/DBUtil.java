package com.acorncampus_studylog.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** DB 연결 유틸. getConnection()으로 연결, 작업 후 반드시 close() 호출. */
public class DBUtil {

    // DB 접속 정보
    private static final String URL  = "jdbc:oracle:thin:@3.36.62.46:1521:xe";
    private static final String USER = "system";
    private static final String PASS = "1234";

    // 클래스 로드 시 Oracle 드라이버 등록 (딱 한 번 실행)
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Oracle JDBC Driver not found", e);
        }
    }

    /** DB Connection 반환. 사용 후 close() 필수 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** ResultSet → PreparedStatement → Connection 순서로 닫음. null 안전 처리 포함 */
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try { if (rs    != null) rs.close();    } catch (SQLException ignored) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException ignored) {}
        try { if (conn  != null) conn.close();  } catch (SQLException ignored) {}
    }
}
