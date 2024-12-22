package com.raka.jobms.service;


import com.raka.jobms.clients.CompanyClient;
import com.raka.jobms.clients.ReviewClient;
import com.raka.jobms.exceptions.ResourceNotFoundException;
import com.raka.jobms.external.Company;
import com.raka.jobms.external.Review;
import com.raka.jobms.mapper.JobMapper;
import com.raka.jobms.model.Job;
import com.raka.jobms.payload.JobDTO;
import com.raka.jobms.payload.JobResponse;
import com.raka.jobms.repository.JobRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    public JobServiceImpl(JobRepository jobRepository, CompanyClient companyClient,
                          ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }

    @Autowired
    JobRepository jobRepository;

    @Autowired
    ModelMapper modelMapper;

    // used to communicate between microservices
    @Autowired
    RestTemplate restTemplate;

    private CompanyClient companyClient;

    private ReviewClient reviewClient;

    @Override
//    @CircuitBreaker(name = "companyBreaker",
//    fallbackMethod = "companyBreakerFallback")
    @Retry(name = "companyBreaker",fallbackMethod = "companyBreakerFallback"
    )
    public List<JobResponse> findAll() {
        List<Job> jobs = jobRepository.findAll();
        List<JobResponse> jobResponses = new ArrayList<>();
        return jobs.stream().map(this::convertToDTO).toList();
    }
    // method name must be the same with fallbackmethod value above
    public List<String> companyBreakerFallback(Exception e){
        List<String> list = new ArrayList<>();
        list.add("this is an example of an information returned on fallback mechanism");
        return list;
    }

    /**
     * Mapper class to convert Job to JobWithCompanyDTO
     *
     * @param job instance of type Job
     * @return JobWithCompanyDTO
     */
    private JobResponse convertToDTO(Job job) {
        // an api to get data from Company microservice
        // getForObject is more useful when the return type is already known
        Company company = companyClient.getCompany(job.getCompanyId());
        // exchange is more useful when the return type is generic
        List<Review> reviews = reviewClient.getReviews(company.getCompanyId());
        return JobMapper.mapToJobResponse(job, company, reviews);
    }

    @Override
    public void createJob(JobDTO job) {
        Job newJob = modelMapper.map(job, Job.class);
        Company company = companyClient.getCompany(job.getCompanyId());
        if (company == null) {
            throw new ResourceNotFoundException("Company", "Company Id", job.getCompanyId());
        }
        jobRepository.save(newJob);
    }

    @Override
    public JobResponse findJobById(Long jobId) {
        Job savedJob = jobRepository.findById(jobId).orElseThrow(
                () -> new ResourceNotFoundException("Job", "JobId", jobId)
        );
        return convertToDTO(savedJob);
    }

    @Override
    public String deleteJob(Long jobId) {
        Job savedJob = jobRepository.findById(jobId).orElseThrow(
                () -> new ResourceNotFoundException("Job", "JobId", jobId)
        );
        jobRepository.delete(savedJob);
        return "Delete is successfull";
    }

    @Override
    public Boolean updateJob(Long jobId, JobDTO job) {
        Job savedJob = jobRepository.findById(jobId).orElseThrow(
                () -> new ResourceNotFoundException("Job", "JobId", jobId)
        );
        savedJob.setTitle(job.getTitle());
        savedJob.setDescription(job.getDescription());
        savedJob.setLocation(job.getLocation());
        savedJob.setMaxSalary(job.getMaxSalary());
        savedJob.setMinSalary(job.getMinSalary());
        jobRepository.save(savedJob);
        return false;
    }
}
