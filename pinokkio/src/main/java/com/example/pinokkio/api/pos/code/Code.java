package com.example.pinokkio.api.pos.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "code")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Code {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "code_id")
    private UUID id;

    @Column(nullable = false)
    private String name;

    public Code(String name) {
        this.name = name;
    }

}
