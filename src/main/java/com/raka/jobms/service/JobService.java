package com.raka.jobms.service;


import com.raka.jobms.payload.JobDTO;
import com.raka.jobms.payload.JobResponse;

import java.util.List;

public interface JobService {
    List<JobResponse> findAll();
    void createJob(JobDTO job);

    JobResponse findJobById(Long jobId);

    String deleteJob(Long jobId);

    Boolean updateJob(Long jobId, JobDTO job);
}
