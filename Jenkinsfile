pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        DOCKER_IMAGE = 'dereselim/travel-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
    }

    stages {
        stage('Clone from GitHub') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/krezon/travel-management.git',
                    credentialsId: 'github-credentials'
                sh 'echo "Cloned commit: $(git rev-parse --short HEAD)"'
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar --no-daemon'
                sh 'ls -lh build/libs/'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -t ${DOCKER_IMAGE}:latest ."
                sh "docker images | grep travel-management"
            }
        }

        stage('Login to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Push Image to DockerHub') {
            steps {
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/deployment.yaml'
                sh 'kubectl apply -f k8s/service.yaml'
                sh "kubectl set image deployment/travel-management travel-management=${DOCKER_IMAGE}:${DOCKER_TAG} --record"
                sh 'kubectl rollout status deployment/travel-management --timeout=180s'
                sh 'kubectl get pods -l app=travel-management -o wide'
                sh 'kubectl get svc travel-management'
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
        success {
            echo "✅ Pipeline başarılı! Image: ${DOCKER_IMAGE}:${DOCKER_TAG} deploy edildi."
        }
        failure {
            echo "❌ Pipeline başarısız oldu."
        }
    }
}
