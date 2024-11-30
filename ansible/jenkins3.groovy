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
        GOOGLE_CREDENTIALS = credentials('my-key')  // Jenkins credential for GCP service account
    }

    stages {
        stage('Run Ansible Playbook to Create and Attach Disk') {
            steps {
                script {
                    // Decode the Base64-encoded GCP service account credentials and save them to a file
                    sh '''
                    echo $GOOGLE_CREDENTIALS  > ${WORKSPACE}/gcp-key.json
                    export GOOGLE_APPLICATION_CREDENTIALS=${WORKSPACE}/gcp-key.json
                    ansible-playbook -i inventory playbook3.yml --extra-vars "project_id=${params.PROJECT_ID} vm_name=${params.VM_NAME} disk_name=${params.DISK_NAME} disk_size=${params.DISK_SIZE} zone=${params.ZONE} custom_mount_point=$CUSTOM_MOUNT_POINT"
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()  // Clean up workspace
        }
    }
}
