package school.hei.haapi.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PlaceApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = EventIT.ContextInitializer.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor
class EventIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @Autowired
    private EventMapper eventMapper;
    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }

    public static Event event1() {
        Event event = new Event();
        event.setId("event1_id");
        event.setDescription("Examen prog1");
        event.setPlaceName("HEI Ivandry");
        event.setStartEventDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        event.setEndEventDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        event.setSupervisor(Event.SupervisorEnum.TEACHER);
        event.setStatus(Event.StatusEnum.END);
        return event;
    }

    public static Event event2() {
        Event event = new Event();
        event.setId("event2_id");
        event.setDescription("Examen sys2");
        event.setPlaceName("HEI Ivandry");
        event.setStartEventDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        event.endEventDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        event.setSupervisor(Event.SupervisorEnum.TEACHER);
        event.setStatus(Event.StatusEnum.END);

        return event;
    }

    public static Event someCreatableEvent() {
        Event event = new Event();
        event.setId("EVT-"+String.valueOf(randomUUID()));
        event.setDescription("Some description");
        event.setPlaceName("Aliance Française");
        event.setStartEventDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        event.setEndEventDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        event.supervisor(Event.SupervisorEnum.ADMINISTRATOR);
        event.setStatus(Event.StatusEnum.EXPECTED);
        return event;
    }
    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        PlaceApi api = new PlaceApi(anonymousClient);
        assertThrowsForbiddenException(api::getAllEvent);
    }

    @Test
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);
        PlaceApi api = new PlaceApi(anonymousClient);

        List<Event> toWrite = new ArrayList<>();
        toWrite.add(event1());
        assertThrowsForbiddenException(() -> api.createEventOrUpdate(toWrite));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(student1Client);

        List<Event> actualEvent = api.getAllEvent();
        Event actual1 = api.getEventById(EVENT1_ID);

        Assertions.assertEquals(event1(), actualEvent.get(0));
        Assertions.assertEquals(event1(), actual1);
    }

    @Test
    void student_write_ko() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(student1Client);

        assertThrowsForbiddenException(() -> api.createEventOrUpdate(List.of(
                someCreatableEvent(),
                someCreatableEvent()
        )));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        List<Event> actualEvent = api.getAllEvent();
        Event actual1 = api.getEventById(EVENT1_ID);

        Assertions.assertEquals(event1(), actualEvent.get(0));
        Assertions.assertEquals(event1(), actual1);
    }
    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        PlaceApi api = new PlaceApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createEventOrUpdate(List.of()));
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(student1Client);

        List<Event> actualEvent = api.getAllEvent();
        Event actual1 = api.getEventById(EVENT1_ID);

        Assertions.assertEquals(event1(), actualEvent.get(0));
        Assertions.assertEquals(event1(), actual1);
    }


    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        Event toCreate3 = someCreatableEvent();
        Event toCreate4 = someCreatableEvent();

        PlaceApi api = new PlaceApi(manager1Client);
        List<Event> created = api.createEventOrUpdate(List.of(toCreate3, toCreate4));

        Assertions.assertEquals(2, created.size());
        Assertions.assertEquals(created.get(0),null);
        //assertTrue(created.contains(toCreate3));
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(manager1Client);
        List<Event> toUpdate = api.createEventOrUpdate(List.of(
                someCreatableEvent(),
                someCreatableEvent()));
        Event toUpdate0 = toUpdate.get(0);
        toUpdate0.setId("event1_id");
        toUpdate0.setDescription("A new description zero");
        toUpdate0.setStartEventDatetime(Instant.now());
        toUpdate0.setEndEventDatetime(Instant.now());
        toUpdate0.setSupervisor(Event.SupervisorEnum.TEACHER);
        toUpdate0.setStatus(Event.StatusEnum.EXPECTED);
        Event toUpdate1 = toUpdate.get(1);
        toUpdate1.setDescription("A new description one");

        List<Event> updated = api.createEventOrUpdate(toUpdate);

        Assertions.assertEquals(2, updated.size());
        assertTrue(updated.contains(toUpdate0));
        assertTrue(updated.contains(toUpdate1));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();
        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
