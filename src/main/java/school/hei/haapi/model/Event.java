package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Entity
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"event\"")
public class Event implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    //@NotNull(message = "Description is mandatory")
    private String description;

    //@NotNull(message = "Place is mandatory")
    private String place_name;

    //@NotNull(message = "Start datetime is mandatory")
    private Instant start_event_datetime;

    //@NotNull(message = "End datetime is mandatory")
    private Instant end_event_datetime;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private Supervisor supervisor;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private Status status;

    public Instant getStart_event_datetime() {
        return start_event_datetime.truncatedTo(ChronoUnit.MILLIS);
    }

    public Instant getEnd_event_datetime() {
        return end_event_datetime.truncatedTo(ChronoUnit.MILLIS);
    }
    public enum Supervisor {
        TEACHER, ADMINISTRATOR
    }

    public enum Status {
        END, EXPECTED, CANCELLED
    }
}