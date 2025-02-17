pipelineJob('test_fetch_domains') {
    description('Fetches available subdomains from AWS Route 53.')
    parameters {
        stringParam('AWS_ACCOUNT_ID', '', 'AWS Account ID')
        stringParam('ROUTE53_ZONE_ID', '', 'Route 53 Hosted Zone ID')
    }
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Fetch Subdomains') {
                            steps {
                                script {
                                    sh '''
                                    aws route53 list-resource-record-sets --hosted-zone-id $ROUTE53_ZONE_ID | jq -r '.ResourceRecordSets[].Name' > subdomains.txt
                                    '''
                                }
                            }
                        }
                        stage('Archive Results') {
                            steps {
                                archiveArtifacts artifacts: 'subdomains.txt', fingerprint: true
                            }
                        }
                    }
                }
            """.stripIndent())
        }
    }
}

pipelineJob('test_fetch_repos') {
    description('Fetches available repositories from GitHub.')
    parameters {
        stringParam('GITHUB_USER', '', 'GitHub Username or Organization')
        stringParam('GITHUB_TOKEN', '', 'GitHub API Token (Optional for private repos)')
    }
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Fetch Repositories') {
                            steps {
                                script {
                                    sh '''
                                    curl -H "Authorization: token $GITHUB_TOKEN" "https://api.github.com/users/$GITHUB_USER/repos" | jq -r '.[].name' > repos.txt
                                    '''
                                }
                            }
                        }
                        stage('Archive Results') {
                            steps {
                                archiveArtifacts artifacts: 'repos.txt', fingerprint: true
                            }
                        }
                    }
                }
            """.stripIndent())
        }
    }
}

pipelineJob('test_deploy_ecs') {
    description('Deploys a selected repository to ECS.')
    parameters {
        choiceParam('SELECTED_REPO', [], 'Select a repository to deploy') // Will be populated dynamically
        choiceParam('SELECTED_SUBDOMAIN', [], 'Select a subdomain') // Will be populated dynamically
        stringParam('AWS_ACCOUNT_ID', '', 'AWS Account ID')
        stringParam('AWS_REGION', 'us-east-1', 'AWS Region')
    }
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Checkout Repo') {
                            steps {
                                script {
                                    git url: "https://github.com/$GITHUB_USER/${SELECTED_REPO}.git", branch: 'main'
                                }
                            }
                        }
                        stage('Deploy to ECS') {
                            steps {
                                script {
                                    sh '''
                                    terraform init
                                    terraform apply -auto-approve -var="aws_account_id=$AWS_ACCOUNT_ID" -var="aws_region=$AWS_REGION" -var="subdomain=$SELECTED_SUBDOMAIN"
                                    '''
                                }
                            }
                        }
                    }
                }
            """.stripIndent())
        }
    }
}
