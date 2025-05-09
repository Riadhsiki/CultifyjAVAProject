package utils;

import java.util.List;
import java.util.stream.Collectors;

public class PaginationUtils<T> {
    private List<T> fullList;
    private int pageSize;
    private int currentPage;
    private int totalPages;

    public PaginationUtils(List<T> list, int pageSize) {
        this.fullList = list;
        this.pageSize = pageSize;
        this.currentPage = 1;
        calculateTotalPages();
    }

    private void calculateTotalPages() {
        this.totalPages = (int) Math.ceil((double) fullList.size() / pageSize);
    }

    public List<T> getCurrentPageItems() {
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, fullList.size());

        if (fromIndex >= fullList.size()) {
            return List.of();
        }

        return fullList.subList(fromIndex, toIndex);
    }

    public void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
        }
    }

    public void goToPage(int page) {
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
        }
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateTotalPages();
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        }
    }

    public void updateFullList(List<T> newList) {
        this.fullList = newList;
        calculateTotalPages();
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return fullList.size();
    }
}
