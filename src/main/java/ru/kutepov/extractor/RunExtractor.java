package ru.kutepov.extractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import ru.kutepov.config.SitesConfig;
import ru.kutepov.model.*;
import ru.kutepov.repository.*;
import ru.kutepov.responses.ResultResponse;
import ru.kutepov.service.LemmaService;
import ru.kutepov.service.PageService;
import ru.kutepov.service.SiteService;
import ru.kutepov.utils.LemmaFinder;

@Component
@Transactional
public class RunExtractor {
    private static Logger mainExceptions = LogManager.getLogger("searchFile");
    private final PageRepository pageRepository;
    private final LinkRepository linkRepository;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesConfig sitesConfig;
    private final SiteService siteService;
    private LemmaService lemmaService;
    PageService pageService;
    List<Lemma> lemmaEntityList = new ArrayList<>();
    @Autowired
    public RunExtractor(PageRepository pageRepository, LinkRepository linkRepository,
                        FieldRepository fieldRepository, LemmaRepository lemmaRepository,
                        IndexRepository indexRepository, SitesConfig sitesConfig,
                        SiteService siteService, PageService pageService,
                        LemmaService lemmaService){
        this.pageRepository = pageRepository;
        this.linkRepository = linkRepository;
        this.fieldRepository = fieldRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.sitesConfig = sitesConfig;
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
    }

//    @EventListener(ApplicationReadyEvent.class)                                              // Autostart
    public Object startExtract() throws IOException {                                       // Полная индексация
        List<Site> sitesConfigSites = sitesConfig.getFileTypes();
        siteService.setIndexingStarted(true);
        siteService.setIndexingStopFlag(false);
        List<String> indentedUrls;
        for(Site siteConfig : sitesConfigSites) {
            try {
                jsoupConnection(siteConfig.getUrl());
            } catch (Exception exp) {
                mainExceptions.error(exp);
            }
            Set<String> urls = Collections.synchronizedSet(new HashSet<>());                   // потокобезопасная коллекция для ссылок на дочерние страницы
            SiteExtractor rootTask = new SiteExtractor(urls, siteConfig.getUrl(), sitesConfig);           // собираем переходы на дочерние страницы со всех страниц сайта, начиная с корневого
            new ForkJoinPool().invoke(rootTask);

            indentedUrls = urls.stream().sorted(Comparator.comparing(u -> u))        // упорядочиваем собранные ссылки, добавлем отступы
                    .collect(Collectors.toList());

            SiteExtractor site = new SiteExtractor(urls, siteConfig.getUrl(), sitesConfig);

            List<HashMap<String, String>> listField = site.getField();
            List<String> listContent = site.getSiteContent();
            List<Integer> listSiteStatusCode = site.getSiteStatus();
            ArrayList<String> lemmatizeListContent = new ArrayList<>();
            Set<String> uniqalLemmaKeySet = new HashSet<>();                                                  // Создаём список уникальных лемм всего сайта
            for (String l : listContent) {
                lemmatizeListContent.add(LemmaFinder.lemmatize(l).keySet().toString());
            }

            // Saving entity to DB:

            Site dbSite = siteService.saveSiteIfNotExist(siteConfig);
            siteConfig.setStatus(SiteStatusType.INDEXING);
            dbSite.setStatus(SiteStatusType.INDEXING);
            savingPageToDataBase(indentedUrls, listSiteStatusCode, listContent, dbSite);

            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                return new ResultResponse();
            }

            savingLinkToDataBase(indentedUrls, listSiteStatusCode, listContent, dbSite);

            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                return new ResultResponse();
            }

