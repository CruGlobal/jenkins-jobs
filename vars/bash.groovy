#!/usr/bin/env groovy

/**
 * Runs the given script with bash (instead of sh).
 */
def call(String script) {
  def header = """\
    #!/usr/bin/env bash
    set -e
    
    """.stripIndent()
  sh header + script
}
