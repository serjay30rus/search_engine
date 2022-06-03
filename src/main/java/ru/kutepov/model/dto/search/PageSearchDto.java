package ru.kutepov.model.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class PageSearchDto implements Comparable<PageSearchDto> {
    @Id
    private int id;
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;

    @Override
    public int compareTo(PageSearchDto o) {
        return (int) ((o.getRelevance() - this.getRelevance()) * 1000);
    }
}
