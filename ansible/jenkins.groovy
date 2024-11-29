pipeline {
    agent any
    environment {
        ANSIBLE_INVENTORY = 'ansible/inventory'  // Path to your inventory file
    }
    parameters {
        choice(name: 'PLAYBOOK', choices: ['install_packages', 'multiple_packages'], description: 'Select the playbook to execute')
    }
    stages {
        stage('Define Parameters') {
            steps {
                script {
                    // Active Choice for PACKAGES
                    properties([
                        parameters([
                            activeChoiceParam(name: 'PACKAGES') {
                                description('Select a package to install')
                                filterable()
                                groovyScript {
                                    script("""
                                        if (PLAYBOOK == 'install_packages') {
                                            return ['apache2']
                                        } else if (PLAYBOOK == 'multiple_packages') {
                                            return ['nginx', 'mysql', 'postgresql']
                                        } else {
                                            return []
                                        }
                                    """)
                                    fallbackScript('return ["apache2"]')
                                }
                            },

                            // Active Choice for SERVICES
                            activeChoiceParam(name: 'SERVICES') {
                                description('Select a service to restart')
                                filterable()
                                groovyScript {
                                    script("""
                                        if (PLAYBOOK == 'install_packages') {
                                            return ['apache2']
                                        } else if (PLAYBOOK == 'multiple_packages') {
                                            return ['apache2', 'nginx', 'mysql', 'postgresql']
                                        } else {
                                            return []
                                        }
                                    """)
                                    fallbackScript('return ["apache2"]')
                                }
                            }
                        ])
                    ])
                }
            }
        }
        stage('Run Ansible Playbook') {
            steps {
                script {
                    // Map the playbook name to its path
                    playbookPath = [
                        'install_packages': 'ansible/install_packages.yml',
                        'multiple_packages': 'ansible/multiple_packages.yml'
                    ][PLAYBOOK]

                    selectedPackage = PACKAGES
                    selectedService = SERVICES

                    // Run the selected playbook with the provided packages and services
                    sh """
                    ansible-playbook -i ${ANSIBLE_INVENTORY} ${playbookPath} \
                    --extra-vars "package=${selectedPackage} service=${selectedService}"
                    """
                }
            }
        }
    }
}
