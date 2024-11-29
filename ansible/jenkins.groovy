pipeline {
    agent any
    parameters {
        // Parameter for selecting the playbook
        choice(name: 'PLAYBOOK', choices: ['install_packages', 'multiple_packages'], description: 'Select the playbook to execute')

        // Dynamically set choices for PACKAGES based on the selected PLAYBOOK
        activeChoiceParam(name: 'PACKAGES') {
            description('Select a package to install')
            filterable()
            groovyScript {
                script("""
                    if (PLAYBOOK == 'install_packages') {
                        return ['apache2'] // Show only apache2 for the first playbook
                    } else if (PLAYBOOK == 'multiple_packages') {
                        return ['nginx', 'mysql', 'postgresql'] // Show other packages for the second playbook
                    }
                    return [] // Default empty list if no playbook is selected
                """)
                fallbackScript("return []") // Fallback if the script fails
            }
        }

        // Dynamically set choices for SERVICES based on the selected PLAYBOOK
        activeChoiceParam(name: 'SERVICES') {
            description('Select a service to restart')
            filterable()
            groovyScript {
                script("""
                    if (PLAYBOOK == 'install_packages') {
                        return ['apache2'] // Show apache2 for the first playbook
                    } else if (PLAYBOOK == 'multiple_packages') {
                        return ['apache2', 'nginx', 'mysql', 'postgresql'] // Show other services for multiple packages playbook
                    }
                    return [] // Default empty list if no playbook is selected
                """)
                fallbackScript("return []") // Fallback if the script fails
            }
        }
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
                    // Map the playbook name to its path
                    playbookMap = [
                        'install_packages': 'ansible/install_packages.yml',
                        'multiple_packages': 'ansible/multiple_packages.yml'
                    ]
                    // Determine the path based on selected PLAYBOOK
                    playbookPath = playbookMap[PLAYBOOK]

                    // Get selected values for PACKAGE and SERVICE
                    selectedPackage = PACKAGES
                    selectedService = SERVICES

                    // Run the Ansible playbook with extra-vars
                    sh """
                    ansible-playbook -i ansible/inventory ${playbookPath} \
                    --extra-vars "package=${selectedPackage} service=${selectedService}"
                    """
                }
            }
        }
    }
}
