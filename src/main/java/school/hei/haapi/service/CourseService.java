package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.model.*;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    public Course getById(String courseId) {return courseRepository.getById(courseId);}
    public List<Course> getAll() {
        return courseRepository.findAll();
    }
    public Course saveOne(Course course) { return courseRepository.save(course);}

}
