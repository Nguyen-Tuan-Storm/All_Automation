def isValidPid(pid) {
    return pid.isInteger() && pid.toInteger() > 0
}

def startAppiumServer(port, systemPort, udid, platform, automationName, ipAddress) {
    def pidFile = "appium_${port}.pid" 
    stopAppiumServer(pidFile)
    sleep(time: 2, unit: 'SECONDS')

    def nodeConfigFile = "nodeconfig_${port}.json"
    writeFile file: nodeConfigFile, text: """
    {
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
    }
    """

    sh """
    appium --port ${port} \
           --default-capabilities '{"systemPort": ${systemPort}, "udid": "${udid}", "newCommandTimeout": 300}' \
           --nodeconfig ${nodeConfigFile} > /dev/null 2>&1 & echo \$! > ${pidFile}
    """
}

def stopAppiumServer(pidFile) {
    if (fileExists(pidFile)) {
        def pid = readFile(pidFile).trim()
        if (pid) {
            sh "kill -9 ${pid} || true"
            echo "Stopped Appium server with PID: ${pid}"
            writeFile(file: pidFile, text: "")
        } else {
            echo "No PID found in file ${pidFile}"
        }
    } else {
        echo "PID file ${pidFile} does not exist"
    }
}

def runRobotTests(folder_run, folder_report, tag_run, retailer, isHeadless, adv_1, adv_2, udid_1, udid_2, platformVersion, remote_node_1, remote_node_2, port_1, port_2, os_type, app_path_pos, update_app) {
    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                sh """
                    robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True --variable os_type:${os_type} \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                    -o ${folder_report}/del.xml -r ${folder_report}/del.html -l ${folder_report}/log_del.html prepare-data
                """
                sh """
                    robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True --variable os_type:${os_type} \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable app_path_pos:${app_path_pos}  --variable update_app:${update_app} -i ${tag_run} \
                    -o ${folder_report}/output.xml -r ${folder_report}/output.html -l ${folder_report}/log.html testsuites
                """
            }
        }
    } catch (Exception e) {
        echo "Initial Robot test run failed: ${e}"
    }

    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                sh """
                    robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True --variable os_type:${os_type} \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                    -o ${folder_report}/del.xml -r ${folder_report}/del.html -l ${folder_report}/log_del.html prepare-data
                """
                sh """
                    robot --rerunfailed ${folder_report}/output.xml -o ${folder_report}/output1.xml -r ${folder_report}/output1.html -l ${folder_report}/log1.html --variable os_type:${os_type} \
                    --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable app_path_pos:${app_path_pos} --variable update_app:${update_app} -i ${tag_run} testsuites
                """
            }
        }
    } catch (Exception e) {
        echo "Rerun of failed Robot tests failed: ${e}"
    }

    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                sh """
                    rebot --removekeywords all -o ${folder_report}/output_final.xml ${folder_report}/output.xml
                """
            }
        }
    } catch (Exception e) {
        echo "Merging of Robot test results failed: ${e}"
    }

    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                sh """
                    rebot --removekeywords all -o ${folder_report}/output_final_1.xml ${folder_report}/output1.xml
                """
            }
        }
    } catch (Exception e) {
        echo "Merging of rerun Robot test results failed: ${e}"
    }
}



