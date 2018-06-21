#!/usr/bin/env bash
set -e

groovy -cp src test/org/cru/jenkins/lib/DeploymentsTest.groovy
