Cru Jenkins Job Configuration
=============================

This project contains configuration for some of the jobs at jenkins.uscm.org.
Eventually, hopefully, it will configure all of the jobs there.


Getting set up
--------------

 *  clone this repo
 *  install python 2.x via homebrew (Mac OS's version [doesn't work well enough][mediawiki])
 *  clone our fork of [jenkins-job-builder][fork], and `cd` into it
 *  checkout the branch `modifications-for-cru-jenkins`
 *  install [virtualenv][virtualenv] (see also [this guide][virtualenv-guide])
 *  create a new env (eg `virtualenv venv`) and activate it (`source venv/bin/activate`)
 *  run `pip install -e .`
 *  run `pip install requests`
 *  you should be able to run `jenkins-jobs --version` now, and verify that version `1.4.1` printed
 *  in the same terminal (to keep `venv` active), change your working directory to the `jenkins-jobs` project
 *  generate the xml config for one of our jobs; eg `jenkins-jobs test jobs/app-jobs.yml mpdx | less`
 *  create `jenkins_jobs.ini` by copying `jenkins_jobs.ini.example` and fill in your username and api key
    (go to the [people][jenkins-people] page, find yourself, click 'Configure', and click 'Show API Token').

Updating jobs
-------------

 *  Tweak `jobs/jobs.yml` (see syntax docs at [jjb-docs])
 *  Test that your changes parse via `jenkins-jobs test jobs/jobs.yml`
 *  Update the job you want to update via `update_jobs.sh {jobname}`
 *  Or, if you want to update all jobs, just run `update_jobs.sh`

If you'd like to test jenkins builds based on github hooks,
you can use the rails-infrastructure-canary project (and its `test_github_jenkins_autobuild.sh`).
This way you can avoid polluting a real project with dummy commits.

Having Trouble?
---------------

[mediawiki]: https://www.mediawiki.org/wiki/Continuous_integration/Jenkins_job_builder#six.moves
[fork]: https://github.com/CruGlobal/jenkins-job-builder
[virtualenv]: https://virtualenv.pypa.io/en/latest/
[virtualenv-guide]: http://docs.python-guide.org/en/latest/dev/virtualenvs/
[jenkins-people]: http://jenkins.uscm.org/asynchPeople/
[jjb-docs]: http://docs.openstack.org/infra/jenkins-job-builder/
