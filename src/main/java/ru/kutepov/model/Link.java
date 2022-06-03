package ru.kutepov.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "_link")
@Data
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;                      // Номер ID
    private String path;                 // Путь
    private int code;                    // Код ответа
    @Column(length = 100_000)            // Увеличиваем длину колонки
    private String content;              // Содержимое

    public Link() {                      // Пустой конструктор
    }

    public Link(int id, String path, int code, String content) {
        this.id = id;
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
