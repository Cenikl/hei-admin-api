package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.repository.EventRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> getAll () {
        return eventRepository.findAll();
    }

    public List<Event> saveAll (List<Event> newEvent) {
        return eventRepository.saveAll(newEvent);
    }

    public Event getEventById (String id) {
        return eventRepository.getById(id);
    }
}