package ru.kutepov.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kutepov.model.Page;
import org.springframework.stereotype.Repository;
import ru.kutepov.model.Site;
import ru.kutepov.model.dto.interfaces.ModelId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    long countBySiteBySiteId(Site siteBySiteId);

    Optional<Page> findByPathAndSiteBySiteId(String path, Site site);

    @Override
    @Modifying
    @Query("DELETE FROM Page")
    void deleteAll();

    @Query(value = "SELECT _page.page_id, _lemma.lemma_id FROM _page " +
            "JOIN _index ON _page.page_id = _index.page_id " +
            "JOIN _lemma ON _index.lemma_id = _lemma.lemma_id", nativeQuery = true)
    Set<Integer> getPageIdByLemmaId(ModelId id);

    @Query(value = "SELECT _page.page_id, _lemma.lemma_id FROM _page " +
            "JOIN _index ON _page.page_id = _index.page_id " +
            "JOIN _lemma ON _index.lemma_id = _lemma.lemma_id", nativeQuery = true)
    Set<Integer> getPageIdByLemmaId(Integer id);

    @Query(value = "SELECT * FROM _page WHERE page_id = :id", nativeQuery = true)
    Page getById(@Param("id") Integer id);

    @Query(value = "SELECT _page.page_id, _page.code, _page.content, _page.path, _page.site_id FROM _page " +
            "JOIN _index ON _page.page_id = _index.page_id " +
            "JOIN _lemma ON _index.lemma_id = _lemma.lemma_id WHERE _lemma.lemma_id = :id", nativeQuery = true)
    List<Page> getByLemmaId(@Param("id") Integer id, Pageable pageable);
}
