package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.SeriesDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.SeriesDto;

import java.util.List;

/** 시리즈 비즈니스 로직 (CRUD, 소속 게시글 조회) */
public class SeriesService {

    private final SeriesDao seriesDao = new SeriesDao();

    /**
     * 공개 시리즈 목록 (페이지네이션)
     * @param pageNo 현재 페이지
     */
    public List<SeriesDto> getSeriesList(int pageNo) {
        // TODO: seriesDao.countAll → PageDto → seriesDao.findAll
        return null;
    }

    /** 시리즈 목록 페이지 정보 */
    public PageDto getSeriesPage(int pageNo) {
        // TODO: new PageDto(pageNo, 10, seriesDao.countAll())
        return null;
    }

    /**
     * 시리즈 상세 조회 (소속 게시글 목록 포함)
     * @return SeriesDto (postList 포함), 없으면 null
     */
    public SeriesDto getSeriesDetail(int seriesId) {
        // TODO: seriesDao.findById → seriesDao.findPostsBySeries → series.setPostList
        return null;
    }

    /**
     * 시리즈 생성
     * @return 생성된 series_id
     */
    public int createSeries(int userId, String name, String description, String isPublic) {
        // TODO: name 유효성 검사 → seriesDao.insert
        return 0;
    }

    /**
     * 시리즈 수정
     * 본인 소유 확인은 Controller에서 처리
     */
    public void updateSeries(int seriesId, String name, String description, String isPublic) {
        // TODO: seriesDao.update
    }

    /**
     * 시리즈 삭제 (소속 게시글의 series_id는 NULL로 초기화)
     * 본인 소유 확인은 Controller에서 처리
     */
    public void deleteSeries(int seriesId) {
        // TODO: seriesDao.delete (내부에서 게시글 연결 해제 후 삭제 트랜잭션 처리)
    }

    /**
     * 특정 사용자의 시리즈 목록 조회 (마이페이지/프로필)
     */
    public List<SeriesDto> getSeriesByUser(int userId) {
        // TODO: seriesDao.findByUserId
        return null;
    }
}
