pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        PROJECT_NAME = 'analytics-app'
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Pull the latest code from GitHub
                git branch: 'main', url: 'https://github.com/Guru2626/Risk-Analytics'
            }
        }

        stage('Build Docker Images') {
            steps {
                // Build your Docker images for the services
                sh 'docker-compose -f $DOCKER_COMPOSE_FILE build'
            }
        }

        stage('Bring Up Services') {
            steps {
                // Start all services in detached mode
                sh 'docker-compose -f $DOCKER_COMPOSE_FILE up -d'
            }
        }

    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed. Check the logs."
        }
    }
}
