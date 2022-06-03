package ru.kutepov.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kutepov.config.SitesConfig;
import ru.kutepov.extractor.RunExtractor;
//import ru.kutepov.extractor.UrlParserService;
import ru.kutepov.model.Site;
import ru.kutepov.responses.ErrorResponse;
import ru.kutepov.service.FileTypeService;
import ru.kutepov.service.SiteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexingController {
    private RunExtractor runExtractor;
    private SitesConfig sitesConfig;
    private SiteService siteService;


    @Autowired
    public IndexingController(RunExtractor runExtractor, SitesConfig sitesConfig, SiteService siteService) {
        this.runExtractor = runExtractor;
        this.sitesConfig = sitesConfig;
        this.siteService = siteService;
    }


    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing() throws IOException {
        System.out.println("Start indexing all");
        if (!siteService.isIndexingStarted()) {
            return ResponseEntity.ok(runExtractor.startExtract());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("Индексация уже запущена"));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing() {
        if (siteService.isIndexingStarted()) {
            return ResponseEntity.ok(runExtractor.stopIndexing());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("Индексация не запущена"));
    }

    @GetMapping("/indexPage")      // /api/indexPage?url=http://www.playback.ru
    public ResponseEntity<Object> indexPage(@RequestParam(value = "url") String url) throws SQLException, IOException, InterruptedException {
        System.out.printf("Start indexing: %s\n", url);
        if (!siteService.isIndexingStarted()) {
            List<Site> siteArrayList = sitesConfig.getFileTypes();
            for (Site siteFromConfig : siteArrayList) {
                if (url.toLowerCase(Locale.ROOT).contains(siteFromConfig.getUrl())) {
                    return ResponseEntity.ok(runExtractor.startExtractSinglePage(url, siteFromConfig));
                }
            }
            return ResponseEntity.badRequest().body(new ErrorResponse("Данная страница находится за пределами сайтов, " +
                    "указаных в конфигурационном файле."));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("Индексация уже запущена. Остановите индексацию, " +
                "или дождитесь ее окончания"));
    }

}
