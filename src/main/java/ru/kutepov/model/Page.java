package ru.kutepov.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "_page")
@Setter
@Getter
@NoArgsConstructor
public class Page implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private int id;
    private String path;                            // Адрес страницы от корня сайта (должен начинаться со слеша);
    private int code;                               // Код ответа, полученный при запросе страницы
    @Column(length = 100_000)
    private String content;                         // Контент страницы (HTML-код).
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<Index> index;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private Site siteBySiteId;


    public Page(int id, String path, int code, String content, Site siteBySiteId) {
        this.id = id;
        this.path = path;
        this.code = code;
        this.content = content;
        this.siteBySiteId = siteBySiteId;
    }


}
