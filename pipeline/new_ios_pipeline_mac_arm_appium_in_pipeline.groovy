def isValidPid(pid) {
    return pid.isInteger() && pid.toInteger() > 0
}

def startAppiumServer(port, systemPort, udid, platform, automationName, ipAddress) {
    sleep(time: 2, unit: 'SECONDS')

    def nodeConfig = """
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
       --default-capabilities '{"systemPort": ${systemPort}, "udid": "${udid}", "newCommandTimeout": 600, "deviceReadyTimeout": 120, "appWaitDuration": 80000}' \
       --nodeconfig '${nodeConfig}' > /dev/null 2>&1 &

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

def runRobotTests(folder_run, folder_report, tag_run, retailer, isHeadless, udid_1, udid_2, platformVersion, remote_node_1, remote_node_2, port_1, port_2, update_app) {
    try {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${folder_run}") {
                sh """
                    robot --variable app_name:POS --variable retailer:${retailer} --variable using_browserstack:False --variable username:admin --variable enable_log:True  \
                    --variable isHeadless:${isHeadless} --variable device:iphone --variable deviceName:iphone --variable platformVersion:${platformVersion} \
                    --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                    -o ${folder_report}/del.xml -r ${folder_report}/del.html -l ${folder_report}/log_del.html prepare-data
                """
                sh """
                    robot --variable app_name:POS --variable retailer:${retailer} --variable using_browserstack:False --variable username:admin --variable enable_log:True  \
                    --variable isHeadless:${isHeadless} --variable device:iphone --variable deviceName:iphone --variable platformVersion:${platformVersion} \
                    --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2}   --variable update_app:${update_app} -i ${tag_run} \
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
                    robot --variable app_name:POS --variable retailer:${retailer} --variable using_browserstack:False --variable username:admin --variable enable_log:True  \
                    --variable isHeadless:${isHeadless} --variable device:iphone --variable deviceName:iphone --variable platformVersion:${platformVersion} \
                    --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                    --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                    -o ${folder_report}/del.xml -r ${folder_report}/del.html -l ${folder_report}/log_del.html prepare-data
                """
                sh """
                    robot --rerunfailed ${folder_report}/output.xml -o ${folder_report}/output1.xml -r ${folder_report}/output1.html -l ${folder_report}/log1.html  \
                    --variable app_name:POS --variable retailer:${retailer} --variable using_browserstack:False --variable username:admin --variable enable_log:True \
                    --variable isHeadless:${isHeadless} --variable device:iphone --variable deviceName:iphone --variable platformVersion:${platformVersion} \
                    --variable udid_1:${udid_1} --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
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
        // sh(returnStdout: true, script: "ifconfig | grep 'inet ' | grep -v 127.0.0.1 | awk '{print \$2}' | head -n 1").trim()
        IP_ADDRESS = "192.168.134.250"
        // ANDROID_HOME = "/Users/${USER_NAME}/Library/Android/sdk"
        // PATH = "${env.PATH}:${env.ANDROID_HOME}/emulator:${env.ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/emulator"

        APPIUM_AUTOMATION_PLATFORM_ANDROID='Android'
        APPIUM_AUTOMATION_PLATFORM_IOS='iOS'
        APPIUM_AUTOMATION_NAME_ANDROID='UiAutomator2'
        APPIUM_AUTOMATION_PLATFORM_VERSION='15.5'
        APPIUM_AUTOMATION_NAME_IOS='XCUITest'
        IS_HEADLESS = "false"
        //IS_HEADLESS = "true"
        UPDATE_APP = "False"

        APPIUM_SERVER_PORT_1 = '4727'
        APPIUM_SERVER_SYSTEM_PORT_1 = '8800'
        // iPhone 12 Pro Simulator (15.5) 
        APPIUM_SERVER_UDID_1 = '780FDDB1-7E59-4203-A11F-FA91F1410464'
        APPIUM_SERVER_DEVICE_NAME_1 = 'iPhone'
        RETAILER_1 = "autoios22"
        
        APPIUM_SERVER_PORT_2 = '4728'
        APPIUM_SERVER_SYSTEM_PORT_2 = '8801'
        // iPhone 13 Simulator (15.5) 
        APPIUM_SERVER_UDID_2 = 'AFD76319-DB6F-4152-AD90-089BCA42F46F'
        APPIUM_SERVER_DEVICE_NAME_2 = 'iPhone'
        RETAILER_2 = "autoios23"

        APPIUM_SERVER_PORT_3 = '4729'
        APPIUM_SERVER_SYSTEM_PORT_3 = '8802'
        // iPhone 13 Pro Simulator (15.5)
        APPIUM_SERVER_UDID_3 = '8D80A693-526F-43E8-ABEA-D4C5B9C2C156'
        APPIUM_SERVER_DEVICE_NAME_3 = 'iPhone'

        APPIUM_SERVER_PORT_4 = '4730'
        APPIUM_SERVER_SYSTEM_PORT_4 = '8803'
        // iPhone 12 Pro Max Simulator (15.5)
        APPIUM_SERVER_UDID_4 = '6F68E34F-286F-4FCE-B757-9138907E6F38'
        APPIUM_SERVER_DEVICE_NAME_4 = 'iPhone'
        APPIUM_SERVER_COMMAND_TIMEOUT = '300'
        RETAILER_3 = "autoandroid4"

        // Create List Tag
        TAG_RUN_1 = "SYS1"
        TAG_RUN_2 = "SYS2"
        TAG_RUN_3 = "SYS3"
        TAG_RUN_2MAY_1 = "SYS2M_1"
        TAG_RUN_2MAY_2 = "SYS2M_2"
        
     

        REMOTE_NODE_1 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_1}/wd/hub"
        REMOTE_NODE_2 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_2}/wd/hub"
        REMOTE_NODE_3 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_3}/wd/hub"
        REMOTE_NODE_4 = "http://${env.IP_ADDRESS}:${env.APPIUM_SERVER_PORT_4}/wd/hub"

        FOLDER_AUTO_IOS = "${ROOT_MAC}/Auto_IOS_Jenkins_NewUI"
        DOWNLOAD_DIR_OLD = "${FOLDER_AUTO_IOS}/drivers"
        DOWNLOAD_DIR_NEW = "${FOLDER_AUTO_IOS}/drivers"
        // PATH_ANDROID_POS = "${ROOT_MAC}/Automation_Android_Jenkins/drivers/AND-FNB-POS-AF.apk"

        FOLDER_REPORT_IOS = "${ROOT_MAC}/report_ios"
        FOLDER_REPORT_1 = "${FOLDER_REPORT_IOS}/report1"
        FOLDER_REPORT_2 = "${FOLDER_REPORT_IOS}/report2"
        FOLDER_REPORT_3 = "${FOLDER_REPORT_IOS}/report3"
        FOLDER_REPORT_4 = "${FOLDER_REPORT_IOS}/report2may_1"
        FOLDER_REPORT_5 = "${FOLDER_REPORT_IOS}/report2may_2"
        FOLDER_REPORT_6 = "${FOLDER_REPORT_IOS}/report_all"

        OUTPUT_FILES_REMOVE_KEY_XML = """${FOLDER_REPORT_1}/output_final.xml,${FOLDER_REPORT_1}/output_final_1.xml,${FOLDER_REPORT_2}/output_final.xml,${FOLDER_REPORT_2}/output_final_1.xml,${FOLDER_REPORT_3}/output_final.xml,${FOLDER_REPORT_3}/output_final_1.xml,${FOLDER_REPORT_4}/output_final.xml,${FOLDER_REPORT_4}/output_final_1.xml,${FOLDER_REPORT_5}/output_final.xml,${FOLDER_REPORT_5}/output_final_1.xml"""
        MERGED_OUTPUT_XML = "${FOLDER_REPORT_6}/merge.xml"
        MERGED_OUTPUT_LOG = "${FOLDER_REPORT_6}/merge_log.html"
        MERGED_OUTPUT_REPORT = "${FOLDER_REPORT_6}/merge_report.html"
        JIRA_OUTPUT_PDF = "${FOLDER_REPORT_6}/merge_log.pdf"
        JIRA_DATA_TICKET_TXT = "${ROOT_MAC}/data_jira/ios/data.txt"
        PUSH_CHAT_BOT_PY = "${ROOT_MAC}/push_chat_bot/bot.py"
        PUSH_JIRA_DESCRIPTIONS="${ROOT_MAC}/push_chat_bot/jira.py"
        // CREATE_HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env1234"
        // CREATE_JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv1234"
        HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env/bin/activate"
        JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv/bin/activate"
        CLIENT_RUN_AUTO = "IOS POS SYSTEM - UPDATE APP"
    }
    stages {
        stage('Check And Create Report Folders') {
            steps {
                script {
                    def folders = [
                        "${FOLDER_REPORT_IOS}",
                        "${FOLDER_REPORT_1}",
                        "${FOLDER_REPORT_2}",
                        "${FOLDER_REPORT_3}",
                        "${FOLDER_REPORT_4}",
                        "${FOLDER_REPORT_5}",
                        "${FOLDER_REPORT_6}",
                        // "${ROOT_MAC}/push_chat_bot",
                        // "${ROOT_MAC}/data_jira",
                        // "${ROOT_MAC}/data_jira/man",
                        // "${ROOT_MAC}/data_jira/ios",
                        // "${ROOT_MAC}/data_jira/postouch",
                        // "${ROOT_MAC}/data_jira/android",
                        // "${ROOT_MAC}/data_jira/web",
                        "${FOLDER_AUTO_IOS}"
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
                         branches: [[name: '*/new-ui']],
                         userRemoteConfigs: [[credentialsId: 'MAC',
                                          url: 'https://gitlab.citigo.com.vn/kvfnb/automation-test/kv-fnb-ios-auto-test']],
                      extensions: [
                      [$class: 'RelativeTargetDirectory', relativeTargetDir: "${FOLDER_AUTO_IOS}"], 
                      [$class: 'CloneOption', timeout: 30],
                      [$class: 'CheckoutOption', timeout: 30],
                  ]
        ])
             }
        }
        stage("Start list simulator") {
    steps {
        script {
            def startSimulatorCommands = [
                "xcrun simctl boot ${APPIUM_SERVER_UDID_1}",
                "xcrun simctl boot ${APPIUM_SERVER_UDID_2}",
                "xcrun simctl boot ${APPIUM_SERVER_UDID_3}",
                "xcrun simctl boot ${APPIUM_SERVER_UDID_4}"
            ]
            
            def parallelStages = [:]  // Tạo đối tượng chứa các stage chạy song song

            startSimulatorCommands.eachWithIndex { command, index ->
                parallelStages["Start Simulator ${index + 1}"] = {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh command
                    }
                }
            }

            parallel parallelStages
           // Mở giao diện Simulator lên màn hình
            sh "open -a Simulator"
        }
    }
}

        stage('Delete All File .zip') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${DOWNLOAD_DIR_OLD}") {
                            sh "rm -r ${DOWNLOAD_DIR_OLD}/*.zip"
                        }
                    }
                }
            }
        stage('Delete All .app OLD Files IOS POS') {
                 steps {
                     catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                         dir("${DOWNLOAD_DIR_OLD}") {
                             sh "rm -r ${DOWNLOAD_DIR_OLD}/*.app"
                         }
                     }
                 }
             }
        //Hiện tại source folder file .app old và .app new để cùng nhau nên là chỉ cần xóa 1 cái là đc
        // stage('Delete All .app NEW Files IOS POS') {
        //          steps {
        //              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //                  dir("${DOWNLOAD_DIR_NEW}") {
        //                      sh "rm -r ${DOWNLOAD_DIR_NEW}/*.app"
        //                  }
        //              }
        //          }
        //      }
         stage('Cleanup Report Folders') {
            steps {
                script {
                    def folders = sh(script: "ls -d ${env.FOLDER_REPORT_IOS}/*/", returnStdout: true).trim().split('\n')
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
        stage('Download Version App POS_IOS OLD') {
            steps {
                dir("${DOWNLOAD_DIR_OLD}") {
                    sh """
                        gdrive download --force ${params.driveId_old}
                        zip_file=\$(ls *.zip)
                        unzip -o \$zip_file 
                        rm \$zip_file
                    """
                }
            }
        }
        stage('Download Version App POS_IOS NEW') {
            steps {
                dir("${DOWNLOAD_DIR_NEW}") {
                    sh """
                        gdrive download --force ${params.driveId_new}
                        zip_file=\$(ls *.zip)
                        mkdir temp_unzip_dir
                        unzip -o \$zip_file -d temp_unzip_dir
                        rm \$zip_file
                        mv temp_unzip_dir/FnB.app FnB_new.app
                        rm -rf temp_unzip_dir
                    """
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
                        env.APPIUM_AUTOMATION_PLATFORM_IOS,
                        env.APPIUM_AUTOMATION_NAME_IOS,
                        env.IP_ADDRESS
                    )
                   
                    startAppiumServer(
                        env.APPIUM_SERVER_PORT_2,
                        env.APPIUM_SERVER_SYSTEM_PORT_2,
                        env.APPIUM_SERVER_UDID_2,
                        env.APPIUM_AUTOMATION_PLATFORM_IOS,
                        env.APPIUM_AUTOMATION_NAME_IOS,
                        env.IP_ADDRESS
                    )

                    startAppiumServer(
                        env.APPIUM_SERVER_PORT_3,
                        env.APPIUM_SERVER_SYSTEM_PORT_3,
                        env.APPIUM_SERVER_UDID_3,
                        env.APPIUM_AUTOMATION_PLATFORM_IOS,
                        env.APPIUM_AUTOMATION_NAME_IOS,
                        env.IP_ADDRESS
                    )
                    startAppiumServer(
                        env.APPIUM_SERVER_PORT_4,
                        env.APPIUM_SERVER_SYSTEM_PORT_4,
                        env.APPIUM_SERVER_UDID_4,
                        env.APPIUM_AUTOMATION_PLATFORM_IOS,
                        env.APPIUM_AUTOMATION_NAME_IOS,
                        env.IP_ADDRESS
                    )
                    
                }
            }
        }
        stage("Parallel test 2MAY") {
        parallel {
            stage('Run Tag SYS2M_1') {
                steps {
                    dir("${FOLDER_AUTO_IOS}") {
                            script {
                        sh "sleep 3"
                        runRobotTests(FOLDER_AUTO_IOS,
                        FOLDER_REPORT_4, 
                    TAG_RUN_2MAY_1,
                        RETAILER_1, IS_HEADLESS, 
                        APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_VERSION,
                        REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_SYSTEM_PORT_1, APPIUM_SERVER_SYSTEM_PORT_2,
                        UPDATE_APP)
                }
                    }
                }
            }
            stage('Run Tag SYS2M_2') {
                steps {
                    dir("${FOLDER_AUTO_IOS}") {
                            script {
                        sh "sleep 3"
                        runRobotTests(FOLDER_AUTO_IOS,
                        FOLDER_REPORT_5, 
                        TAG_RUN_2MAY_2,
                        RETAILER_3, IS_HEADLESS, 
                        APPIUM_SERVER_UDID_4, APPIUM_SERVER_UDID_4, APPIUM_AUTOMATION_PLATFORM_VERSION,
                        REMOTE_NODE_3, REMOTE_NODE_4, APPIUM_SERVER_SYSTEM_PORT_3, APPIUM_SERVER_SYSTEM_PORT_4,
                        UPDATE_APP)
                }
                    }
                }
            }
        }
        }
        stage('Shutdown APPIUM_SERVER_UDID_4') {
            steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        timeout(time: 10, unit: 'SECONDS') {
                            sh "xcrun simctl shutdown ${APPIUM_SERVER_UDID_4}"
                        }
                    }
            }
        }
        stage("Parallel test SYS1 SYS2 SYS3") {
        parallel {
            stage("Run Tag SYS1") {
                steps {
                     dir("${FOLDER_AUTO_IOS}") {
                         script {
                    runRobotTests(FOLDER_AUTO_IOS,
                    FOLDER_REPORT_1, 
                    TAG_RUN_1,
                    RETAILER_1, IS_HEADLESS, 
                    APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_SYSTEM_PORT_1, APPIUM_SERVER_SYSTEM_PORT_2,
                    UPDATE_APP)
            }
                }
                }
            }
            stage("Run Tag SYS2") {
                steps {
                     dir("${FOLDER_AUTO_IOS}") {
                         script {
                    runRobotTests(FOLDER_AUTO_IOS,
                    FOLDER_REPORT_2, 
                    TAG_RUN_2,
                    RETAILER_2, IS_HEADLESS, 
                    APPIUM_SERVER_UDID_2, APPIUM_SERVER_UDID_1, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_2, REMOTE_NODE_1, APPIUM_SERVER_SYSTEM_PORT_2, APPIUM_SERVER_SYSTEM_PORT_1,
                    UPDATE_APP)
            }
                }
                }
            }
            stage("Run Tag SYS3") {
                steps {
                     dir("${FOLDER_AUTO_IOS}") {
                         script {
                    runRobotTests(FOLDER_AUTO_IOS,
                    FOLDER_REPORT_3, 
                    TAG_RUN_3,
                    RETAILER_3, IS_HEADLESS, 
                    APPIUM_SERVER_UDID_3, APPIUM_SERVER_UDID_4, APPIUM_AUTOMATION_PLATFORM_VERSION,
                    REMOTE_NODE_3, REMOTE_NODE_4, APPIUM_SERVER_SYSTEM_PORT_3, APPIUM_SERVER_SYSTEM_PORT_4,
                    UPDATE_APP)
            }
                }
                }
            }
        }
        }
        stage('Run rebot merge file') {
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
        stage('Push infor with botchat') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                  script {
                    sh "sleep 3"
                    sh """
                source ${HTTPLIB2_ENV} &&
                python3 ${PUSH_CHAT_BOT_PY} ${MERGED_OUTPUT_XML} ${params.Jira_Xray_Ticket} "${CLIENT_RUN_AUTO}" 
            """
                }
            }
            }
        }
        stage('Push jira-descriptions') {
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
    post {
        always {
            script {
                // def shutdownCommands = [
                //     "xcrun simctl shutdown ${APPIUM_SERVER_UDID_1}",
                //     "xcrun simctl shutdown ${APPIUM_SERVER_UDID_2}",
                //     "xcrun simctl shutdown ${APPIUM_SERVER_UDID_3}",
                //     "xcrun simctl shutdown ${APPIUM_SERVER_UDID_4}"
                // ]

                // shutdownCommands.each { command ->
                //     catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                //         timeout(time: 10, unit: 'SECONDS') {
                //             sh command
                //         }
                //     }
                // }
                 dir("${FOLDER_AUTO_IOS}") {
                    sh "sleep 2"
                    sh "killall \"Simulator\" || true"
                }
            }
        }
    }    
}
