def stopAppiumServer(port) {
    sh """
    PID=\$(lsof -t -i:${port})
    if [ ! -z "\$PID" ]; then
        kill -9 \$PID
    fi
    """
}

pipeline {
    agent any
    environment {
        APPIUM_SERVER_COMMAND_TIMEOUT = 60
    }
    stages {
        stage('Start Appium Server') {
            steps {
                 sh "sleep 30"
                script {
                    
                }
            }
        }
        stage('Stop Appium Server') {
            steps {
                script {
                    stopAppiumServer(4727)
                }
            }
        }
    }
}
