package ru.kutepov.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.kutepov.model.Lemma;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kutepov.model.Site;
import ru.kutepov.model.dto.interfaces.ModelId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO _lemma (frequency, lemma, site_id) " +
            "VALUES (?,?,?) ON DUPLICATE KEY UPDATE frequency = frequency + 1;", nativeQuery = true)
    void insertOnDuplicateUpdate(int frequency, String lemma, int site_id);

    Optional<Lemma> findLemmaByLemmaAndSiteBySiteId(String lemmaString, Site siteBySiteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE _lemma set frequency = frequency - 1 " +
            "where id in (select lemma_id from _index where page_id = ?);", nativeQuery = true)
    void unCountLemmasOfPage(int pageId);

    long countBySiteBySiteId(Site siteBySiteId);

    @Query(value = "SELECT lemma_id, site_id FROM _lemma ORDER BY frequency DESC", nativeQuery = true)
    List<ModelId> findByLemmaIdAndSiteBySiteIdOrderByFrequency(Collection<String> lemmas, Site site);

    @Override
    @Modifying
    @Query("DELETE FROM Lemma")
    void deleteAll();

    List<Lemma> findByLemma (String lemma);

    @Query(value = "SELECT DISTINCT url FROM _lemma JOIN _site ON site_id = _site.id", nativeQuery = true)
    String getUrlByPageId(Integer id);

    @Query(value = "SELECT name FROM _lemma JOIN _site ON site_id = _site.id LIMIT 1", nativeQuery = true)
    String getNameByLemmaId(Integer id);

    @Query(value = "SELECT path FROM _lemma JOIN _index ON _lemma.lemma_id = _index.id JOIN _page ON _index.page_id = _page.page_id WHERE _lemma.lemma_id = :id", nativeQuery = true)
    String getUriByLemmaId(@Param("id") Integer id);

    Lemma getById(Integer id);

    @Query(value = "SELECT lemma_rank FROM _lemma JOIN _index ON _lemma.lemma_id = _index.lemma_id WHERE id = ?1", nativeQuery = true)
    Float getLemmaRank(Integer id);

    Lemma getByLemma(String lemma);
}