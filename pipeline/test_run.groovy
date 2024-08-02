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
def runRobotTests(folder_run, folder_report, tag_run , retailer, isHeadless, adv_1, adv_2, udid_1, udid_2, platformVersion, remote_node_1, remote_node_2, port_1, port_2, os_type, app_path_pos, update_app ) {
    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${folder_run}") {
            sh """
                robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True  --variable os_type:${os_type}\
                --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1}  --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                -o ${folder_report}/del.xml  -r ${folder_report}/del.html  -l ${folder_report}/log_del.html   prepare-data
            """
            sh """
                robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True  --variable os_type:${os_type} \
                --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1}  --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i ${tag_run} \
                -o ${folder_report}/output.xml  -r ${folder_report}/output.html  -l ${folder_report}/log.html    testsuites
            """
        }
    }
    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${folder_run}") {
             sh """
                robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True --variable os_type:${os_type} \
                --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1}  --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                -o ${folder_report}/del.xml  -r ${folder_report}/del.html  -l ${folder_report}/log_del.html   prepare-data
            """
            sh """
                robot --rerunfailed  ${folder_report}/output.xml   -o ${folder_report}/output1.xml  -r ${folder_report}/output1.html  -l ${folder_report}/log1.html  --variable os_type:${os_type}\
                --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True \
                --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1}  --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i ${tag_run}  testsuites
            """
        }
    }
    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${folder_run}") {
             sh """
                rebot --removekeywords all -o ${folder_report}/output_final.xml   ${folder_report}/output.xml"
             """
        }
    }
    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${folder_run}") {
             sh """
                rebot --removekeywords all -o  ${folder_report}/output_final_1.xml   ${folder_report}/output1.xml"
             """
        }
    }
}


