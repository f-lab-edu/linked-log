package flab.Linkedlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    private LocalDateTime deletedAt;

    @Builder
    public Category(String name) {
        this.name = name;
    }

    public void restoreAfterDeletion() {
        deletedAt = null;
    }

    public void markAsDeleted() {
        deletedAt = LocalDateTime.now();
    }
}
