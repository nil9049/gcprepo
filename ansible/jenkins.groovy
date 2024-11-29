pipeline {
    agent any
    parameters {
        // Parameter for selecting the playbook
        choice(name: 'PACKAGE', choices: ['apache2','nginx', 'mysql', 'postgresql'], description: 'Select a package to install')
        choice(name: 'SERVICE', choices: ['apache2', 'nginx', 'mysql', 'postgresql'], description: 'Select a service to restart')
    }
    stages {
        stage('Input Parameters') {
            steps {
                script {
                    // Set selected package and service into environment variables
                    env.PACKAGE = params.PACKAGE
                    env.SERVICE = params.SERVICE
                }
            }
        }
        stage('Clone Repository') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/nil9049/gcprepo.git'
            }
        }
        stage('Run Ansible Playbook') {
            steps {
                script {
                    // Run the Ansible playbook with extra-vars for package and service
                    sh """
                    ansible-playbook -i ansible/inventory ansible/playbook2.yml \
                    --extra-vars "package=${env.PACKAGE} service=${env.SERVICE}"
                    """
                }
            }
        }
    }
}
