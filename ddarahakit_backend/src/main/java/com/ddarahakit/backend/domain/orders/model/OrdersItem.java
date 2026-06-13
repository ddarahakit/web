package com.ddarahakit.backend.domain.orders.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.course.model.Course;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "orders_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"orders_idx", "course_idx"})
        }
)
public class OrdersItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "orders_idx")
    private Orders orders;

    @ManyToOne
    @JoinColumn(name = "course_idx")
    private Course course;
}
