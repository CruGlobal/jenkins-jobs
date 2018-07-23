Cru Jenkins Job Configuration
=============================

This project contains configuration for all jobs running on jenkins-prod.cru.org.

How to Add or Update a Job
----------------
* Create a feature branch and add your job definition to the appropriate yaml file.
  * app-jobs.yml --> Ruby on Rails, PHP, openresty and react apps running on ECS/Docker
  * ep-jobs.yml --> ElasticPath jobs
  * ep-promotable-jobs.yml --> ElasticPath jobs that use the promotable jobs plugin
  * java-docker-jobs.yml --> Java apps running on ECS/docker
  * java-jobs.yml --> Java apps running in a non-containerized environment
  * simple-jobs.yml --> Docker image build jobs and other misc jobs
  * pipeline-jobs.yml --> Multibranch pipeline jobs
* Test your work
  * Push your feature branch to github
  * Request Mike Albert to turn on Jenkins Lab, if jenkins-lab.cru.org gives you a 503.
  * Manually tweak the "Execute Shell" step of create-jenkins-job's [config][1]
    to use your feature branch.
    (See the comment there for the syntax.)
  * Run create-jenkins-job to make your job changes, and run your new or updated job.
  * Repeat until bugs are gone
* Issue a pull request against master and request a code review from Mike Albert or Matt Drees
* After approval has been given, merge to master and delete the feature branch
* To create your new job on Jenkins, run the "create-jenkins-jobs" [job][2] on Jenkins Production.


Pipeline Library
----------------
This repository also serves as a Jenkins Pipeline shared library.
As such it follows the layout requirements:
 * global variables (which can act like steps) go in `vars`
 * classes (which can hold state or just encapsulate complexity) go in `src`
 * tests go in `test`

[1]: https://jenkins-lab.cru.org/job/create-jenkins-jobs/configure
[2]: https://jenkins-prod.cru.org/job/create-jenkins-jobs/
