job('test_fetch_repos') {
    description('Job to fetch a list of repositories and branches from a GitHub account.')
    
    parameters {
        stringParam('GITHUB_USER', 'syuhas', 'GitHub organization or user from which to fetch repositories')
    }

    properties {
        githubProjectUrl('https://github.com/syuhas/repo-select/')
    }

    scm {
        git {
            remote {
                url('https://github.com/syuhas/repo-select.git')
                credentials('GITHUB_TOKEN') // Use the stored credential ID
            }
            branch('*/main')
        }
    }

    wrappers {
        credentialsBinding {
            string('TOKEN', 'GITHUB_TOKEN') // Injects the GitHub Token into the environment
        }
    }

    definition {
        cps {
            script("""
            pipeline {
                agent any

                environment {
                    GITHUB_USER = "\${params.GITHUB_USER}"
                    TOKEN = credentials('GITHUB_TOKEN') // Correctly reference Jenkins stored credential
                }

                stages {
                    stage('Fetch Repositories') {
                        steps {
                            script {
                                sh 'chmod +x deploy/repos.sh'
                                sh './deploy/repos.sh'
                            }
                        }
                    }
                }
            }
            """)
        }
    }
}
