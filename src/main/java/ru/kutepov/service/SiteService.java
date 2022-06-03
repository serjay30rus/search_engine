package ru.kutepov.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kutepov.model.Site;
import ru.kutepov.model.SiteStatusType;
import ru.kutepov.repository.SiteRepository;
import java.sql.Timestamp;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private SiteRepository siteRepository;

    @Getter
    @Setter
    private boolean isIndexingStarted;

    @Getter
    @Setter
    private boolean indexingStopFlag;


    @Transactional
    public Site saveSiteIfNotExist(Site site) {
        Optional<Site> siteOptional = siteRepository.findByName(site.getName());
        return siteOptional.orElseGet(() -> siteRepository.save(site));
    }


    @Transactional
    public void updateStatus(Site site, SiteStatusType statusType) {
        site.setStatus(statusType);
        siteRepository.save(site);
    }

    @Transactional
    public void updateErrorMessage(Site site, String error) {
        site.setLastError(error);
        siteRepository.save(site);
    }


    public Site getById(Integer id) {
        return siteRepository.getById(id);
    }
}
