package com.example.pinokkio.api.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {

    private int age;
    private String gender;
    private boolean isFace;
    private String encryptedEmbedding;

    // equals 메서드
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnalysisResult that = (AnalysisResult) o;
        return age == that.age &&
                isFace == that.isFace &&
                Objects.equals(gender, that.gender) &&
                Objects.equals(encryptedEmbedding, that.encryptedEmbedding);
    }

    // hashCode 메서드
    @Override
    public int hashCode() {
        return Objects.hash(age, gender, isFace, encryptedEmbedding);
    }

}
