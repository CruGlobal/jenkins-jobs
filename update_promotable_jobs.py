"""
    Adds both Production and Staging deployment promotion definitions to
    jobs defined in jobs/app-jobs.yml and jobs/java-app-jobs.yml

    Adds custom promotion definitions to ep jobs in jobs/ep-promotable-jobs.yml
"""
import ConfigParser

import requests
import sys
import yaml


def run():
    job_data = parse_job_data(['jobs/app-jobs.yml','jobs/java-docker-jobs.yml'])
    try:
       jobs = determine_jobs(job_data)
       xml_config_file = "jobs/promotion-config.xml"
       for job in jobs:
           cluster = job_data[job].get('cluster', '')
           for environment in ['staging', 'production']:
               deploy_job_key = 'deploy-%s-job' % environment
               deploy_job = job_data[job].get(deploy_job_key, 'deploy-ecs')
               update_job_promotion(job, environment, cluster, xml_config_file, deploy_job)
    finally:
        job_data = parse_job_data(['jobs/ep-promotable-jobs.yml'])
        jobs = determine_jobs(job_data)
        xml_config_file = "jobs/ep-promotion-config.xml"
        for job in jobs:
            cluster = job_data[job].get('cluster', '')
            environment = job_data[job].get('environment', 'staging')
            update_job_promotion(job, environment, cluster, xml_config_file, 'deploy-ecs')

def parse_job_data(files):
    jobs = dict()
    for file in files:
        with open(file, 'r') as stream:
            data = yaml.safe_load(stream)
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


def update_job_promotion(job, environment, cluster, xml_config_file, deploy_job):
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

    content = load_config_content(cluster, environment, icon, xml_config_file, deploy_job)
    print("Updating job: ", job)
    create_or_update_job(config, content, job, promotion_process)


def create_or_update_job(config, config_content, job, promotion_process):
    base_url = config.get('jenkins', 'url')
    user = config.get('jenkins', 'user')
    password = config.get('jenkins', 'password')
    url = "%s/job/%s/promotion/process/%s/config.xml" % (base_url, job, promotion_process)
    r = post_promotion_process_config(config_content, password, base_url, url, user)
    if r.status_code == 404:
        url = "%s/job/%s/promotion/createProcess?name=%s" % (base_url, job, promotion_process)
        r = post_promotion_process_config(config_content, password, base_url, url, user)
        if not r.ok:
            raise Exception("unsuccessful request:" + r.reason)
        print("Created job: %s; process: %s; Response Code: %s" % (job, promotion_process, r))
    elif not r.ok:
        raise Exception("unsuccessful request:" + r.reason)
    else:
        print("Updated job: %s; process: %s; Response Code: %s" % (job, promotion_process, r))


def load_config_content(cluster, environment, icon, xml_config_file, deploy_job='deploy-ecs'):
    config_to_use = xml_config_file
    with open(config_to_use, 'r') as config_file:
        content = config_file.read()
    content = content.replace("{{icon}}", icon)
    content = content.replace("{{environment}}", environment)
    content = content.replace("{{cluster}}", cluster)
    content = content.replace("{{deploy-job}}", deploy_job)
    return content


def post_promotion_process_config(content, password, base_url, url, user):
    headers = {'content-type': 'application/xml'}
    headers.update(get_crumb_header(password, base_url, user))
    return requests.post(url, data=content, headers=headers, auth=(user, password))


def get_crumb_header(password, base_url, user):
    delimiter = ":"
    url = base_url + "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\"" + delimiter + "\",//crumb)"
    r = requests.get(url, auth=(user, password))
    if not r.ok:
        if r.status_code == 404:
            print("crumb unavailable; jenkins probably does not have csrf protection enabled")
            return {}
        else:
            raise Exception(
                "unsuccessful request for crumb: " + r.reason + "\n\n" + r.text
            )
    pieces = r.text.split(delimiter)
    return {pieces[0]: pieces[1]}


run()
