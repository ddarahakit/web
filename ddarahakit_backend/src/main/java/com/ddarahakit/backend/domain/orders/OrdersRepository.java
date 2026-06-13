package com.ddarahakit.backend.domain.orders;


import com.ddarahakit.backend.domain.orders.model.Orders;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByIdxAndUserAndPaidFalse(Long ordersIdx, User user);
    Optional<Orders> findByIdxAndUser(Long ordersIdx, User user);
    List<Orders> findByUserAndPaidTrue(User user);

    /**
     * 결제 내역 페이징 조회(결제 완료 주문). items+course fetch join 으로 목록 N+1 방지.
     */
    @Query(value = """
            SELECT DISTINCT o FROM Orders o
            JOIN FETCH o.items i
            JOIN FETCH i.course
            WHERE o.user = :user AND o.paid = true
            ORDER BY o.idx DESC
            """,
            countQuery = "SELECT COUNT(o) FROM Orders o WHERE o.user = :user AND o.paid = true")
    Page<Orders> findPaidPageByUser(@Param("user") User user, Pageable pageable);

    /**
     * 영수증 조회용 단건(본인 결제 완료 주문, items+course fetch join).
     */
    @Query("""
            SELECT DISTINCT o FROM Orders o
            JOIN FETCH o.items i
            JOIN FETCH i.course
            WHERE o.idx = :ordersIdx AND o.user = :user AND o.paid = true
            """)
    Optional<Orders> findReceipt(@Param("ordersIdx") Long ordersIdx, @Param("user") User user);

    /**
     * 결제 검증용 미결제 주문 조회 (N+1 방지: items + course 를 fetch join).
     */
    @Query("""
            SELECT DISTINCT o FROM Orders o
            JOIN FETCH o.items i
            JOIN FETCH i.course
            WHERE o.idx = :ordersIdx AND o.user = :user AND o.paid = false
            """)
    Optional<Orders> findUnpaidWithItemsForVerify(@Param("ordersIdx") Long ordersIdx, @Param("user") User user);

    /**
     * 무료(0원) 주문 확정 조회용 미결제 주문 (items + course fetch join).
     * verify 와 동일하게 본인 미결제 주문만 대상으로 한다.
     */
    @Query("""
            SELECT DISTINCT o FROM Orders o
            JOIN FETCH o.items i
            JOIN FETCH i.course
            WHERE o.idx = :ordersIdx AND o.user = :user AND o.paid = false
            """)
    Optional<Orders> findUnpaidWithItemsForFreeComplete(@Param("ordersIdx") Long ordersIdx, @Param("user") User user);

    /**
     * 결제 확정의 멱등 처리.
     * paid=false 인 경우에만 paid=true 로 전이시키고 영향 행 수를 반환한다.
     * 동시/중복 verify 가 들어와도 단 한 번만 1 을 반환(원자적 조건부 UPDATE)하여 이중 확정을 막는다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Orders o SET o.paid = true, o.paymentId = :paymentId WHERE o.idx = :ordersIdx AND o.paid = false")
    int markPaidIfUnpaid(@Param("ordersIdx") Long ordersIdx, @Param("paymentId") String paymentId);

    /** 통계용: 결제 완료(환불 제외) 주문을 보유한 고유 수강생 수. */
    @Query("SELECT COUNT(DISTINCT o.user.idx) FROM Orders o WHERE o.paid = true AND o.refunded = false")
    long countDistinctPaidStudents();
}
