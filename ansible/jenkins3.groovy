pipeline {
    agent any

    parameters {
        string(name: 'PROJECT_ID', defaultValue: '', description: 'GCP Project ID')
        string(name: 'VM_NAME', defaultValue: '', description: 'VM Name to attach disk to')
        string(name: 'DISK_NAME', defaultValue: '', description: 'Disk Name')
        string(name: 'DISK_SIZE', defaultValue: '', description: 'Disk Size (e.g., 10GB)')
        string(name: 'ZONE', defaultValue: '', description: 'GCP Zone')
        string(name: 'CUSTOM_MOUNT_POINT', defaultValue: '/mnt/custom', description: 'Custom Mount Point (e.g., /mnt/data)')
    }

    environment {
        GOOGLE_CREDENTIALS = credentials('my-key')  // Jenkins credential for GCP service account (Plain-text JSON)
    }

    stages {
        stage('Checkout Git Repository') {
            steps {
                script {
                    // Clone the repository containing the Ansible playbook
                    git branch: 'main', url: 'https://github.com/nil9049/gcprepo.git'
                }
            }
        }

        stage('Authenticate with GCP') {
            steps {
                script {
                    // Write the service account JSON key to a file in the workspace
                    writeFile file: "${WORKSPACE}/gcp-key.json", text: "$GOOGLE_CREDENTIALS"
                    
                    // Authenticate with GCP using the service account
                    sh '''
                
                     gcloud auth activate-service-account --key-file=${WORKSPACE}/gcp-key.json --scopes=https://www.googleapis.com/auth/cloud-platform


                    gcloud auth activate-service-account --key-file=${WORKSPACE}/gcp-key.json
                    '''
                }
            }
        }

        stage('Run Ansible Playbook to Create and Attach Disk') {
            steps {
                script {
                    // Run the Ansible playbook and pass all required parameters
                    sh '''#!/bin/bash
                    export GOOGLE_APPLICATION_CREDENTIALS=${WORKSPACE}/gcp-key.json
                    
                    ansible-playbook -i ansible/inventory ansible/playbook3.yml --extra-vars "project_id=$PROJECT_ID vm_name=$VM_NAME disk_name=$DISK_NAME disk_size=$DISK_SIZE zone=$ZONE custom_mount_point=$CUSTOM_MOUNT_POINT google_credentials_path=${WORKSPACE}/gcp-key.json"
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()  // Clean up workspace after pipeline execution
        }
    }
}
