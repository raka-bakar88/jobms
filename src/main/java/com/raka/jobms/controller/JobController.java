package com.raka.jobms.controller;

import com.raka.jobms.payload.JobDTO;
import com.raka.jobms.payload.JobResponse;
import com.raka.jobms.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> findAll() {
        List<JobResponse> jobs = jobService.findAll();
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }



    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> findJobById(@PathVariable Long jobId) {
        JobResponse job = jobService.findJobById(jobId);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> createJob(@RequestBody JobDTO jobDTO) {
        jobService.createJob(jobDTO);
        return new ResponseEntity<>("Job is added successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId){
        String status = jobService.deleteJob(jobId);
            return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<Boolean> updateJob(@PathVariable Long jobId,
                                            @RequestBody JobDTO job){
        Boolean status = jobService.updateJob(jobId, job);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
