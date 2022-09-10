package school.hei.haapi.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.endpoint.rest.model.Course;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }
    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setRef("PROG2");
        course.setName("Name of course one");
        course.setCredits(5000);
        course.setTotalHours(25);
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setRef("WEB1");
        course.setName("Name of course two");
        course.setCredits(2300);
        course.setTotalHours(24);
        return course;
    }
    public static Course someCreatableCourse() {
        Course course = new Course();
        course.setName("Basic name");
        course.setRef("" + randomUUID());
        course.setCredits(10);
        return course;
    }
    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        TeachingApi api = new TeachingApi(anonymousClient);
        assertThrowsForbiddenException(api::getCourses);
    }

    @Test
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);
        TeachingApi api = new TeachingApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        List<Course> actualCourse = api.getCourses();
        Course actual1 = api.getCourseById(COURSE1_ID);

        assertEquals(course1(), actual1);
        assertTrue(actualCourse.contains(course1()));
        assertTrue(actualCourse.contains(course2()));
    }
    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teacher1Client);

        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        Course test0 = api.createOrUpdateCourses(someCreatableCourse());
        Course test1 = api.createOrUpdateCourses(someCreatableCourse());

        Course created3 = test0;
        assertTrue(isValidUUID(created3.getId()));
        test0.setId(created3.getId());
        assertNotNull(created3.getRef());
        test0.setRef(created3.getRef());
        assertEquals(created3, test0);
        //
        Course created4 = test1;
        assertTrue(isValidUUID(created4.getId()));
        test1.setId(created4.getId());
        assertNotNull(created4.getRef());
        test1.setRef(created4.getRef());
        assertEquals(created4, test1);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        Course test0 = api.createOrUpdateCourses(someCreatableCourse());
        Course test1 = api.createOrUpdateCourses(someCreatableCourse());

        Course toUpdate0 = test0;
        Course toUpdate1 = test1;
        toUpdate0.setName("A new name zero");
        toUpdate1.setName("A new name one");

        api.createOrUpdateCourses(toUpdate0);
        api.createOrUpdateCourses(toUpdate1);

        assertTrue(test0.getCredits() == 10 );
        assertTrue(test1.getCredits() == 10 );
        assertEquals(test0.getName(),"A new name zero");
        assertEquals(test1.getName(), "A new name one");
        assertEquals(test0.getRef(), toUpdate0.getRef());
        assertEquals(test1.getRef(), toUpdate1.getRef());


    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
