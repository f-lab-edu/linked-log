package flab.Linkedlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.valueOf(0);

    private LocalDateTime deletedAt;


    @Builder
    public Post(Category category, Member member, String title,
                String content, int views, BigDecimal price) {
        this.category = category;
        this.member = member;
        this.title = title;
        this.content = content;
        this.views = views;
        this.price = price != null ? price : BigDecimal.ZERO;
    }


    public void incrementViews() {
        this.views++;
    }
}
