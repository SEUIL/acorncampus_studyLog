package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.SeriesDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.SeriesDto;

import java.util.List;

//1. getSeriesList(int pageNo)
//공개 시리즈 목록을 페이지 단위로 가져옴
//  2. getSeriesPage(int pageNo)
//시리즈 목록 페이지 정보를 만듦
//  3. getSeriesDetail(int seriesId)
//시리즈 상세 정보와 그 안의 게시글 목록을 같이 가져옴
//  4. createSeries(int userId, String name, String description, String isPublic)
//새 시리즈를 생성함
//  5. updateSeries(int seriesId, String name, String description, String isPublic)
//기존 시리즈 제목, 설명, 공개 여부를 수정함
//  6. deleteSeries(int seriesId)
//시리즈를 삭제함
//이때 소속 게시글의 series_id는 DAO에서 NULL로 바꿈
//  7. getSeriesByUser(int userId)
//특정 사용자의 시리즈 목록을 가져옴
//현겸 게시글 작성 화면 드롭다운에서 쓰는 메서드야.

/** 시리즈 비즈니스 로직 (CRUD, 소속 게시글 조회) */
public class SeriesService {

    private final SeriesDao seriesDao = new SeriesDao();

    /**
     * 공개 시리즈 목록 (페이지네이션)
     * @param pageNo 현재 페이지
     */
    public List<SeriesDto> getSeriesList(int pageNo) {
        // 페이지 정보를 만든 뒤 공개 시리즈 목록을 조회
        PageDto page = getSeriesPage(pageNo);
        return seriesDao.findAll(page.getOffset(), page.getPageSize());
    }

    /** 시리즈 목록 페이지 정보 */
    public PageDto getSeriesPage(int pageNo) {
        return new PageDto(pageNo, 10, seriesDao.countAll());
    }

    /** 키워드로 시리즈명 검색 */
    public List<SeriesDto> search(String keyword, int pageNo) {
        PageDto page = getSearchPage(keyword, pageNo);
        return seriesDao.search(keyword, page.getOffset(), page.getPageSize());
    }

    /** 검색 결과 페이지 정보 */
    public PageDto getSearchPage(String keyword, int pageNo) {
        return new PageDto(pageNo, 10, seriesDao.countSearch(keyword));
    }

    /**
     * 시리즈 상세 조회 (소속 게시글 목록 포함)
     * @return SeriesDto (postList 포함), 없으면 null
     */
    public SeriesDto getSeriesDetail(int seriesId) {
        // 시리즈 기본 정보 조회
        SeriesDto series = seriesDao.findById(seriesId);
        if (series == null) {
            return null;
        }

        // 상세 화면에서 사용할 소속 게시글 목록까지 함께 담기
        series.setPostList(seriesDao.findPostsBySeries(seriesId));
        return series;
    }

    /**
     * 시리즈 생성
     * @return 생성된 series_id
     */
    public int createSeries(int userId, String name, String description, String isPublic) {
        // 입력값 정리/검증 후 시리즈 생성
        validateUserId(userId);
        String validName = normalizeName(name);
        String validIsPublic = normalizeIsPublic(isPublic);
        return seriesDao.insert(userId, validName, normalizeDescription(description), validIsPublic);
    }

    /**
     * 시리즈 수정
     * 본인 소유 확인은 Controller에서 처리
     */
    public void updateSeries(int seriesId, String name, String description, String isPublic) {
        // 입력값 정리/검증 후 시리즈 수정
        validateSeriesId(seriesId);
        String validName = normalizeName(name);
        String validIsPublic = normalizeIsPublic(isPublic);
        seriesDao.update(seriesId, validName, normalizeDescription(description), validIsPublic);
    }

    /**
     * 시리즈 삭제 (소속 게시글의 series_id는 NULL로 초기화)
     * 본인 소유 확인은 Controller에서 처리
     */
    public void deleteSeries(int seriesId) {
        // DAO에서 소속 게시글 연결 해제 후 시리즈 삭제
        validateSeriesId(seriesId);
        seriesDao.delete(seriesId);
    }

    /**
     * 특정 사용자의 시리즈 목록 조회 (마이페이지/프로필)
     */
    public List<SeriesDto> getSeriesByUser(int userId) {
        // 현겸 post_write.jsp 드롭다운에서 사용할 사용자별 시리즈 목록
        validateUserId(userId);
        return seriesDao.findByUserId(userId);
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
    }

    private void validateSeriesId(int seriesId) {
        if (seriesId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 시리즈 ID입니다.");
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("시리즈 이름은 필수입니다.");
        }
        return name.trim();
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    private String normalizeIsPublic(String isPublic) {
        // 공개가 명확히 선택된 경우만 Y, 그 외에는 비공개로 저장한다.
        return "Y".equalsIgnoreCase(isPublic) ? "Y" : "N";
    }
    /**
     * 특정 태그를 가진 게시글이 포함된 시리즈 목록 조회 (페이지네이션)
     * @param tagName 검색할 태그명 (예: "Java")
     */
    public List<SeriesDto> getSeriesByTag(String tagName, int pageNo) {
        PageDto page = getSeriesPageByTag(tagName, pageNo);
        // seriesDao에 findByTag(tagName, offset, pageSize) 구현 필요
        return seriesDao.findByTag(tagName, page.getOffset(), page.getPageSize());
    }

    /** 태그별 시리즈 검색 결과 페이지 정보 */
    public PageDto getSeriesPageByTag(String tagName, int pageNo) {
        // seriesDao에 countByTag(tagName) 구현 필요
        return new PageDto(pageNo, 10, seriesDao.countByTag(tagName));
    }
}
