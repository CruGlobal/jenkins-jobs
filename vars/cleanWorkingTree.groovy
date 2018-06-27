#!/usr/bin/env groovy

/**
 * Removes untracked and ignored files and directories.
 * An exclusion pattern may be passed in the 'except' config option.
 * This must be a git checkout.
 */
void call(Map config) {
  def exclude = config.except ? " --exclude ${config.except}" : ""
  sh "git clean -d --force --force -x" + exclude
}