            savingFieldToDataBase(listField, dbSite);

            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                return new ResultResponse();
            }

            savingLemmaToDataBase(listContent, site, lemmatizeListContent, uniqalLemmaKeySet, dbSite);

            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                return new ResultResponse();
            }

            savingIndexToDataBase(uniqalLemmaKeySet, listSiteStatusCode, site, lemmatizeListContent, dbSite);

            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                return new ResultResponse();
            }

            siteConfig.setStatusTime(new Timestamp(System.currentTimeMillis()));

            siteService.setIndexingStarted(false);
            siteService.updateStatus(dbSite, SiteStatusType.INDEXED);
        }

        return new ResultResponse();
    }

    public Object startExtractSinglePage(String url, Site siteConfig) throws IOException {
        siteService.setIndexingStarted(true);
        siteService.setIndexingStopFlag(false);
        List<String> indentedUrls = null;

            try {
                jsoupConnection(url);
            } catch (Exception exp) {
                mainExceptions.error(exp);
            }
            Set<String> urls = Collections.synchronizedSet(new HashSet<>());                   // потокобезопасная коллекция для ссылок на дочерние страницы
            SiteExtractor rootTask = new SiteExtractor(urls, url, sitesConfig);           // собираем переходы на дочерние страницы со всех страниц сайта, начиная с корневого
            new ForkJoinPool().invoke(rootTask);

            indentedUrls = urls.stream().sorted(Comparator.comparing(u -> u))        // упорядочиваем собранные ссылки, добавлем отступы и сохраняем в файл
                    .collect(Collectors.toList());

            SiteExtractor site = new SiteExtractor(urls, url, sitesConfig);

            List<HashMap<String, String>> listField = site.getField();
            List<String> listContent = site.getSiteContent();
            List<Integer> listSiteStatusCode = site.getSiteStatus();
            ArrayList<String> lemmatizeListContent = new ArrayList<>();
            Set<String> uniqalLemmaKeySet = new HashSet<>();                                                  // Создаём список уникальных лемм всего сайта
            for (String l : listContent) {
                lemmatizeListContent.add(LemmaFinder.lemmatize(l).keySet().toString());
            }

            // Saving entity to DB:

            Site dbSite = siteService.saveSiteIfNotExist(siteConfig);
            savingPageToDataBase(indentedUrls, listSiteStatusCode, listContent, dbSite);

        if (siteService.isIndexingStopFlag()) {
            siteService.updateStatus(dbSite, SiteStatusType.FAILED);
            siteService.updateErrorMessage(dbSite, "Indexing Stopped");
            return new ResultResponse();
        }

            savingLinkToDataBase(indentedUrls, listSiteStatusCode, listContent, dbSite);

        if (siteService.isIndexingStopFlag()) {
            siteService.updateStatus(dbSite, SiteStatusType.FAILED);
            siteService.updateErrorMessage(dbSite, "Indexing Stopped");
            return new ResultResponse();
        }

            savingFieldToDataBase(listField, dbSite);

        if (siteService.isIndexingStopFlag()) {
            siteService.updateStatus(dbSite, SiteStatusType.FAILED);
            siteService.updateErrorMessage(dbSite, "Indexing Stopped");
            return new ResultResponse();
        }

            savingLemmaToDataBase(listContent, site, lemmatizeListContent, uniqalLemmaKeySet, dbSite);

        if (siteService.isIndexingStopFlag()) {
            siteService.updateStatus(dbSite, SiteStatusType.FAILED);
            siteService.updateErrorMessage(dbSite, "Indexing Stopped");
            return new ResultResponse();
        }

            savingIndexToDataBase(uniqalLemmaKeySet, listSiteStatusCode, site, lemmatizeListContent, dbSite);

        if (siteService.isIndexingStopFlag()) {
            siteService.updateStatus(dbSite, SiteStatusType.FAILED);
            siteService.updateErrorMessage(dbSite, "Indexing Stopped");
            return new ResultResponse();
        }
        siteConfig.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteConfig.setStatus(SiteStatusType.INDEXING);
        dbSite.setStatus(SiteStatusType.INDEXING);
        siteService.setIndexingStarted(false);
        siteService.updateStatus(dbSite, SiteStatusType.INDEXED);

        return new ResultResponse();
    }

    private void jsoupConnection(String urlSource) throws IOException {
        Document doc = Jsoup.connect(urlSource).maxBodySize(0)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .get();
    }


    private void savingPageToDataBase(List<String> indentedUrls, List<Integer> listSiteStatusCode, List<String> listContent, Site dbSite) {
        for (int i = 0; i < 100; i++) {                                                              // !!!! 10 поменять на indentedUrls.size()
            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                break;
            }
            savePage(i, indentedUrls.get(i), listSiteStatusCode.get(i), listContent.get(i), siteService.saveSiteIfNotExist(dbSite));
        }
    }


    private Page savePage(int id, String path, int code, String content, Site site) {
        try {
            Page page = new Page(id, path, code, content, site);
            pageRepository.save(page);
            System.out.println(page.toString());
            return page;
        } catch (Exception exp) {
            mainExceptions.error(exp);
            return null;
        }
    }


    private void savingLinkToDataBase(List<String> indentedUrls, List<Integer> listSiteStatusCode, List<String> listContent, Site dbSite) {
        for (int i = 0; i < 100; i++) {                                                              // !!!! 10 поменять на indentedUrls.size()
            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                break;
            } else {
                siteService.updateStatus(dbSite, SiteStatusType.INDEXING);
            }

            saveLink(i, indentedUrls.get(i), listSiteStatusCode.get(i), listContent.get(i));
        }
    }


    private Link saveLink(int id, String path, Integer code, String content) {
        try {
            Link link = new Link(id, path, code, content);
            linkRepository.save(link);
            System.out.println(link.toString());
            return link;
        } catch (Exception exp) {
            mainExceptions.error(exp);
            return null;
        }
    }


    private void savingFieldToDataBase(List<HashMap<String, String>> listField, Site dbSite) {
        for (int i = 0; i < listField.size(); i += 2) {
            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                break;
            }

            saveField(i, "title", listField.get(i).get("title"));
            saveField(i, "body", listField.get(i + 1).get("body"));
        }
    }


    private Field saveField(int id, String name, String selector) {
        try {
            float weight = name.equals("title") ? 1 : 0.8F;
            Field field = new Field(id, name, selector, weight);
            fieldRepository.save(field);
            System.out.println(field.toString());
            return field;
        } catch (Exception exp) {
            mainExceptions.error(exp);
            return null;
        }
    }


    private void savingLemmaToDataBase(List<String> listContent, SiteExtractor site,
                                       ArrayList<String> lemmatizeListContent, Set<String> uniqalLemmaKeySet, Site dbSite) throws IOException {
        for (String s : listContent) {
            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                break;
            }

            HashMap<String, Integer> lemmaMap = LemmaFinder.lemmatize(s);
            uniqalLemmaKeySet.addAll(lemmaMap.keySet());
        }

        for (int i = 0; i < 100; i++) {                                                                   // !!!! 10 поменять на uniqalLemmaKeySet.size()
            if (siteService.isIndexingStopFlag()) {
                siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                break;
            }

            String singleLemma = uniqalLemmaKeySet.toArray()[i].toString();
            saveLemma(i, singleLemma, site.getFrequency(lemmatizeListContent, singleLemma), siteService.saveSiteIfNotExist(dbSite));
        }
    }


    private Lemma saveLemma(int id, String lemma, int frequency, Site site) {
        try {
            Lemma newLemma = new Lemma(id, lemma, frequency, site);
            lemmaRepository.save(newLemma);
            lemmaEntityList.add(newLemma);
            System.out.println(newLemma.toString());
            return newLemma;
        } catch (Exception exp) {
            mainExceptions.error(exp);
            return null;
        }
    }


    private void savingIndexToDataBase(Set<String> uniqalLemmaKeySet, List<Integer> listSiteStatusCode,
                                       SiteExtractor site, ArrayList<String> lemmatizeListContent, Site dbSite) {
        int indexId = 0;
        for (int i = 0; i < 100 - 1; i++) {                                                   // !!!! 100 поменять на indentedUrls.size()
            for (int j = 0; j < 10; j++) {                                                // !!!! 10 поменять на uniqalLemmaKeySet.size()
                if (siteService.isIndexingStopFlag()) {
                    siteService.updateStatus(dbSite, SiteStatusType.FAILED);
                    siteService.updateErrorMessage(dbSite, "Indexing Stopped");
                    break;
                }

                Page page = pageService.getById(i + 1);
                Lemma lemma = lemmaService.getById(j + 1);

                indexId++;
                saveIndex(indexId, page, lemma, site.getRank(site.getFrequency(lemmatizeListContent, uniqalLemmaKeySet.toArray()[j].toString())), listSiteStatusCode.get(i));
            }
        }
    }


    private void saveIndex(int id, Page page, Lemma lemma, float rank, int code) {
        try {
            if (code == 200) {
                Index index = new Index(id, page, lemma, rank);
                indexRepository.save(index);
                System.out.println(index.toString());
            }
        } catch (Exception exp) {
            mainExceptions.error(exp);
        }
    }


    public static String getTitle(String htmlText) throws IOException {
        Document doc = Jsoup.connect(htmlText).maxBodySize(0)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .get();

        return doc.title();
    }

    public static String getSnippet(String htmlTxt) {
        return htmlTxt.substring(0, 100).concat("...");
    }


    public Object stopIndexing() {
        siteService.setIndexingStarted(false);
        siteService.setIndexingStopFlag(true);
        System.out.println("Indexing Stopped");
        return new ResultResponse();
    }

    public static Float getRelevance(Float number) {
        Float result = number / (float) (Math.random() * 10);
        return result;
    }

}

