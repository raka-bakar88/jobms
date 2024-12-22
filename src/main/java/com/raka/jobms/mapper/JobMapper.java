package com.raka.jobms.mapper;

import com.raka.jobms.external.Company;
import com.raka.jobms.external.Review;
import com.raka.jobms.model.Job;
import com.raka.jobms.payload.JobResponse;

import java.util.List;

public class JobMapper {

    public static JobResponse mapToJobResponse(
            Job job,
            Company company,
            List<Review> reviews
    ){
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(job.getId());
        jobResponse.setTitle(job.getTitle());
        jobResponse.setLocation(job.getLocation());
        jobResponse.setDescription(job.getDescription());
        jobResponse.setMaxSalary(job.getMaxSalary());
        jobResponse.setMinSalary(job.getMinSalary());
        jobResponse.setCompany(company);
        jobResponse.setReview(reviews);
        return jobResponse;
    }
}
