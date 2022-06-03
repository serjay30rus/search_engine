package ru.kutepov.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kutepov.model.Lemma;
import ru.kutepov.model.Site;
import ru.kutepov.model.dto.interfaces.ModelId;
import ru.kutepov.repository.LemmaRepository;
import java.util.List;
import java.util.Set;

@Service
public class LemmaService {
    @Autowired
    private LemmaRepository lemmaRepository;


    String  getUrlByPageId(Integer id) {
        return lemmaRepository. getUrlByPageId(id);
    }

    String getNameByLemmaId(Integer id) {
        return lemmaRepository.getNameByLemmaId(id);
    }


    String getUriByLemmaId(Integer id) {
        return lemmaRepository.getUriByLemmaId(id);
    }


    public Lemma getById(Integer id) {
        return lemmaRepository.getById(id);
    }


    public Float getLemmaRank(Integer id) {
        return lemmaRepository.getLemmaRank(id);
    }


    public Lemma getByLemma(String lemma) {
        return lemmaRepository.getByLemma(lemma);
    }
}