pipeline {
    agent any
    environment {
        USER_NAME = sh(script: 'whoami', returnStdout: true).trim()
        ROOT_MAC = "/Users/${USER_NAME}/Desktop"
        IP_ADDRESS = sh(returnStdout: true, script: "ifconfig | grep 'inet ' | grep -v 127.0.0.1 | awk '{print \$2}' | head -n 1").trim()
        FILE_PID_SERVER_1 = "${env.WORKSPACE}/appium_server_1.pid"
        FILE_PID_SERVER_2 = "${env.WORKSPACE}/appium_server_2.pid"
        ANDROID_HOME = "/Users/${USER_NAME}/Library/Android/sdk"
        PATH = "${env.PATH}:${env.ANDROID_HOME}/emulator:${env.ANDROID_HOME}/platform-tools"

        APPIUM_AUTOMATION_PLATFORM_ANDROID='Android'
        APPIUM_AUTOMATION_PLATFORM_IOS='iOS'
        APPIUM_AUTOMATION_NAME_ANDROID='UiAutomator2'
        APPIUM_AUTOMATION_PLATFORM_VERSION='10.0'
        APPIUM_AUTOMATION_NAME_IOS='XCUITest'
        IS_HEADLESS = "false"
        UPDATE_APP = "True"

        APPIUM_SERVER_PORT_1 = '4727'
        APPIUM_SERVER_SYSTEM_PORT_1 = '8001'
        APPIUM_SERVER_UDID_1 = 'emulator-5554'
        APPIUM_SERVER_DEVICE_NAME_1 = 'May_1'
        RETAILER_1 = "auto17"

        APPIUM_SERVER_PORT_2 = '4728'
        APPIUM_SERVER_SYSTEM_PORT_2 = '8002'
        APPIUM_SERVER_UDID_2 = 'emulator-5556'
        APPIUM_SERVER_DEVICE_NAME_2 = 'May_2'
        RETAILER_2 = "auto17"
        APPIUM_SERVER_COMMAND_TIMEOUT = '300'
        OS_TYPE = "android"

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
        stage('Check And Create Report Folders') {
            steps {
                script {
                    def folders = [
                        "${FOLDER_REPORT_MAN}",
                        "${FOLDER_REPORT_MAN}/report1",
                        "${FOLDER_REPORT_MAN}/report2",
                        "${FOLDER_REPORT_MAN}/report2may",
                        "${FOLDER_REPORT_MAN}/report_all",
                        "${ROOT_MAC}/push_chat_bot",
                        "${ROOT_MAC}/data_jira",
                        "${ROOT_MAC}/data_jira/man",
                        "${ROOT_MAC}/data_jira/ios",
                        "${ROOT_MAC}/data_jira/postouch",
                        "${ROOT_MAC}/data_jira/android",
                        "${ROOT_MAC}/data_jira/web",
                        "${FOLDER_AUTO_MAN}"
                    ]
                    folders.each { folder ->
                        if (!fileExists(folder)) {
                            sh "mkdir -p ${folder}"
                        }
                    }
                }
            }
        }
    stage('Checkout') {
                steps {
                        checkout([$class: 'GitSCM',
                            branches: [[name: '*/core']],
                            userRemoteConfigs: [[credentialsId: 'MAC',
                                            url: 'https://gitlab.citigo.com.vn/kvfnb/automation-test/kiotviet-fnb-automation-test-app-man']],
                                extensions: [[$class: 'RelativeTargetDirectory', 
                                relativeTargetDir: "${FOLDER_AUTO_MAN}"]]])    
                }
            }
        // stage('Delete All .apk OLD Files') {
        //         steps {
        //             catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //                 dir("${DOWNLOAD_DIR_OLD}") {
        //                     sh "rm -r ${DOWNLOAD_DIR_OLD}/*.apk"
        //                 }
        //             }
        //         }
        //     }
        // stage('Delete All .apk NEW Files') {
        //         steps {
        //             catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //                 dir("${DOWNLOAD_DIR_NEW}") {
        //                     sh "rm -r ${DOWNLOAD_DIR_NEW}/*.apk"
        //                 }
        //             }
        //         }
        //     }
         stage('Cleanup Report Folders') {
            steps {
                script {
                    def folders = sh(script: "ls -d ${env.FOLDER_REPORT_MAN}/*/", returnStdout: true).trim().split('\n')
                    for (folder in folders) {
                        echo "Cleaning folder: ${folder}"
                        try {
                            sh "rm -f ${folder}/*"
                        } catch (Exception e) {
                            echo "Files not exsist in ${folder}: ${e.message}"
                        }
                    }
                }
            }
        }
        // stage('Download OLD Version App Man Android') {
        //     steps {
        //         dir("${DOWNLOAD_DIR_OLD}") {
        //             script {
        //                 sh "curl -o FNB_MAN.apk '${params.link_old}'"
        //                 if (fileExists('FNB_MAN.apk')) {
        //                     echo "Download old version app man android success"
        //                 } else {
        //                     error "Download old version error"
        //                 }
        //             }
        //         }
        //     }
        // }
        // stage('Download NEW Version App Man Android') {
        //     steps {
        //         dir("${DOWNLOAD_DIR_NEW}") {
        //             script {
        //                 sh "curl -o FNB_MAN_NEW.apk '${params.link_new}'"
        //                 if (fileExists('FNB_MAN_NEW.apk')) {
        //                     echo "Download new version app man android success"
        //                 } else {
        //                     error "Download new version error"
        //                 }
        //             }
        //         }
        //     }
        // }
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
        stage('Check AVDs') {
            steps {
                script {
                    // Liệt kê tất cả các AVD đã được cài đặt
                    sh "emulator -list-avds"
                    // hoặc
                    // sh "${ANDROID_HOME}/tools/bin/avdmanager list avd"
                }
            }
        }
        // stage('Start Emulator') {
        //     steps {
        //         script {
        //             sh """
        //                 emulator -avd  ${APPIUM_SERVER_DEVICE_NAME_1} -no-snapshot-load -no-boot-anim &
        //                 adb wait-for-device
        //             """
        //         }
        //     }
        // }
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
        stage('Other Stage 2') {
            steps {
                sh "sleep 10"
                script {
                  runRobotTests(FOLDER_AUTO_MAN,
                    FOLDER_REPORT_1, 
                   "MAN_ADD_ACCOUNT_01",
                    RETAILER_1, IS_HEADLESS, 
                    APPIUM_SERVER_DEVICE_NAME_1, APPIUM_SERVER_DEVICE_NAME_2, 
                    APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_PORT_1, APPIUM_SERVER_PORT_2,
                    OS_TYPE, PATH_ANDROID_POS, UPDATE_APP)
            }
            }
        }

    }
}