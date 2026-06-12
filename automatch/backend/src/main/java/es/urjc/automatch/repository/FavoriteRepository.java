package es.urjc.automatch.repository;

import es.urjc.automatch.model.Favorite;
import es.urjc.automatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserIdAndCarId(Long userId, Long carId);
    boolean existsByUserIdAndCarId(Long userId, Long carId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.car.id = :carId")
    void deleteByCarId(@Param("carId") Long carId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
