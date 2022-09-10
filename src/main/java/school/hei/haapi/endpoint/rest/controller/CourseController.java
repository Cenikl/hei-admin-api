package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private CourseService courseService;
    private CourseMapper courseMapper;

    @GetMapping("/courses")
    public List<Course> getAllCourses(){
        return courseService.getAll();
    }
    @GetMapping("/courses/{courseId}")
    public Course getCourseById(
            @PathVariable String courseId){
        return courseMapper.toRest(courseService.getById(courseId));
    }
    @PutMapping("/courses")
    public Course createOrUpdateCourse(@RequestBody Course toWrite) {;
        return courseService.saveOne(toWrite);
    }
}
