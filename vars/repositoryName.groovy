#!/usr/bin/env groovy

/**
 * Returns the current directory's git repository name.
 */
String call() {
  // NOTE: this uses sh/git to get the name,
  // to avoid the sandbox/script approval process for using the jenkins scm object.
  def url = sh(returnStdout: true, script: 'git config remote.origin.url').trim()
  return url.tokenize('/.')[-2]
}
