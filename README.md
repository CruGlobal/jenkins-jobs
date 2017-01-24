Cru Jenkins Job Configuration
=============================

This project contains configuration for all jobs running on jenkins-prod.cru.org.

How to Add a Job
----------------
* Create a feature branch and add your job definition to the appropriate yaml file.
 * app -jobs.yml --> Ruby on Rails, PHP, openresty and react apps running on ECS/Docker
 * ep-jobs.yml --> ElasticPath jobs
 * java-docker-jobs.yml --> Java apps running on ECS/docker
 * java-jobs.yml --> Java apps running in a non-containerized environment
 * simple-jobs.yml --> Docker image build jobs and other misc jobs
* Issue a pull request against master and assign to Mike Albert or Matt Drees
* After approval has been given, merge to master and delete the feature branch
* To create your new job on Jenkins, run the "create-jenkins-jobs" job on Jenkins Production. **Note: Contact Matt Drees or Karl Kranich before executing this job as it will affect existing ElasticPath jobs**
