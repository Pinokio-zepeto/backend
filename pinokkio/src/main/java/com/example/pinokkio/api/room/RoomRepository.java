package com.example.pinokkio.api.room;

import com.example.pinokkio.api.teller.Teller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * 특정 텔러와 연관된 방을 삭제합니다.
     * @param teller 방을 삭제할 텔러 객체
     *               이 메서드는 주어진 텔러와 연관된 모든 방을 데이터베이스에서 제거합니다.
     *               연관된 방이 없는 경우 아무 동작도 수행하지 않습니다.
     */
    void deleteByTeller(Teller teller);

    /**
     * 특정 텔러와 연관된 방을 찾습니다.
     *
     * @param teller 방을 찾을 텔러 객체
     * @return 텔러와 연관된 방을 포함하는 Optional 객체.
     * 방이 존재하지 않는 경우 빈 Optional을 반환합니다.
     * 이 메서드는 주어진 텔러와 연관된 방을 데이터베이스에서 조회합니다.
     * 한 텔러는 최대 하나의 방만 가질 수 있다고 가정합니다.
     */
    Optional<Room> findByTeller(Teller teller);
}