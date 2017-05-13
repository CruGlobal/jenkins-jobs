#!/bin/bash

cd `dirname $0`
jenkins-jobs --conf jenkins_jobs.ini update jobs/jobs.yml $@
jenkins-jobs --conf jenkins_jobs.ini update jobs/mobile-jobs.yml $@
python update_promotion_process.py $@
