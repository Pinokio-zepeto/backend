//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.pinokkio.api.teller;

import com.example.pinokkio.api.pos.code.Code;
import com.example.pinokkio.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "teller")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Teller extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "teller_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_id")
    private Code code;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;


    @Builder
    public Teller(Code code, String email, String password) {
        this.code = code;
        this.email = email;
        this.password = password;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

}
