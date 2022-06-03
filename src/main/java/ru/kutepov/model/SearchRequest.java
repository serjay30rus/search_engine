package ru.kutepov.model;

public class SearchRequest {
    private String query;   // поисковый запрос;
    private String site;    // сайт, по которому осуществлять поиск
    private int offset;     // сдвиг от 0 для постраничного вывода
    private int limit;      // количество результатов, которое необходимо вывести.


    public SearchRequest(){}


    public SearchRequest(String query, String site, int offset, int limit) {
        this.query = query;
        this.site = site;
        this.offset = offset;
        this.limit = limit;
    }


    public String getQuery() {
        return query;
    }


    public String getSite() {
        return site;
    }


    public int getOffset() {
        return offset;
    }


    public int getLimit() {
        return limit;
    }


    public void setQuery(String query) {
        this.query = query;
    }

}
