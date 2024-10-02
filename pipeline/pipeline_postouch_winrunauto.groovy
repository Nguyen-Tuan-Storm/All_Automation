def isValidPid(pid) {
    return pid.isInteger() && pid.toInteger() > 0
}

def startAppiumServer(port, systemPort, udid, platform, automationName, ipAddress) {
    sleep(time: 2, unit: 'SECONDS')

    def nodeConfig = "nodeConfig_${port}.json"
    writeFile(file: nodeConfig, text: """
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
    """)
    bat """
        start /b  appium --port ${port} ^
           --default-capabilities "{\\"systemPort\\": ${systemPort}, \\"udid\\": \\"${udid}\\", \\"newCommandTimeout\\": 600}" ^
           --nodeconfig "${nodeConfig}" > NUL 2>&1
    """

}

def runRobotTests(folder_run, folder_report, tag_run, retailer, isHeadless, adv_1, adv_2, udid_1, udid_2, platformVersion, remote_node_1, remote_node_2, port_1, port_2, update_app) {
    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                bat """
                    robot --variable app_name:POS --variable using_browserstack:False --variable retailer:${retailer} --variable username:admin --variable enable_log:True  \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                    -o ${folder_report}\\del.xml -r ${folder_report}\\del.html -l ${folder_report}\\log_del.html prepare-data
                """
                bat """
                    robot --variable app_name:POS --variable using_browserstack:False --variable retailer:${retailer} --variable username:admin --variable enable_log:True  \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2}   --variable update_app:${update_app} -i ${tag_run} \
                    -o ${folder_report}\\output.xml -r ${folder_report}\\output.html -l ${folder_report}\\log.html testsuites
                """
            }
        }
    } catch (Exception e) {
        echo "Initial Robot test run failed: ${e}"
    }

    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                bat """
                    robot --variable app_name:POS --variable using_browserstack:False --variable retailer:${retailer} --variable username:admin --variable enable_log:True  \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                    -o ${folder_report}\\del.xml -r ${folder_report}\\del.html -l ${folder_report}\\log_del.html prepare-data
                """
                bat """
                    robot --rerunfailed ${folder_report}\\output.xml -o ${folder_report}\\output1.xml -r ${folder_report}\\output1.html -l ${folder_report}\\log1.html  \
                    --variable app_name:POS --variable using_browserstack:False --variable retailer:${retailer} --variable username:admin --variable enable_log:True \
                    --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                    --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2}  --variable update_app:${update_app} -i ${tag_run} testsuites
                """
            }
        }
    } catch (Exception e) {
        echo "Rerun of failed Robot tests failed: ${e}"
    }

    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                bat """
                    rebot --removekeywords all -o ${folder_report}\\output_final.xml ${folder_report}\\output.xml
                """
            }
        }
    } catch (Exception e) {
        echo "Merging of Robot test results failed: ${e}"
    }

    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                bat """
                    rebot --removekeywords all -o ${folder_report}\\output_final_1.xml ${folder_report}\\output1.xml
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
        USER_NAME = "thanh.pc"
        ROOT_MAC = "C:\\Users\\${USER_NAME}\\Desktop"
        IP_ADDRESS = "192.168.134.109"
        ANDROID_HOME = 'C:\\Users\\thanh.pc\\AppData\\Local\\Android\\Sdk'
        ANDROID_SDK_ROOT = 'C:\\Users\\thanh.pc\\AppData\\Local\\Android\\Sdk'
        ANDROID_AVD_HOME = 'C:\\Users\\thanh.pc\\.android\\avd'
        ANDROID_SDK_HOME = 'C:\\Users\\thanh.pc\\.android'
        HOME = 'C:\\Users\\thanh.pc'
        PATH = "${env.PATH};${env.ANDROID_HOME}\\platform-tools;${env.ANDROID_HOME}\\emulator"


        APPIUM_AUTOMATION_PLATFORM_ANDROID='Android'
        APPIUM_AUTOMATION_PLATFORM_IOS='iOS'
        APPIUM_AUTOMATION_NAME_ANDROID='UiAutomator2'
        APPIUM_AUTOMATION_PLATFORM_VERSION='11.0'
        APPIUM_AUTOMATION_NAME_IOS='XCUITest'
        IS_HEADLESS = "false"
        UPDATE_APP = "True"

        APPIUM_SERVER_PORT_1 = '4727'
        APPIUM_SERVER_SYSTEM_PORT_1 = '8800'
        APPIUM_SERVER_UDID_1 = 'emulator-5554'
        APPIUM_SERVER_DEVICE_NAME_1 = 'PT_1'
        RETAILER_1 = "autopostouch1"

        APPIUM_SERVER_PORT_2 = '4728'
        APPIUM_SERVER_SYSTEM_PORT_2 = '8801'
        APPIUM_SERVER_UDID_2 = 'emulator-5556'
        APPIUM_SERVER_DEVICE_NAME_2 = 'PT_2'
        RETAILER_2 = "autopostouch2"
        APPIUM_SERVER_COMMAND_TIMEOUT = '300'

        REMOTE_NODE_1 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_1}/wd/hub"
        REMOTE_NODE_2 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_2}/wd/hub"

        FOLDER_AUTO_POSTOUCH = "${ROOT_MAC}\\Auto_POSTOUCH_Jenkins"
        DOWNLOAD_DIR_OLD = "${FOLDER_AUTO_POSTOUCH}\\drivers"
        DOWNLOAD_DIR_NEW = "${FOLDER_AUTO_POSTOUCH}\\drivers"
        
        FOLDER_REPORT_POSTOUCH = "${ROOT_MAC}\\report_postouch"
        FOLDER_REPORT_1 = "${FOLDER_REPORT_POSTOUCH}\\report1"
        FOLDER_REPORT_2 = "${FOLDER_REPORT_POSTOUCH}\\report2"
        FOLDER_REPORT_4 = "${FOLDER_REPORT_POSTOUCH}\\report_all"

        PACKAGE_POSTOUCH = "net.citigo.kiotviet.fnb.pos.touch.dev"

        OUTPUT_FILES_REMOVE_KEY_XML = """${FOLDER_REPORT_1}\\output_final.xml,${FOLDER_REPORT_1}\\output_final_1.xml,${FOLDER_REPORT_2}\\output_final.xml,${FOLDER_REPORT_2}\\output_final_1.xml"""
        MERGED_OUTPUT_XML = "${FOLDER_REPORT_4}\\merge.xml"
        MERGED_OUTPUT_LOG = "${FOLDER_REPORT_4}\\merge_log.html"
        MERGED_OUTPUT_REPORT = "${FOLDER_REPORT_4}\\merge_report.html"
        JIRA_OUTPUT_PDF = "${FOLDER_REPORT_4}\\merge_log.pdf"
        JIRA_DATA_TICKET_TXT = "${ROOT_MAC}\\data_jira\\postouch\\data.txt"
        PUSH_CHAT_BOT_PY = "${ROOT_MAC}\\push_chat_bot\\bot.py"
        PUSH_JIRA_DESCRIPTIONS="${ROOT_MAC}\\push_chat_bot\\jira.py"
        HTTPLIB2_ENV = "${ROOT_MAC}\\push_chat_bot\\httplib2env\\Scripts\\activate.bat"
        JIRA_ENV = "${ROOT_MAC}\\push_chat_bot\\jiraenv\\Scripts\\activate.bat"
        CLIENT_RUN_AUTO = "POSTOUCH SYSTEM - UPDATE APP"
    }
    stages{
        // stage('Check And Create Report Folders') {
        //     steps {
        //         script {
        //             def folders = [
        //                 "${FOLDER_REPORT_POSTOUCH}",
        //                 "${FOLDER_REPORT_POSTOUCH}\\report1",
        //                 "${FOLDER_REPORT_POSTOUCH}\\report2",
        //                 "${FOLDER_REPORT_POSTOUCH}\\report2may",
        //                 "${FOLDER_REPORT_POSTOUCH}\\report_all",
        //                 "${ROOT_MAC}\\push_chat_bot",
        //                 "${ROOT_MAC}\\data_jira",
        //                 "${ROOT_MAC}\\data_jira\\postouch",
        //                 "${ROOT_MAC}\\data_jira\\ios",
        //                 "${ROOT_MAC}\\data_jira\\android",
        //                 "${ROOT_MAC}\\data_jira\\web",
        //                 "${FOLDER_AUTO_POSTOUCH}"
        //             ]
        //             folders.each { folder ->
        //                 if (!fileExists(folder)) {
        //                     bat "mkdir ${folder}"
        //                 }
        //             }
        //         }
        //     }
        // }
        stage('Checkout') {
                    steps {
                            checkout([$class: 'GitSCM',
                                branches: [[name: '*/jenkins_pt']],
                                userRemoteConfigs: [[credentialsId: 'MAC',
                                                url: 'https://gitlab.citigo.com.vn/kvfnb/automation-test/kiotvietfnb-pos-touch-automation-test']],
                                    extensions: [[$class: 'RelativeTargetDirectory', 
                                    relativeTargetDir: "${FOLDER_AUTO_POSTOUCH}"]]])    
                    }
                }
        stage('Delete All File .apk') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${DOWNLOAD_DIR_OLD}") {
                            bat "del /Q *.apk"
                        }
                    }
                }
            }
        // // Hiện tại source folder file apk old và new để cùng nhau nên là chỉ cần xóa 1 cái là đc
        // stage('Delete All .apk NEW Files') {
        //         steps {
        //             catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //                 dir("${DOWNLOAD_DIR_NEW}") {
        //                      bat "del /Q *.apk"
        //                 }
        //             }
        //         }
        //     }
        stage('Cleanup Report Folders') {
            steps {
                script {
                    // Sử dụng "call" trong lệnh bat để tránh lỗi cú pháp
                    def folders = bat(script: """
                        for /d %%i in ("${env.FOLDER_REPORT_POSTOUCH}\\*") do @echo %%i
                    """, returnStdout: true).trim().split('\r\n')
                    
                    for (folder in folders) {
                        echo "Cleaning folder: ${folder}"
                        try {
                            // Xóa các file trong từng thư mục
                            bat "del /Q \"${folder}\\*.*\""
                        } catch (Exception e) {
                            echo "Files not exist in ${folder}: ${e.message}"
                        }
                    }
                }
            }
        }
        stage('Download OLD Version App POSTOUCH') {
            steps {
                dir("${DOWNLOAD_DIR_OLD}") {
                    script {
                        bat """
                        curl -o "POSTOUCH.apk" "${params.link_old}"
                        """
                        if (fileExists('POSTOUCH.apk')) {
                            echo "Download old version app postouch success"
                        } else {
                            error "Download old version error"
                        }
                    }
                }
            }
        }
        stage('Download NEW Version App POSTOUCH') {
            steps {
                dir("${DOWNLOAD_DIR_NEW}") {
                    script {
                        bat """
                        curl -o POSTOUCH_NEW.apk "${params.link_new}"
                        """
                        if (fileExists('POSTOUCH_NEW.apk')) {
                            echo "Download new version app postouch success"
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
        stage("Parallel Test") {
            parallel {
                stage("Run Tag PT_SYSTEM_1") {
                    steps {
                        dir("${FOLDER_AUTO_POSTOUCH}") {
                            script {
                        runRobotTests(FOLDER_AUTO_POSTOUCH,
                        FOLDER_REPORT_1, 
                        "PT_SYSTEM_1",
                        RETAILER_1, IS_HEADLESS, 
                        APPIUM_SERVER_DEVICE_NAME_1, APPIUM_SERVER_DEVICE_NAME_2, 
                        APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_VERSION,
                        REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_SYSTEM_PORT_1, APPIUM_SERVER_SYSTEM_PORT_2,
                        UPDATE_APP)
                }
                    }
                    }
                }
                stage("Run Tag PT_SYSTEM_2") {
                    steps {
                        dir("${FOLDER_AUTO_POSTOUCH}") {
                            script {
                        runRobotTests(FOLDER_AUTO_POSTOUCH,
                        FOLDER_REPORT_2, 
                        "PT_SYSTEM_2",
                        RETAILER_2, IS_HEADLESS, 
                        APPIUM_SERVER_DEVICE_NAME_2, APPIUM_SERVER_DEVICE_NAME_1,
                        APPIUM_SERVER_UDID_2, APPIUM_SERVER_UDID_1, APPIUM_AUTOMATION_PLATFORM_VERSION,
                        REMOTE_NODE_2, REMOTE_NODE_1, APPIUM_SERVER_SYSTEM_PORT_2, APPIUM_SERVER_SYSTEM_PORT_1, 
                        UPDATE_APP)
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
                        bat "rebot --outputdir ${FOLDER_REPORT_4} --merge --output ${MERGED_OUTPUT_XML} -l ${MERGED_OUTPUT_LOG} -r ${MERGED_OUTPUT_REPORT}  ${filesToMerge}"
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
                        bat """
                        call ${HTTPLIB2_ENV} 
                        python ${PUSH_CHAT_BOT_PY} ${MERGED_OUTPUT_XML} ${params.Jira_Xray_Ticket} "${CLIENT_RUN_AUTO}"
                        """
                    }
                }
            }
    }
        stage('Push Jira-Descriptions') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    script {
                        bat """
                        call ${JIRA_ENV} 
                        python ${PUSH_JIRA_DESCRIPTIONS} --file_path=${MERGED_OUTPUT_LOG} --output_path=${JIRA_OUTPUT_PDF} --xml_file_path=${MERGED_OUTPUT_XML} --mapping_tag=${JIRA_DATA_TICKET_TXT} --id_ticket=${params.Jira_Xray_Ticket}
                        """
                    }
                }
            }
        }
   

        
}
 post {
        always {
            script {
                def uninstallCommands = [
                    "adb -s ${env.APPIUM_SERVER_UDID_1} uninstall ${env.PACKAGE_POSTOUCH}",
                    "adb -s ${env.APPIUM_SERVER_UDID_2} uninstall ${env.PACKAGE_POSTOUCH}"    
                ]

                uninstallCommands.each { command ->
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        bat command
                    }
                }
            }
        }
    }

}

