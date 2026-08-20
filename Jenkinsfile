pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out Java project from GitHub...'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling Java source code...'

                bat '''
                    cd employee-management-system-java-html-css

                    if exist out rmdir /s /q out
                    mkdir out

                    javac -d out src\\com\\example\\employee\\Employee.java src\\com\\example\\employee\\Main.java
                '''
            }
        }

        stage('Test') {
            steps {
                echo 'Checking compiled Java classes...'

                bat '''
                    cd employee-management-system-java-html-css

                    if not exist out\\com\\example\\employee\\Employee.class (
                        echo Employee.class not found!
                        exit /b 1
                    )

                    if not exist out\\com\\example\\employee\\Main.class (
                        echo Main.class not found!
                        exit /b 1
                    )

                    echo Java compilation test passed successfully.
                '''
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'

                bat '''
                    cd employee-management-system-java-html-css

                    if exist package rmdir /s /q package

                    mkdir package
                    mkdir package\\out
                    mkdir package\\frontend

                    xcopy /E /I /Y out package\\out
                    xcopy /E /I /Y frontend package\\frontend

                    copy /Y run.bat package\\run.bat

                    echo Application packaged successfully.
                '''
            }
        }

        stage('Archive') {
            steps {
                echo 'Archiving application...'

                archiveArtifacts artifacts: 'employee-management-system-java-html-css/package/**',
                                 fingerprint: true
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'

                bat '''
                    cd employee-management-system-java-html-css

                    if exist deploy rmdir /s /q deploy

                    mkdir deploy

                    xcopy /E /I /Y package deploy

                    echo Application deployment completed.
                '''
            }
        }
    }

    post {
        success {
            echo '======================================'
            echo ' Employee Management System'
            echo ' CI/CD PIPELINE SUCCESSFUL'
            echo '======================================'
        }

        failure {
            echo '======================================'
            echo ' Employee Management System'
            echo ' CI/CD PIPELINE FAILED'
            echo '======================================'
        }
    }
}
