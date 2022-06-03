package ru.kutepov.controllers;


import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kutepov.model.dto.search.PageSearchDto;
import ru.kutepov.responses.ErrorResponse;
import ru.kutepov.service.SearchService;

import java.io.IOException;
import java.sql.SQLException;
//import java.util.List;

@Controller
@RequestMapping("/api")
public class SearchController {
    SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    @GetMapping("/search")   //    /api/search?query=банк&site=playback.ru
    public ResponseEntity<Object> search(
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "offset", defaultValue = "10", required = false) int offset,
            @RequestParam(value = "limit", defaultValue = "50", required = false) int limit)
            throws SQLException, IOException {
        if (query.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Задан пустой поисковый запрос"));
        }
//        return ResponseEntity.ok(searchService.search(query, site, offset, limit));
        return ResponseEntity.ok(searchService.searchRequest(query, site, offset, limit));
    }


}
