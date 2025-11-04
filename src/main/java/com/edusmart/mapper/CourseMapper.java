package com.edusmart.mapper;

import com.edusmart.dto.CourseResponse;
import com.edusmart.entity.Course;

import java.util.Collections;

public class CourseMapper
{
	public static CourseResponse toResponse(Course course) {
	    return new CourseResponse(
	            course.getId(),
	            course.getTitle(),
	            course.getDescription(), // if you added a description field
	            course.getTeacher() != null ? course.getTeacher().getName() : null,
	            course.getStudent() != null ? course.getStudent().getName() : null
	    );
	}
}