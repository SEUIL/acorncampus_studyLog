package com.acorncampus_studylog.dto;

/** 페이지네이션 정보 헬퍼 — Service에서 생성해 Controller/JSP로 전달 */
public class PageDto {

    private int pageNo;      // 현재 페이지 (1-based)
    private int pageSize;    // 페이지당 항목 수
    private int totalCount;  // 전체 항목 수 (COUNT 쿼리 결과)

    public PageDto(int pageNo, int pageSize, int totalCount) {
        this.pageNo = (pageNo < 1) ? 1 : pageNo;
        this.pageSize = (pageSize < 1) ? 10 : pageSize;
        this.totalCount = totalCount;
    }

    /** OFFSET ? ROWS FETCH NEXT ? ROWS ONLY 에 넘길 offset 값 */
    public int getOffset() {
        return (pageNo - 1) * pageSize;
    }

    /** 전체 페이지 수 */
    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public boolean hasPrev() { return pageNo > 1; }
    public boolean hasNext() { return pageNo < getTotalPages(); }

    public int getPrevPage() { return pageNo - 1; }
    public int getNextPage() { return pageNo + 1; }

    public int getPageNo() { return pageNo; }
    public int getPageSize() { return pageSize; }
    public int getTotalCount() { return totalCount; }
}
