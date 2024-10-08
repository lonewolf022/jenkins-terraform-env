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
                script {
                    echo "Check Terraform version: ${TF_VERSION}"
                    echo "Check Terraform version: ${params.TF_VERSION}"
                }
                sh """
                    ls -l terraform_${params.TF_VERSION}_linux_amd64.zip || wget https://releases.hashicorp.com/terraform/"${params.TF_VERSION}"/terraform_"${params.TF_VERSION}"_linux_amd64.zip
                    unzip -o terraform_"${params.TF_VERSION}"_linux_amd64.zip
                    pwd
                    ./terraform version
                """
            }
        }

        stage('Ensure Provider Version') {
            steps {
                script {
                    def versionFile = 'version.tf'

                    if (fileExists(versionFile)) {
                        sh "sed -i 's/version = \".*\"/version = \"${params.AZ_PV_VERSION}\"/' ${versionFile}"
                    } else {
                        writeFile file: versionFile, text: """
                        terraform {
                          required_providers {
                            azurerm = {
                              source  = "hashicorp/azurerm"
                              version = "~> ${params.AZ_PV_VERSION}"
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
                sh './terraform init'
            }
        }

    }
    
    post {
        always {
            script {
                deleteDir()
            }
        }
    }
}
