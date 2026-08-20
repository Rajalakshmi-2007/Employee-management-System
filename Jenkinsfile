pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out Java project from GitHub...'
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling Java source code...'

                bat '''
                    if exist out rmdir /s /q out
                    mkdir out

                    javac -d out src\\com\\example\\employee\\Employee.java src\\com\\example\\employee\\Main.java
                '''
            }
        }

        stage('Test') {
            steps {
                echo 'Checking compiled classes...'

                bat '''
                    if not exist out\\com\\example\\employee\\Employee.class (
                        echo Employee.class not found!
                        exit /b 1
                    )

                    if not exist out\\com\\example\\employee\\Main.class (
                        echo Main.class not found!
                        exit /b 1
                    )

                    echo Java compilation test passed.
                '''
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'

                bat '''
                    if exist package rmdir /s /q package

                    mkdir package
                    mkdir package\\out
                    mkdir package\\frontend

                    xcopy /E /I /Y out package\\out
                    xcopy /E /I /Y frontend package\\frontend

                    copy /Y run.bat package\\run.bat
                '''
            }
        }

        stage('Archive') {
            steps {
                echo 'Archiving application package...'

                archiveArtifacts artifacts: 'package/**',
                                 fingerprint: true
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'

                bat '''
                    if exist deploy rmdir /s /q deploy

                    mkdir deploy

                    xcopy /E /I /Y package deploy

                    echo Application deployed successfully.
                '''
            }
        }
    }

    post {
        success {
            echo '======================================'
            echo ' Employee Management System'
            echo ' CI/CD Pipeline SUCCESS'
            echo '======================================'
        }

        failure {
            echo '======================================'
            echo ' Employee Management System'
            echo ' CI/CD Pipeline FAILED'
            echo '======================================'
        }
    }
}
