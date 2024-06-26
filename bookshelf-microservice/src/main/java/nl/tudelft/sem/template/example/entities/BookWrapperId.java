package nl.tudelft.sem.template.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookWrapperId implements Serializable {
    private UUID bookId;
    private UUID userId;
}
