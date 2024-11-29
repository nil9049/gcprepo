pipeline {
    agent any

    environment {
        ANSIBLE_INVENTORY = 'ansible/inventory'  // Relative path to inventory file
        PLAYBOOK = 'ansible/playbook.yml'         // Playbook file name
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the repository from GitHub
                git branch: 'main', url: 'https://github.com/nil9049/gcprepo.git'
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                script {
                    // Execute the Ansible playbook
                    sh "ansible-playbook -i ${ANSIBLE_INVENTORY} ${PLAYBOOK}"
                }
            }
        }
    }

    post {
        success {
            echo 'Ansible playbook executed successfully!'
        }
        failure {
            echo 'Ansible playbook execution failed.'
        }
    }
}
