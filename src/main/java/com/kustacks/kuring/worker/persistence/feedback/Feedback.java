package com.kustacks.kuring.worker.persistence.feedback;

import com.kustacks.kuring.worker.persistence.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "content", length = 256, nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = true)
    private User user;

    @Builder
    public Feedback(String content, User user) {
        this.content = content;
        this.user = user;
    }
}
