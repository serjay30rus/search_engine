package ru.kutepov.responses;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.kutepov.model.dto.search.PageSearchDto;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SearchResponse {
    private String result = "true";
    private int count;
    private List<PageSearchDto> data;

    public SearchResponse(int count, List<PageSearchDto> searchResult) {
        this.count = count;
        this.data = searchResult;
    }
}
