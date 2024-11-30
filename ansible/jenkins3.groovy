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
        stage('Run Ansible Playbook to Create and Attach Disk') {
            steps {
                script {
                    // Use Bash explicitly to handle the credentials and run the playbook
                    sh '''#!/bin/bash
                    echo "$GOOGLE_CREDENTIALS" > ${WORKSPACE}/gcp-key.json
                    export GOOGLE_APPLICATION_CREDENTIALS=${WORKSPACE}/gcp-key.json
                    
                    # Run the Ansible playbook and pass all required parameters
                    ansible-playbook create_and_attach_disk.yml --extra-vars "project_id=$PROJECT_ID vm_name=$VM_NAME disk_name=$DISK_NAME disk_size=$DISK_SIZE zone=$ZONE custom_mount_point=$CUSTOM_MOUNT_POINT"
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
