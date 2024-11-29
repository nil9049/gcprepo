pipeline {
    agent any
    parameters {
        choice(name: 'PACKAGES', choices: ['apache2', 'nginx', 'mysql', 'postgresql'], description: 'Select a package to install')
        choice(name: 'SERVICES', choices: ['apache2', 'nginx', 'mysql', 'postgresql'], description: 'Select a service to restart')
        choice(name: 'PLAYBOOK', choices: ['install_packages', 'multiple_packages'], description: 'Select the playbook to execute')
    }
    environment {
        ANSIBLE_INVENTORY = 'ansible/inventory'  // Define the inventory path
    }
    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/nil9049/gcprepo.git'
            }
        }
        stage('Run Ansible Playbook') {
            steps {
                script {
                    // Define a map of playbook choices
                    playbookMap = [
                        'install_packages': 'ansible/playbook.yml',    // Corrected the typo here
                        'multiple_packages': 'ansible/configure_services.yml'
                    ]
                    
                    // Fetch the selected package and service
                    package = params.PACKAGES
                    service = params.SERVICES
                    playbookPath = playbookMap[params.PLAYBOOK]  // Get the mapped playbook path

                    // Execute the selected playbook with extra-vars
                    sh """
                    ansible-playbook -i ${ANSIBLE_INVENTORY} ${playbookPath} \
                    --extra-vars "package=${package} service=${service}"
                    """
                }
            }
        }
    }
}
