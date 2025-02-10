import json
import boto3
from jenkinsapi.api import Jenkins
from loguru import logger

def lambda_handler(event, context):
    
    jenkins_url = 'https://jenkins.digitalsteve.net/'
    job_name = "fetch_repos"

    ssm = boto3.client('ssm')
    
    username = ssm.get_parameter(Name='/jenkins/user', WithDecryption=True)['Parameter']['Value']
    token = ssm.get_parameter(Name='/jenkins/token', WithDecryption=True)['Parameter']['Value']

    session = Jenkins(
        jenkins_url,
        username=username,
        password=token
    )

    response = session.build_job(job_name)

    return {
        'statusCode': 200,
        'body': json.dumps(response)
    }