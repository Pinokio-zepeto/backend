package com.example.pinokkio.api.room;

import com.example.pinokkio.api.teller.Teller;
import com.example.pinokkio.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "room")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Room extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "room_id")
    private UUID roomId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teller_id")
    private Teller teller;

    @ColumnDefault("0")
    private Integer numberOfCustomers;

    @Builder
    public Room(UUID roomId, Teller teller, Integer numberOfCustomers) {
        this.roomId = roomId;
        this.teller = teller;
        this.numberOfCustomers = numberOfCustomers;
    }

    public void updateNumberOfCustomers(Integer numberOfCustomers) {
        this.numberOfCustomers = numberOfCustomers;
    }
}
