pipeline {
    agent any
    parameters {
        choice(name: 'PLAYBOOK', choices: ['install_packages', 'multiple_packages'], description: 'Select the playbook to execute')
        
        // Initially, PACKAGES and SERVICES will be empty, they will be populated dynamically based on the selected playbook
        choice(name: 'PACKAGES', choices: [], description: 'Select a package to install')
        
        choice(name: 'SERVICES', choices: [], description: 'Select a service to restart')
    }
    stages {
        stage('Set Dynamic Parameters') {
            steps {
                script {
                    // Dynamically set the available packages and services based on selected playbook
                    if (params.PLAYBOOK == 'install_packages') {
                        // If 'install_packages' is selected, only allow the 'apache2' package and 'apache2' service
                        currentBuild.description = "Installing Apache"
                        // Set the PACKAGES and SERVICES choice dynamically
                        currentBuild.rawBuild.addAction(new ParametersAction([
                            new ChoiceParameterValue("PACKAGES", "apache2"),
                            new ChoiceParameterValue("SERVICES", "apache2")
                        ]))
                    } 
                    else if (params.PLAYBOOK == 'multiple_packages') {
                        // If 'multiple_packages' is selected, show multiple packages and services
                        currentBuild.description = "Installing Multiple Packages"
                        // Set the PACKAGES and SERVICES choice dynamically
                        currentBuild.rawBuild.addAction(new ParametersAction([
                            new ChoiceParameterValue("PACKAGES", "nginx, mysql, postgresql"),
                            new ChoiceParameterValue("SERVICES", "apache2, nginx, mysql, postgresql")
                        ]))
                    }
                }
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                script {
                    // Determine the playbook path based on the selected playbook
                    playbookPath = [
                        'install_packages': 'ansible/install_packages.yml',
                        'multiple_packages': 'ansible/multiple_packages.yml'
                    ][params.PLAYBOOK]

                    package = params.PACKAGES
                    service = params.SERVICES

                    // If installing 'apache2', automatically set 'apache2' service to restart
                    if (package == 'apache2' && params.PLAYBOOK == 'install_packages') {
                        service = 'apache2'
                    }

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
