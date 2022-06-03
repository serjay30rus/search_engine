package ru.kutepov.model;

import javax.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "_lemma")
@Getter
@Setter
@NoArgsConstructor
public class Lemma implements Serializable, Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lemma_id")
    private int id;
    @Column(length = 100_000)                            // Увеличиваем длину колонки
    private String lemma;                              // Нормальная форма слова;
    private int frequency;                             // Количество страниц, на которых слово встречается хотя бы один раз.
    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<Index> index;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private Site siteBySiteId;

    public Lemma(int id, String lemma, int frequency, Site siteBySiteId) {
        this.id = id;
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteBySiteId = siteBySiteId;
    }


    public Lemma(String lemma, int frequency, Site siteBySiteId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteBySiteId = siteBySiteId;
    }



    @Override
    public int compareTo(Lemma lemma) {
        return this.getFrequency() - lemma.getFrequency();
    }
}