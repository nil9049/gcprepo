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
        SERVICE_ACCOUNT = 'disk-manager-service-account@influential-rex-442613-b4.iam.gserviceaccount.com'
    }

    stages {
        stage('Setup Environment') {
            steps {
                withCredentials([file(credentialsId: "my-key", variable: 'GC_KEY')]) {
                    sh '''#!/bin/bash
                    cp "$GC_KEY" "${WORKSPACE}/gcp-key.json"
                    gcloud auth activate-service-account --key-file=${WORKSPACE}/gcp-key.json

                    '''
                }
            }
        }

        stage('Run Ansible Playbook to Create and Attach Disk') {
            steps {
                sh '''#!/bin/bash
                ansible-playbook -i ansible/inventory ansible/playbook4.yml --extra-vars "project_id=$PROJECT_ID vm_name=$VM_NAME disk_name=$DISK_NAME disk_size=$DISK_SIZE zone=$ZONE custom_mount_point=$CUSTOM_MOUNT_POINT google_credentials_path=${WORKSPACE}/gcp-key.json"
                '''
            }
        }
    }

    post {
        always {
            cleanWs() // Clean up workspace after pipeline execution
        }
    }
}
