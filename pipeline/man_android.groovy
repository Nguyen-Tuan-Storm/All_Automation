def ipAddress
pipeline {
    agent any
    environment {
                REMOTE_NODE_1 = ''
                REMOTE_NODE_2 = ''
                REMOTE_NODE_3 = ''
                REMOTE_NODE_4 = ''
                ROOT_MAC = "/Users/thanh.pc/Desktop"
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
                PUSH_CHAT_BOT_PY = "${ROOT_MAC}/push_chat_bot/bot.py"
                PUSH_JIRA_DESCRIPTIONS="${ROOT_MAC}/push_chat_bot/jira.py"
                HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env/bin/activate"
                JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv/bin/activate"
                CLIENT_RUN_AUTO = "ANDROID MAN SYSTEM - UPDATE APP"
            }
    stages {
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
        stage('Check And Create Report Folders') {
            steps {
                script {
                    def folders = [
                        "${FOLDER_REPORT_MAN}",
                        "${FOLDER_REPORT_MAN}/report1",
                        "${FOLDER_REPORT_MAN}/report2",
                        "${FOLDER_REPORT_MAN}/report2may",
                        "${FOLDER_REPORT_MAN}/report_all"
                    ]
                    folders.each { folder ->
                        if (!fileExists(folder)) {
                            sh "mkdir -p ${folder}"
                        }
                    }
                }
            }
    }

       stage('Delete all .apk files app Man') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    dir("${DOWNLOAD_DIR_NEW}") {
                        sh "rm -r ${DOWNLOAD_DIR_NEW}/*.apk"
                    }
                }
            }
        }

        stage('Get IP Address') {
            steps {
                dir("${ROOT_MAC}"){
                sh "chmod +x get_ip_address.sh"
                script {
                ipAddress = sh(returnStdout: true, script: 'sh get_ip_address.sh').trim()
                REMOTE_NODE_1 = "http://${ipAddress}:4727/wd/hub"
                REMOTE_NODE_2 = "http://${ipAddress}:4728/wd/hub"
                REMOTE_NODE_3 = "http://${ipAddress}:4729/wd/hub"
                REMOTE_NODE_4 = "http://${ipAddress}:4730/wd/hub"
                }
            }
         }
        }
         stage('Download version app Android old version and Rename APK') {
        steps {
            dir('/Users/thanh.pc/Desktop/Automation_Android_Jenkins/drivers') {
                script {
                    // Thực hiện tải xuống tệp APK
                    sh "curl -o app_old.apk '${params.link_old}'"

                    // Kiểm tra xem tệp APK đã tải xuống thành công hay không
                    if (fileExists('app_old.apk')) {
                        // Nếu thành công, đổi tên tệp APK thành AND-FNB-POS.apk
                        sh "mv app_old.apk AND-FNB-POS.apk"
                    } else {
                        error 'Lỗi: Không thể tải xuống tệp APK old.'
                    }
                }
            }
        }
    }
        // stage('Download version app Android new version') {
        //     steps {
        //         dir('/Users/thanh.pc/Desktop/Automation_Android_Jenkins/drivers') {
        //             sh "gdrive download --force ${params.link_new}"
        //             sh "mv FNB-41642.apk AND-FNB-POS-AF.apk"
        //         }
        //     }
        // }
    stage('Download version app Android new version and Rename APK') {
        steps {
            dir('/Users/thanh.pc/Desktop/Automation_Android_Jenkins/drivers') {
                script {
                    // Thực hiện tải xuống tệp APK
                    sh "curl -o app.apk '${params.link_new}'"

                    // Kiểm tra xem tệp APK đã tải xuống thành công hay không
                    if (fileExists('app.apk')) {
                        // Nếu thành công, đổi tên tệp APK thành AND-FNB-POS.apk
                        sh 'mv app.apk AND-FNB-POS-AF.apk'
                    } else {
                        error 'Lỗi: Không thể tải xuống tệp APK new.'
                    }
                }
            }
        }
    }
        stage('Download version app IOS OLD') {
            steps {
                dir("${DOWNLOAD_DIR_OLD}") {
                    sh "gdrive download --force ${params.driveId_old} && unzip -o FnB.zip"
                }
            }
        }
        stage('Download version app IOS NEW') {
            steps {
                dir("${DOWNLOAD_DIR_NEW}") {
                    sh "gdrive download --force ${params.driveId_new} && unzip -o FnB.zip"
                    sh "mv ${DOWNLOAD_DIR_NEW}/FnB.app     ${DOWNLOAD_DIR_NEW}/FnB_new.app"
                }
            }
        }
        stage('Remove all, delete file and start hub node') {
            parallel {
                 stage('Delete Fnb.zip IOS OLD') {
            steps {
                dir("${DOWNLOAD_DIR_OLD}") {
                    sh "chmod +x ${DOWNLOAD_DIR_OLD}/FnB.zip"
                    sh "rm -r ${DOWNLOAD_DIR_OLD}/FnB.zip"
                }
            }
        }
        stage('Delete Fnb.zip IOS NEW') {
            steps {
                dir("${DOWNLOAD_DIR_NEW}") {
                    sh "chmod +x ${DOWNLOAD_DIR_NEW}/FnB.zip"
                    sh "rm -r ${DOWNLOAD_DIR_NEW}/FnB.zip"
                }
            }
        }
        stage('Start hub and node') {
            steps {
                dir("${DOWNLOAD_DIR_OLD}") {
                    sh "open -a Terminal.app   ./start_selenium_grid_hub.sh"
                    sh "Sleep   2"
                    sh "open -a Terminal.app   ./start_selenium_grid_node_1.sh"
                    sh "Sleep   2"
                    sh "open -a Terminal.app   ./start_selenium_grid_node_2.sh"
                    sh "Sleep   2"
                    sh "open -a Terminal.app   ./start_selenium_grid_node_3.sh"
                    sh "Sleep   2"
                    sh "open -a Terminal.app   ./start_selenium_grid_node_4.sh"
                    sh "Sleep   2"
                }
            }
        }
        stage('Remove report folder 1') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    dir("${FOLDER_AUTO_IOS}") {
                        sh "rm -r ../report1/*"
                    }
                }
            }
        }
        stage('Remove report folder 2') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    dir("${FOLDER_AUTO_IOS}") {
                        sh "rm -r ../report2/*"
                    }
                }
            }
        }
        stage('Remove report folder 3') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    dir("${FOLDER_AUTO_IOS}") {
                        sh "rm -r ../report2may/*"
                    }
                }
            }
        }
        stage('Remove report folder report_all ios') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    dir("${FOLDER_AUTO_IOS}") {
                        sh "rm -r ${FOLDER_REPORT_4}/*"
                    }
                }
            }
        }
            }
        }       
        stage('Run automation') {
            parallel {
                stage('Thread 1') {
                    steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
                                 --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false  \
                                 --variable remote_node_1:${REMOTE_NODE_1} --variable port_1:8100  --variable update_app:True -i DEL_ALL_MHTN --output ../report1/outputdel.xml   prepare-data"
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_1} --variable port_1:8100  --variable update_app:True -o ../report1/output.xml -l ../report1/output.html -r ../report1/report.html  -i  SYS1   testsuites"
                            }
                        }
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_1} --variable port_1:8100 --variable update_app:True -i DEL_ALL_MHTN --output ../report1/outputdel.xml   prepare-data"
                                sh "robot --rerunfailed  ../report1/output.xml -o ../report1/output1.xml -l ../report1/output1.html -r ../report1/report1.html --variable app_name:POS \
                                --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False --variable isHeadless:${params.statusSimulator} \
                                --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false --variable remote_node_1:${REMOTE_NODE_1} \
                                 --variable port_1:8100 --variable update_app:True  testsuites"
                            }
                        }
                    }
                }
                stage('Thread 2') {
                    steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True -i DEL_ALL_MHTN --output ../report2/outputdel.xml   prepare-data"
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True -o ../report2/output.xml -l ../report2/output.html -r ../report2/report.html   -i SYS2  testsuites"
                            }
                        }
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True -i DEL_ALL_MHTN --output ../report2/outputdel.xml   prepare-data"
                                sh "robot --rerunfailed  ../report2/output.xml -o ../report2/output1.xml -l ../report2/output1.html -r ../report2/report1.html --variable app_name:POS \
                                --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True  testsuites"
                            }
                        }
                    }
                }
                stage('Thread 3') {
                    steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_3} --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102 --variable update_app:True --variable port_2:8103 -i DEL_ALL_MHTN \
                                --output ../report2may/outputdel.xml   prepare-data"
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_3} --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102 --variable update_app:True --variable port_2:8103 -o ../report2may/output.xml -l \
                                ../report2may/output.html -r ../report2may/report.html -i  SYS2M  testsuites"
                            }
                        }
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True --variable using_browserstack:False \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_3} --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102 --variable update_app:True --variable port_2:8103 -i DEL_ALL_MHTN --output ../report2may/outputdel.xml   prepare-data"
                                sh "robot --rerunfailed  ../report2may/output.xml -o ../report2may/output1.xml -l ../report2may/output1.html -r ../report2may/report1.html --variable app_name:POS \
                                --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True --variable using_browserstack:False --variable isHeadless:${params.statusSimulator} \
                                --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false --variable remote_node_1:${REMOTE_NODE_3} \
                                --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102  --variable port_2:8103 --variable update_app:True  testsuites"
                            }
                        }
                    }
                }
            }
        }
            stage('Thread remove key report1 1') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${FOLDER_AUTO_IOS}") {
                            sh "rebot --removekeywords all --output   ../report1/output_final.xml   ../report1/output.xml"
                    }
                }
            }
        }
            stage('Thread remove key report1 2') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${FOLDER_AUTO_IOS}") {
                            sh "rebot --removekeywords all --output   ../report1/output_final_1.xml   ../report1/output1.xml"
                    }
                }
            }
        }
       
            stage('Thread remove key report2 1') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${FOLDER_AUTO_IOS}") {
                            sh "rebot --removekeywords all --output   ../report2/output_final.xml   ../report2/output.xml"
                    }
                }
            }
        }
            stage('Thread remove key report2 2') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${FOLDER_AUTO_IOS}") {
                            sh "rebot --removekeywords all --output   ../report2/output_final_1.xml   ../report2/output1.xml"
                    }
                }
            }
        }
            stage('Thread remove key report2may 1') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${FOLDER_AUTO_IOS}") {
                            sh "rebot --removekeywords all --output   ../report2may/output_final.xml   ../report2may/output.xml"
                    }
                }
            }
        }
            stage('Thread remove key report2may 2') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${FOLDER_AUTO_IOS}") {
                            sh "rebot --removekeywords all --output   ../report2may/output_final_1.xml   ../report2may/output1.xml"
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
                        sh "rebot --outputdir ${FOLDER_REPORT_3} --merge --output ${MERGED_OUTPUT_XML} -l ${MERGED_OUTPUT_LOG} -r ${MERGED_OUTPUT_REPORT}  ${filesToMerge}"
                    } else {
                        echo "No files to merge."
                    }
                }
                  }
            }
        }  
        //   stage('Push Xray') {
        //     steps {
        //         catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //              dir("${FOLDER_AUTO_IOS}") {
        //                         sh "java -DprojectKey=FNB -DtestExecKey=${params.Jira_Xray_Ticket}  -Dfile=${MERGED_OUTPUT_XML} -jar xray_importer.jar"
        //                 }
        //             }
        //     }
        //         }
        stage('Push infor with botchat') {
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
        stage('Close all terminal and simulator last') {
            steps {
                dir("${FOLDER_AUTO_IOS}") {
                    sh "Sleep 2"
                    sh "pkill -a Terminal"
                    sh "killall \"Simulator\" || true"
                }
            }
        }
    }
}
