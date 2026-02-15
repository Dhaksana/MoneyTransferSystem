package com.bd.dto;

import java.util.List;

/**
 * Generic paginated response wrapper
 */
public class PaginatedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private boolean isFirst;

    public PaginatedResponse() {}

    public PaginatedResponse(List<T> content, int pageNumber, int pageSize, 
                             long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.isFirst = pageNumber == 0;
        this.isLast = pageNumber >= totalPages - 1;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isLast() { return isLast; }
    public void setLast(boolean last) { isLast = last; }

    public boolean isFirst() { return isFirst; }
    public void setFirst(boolean first) { isFirst = first; }
}
