#!/bin/bash

cd `dirname $0`
jenkins-jobs --conf jenkins_jobs.ini update jobs/simple-jobs.yml $@
jenkins-jobs --conf jenkins_jobs.ini update jobs/app-jobs.yml $@
python update_promotable_jobs.py $@
