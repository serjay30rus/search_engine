package ru.kutepov.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.kutepov.extractor.RunExtractor;
import ru.kutepov.model.Lemma;
import ru.kutepov.model.Page;
import ru.kutepov.model.dto.search.PageSearchDto;
import ru.kutepov.responses.ErrorResponse;
import ru.kutepov.responses.SearchResponse;
import ru.kutepov.utils.LemmaFinder;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    private final LemmaFinder lemmaFinder;
    private LemmaService lemmaService;
    private IndexService indexService;
    private SiteService siteService;
    private PageService pageService;
    private FieldService fieldService;

    @Autowired
    public SearchService(LemmaService lemmaService, IndexService indexService,
                         SiteService siteService, PageService pageService,
                         FieldService fieldService) throws IOException {
        this.lemmaFinder = new LemmaFinder();
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.siteService = siteService;
        this.pageService = pageService;
        this.fieldService = fieldService;
    }


    public Object searchRequest(String findQuery, String siteUrl, int offset, int limit) throws IOException {
        Set<String> findQueryLemmas = lemmaFinder.getLemmaSet(findQuery);          // Переводим заврос в леммы
        Set<Integer> setPageId = new HashSet<>();
        List<Lemma> lemmaFindedList = new ArrayList<>();
        if (siteUrl.isEmpty()) {
            return new ErrorResponse("Введите имя сайта!");
        } else {
            // For one word
            Pageable pageable = PageRequest.of(0, offset);
            List<Page> pageList;
            for (String l : findQueryLemmas) {
                Lemma lemma = lemmaService.getByLemma(l);
                if (!(lemma == null)) {
                    lemmaFindedList.add(lemma);
                    pageList = pageService.getByLemmaId(lemma.getId(), pageable);
                    for (Page p : pageList) {
                        setPageId.add(p.getId());
                    }
                }
            }
            if (setPageId.size() > limit) {
                return new SearchResponse(limit, createSearchRes(setPageId, lemmaFindedList, limit));
            }
        }
        return new SearchResponse(setPageId.size(), createSearchRes(setPageId, lemmaFindedList, limit));
    }

    List<PageSearchDto> createSearchRes(Set<Integer> setPageId, List<Lemma> lemmaFindedList, int limit) throws IOException {
        List<PageSearchDto> listDto = new ArrayList<>();
        for (Lemma l : lemmaFindedList) {
            for (Integer n : setPageId) {
                PageSearchDto searchDto = new PageSearchDto();
                searchDto.setSite(lemmaService.getUrlByPageId(l.getId()));
                searchDto.setSiteName(lemmaService.getNameByLemmaId(l.getId()));
                searchDto.setUri(lemmaService.getUriByLemmaId(n));
                searchDto.setTitle(RunExtractor.getTitle(lemmaService.getUriByLemmaId(n)));
                searchDto.setSnippet(RunExtractor.getSnippet(pageService.getById(n).getContent()));
                searchDto.setRelevance(RunExtractor.getRelevance(lemmaService.getLemmaRank(l.getId())));
                listDto.add(searchDto);
            }

        }
        Collections.sort(listDto);
        if (listDto.size() > limit) {
            listDto = listDto.subList(0, limit);
        }
        return listDto;
    }

}
