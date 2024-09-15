pipeline {
    agent any

    environment {
        TF_VERSION = '1.5.7' // Specify the desired Terraform version
        PROVIDER_VERSION = '~> 3.0' // Specify the desired azurerm provider version
    }

    stages {
        stage('Install Terraform') {
            steps {
                sh '''
                wget https://releases.hashicorp.com/terraform/${TF_VERSION}/terraform_${TF_VERSION}_linux_amd64.zip
                unzip terraform_${TF_VERSION}_linux_amd64.zip
                mv terraform /usr/local/bin/
                terraform version
                '''
            }
        }

        stage('Ensure Provider Version') {
            steps {
                script {
                    def versionFile = 'version.tf'

                    if (fileExists(versionFile)) {
                        sh "sed -i 's/version = \".*\"/version = \"${PROVIDER_VERSION}\"/' ${versionFile}"
                    } else {
                        writeFile file: versionFile, text: """
                        terraform {
                          required_providers {
                            azurerm = {
                              source  = "hashicorp/azurerm"
                              version = "${PROVIDER_VERSION}"
                            }
                          }
                        }
                        """
                    }
                }
            }
        }

        stage('Terraform Init') {
            steps {
                sh 'terraform init'
            }
        }

        // stage('Terraform Plan') {
        //     steps {
        //         sh 'terraform plan'
        //     }
        // }

        // stage('Terraform Apply') {
        //    steps {
        //        sh 'terraform apply -auto-approve'
        //    }
        // }
    }
}
