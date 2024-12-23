This is a Company Review application that leverages a Distributed/Microservices Architecture to allow users to review companies based on job experiences. The containerized  versions of this applications is available in [my docker hub account ](https://hub.docker.com/u/rkabkr). It employs Docker and Kubernetes for creating and managing images, pods, and other resources.

Link to other Microservices repository:
- [Reviewms](https://github.com/raka-bakar88/reviewms)
- [Companyms](https://github.com/raka-bakar88/companyms)
- [Eureka Service Registration Server ms](https://github.com/raka-bakar88/eurekams)
- [Config Server ms](https://github.com/raka-bakar88/configserverms)
- [Api gateway ms](https://github.com/raka-bakar88/gatewayms)


**Techstack**
- Backend Framework: Spring Boot (Java 17)
- Database: PostgreSQL
- Messaging System: RabbitMQ
- Service Discovery: Spring Eureka
- Orchestration & Scaling: Kubernetes
- Configuration Management: Spring Config Server
- Distributed Tracing: Zipkin
- Health Monitoring: Spring Actuator
- Service-to-Service Communication: OpenFeign
- ORM Framework: Java Persistence API (JPA)
- Utilities: Lombok, Model Mapper
- Build Tool: Maven

  **Architecture**

  ![alt text](https://github.com/raka-bakar88/jobms/blob/main/microservice%20architecture%20diagram.png)

This application follows a Microservices Architecture where each service operates independently with its own database and interacts with others through messaging and service discovery mechanisms.

Microservices Overview:

1. Job Microservice works in conjunction with Company and Review microservices.

2. Each microservice uses its own PostgreSQL database.

3. Communication between microservices is handled through RabbitMQ message queues.

4. Configuration data for each microservice is managed by the Spring Config Server. Before launching, each service fetches its configuration settings.

5. Spring Cloud Eureka Server is used for service discovery, registration, and health monitoring.

6. Zipkin monitors and traces the performance of all microservices.

**API Flow**

The application exposes only one API endpoint to the client via the API Gateway, simplifying client-side interactions.
Review API Flow:

1. A client submits a review through the Review API.

2. The Review Microservice processes the data and saves it in the review database.

3. Simultaneously, the review microservice sends related job and company data to their respective microservices for further processing.

**Database**

Each microservice operates its own PostgreSQL database, ensuring data isolation and scalability.

Database Design:
Despite having separate databases, these microservices maintain logical connections to facilitate data consistency. The Entity Relationship Diagram (ERD) below illustrates the relationships among entities across database. below is the Entity Relationship Diagram of the database
![alt text](https://github.com/raka-bakar88/jobms/blob/main/JobApp%20ER%20Diagram.png)

Key Features

1. Scalability: Easily scales up or down with Kubernetes orchestration.

2. Fault Tolerance: Ensures reliability through distributed architecture.

3. Configuration Management: Centralized configuration storage via Spring Config Server.

4. Monitoring and Tracing: Real-time performance monitoring with Zipkin.

5. Service Discovery: Automatic service registration and lookup using Eureka.

6. Message Queues: Reliable asynchronous communication through RabbitMQ

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
