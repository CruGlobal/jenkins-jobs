"""
    Adds both Production and Staging deployment promotion definitions.
"""
import requests
import ConfigParser
import sys
import yaml


def run():
    job_data = parse_job_data()
    jobs = determine_jobs(job_data)
    for job in jobs:
        cluster = job_data[job].get('cluster', '')
        for environment in ['staging', 'production']:
            update_job_promotion(job, environment, cluster)


def parse_job_data():
    with open('jobs/jobs.yml', 'r') as stream:
        data = yaml.safe_load(stream)
        jobs = dict()
        for item in data:
            if 'project' in item:
                project = item['project']
                name = project['name']
                jobs[name] = project
    return jobs


def determine_jobs(job_data):
    all_jobs = job_data.keys()
    if len(sys.argv) == 1:
        jobs = all_jobs
    else:
        jobs = sys.argv[1:]
        if not set(jobs).issubset(set(all_jobs)):
            raise Exception("invalid jobs; should be one of: %s" % all_jobs)
    return jobs


def update_job_promotion(job, environment, cluster):
    if environment == "production":
        promotion_process = "Deploy to Production"
        icon = "star-gold"
    elif environment == "staging":
        promotion_process = "Deploy to Staging"
        icon = "star-silver"
    else:
        assert False, environment

    config = ConfigParser.RawConfigParser()
    config.read('jenkins_jobs.ini')

    content = load_config_content(cluster, environment, icon)
    print("Updating job: ", job)
    create_or_update_job(config, content, job, promotion_process)


def create_or_update_job(config, config_content, job, promotion_process):
    base_url = config.get('jenkins', 'url')
    user = config.get('jenkins', 'user')
    password = config.get('jenkins', 'password')
    url = "%s/job/%s/promotion/process/%s/config.xml" % (base_url, job, promotion_process)
    r = post_promotion_process_config(config_content, password, url, user)
    if r.status_code == 404:
        url = "%s/job/%s/promotion/createProcess?name=%s" % (base_url, job, promotion_process)
        r = post_promotion_process_config(config_content, password, url, user)
        if not r.ok:
            raise Exception("unsuccessful request:" + r.reason)
        print("Created job: %s; process: %s; Response Code: %s" % (job, promotion_process, r))
    elif not r.ok:
        raise Exception("unsuccessful request:" + r.reason)
    else:
        print("Updated job: %s; process: %s; Response Code: %s" % (job, promotion_process, r))


def load_config_content(cluster, environment, icon):
    config_to_use = "jobs/promotion-config.xml"
    with open(config_to_use, 'r') as config_file:
        content = config_file.read()
    content = content.replace("{{icon}}", icon)
    content = content.replace("{{environment}}", environment)
    content = content.replace("{{cluster}}", cluster)
    return content


def post_promotion_process_config(content, password, url, user):
    headers = {'content-type': 'application/xml'}
    return requests.post(url, data=content, headers=headers, auth=(user, password))


run()