pipeline {
    agent any
    environment {
        //sh(script: 'whoami', returnStdout: true).trim()
        USER_NAME = "thanh.pc"
        ROOT_MAC = "/Users/${USER_NAME}/Desktop"
        //sh(returnStdout: true, script: "ifconfig | grep 'inet ' | grep -v 127.0.0.1 | awk '{print \$2}' | head -n 1").trim()
        IP_ADDRESS = "192.168.134.250"
        ANDROID_HOME = "/Users/${USER_NAME}/Library/Android/sdk"
        PATH = "${env.PATH}:${env.ANDROID_HOME}/emulator:${env.ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/emulator"

        APPIUM_AUTOMATION_PLATFORM_ANDROID='Android'
        APPIUM_AUTOMATION_PLATFORM_IOS='iOS'
        APPIUM_AUTOMATION_NAME_ANDROID='UiAutomator2'
        APPIUM_AUTOMATION_PLATFORM_VERSION='10.0'
        APPIUM_AUTOMATION_NAME_IOS='XCUITest'
        IS_HEADLESS = "false"
        UPDATE_APP = "True"

        APPIUM_SERVER_PORT_1 = '4727'
        APPIUM_SERVER_SYSTEM_PORT_1 = '8800'
        APPIUM_SERVER_UDID_1 = 'emulator-5554'
        APPIUM_SERVER_DEVICE_NAME_1 = 'May_1'
        RETAILER_1 = "autoandroid2"

        APPIUM_SERVER_PORT_2 = '4728'
        APPIUM_SERVER_SYSTEM_PORT_2 = '8801'
        APPIUM_SERVER_UDID_2 = 'emulator-5556'
        APPIUM_SERVER_DEVICE_NAME_2 = 'May_2'
        RETAILER_2 = "autoandroid2"
        APPIUM_SERVER_COMMAND_TIMEOUT = '300'
        OS_TYPE = "android"

        REMOTE_NODE_1 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_1}/wd/hub"
        REMOTE_NODE_2 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_2}/wd/hub"

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
        // CREATE_HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env1234"
        // CREATE_JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv1234"
        HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env1234/bin/activate"
        JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv1234/bin/activate"
        CLIENT_RUN_AUTO = "ANDROID MAN SYSTEM - UPDATE APP"
    }
    stages {
        // stage('Check And Create Report Folders') {
        //     steps {
        //         script {
        //             def folders = [
        //                 "${FOLDER_REPORT_MAN}",
        //                 "${FOLDER_REPORT_MAN}/report1",
        //                 "${FOLDER_REPORT_MAN}/report2",
        //                 "${FOLDER_REPORT_MAN}/report2may",
        //                 "${FOLDER_REPORT_MAN}/report_all",
        //                 "${ROOT_MAC}/push_chat_bot",
        //                 "${ROOT_MAC}/data_jira",
        //                 "${ROOT_MAC}/data_jira/man",
        //                 "${ROOT_MAC}/data_jira/ios",
        //                 "${ROOT_MAC}/data_jira/postouch",
        //                 "${ROOT_MAC}/data_jira/android",
        //                 "${ROOT_MAC}/data_jira/web",
        //                 "${FOLDER_AUTO_MAN}"
        //             ]
        //             folders.each { folder ->
        //                 if (!fileExists(folder)) {
        //                     sh "mkdir -p ${folder}"
        //                 }
        //             }
        //         }
        //     }
        // }
    stage('Checkout') {
                steps {
                        checkout([$class: 'GitSCM',
                            branches: [[name: '*/release']],
                            userRemoteConfigs: [[credentialsId: 'MAC',
                                            url: 'https://gitlab.citigo.com.vn/kvfnb/automation-test/kiotviet-fnb-automation-test-app-man']],
                                extensions: [[$class: 'RelativeTargetDirectory', 
                                relativeTargetDir: "${FOLDER_AUTO_MAN}"]]])    
                }
            }
        stage('Delete All File .apk') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${DOWNLOAD_DIR_OLD}") {
                            sh "rm -r ${DOWNLOAD_DIR_OLD}/*.apk"
                        }
                    }
                }
            }
         // Hiện tại source folder file apk old và new để cùng nhau nên là chỉ cần xóa 1 cái là đc
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
        stage('Download OLD Version App Man Android') {
            steps {
                dir("${DOWNLOAD_DIR_OLD}") {
                    script {
                        sh "curl -o FNB_MAN.apk '${params.link_old}'"
                        if (fileExists('FNB_MAN.apk')) {
                            echo "Download old version app man android success"
                        } else {
                            error "Download old version error"
                        }
                    }
                }
            }
        }
        stage('Download NEW Version App Man Android') {
            steps {
                dir("${DOWNLOAD_DIR_NEW}") {
                    script {
                        sh "curl -o FNB_MAN_NEW.apk '${params.link_new}'"
                        if (fileExists('FNB_MAN_NEW.apk')) {
                            echo "Download new version app man android success"
                        } else {
                            error "Download new version error"
                        }
                    }
                }
            }
        }
        stage('Start Appium Servers') {
            steps {
                script {
                    startAppiumServer(
                        env.APPIUM_SERVER_PORT_1,
                        env.APPIUM_SERVER_SYSTEM_PORT_1,
                        env.APPIUM_SERVER_UDID_1,
                        env.APPIUM_AUTOMATION_PLATFORM_ANDROID,
                        env.APPIUM_AUTOMATION_NAME_ANDROID,
                        env.IP_ADDRESS
                    )
                   
                    startAppiumServer(
                        env.APPIUM_SERVER_PORT_2,
                        env.APPIUM_SERVER_SYSTEM_PORT_2,
                        env.APPIUM_SERVER_UDID_2,
                        env.APPIUM_AUTOMATION_PLATFORM_ANDROID,
                        env.APPIUM_AUTOMATION_NAME_ANDROID,
                        env.IP_ADDRESS
                    )
                    
                }
            }
        }
        stage('Run Tag MAN_SYS2MAY') {
            steps {
                dir("${FOLDER_AUTO_MAN}") {
                         script {
                    sh "sleep 3"
                    runRobotTests(FOLDER_AUTO_MAN,
                    FOLDER_REPORT_3, 
                   "MAN_SYS2MAY",
                    RETAILER_1, IS_HEADLESS, 
                    APPIUM_SERVER_DEVICE_NAME_1, APPIUM_SERVER_DEVICE_NAME_2, 
                    APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_SYSTEM_PORT_1, APPIUM_SERVER_SYSTEM_PORT_2,
                    OS_TYPE, PATH_ANDROID_POS, UPDATE_APP)
            }
                }
            }
        }
        stage("Parallel Test") {
        parallel {
            stage("Run Tag MAN_SYS1") {
                steps {
                     dir("${FOLDER_AUTO_MAN}") {
                         script {
                    runRobotTests(FOLDER_AUTO_MAN,
                    FOLDER_REPORT_1, 
                   "MAN_SYS1",
                    RETAILER_1, IS_HEADLESS, 
                    APPIUM_SERVER_DEVICE_NAME_1, APPIUM_SERVER_DEVICE_NAME_2, 
                    APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_SYSTEM_PORT_1, APPIUM_SERVER_SYSTEM_PORT_2,
                    OS_TYPE, PATH_ANDROID_POS, UPDATE_APP)
            }
                }
                }
            }
            stage("Run Tag MAN_SYS2") {
                steps {
                     dir("${FOLDER_AUTO_MAN}") {
                         script {
                    sh "sleep 5"
                    runRobotTests(FOLDER_AUTO_MAN,
                    FOLDER_REPORT_2, 
                   "MAN_SYS2",
                    RETAILER_2, IS_HEADLESS, 
                    APPIUM_SERVER_DEVICE_NAME_2, APPIUM_SERVER_DEVICE_NAME_1,
                    APPIUM_SERVER_UDID_2, APPIUM_SERVER_UDID_1, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_2, REMOTE_NODE_1, APPIUM_SERVER_SYSTEM_PORT_2, APPIUM_SERVER_SYSTEM_PORT_1, 
                    OS_TYPE, PATH_ANDROID_POS, UPDATE_APP)
            }
                }
                }
            }
        }
        }
        stage('Run Rebot Merge File') {
            steps {
                  catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                script {
                    def existingFiles = []
                    for (String outputFile : env.OUTPUT_FILES_REMOVE_KEY_XML.split(",")) {
                      def trimmedOutputFile = outputFile.trim()
                        if (fileExists(trimmedOutputFile)) {
                            existingFiles.add(trimmedOutputFile)
                        } else {
                            echo "File not found: ${trimmedOutputFile}"
                        }
                    }
                    if (!existingFiles.isEmpty()) {
                        def filesToMerge = existingFiles.join(' ')
                        echo "${filesToMerge}"
                        sh "rebot --outputdir ${FOLDER_REPORT_4} --merge --output ${MERGED_OUTPUT_XML} -l ${MERGED_OUTPUT_LOG} -r ${MERGED_OUTPUT_REPORT}  ${filesToMerge}"
                    } else {
                        echo "No files to merge."
                    }
                }
                  }
            }
        } 
        stage('Push Infor With Botchat') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                  script {
                    sh """
                source ${HTTPLIB2_ENV} &&
                python3 ${PUSH_CHAT_BOT_PY} ${MERGED_OUTPUT_XML} ${params.Jira_Xray_Ticket} "${CLIENT_RUN_AUTO}" 
            """
                }
            }
            }
        }
        stage('Push Jira-Descriptions') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                  script {
                    sh """
                source ${JIRA_ENV} &&
                python3 ${PUSH_JIRA_DESCRIPTIONS}   --file_path=${MERGED_OUTPUT_LOG}  --output_path=${JIRA_OUTPUT_PDF}  --xml_file_path=${MERGED_OUTPUT_XML}  --mapping_tag=${JIRA_DATA_TICKET_TXT}  --id_ticket=${params.Jira_Xray_Ticket}
            """
                }
            }
            }
        }

    }
}