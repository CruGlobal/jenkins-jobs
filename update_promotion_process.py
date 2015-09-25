"""
    Adds both Production and Staging deployment promotion definitions.
"""
import requests
import ConfigParser


def update_job_promotion(job, environment):

    if environment == "production":
        promotion_process = "Deploy to Production"
        icon = "star-gold"
    elif environment == "staging":
        promotion_process = "Deploy to Staging"
        icon = "star-silver"
    else:
        raise Exception("bad environment: " + environment)

    config = ConfigParser.RawConfigParser()
    config.read('jenkins_jobs.ini')

    base_url = config.get('jenkins', 'url')
    print("base_url:", base_url)
    user = config.get('jenkins', 'user')
    print("user:", user)
    password = config.get('jenkins', 'password')

    config_to_use = "jobs/promotion-config.xml"
    with open(config_to_use, 'rb') as config_file:
        content = config_file.read()

    content = content.replace("{{icon}}", icon)
    content = content.replace("{{environment}}", environment)

    headers = {'content-type': 'application/xml'}
    print("Updating job: ", job)

    url = "%s/job/%s/promotion/process/%s/config.xml" % (base_url, job, promotion_process)
    r = requests.post(url, data=content, headers=headers, auth=(user, password))
    print("Updating job: %s; process: %s; Response Code: %s" % (job, promotion_process, r))


job = "rails-infrastructure-canary"
update_job_promotion(job, "production")
update_job_promotion(job, "staging")
