package ru.kutepov.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.kutepov.model.Page;
import ru.kutepov.repository.PageRepository;
import java.util.List;

@Service
public class PageService {
    @Autowired
    private PageRepository pageRepository;


    public Page getById(Integer id) {
        return pageRepository.getById(id);
    }


    public List<Page> getByLemmaId(Integer id, Pageable pageable) {
        return pageRepository.getByLemmaId(id, pageable);
    }

}
