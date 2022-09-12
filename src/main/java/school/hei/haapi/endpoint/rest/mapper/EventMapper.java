package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;

@Component
public class EventMapper {

    public school.hei.haapi.endpoint.rest.model.Event toRest(Event event) {
        var restEvent = new school.hei.haapi.endpoint.rest.model.Event();
        restEvent.setId(event.getId());
        restEvent.setDescription(event.getDescription());
        restEvent.setPlaceName(event.getPlace_name());
        restEvent.setStartEventDatetime(event.getStart_event_datetime());
        restEvent.setEndEventDatetime(event.getEnd_event_datetime());
        restEvent.setSupervisor(school.hei.haapi.endpoint.rest.model.Event.SupervisorEnum
                        .valueOf(event.getSupervisor().toString()));
        restEvent.setStatus(school.hei.haapi.endpoint.rest.model.Event.StatusEnum
                .valueOf(event.getStatus().toString()));
        return restEvent;
    }

    public Event toDomain(school.hei.haapi.endpoint.rest.model.Event restEvent) {
        return Event.builder()
                .id(restEvent.getId())
                .description(restEvent.getDescription())
                .place_name(restEvent.getPlaceName())
                .start_event_datetime(restEvent.getStartEventDatetime())
                .end_event_datetime(restEvent.getEndEventDatetime())
                .supervisor(Event.Supervisor.valueOf(restEvent.getSupervisor().toString()))
                .status(Event.Status.valueOf(restEvent.getStatus().toString()))
                .build();
    }
}
