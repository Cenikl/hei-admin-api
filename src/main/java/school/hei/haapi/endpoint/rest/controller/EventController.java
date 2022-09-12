package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.service.EventService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class EventController {
    private final EventService eventService;

    private EventMapper eventMapper;

    @GetMapping("/event")
    public List<Event> getEvent() {
        return eventService.getAll().stream()
                .map(eventMapper::toRest)
                .collect(toUnmodifiableList());
    }

    @GetMapping("/event/{event_id}")
    public Event getEventById (@PathVariable String event_id) {
        return eventMapper.toRest(eventService.getEventById(event_id));
    }

    @PutMapping(value = "/event")
    public List<Event> createEventOrUpdate(@RequestBody List<Event> toWrite) {
        var saved = eventService.saveAll(toWrite.stream()
                .map(eventMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(eventMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
