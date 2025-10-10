package ese.trab01.Tickets.repository;

import ese.trab01.Tickets.model.Reservation;
import ese.trab01.Tickets.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
              select coalesce(sum(r.quantity),0)
              from Reservation r
              where r.eventId = :eventId
                and r.status = :status
            """)
    long sumByEventAndStatus(@Param("eventId") Long eventId,
                             @Param("status") ReservationStatus status);

    @Query("""
              select coalesce(sum(r.quantity),0)
              from Reservation r
              where r.eventId = :eventId
                and r.status = :status
                and r.expiresAt > CURRENT_TIMESTAMP
            """)
    long sumActiveByEventAndStatus(@Param("eventId") Long eventId,
                                   @Param("status") ReservationStatus status);
}
