def isValidPid(pid) {
    return pid.isInteger() && pid.toInteger() > 0
}

def startAppiumServer(port, systemPort, udid, pidFile, platform, automationName, ipAddress) {
    sh """
    appium --port ${port} \
                --default-capabilities '{"systemPort": ${systemPort}, "udid": "${udid}", "newCommandTimeout": ${env.APPIUM_SERVER_COMMAND_TIMEOUT}}' \
                --nodeconfig '{
                    "capabilities": [
                        {
                        "maxInstances": 1,
                        "platform": "${platform}",
                        "automationName": "${automationName}"
                        }
                    ],
                    "configuration": {
                        "proxy": "org.openqa.grid.selenium.proxy.DefaultRemoteProxy",
                        "url": "http://${ipAddress}:${port}/wd/hub",
                        "port": ${port},
                        "host": "${ipAddress}",
                        "maxSession": 1
                    }
                }' & echo \$! > ${pidFile}
    """
    def pid = readFile(pidFile).trim()
    return pid
}

pipeline {
    agent any
    environment {
        USER_NAME = sh(script: 'whoami', returnStdout: true).trim()
        ROOT_MAC = "/Users/${USER_NAME}/Desktop"
        IP_ADDRESS = sh(returnStdout: true, script: "ifconfig | grep 'inet ' | grep -v 127.0.0.1 | awk '{print \$2}' | head -n 1").trim()
        FILE_PID_SERVER_1 = "${env.WORKSPACE}/appium_server_1.pid"
        FILE_PID_SERVER_2 = "${env.WORKSPACE}/appium_server_2.pid"

        APPIUM_AUTOMATION_PLATFORM_ANDROID='Android'
        APPIUM_AUTOMATION_PLATFORM_IOS='iOS'
        APPIUM_AUTOMATION_NAME_ANDROID='UiAutomator2'
        APPIUM_AUTOMATION_NAME_IOS='XCUITest'

        APPIUM_SERVER_PORT_1 = '4727'
        APPIUM_SERVER_SYSTEM_PORT_1 = '8001'
        APPIUM_SERVER_UDID_1 = 'emulator-5554'
        APPIUM_SERVER_DEVICE_NAME_1 = 'May_1'

        APPIUM_SERVER_PORT_2 = '4728'
        APPIUM_SERVER_SYSTEM_PORT_2 = '8002'
        APPIUM_SERVER_UDID_2 = 'emulator-5556'
        APPIUM_SERVER_DEVICE_NAME_2 = 'May_2'
        APPIUM_SERVER_COMMAND_TIMEOUT = '300'

        REMOTE_NODE_1 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_1}/wd/hub"
        REMOTE_NODE_2 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_2}/wd/hub"

        APPIUM_SERVER_PID_1 = ''
        APPIUM_SERVER_PID_2 = ''

        FOLDER_AUTO_MAN = "${ROOT_MAC}/Auto_Man_Jenkins"
        DOWNLOAD_DIR_OLD = "${FOLDER_AUTO_MAN}/drivers"
        DOWNLOAD_DIR_NEW = "${FOLDER_AUTO_MAN}/drivers"
        PATH_ANDROID_POS = "${ROOT_MAC}/Automation_Android_Jenkins/drivers/AND-FNB-POS-AF.apk"

        FOLDER_REPORT_MAN = "${ROOT_MAC}/reportman"
        FOLDER_REPORT_1 = "${FOLDER_REPORT_MAN}/report1"
        FOLDER_REPORT_2 = "${FOLDER_REPORT_MAN}/report2"
        FOLDER_REPORT_3 = "${FOLDER_REPORT_MAN}/report2may"
        FOLDER_REPORT_4 = "${FOLDER_REPORT_MAN}/report_all"

        OUTPUT_FILES_REMOVE_KEY_XML = """${FOLDER_REPORT_1}/output_final.xml,${FOLDER_REPORT_1}/output_final_1.xml,${FOLDER_REPORT_2}/output_final.xml,${FOLDER_REPORT_2}/output_final_1.xml,${FOLDER_REPORT_3}/output_final.xml,${FOLDER_REPORT_3}/output_final_1.xml"""
        MERGED_OUTPUT_XML = "${FOLDER_REPORT_4}/merge.xml"
        MERGED_OUTPUT_LOG = "${FOLDER_REPORT_4}/merge_log.html"
        MERGED_OUTPUT_REPORT = "${FOLDER_REPORT_4}/merge_report.html"
        JIRA_OUTPUT_PDF = "${FOLDER_REPORT_4}/merge_log.pdf"
        JIRA_DATA_TICKET_TXT = "${ROOT_MAC}/data_jira/man/data.txt"
        PUSH_CHAT_BOT_PY = "${ROOT_MAC}/push_chat_bot/bot1234.py"
        PUSH_JIRA_DESCRIPTIONS="${ROOT_MAC}/push_chat_bot/jira1234.py"
        CREATE_HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env1234"
        CREATE_JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv1234"
        HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env1234/bin/activate"
        JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv1234/bin/activate"
        CLIENT_RUN_AUTO = "ANDROID MAN SYSTEM - UPDATE APP"
    }
    stages {
        stage('Get User Name') {
            steps {
                script {
                    ROOT_MAC = "/Users/${env.USER_NAME}/Desktop"
                    echo "ROOTTTTTT: ${ROOT_MAC}"
                }
            }
        }
        stage('Cleanup Old Appium Servers If Exists') {
            steps {
                script {
                    // Check and kill old Appium server 1 if PID file exists and contains a valid PID
                    if (fileExists("${env.FILE_PID_SERVER_1}")) {
                        def oldPid = readFile("${env.FILE_PID_SERVER_1}").trim()
                        if (isValidPid(oldPid)) {
                            sh "kill ${oldPid} || true"
                            sh "rm -f ${env.FILE_PID_SERVER_1}"
                        } else {
                            echo "Invalid PID in appium_server_1.pid: ${oldPid}"
                        }
                    }

                    // Check and kill old Appium server 2 if PID file exists and contains a valid PID
                    if (fileExists("${env.FILE_PID_SERVER_2}")) {
                        def oldPid = readFile("${env.FILE_PID_SERVER_2}").trim()
                        if (isValidPid(oldPid)) {
                            sh "kill ${oldPid} || true"
                            sh "rm -f ${env.FILE_PID_SERVER_2}"
                        } else {
                            echo "Invalid PID in appium_server_1.pid: ${oldPid}"
                        }
                    }
                }
            }
        }
        stage('Start Appium Servers') {
            steps {
                script {
                    def serverInfo_1 = startAppiumServer(
                        env.APPIUM_SERVER_PORT_1,
                        env.APPIUM_SERVER_SYSTEM_PORT_1,
                        env.APPIUM_SERVER_UDID_1,
                        env.FILE_PID_SERVER_1,
                        env.APPIUM_AUTOMATION_PLATFORM_ANDROID,
                        env.APPIUM_AUTOMATION_NAME_ANDROID,
                        env.IP_ADDRESS
                    )
                    APPIUM_SERVER_PID_1 = serverInfo_1
            

                    def serverInfo_2 = startAppiumServer(
                        env.APPIUM_SERVER_PORT_2,
                        env.APPIUM_SERVER_SYSTEM_PORT_2,
                        env.APPIUM_SERVER_UDID_2,
                        env.FILE_PID_SERVER_2,
                        env.APPIUM_AUTOMATION_PLATFORM_ANDROID,
                        env.APPIUM_AUTOMATION_NAME_ANDROID,
                        env.IP_ADDRESS
                    )
                    APPIUM_SERVER_PID_2 = serverInfo_2
                }
            }
        }
        stage('Print File Paths') {
            steps {
                script {
                    // Print the paths of the files
                    echo "Appium server 1 PID file path: ${env.FILE_PID_SERVER_1}"
                    echo "Appium server 2 PID file path: ${env.FILE_PID_SERVER_2}"
                }
            }
        }
        //Các stages khác của bạn ở đây
        stage('Other Stage 1') {
            steps {
                // Các bước của stage khác
                echo 'Running other stage 1'
            }
        }
        stage('Other Stage 2') {
            steps {
                // Các bước của stage khác
                echo 'Running other stage 2'
                sh "sleep 60"
            }
        }
    }

   post {
    always {
        script {
            // Stop first Appium server
            sh """
            if [ -f ${env.FILE_PID_SERVER_1} ]; then
                kill \$(cat ${env.FILE_PID_SERVER_1})
            fi
            """

            // Stop second Appium server
            sh """
            if [ -f ${env.FILE_PID_SERVER_2} ]; then
                kill \$(cat ${env.FILE_PID_SERVER_2})
            fi
            """
        }
    }
}

}
