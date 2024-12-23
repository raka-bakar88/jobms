This is an Company review application that uses Distributed/Microservices architecture that provides a functionality to review a company based on a job. The containeraized versions of this applications is available in [my docker hub account ](https://hub.docker.com/u/rkabkr). It uses Docker and Kubernetes to create and maintain the Images, Pods etc. 

Link to other Microservices repository:
- [Reviewms](https://github.com/raka-bakar88/reviewms)
- [Companyms](https://github.com/raka-bakar88/companyms)
- [Eureka Service Registration Server ms](https://github.com/raka-bakar88/eurekams)
- [Config Server ms](https://github.com/raka-bakar88/configserverms)
- [Api gateway ms](https://github.com/raka-bakar88/gatewayms)


**Techstack**
- Spring Boot(Java 17)
- PostgreSQL database
- RabbitMQ
- Spring Eureka
- Kubernetes
- Spring Config Server
- Zipkin
- Spring Actuator
- OpenFeign
- Java Persistence Api (JPA)
- Lombok
- Model Mapper
- Maven

  **Architecture**

  ![alt text](https://github.com/raka-bakar88/jobms/blob/main/microservice%20architecture%20diagram.png)

This Job microservice works together with Company and Review Microservice application. Each Microservice has its own PostgresSQL database. The communication between Microservices is handled by Queues Messages tool using RabbitMQ. Configuration data for each Microservice is supported by Spring Config Server, so before each Microservice is run, they will fetch the config data from config server. Spring Cloud Eureka Server is used to discover, register and maintain status of all Microservices applications. Zipkin is also used to monitor the performance of all Microservices.

**API Flow**

There is only one API that is exposed to the clients with the help of API Gateway, which is the Review Api. When a client creates a Review, it will send the data to Review Microservice. Then, the review data will be saved into the review database, while at the same time, review microservice will send the data of job and company to its corresponding microservice and processed there.

**Database**

Each of the Microservice application uses its own PostgresSQL as the database. However, they are connected to each other. below is the Entity Relationship Diagram of the database
![alt text](https://github.com/raka-bakar88/jobms/blob/main/JobApp%20ER%20Diagram.png)

**JOB Endpoints**
![alt text](https://github.com/raka-bakar88/jobms/blob/main/job%20ms%20api%20list.png)

**Example Class**


  Below is an example of code from Class JobServiceImpl
   ````
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

    private CompanyClient companyClient;

    private ReviewClient reviewClient;

    @Override
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

   ````
