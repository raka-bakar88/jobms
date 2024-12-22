package com.raka.jobms.payload;

import com.raka.jobms.external.Company;
import com.raka.jobms.external.Review;
import lombok.Data;

import java.util.List;

@Data
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String minSalary;
    private String maxSalary;
    private String location;
    private Company company;
    private List<Review> review;
}
