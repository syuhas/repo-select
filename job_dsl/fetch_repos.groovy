job('test_fetch_repos') {
    description('Fetches repositories from GitHub and lists branches')

    parameters {
        stringParam('GITHUB_USER', 'syuhas', 'GitHub organization or user from which to fetch repositories')
    }

    environmentVariables {
        env('TOKEN', credentials('GITHUB_TOKEN'))
    }

    steps {
        shell('''
            chmod +x deploy/repos.sh
            ./deploy/repos.sh
        ''')
    }
}
