pipeline {
    agent any
    parameters {
        // Parameter for selecting the playbook
        choice(name: 'PLAYBOOK', choices: ['install_packages', 'multiple_packages'], description: 'Select the playbook to execute')
    }
    stages {
        stage('Input Parameters') {
            steps {
                script {
                    // Initialize variables for available packages and services
                    if (params.PLAYBOOK == 'install_packages') {
                        availablePackages = ['apache2']
                        availableServices = ['apache2']
                    } else if (params.PLAYBOOK == 'multiple_packages') {
                        availablePackages = ['nginx', 'mysql', 'postgresql']
                        availableServices = ['apache2', 'nginx', 'mysql', 'postgresql']
                    }

                    // Use input step to dynamically prompt for parameters based on PLAYBOOK
                    userInput = input(
                        message: "Select packages and services for ${params.PLAYBOOK}",
                        parameters: [
                            choice(name: 'PACKAGES', choices: availablePackages, description: 'Select a package to install'),
                            choice(name: 'SERVICES', choices: availableServices, description: 'Select a service to restart')
                        ]
                    )

                    // Assign user selections to environment variables
                    env.PACKAGES = userInput.PACKAGES
                    env.SERVICES = userInput.SERVICES
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
                    // Map the playbook name to its path
                    playbookMap = [
                        'install_packages': 'ansible/playbook.yml',
                        'multiple_packages': 'ansible/playbook2.yml'
                    ]
                    // Determine the path based on selected PLAYBOOK
                    playbookPath = playbookMap[params.PLAYBOOK]

                    // Run the Ansible playbook with extra-vars
                    sh """
                    ansible-playbook -i ansible/inventory ${playbookPath} \
                    --extra-vars "packages=${env.PACKAGES} services=${env.SERVICES}"
                    """
                }
            }
        }
    }
}
