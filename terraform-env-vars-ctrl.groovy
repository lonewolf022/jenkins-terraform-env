pipeline {
    agent any

    parameters {
        choice(name: 'TF_VERSION', choices: ['1.5.7', '1.9.5'], description: 'Select the terraform version')
        choice(name: 'AZ_PV_VERSION', choices: ['3.0', '4.0'], description: 'Select the Azure provider version')
    }
    
    environment {
        TF_VERSION = '${TF_VERSION}' // Specify the desired Terraform version
        PROVIDER_VERSION = '~> ${AZ_PV_VERSION}' // Specify the desired azurerm provider version
    }

    stages {
        stage('Debug') {
            steps {
                script {
                    echo "Version for Terraform: ${params.TF_VERSION}"
                    echo "Version for Azure Provider: ${params.AZ_PV_VERSION}"
                }
            }
        }
        
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
                        sh "sed -i 's/version = \".*\"/version = \"${AZ_PV_VERSION}\"/' ${versionFile}"
                    } else {
                        writeFile file: versionFile, text: """
                        terraform {
                          required_providers {
                            azurerm = {
                              source  = "hashicorp/azurerm"
                              version = "${AZ_PV_VERSION}"
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
