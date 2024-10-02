def ipAddress
pipeline {
    agent any
    environment {
      REMOTE_NODE_1 = ''
      REMOTE_NODE_2 = ''
      ROOT_MAC = "/Users/thanh.pc/Desktop"
      FOLDER_AUTO_POSTOUCH="${ROOT_MAC}/Automation_Postouch_Jenkins"
      FOLDER_DRIVER_POSTOUCH="${FOLDER_AUTO_POSTOUCH}/drivers"
      FOLDER_REPORT_1 = "${ROOT_MAC}/report_postouch/report1"
      FOLDER_REPORT_2 = "${ROOT_MAC}/report_postouch/report2"
      FOLDER_REPORT_3 = "${ROOT_MAC}/report_postouch/report_all"
      OUTPUT_FILES_REMOVE_KEY_XML = """${FOLDER_REPORT_1}/output_final.xml,${FOLDER_REPORT_1}/output_final_1.xml,${FOLDER_REPORT_2}/output_final.xml,${FOLDER_REPORT_2}/output_final_1.xml"""
      MERGED_OUTPUT_XML = "${FOLDER_REPORT_3}/merge.xml"
      MERGED_OUTPUT_LOG = "${FOLDER_REPORT_3}/merge_log.html"
      MERGED_OUTPUT_REPORT = "${FOLDER_REPORT_3}/merge_report.html"
      JIRA_OUTPUT_PDF = "${FOLDER_REPORT_3}/merge_log.pdf"
      JIRA_DATA_TICKET_TXT = "${ROOT_MAC}/data_jira/postouch/data.txt"
      PUSH_JIRA_DESCRIPTIONS="${ROOT_MAC}/push_chat_bot/jira.py"
      PUSH_CHAT_BOT_PY = "${ROOT_MAC}/push_chat_bot/bot.py"
      SHUTDOWN_SERVER_WITH_PORTS = "${ROOT_MAC}/push_chat_bot/shutdown_server_with_ports.py"
      HTTPLIB2_ENV = "${ROOT_MAC}/push_chat_bot/httplib2env/bin/activate"
      JIRA_ENV = "${ROOT_MAC}/push_chat_bot/jiraenv/bin/activate"
      CLIENT_RUN_AUTO = "POSTOUCH SYSTEM - UPDATE APP"
      LIST_PORT = "4733,4734,9090,8901,8902"
    }
    stages {
            stage('Checkout') {
                 steps {
                     checkout([$class: 'GitSCM',
                         branches: [[name: '*/release']],
                         userRemoteConfigs: [[credentialsId: 'MAC',
                                          url: 'https://gitlab.citigo.com.vn/kvfnb/automation-test/kiotvietfnb-pos-touch-automation-test']],
                              extensions: [[$class: 'RelativeTargetDirectory', 
                              relativeTargetDir: '/Users/thanh.pc/Desktop/Automation_Postouch_Jenkins']]])
                 
             }
        }
    stage('Delete file app old version') {
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${FOLDER_DRIVER_POSTOUCH}") {
            sh "rm -r ${FOLDER_DRIVER_POSTOUCH}/AND-FNB-POS.apk"
        }
        }
    }
    }
    stage('Delete file app new version') {
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${FOLDER_DRIVER_POSTOUCH}") {
            sh "rm -r ${FOLDER_DRIVER_POSTOUCH}/AND-FNB-POS-AF.apk"
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
                REMOTE_NODE_1 = "http://${ipAddress}:4733/wd/hub"
                REMOTE_NODE_2 = "http://${ipAddress}:4734/wd/hub"
                // echo "${REMOTE_NODE_1}"
                }
            }
         }
    }
    //   stage('Download version app Postouch old version and Rename APK') {
    //         steps {
    //             dir("${FOLDER_DRIVER_POSTOUCH}") {
    //                 sh "curl -o app_old.apk '${params.link_old}"
    //                 sh "mv app_old.apk AND-FNB-POS.apk"
    //             }
    //         }
    //     }
    stage('Download version app Postouch old version and Rename APK') {
        steps {
            dir("${FOLDER_DRIVER_POSTOUCH}") {
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
        // stage('Download version app Postouch new version') {
        //     steps {
        //         dir("${FOLDER_DRIVER_POSTOUCH}") {
        //             sh "gdrive download --force ${params.driveId}"
        //             sh "mv app.apk AND-FNB-POS-AF.apk"
        //         }
        //     }
        // }
    // stage('Download version app Postouch new version and Rename APK') {
    //     steps {
    //         dir("${FOLDER_DRIVER_POSTOUCH}") {
    //             script {
    //                 // Thực hiện tải xuống tệp APK
    //                 sh "curl -o app.apk '${params.link_new}'"

    //                 // Kiểm tra xem tệp APK đã tải xuống thành công hay không
    //                 if (fileExists('app.apk')) {
    //                     // Nếu thành công, đổi tên tệp APK thành AND-FNB-POS.apk
    //                     sh 'mv app.apk AND-FNB-POS-AF.apk'
    //                 } else {
    //                     error 'Lỗi: Không thể tải xuống tệp APK new.'
    //                 }
    //             }
    //         }
    //     }
    // }
    stage('Shutdown server with port before') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                script {
                sh """
            source ${HTTPLIB2_ENV} &&
            sudo python3 ${SHUTDOWN_SERVER_WITH_PORTS}  ${LIST_PORT}
        """
            }
        }
        }
    }
    stage('Start hub and node') {
    steps {
        dir("${FOLDER_DRIVER_POSTOUCH}") {
        sh "chmod +x ./start_selenium_grid_hub.sh"
        sh "chmod +x ./start_selenium_grid_node_touch1.sh"
        sh "chmod +x ./start_selenium_grid_node_touch2.sh"
        sh "open -a Terminal.app   ./start_selenium_grid_hub.sh"
        sh "Sleep   2"
        sh "open -a Terminal.app   ./start_selenium_grid_node_touch1.sh"
        sh "Sleep   2"
        sh "open -a Terminal.app   ./start_selenium_grid_node_touch2.sh"
        }
    }
    }
    stage('Remove report folder touch1') {
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${FOLDER_AUTO_POSTOUCH}") {
            sh "rm -r ${FOLDER_REPORT_1}/*"
        }
        }
    }
    }
    stage('Remove report folder touch2') {
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${FOLDER_AUTO_POSTOUCH}") {
            sh "rm -r ${FOLDER_REPORT_2}/*"
        }
        }
    }
    }
    stage('Remove report folder touch all') {
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        dir("${FOLDER_AUTO_POSTOUCH}") {
            sh "rm -r  ${FOLDER_REPORT_3}/*"
        }
        }
    }
    }
    stage('Remove app on devices') {
        parallel {
              stage('Remove app on devices1'){
                  steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_POSTOUCH}") {
                            sh "adb -s DA79229R40059 uninstall net.citigo.kiotviet.fnb.pos.touch.dev"
                             }
                         }
                    }
              }
            //   stage('Remove app on devices2'){
            //       steps {
            //             catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            //                 dir("${FOLDER_AUTO_POSTOUCH}") {
            //                 sh "adb -s DA22229 uninstall net.citigo.kiotviet.fnb.pos.touch.dev"
            //                  }
            //              }
            //         }
            //   }
        }
  
    }
    stage('Run automation') {
    parallel {
        stage('Thread 1') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${FOLDER_AUTO_POSTOUCH}") {
                // sh "robot --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
                // --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:11  --variable remote_node_1:${REMOTE_NODE_1}  \
                // --variable port_1:8901 --variable update_app:False -i DEL_ALL_MHTN  -o ${FOLDER_REPORT_1}/outputdel.xml -l ${FOLDER_REPORT_1}/outputdel.html -r ${FOLDER_REPORT_1}/reportdel.html   prepare-data"
                sh "robot --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
                --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:11  --variable remote_node_1:${REMOTE_NODE_1}  \
                --variable port_1:8901 --variable update_app:False -i PT_SYSTEM   -o ${FOLDER_REPORT_1}/output.xml -l ${FOLDER_REPORT_1}/output.html -r ${FOLDER_REPORT_1}/report.html testsuites"
            }
            }
        }
        }
        // stage('Thread 2') {
        //  steps {
        //     catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //     dir("${FOLDER_AUTO_POSTOUCH}") {
        //         sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
        //         --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:11  --variable remote_node_1:${REMOTE_NODE_2}  \
        //         --variable port_1:8902 --variable update_app:False -i DEL_ALL_MHTN  -o ${FOLDER_REPORT_2}/outputdel.xml -l ${FOLDER_REPORT_2}/outputdel.html -r ${FOLDER_REPORT_2}/reportdel.html   prepare-data"
        //         sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
        //         --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:11  --variable remote_node_1:${REMOTE_NODE_2}  \
        //         --variable port_1:8902 --variable update_app:False -i PT_SYSTEM_2   -o ${FOLDER_REPORT_2}/output.xml -l ${FOLDER_REPORT_2}/output.html -r ${FOLDER_REPORT_2}/report.html testsuites"
        //     }
        //     }
        // }
        // }
    }
    }
    stage('Rerun fail case') {
    parallel {
        stage('Thread rerun 1') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dir("${FOLDER_AUTO_POSTOUCH}") {
                // sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True  \
                //  --variable using_browserstack:False --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone  \
                //  --variable platformVersion:11  --variable remote_node_1:${REMOTE_NODE_1} --variable port_1:8901  \
                //  --variable update_app:False -i DEL_ALL_MHTN -o ${FOLDER_REPORT_1}/outputdel.xml -l ${FOLDER_REPORT_1}/outputdel.html -r ${FOLDER_REPORT_1}/reportdel.html   prepare-data"
                sh "robot --rerunfailed  ${FOLDER_REPORT_1}/output.xml -o ${FOLDER_REPORT_1}/output1.xml -l  ${FOLDER_REPORT_1}/output1.html -r ${FOLDER_REPORT_1}/report1.html  \
                --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
                --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:11  \
                --variable remote_node_1:${REMOTE_NODE_1} --variable port_1:8901 --variable update_app:False testsuites"
            }
            }
        }
        }
        // stage('Thread rerun 2') {
        // steps {
        //     catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
        //     dir("${FOLDER_AUTO_POSTOUCH}") {
        //         sh "robot --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True  \
        //          --variable using_browserstack:False --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone  \
        //          --variable platformVersion:11  --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8902  \
        //          --variable update_app:False -i DEL_ALL_MHTN -o ${FOLDER_REPORT_2}/outputdel.xml -l ${FOLDER_REPORT_2}/outputdel.html -r ${FOLDER_REPORT_2}/reportdel.html   prepare-data"
        //         sh "robot --rerunfailed  ${FOLDER_REPORT_2}/output.xml -o ${FOLDER_REPORT_2}/output1.xml -l  ${FOLDER_REPORT_2}/output1.html -r ${FOLDER_REPORT_2}/report1.html  \
        //         --variable app_name:POS --variable retailer:${params.retailer1} --variable username:admin --variable enable_log:True --variable using_browserstack:False  \
        //         --variable isHeadless:false --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:11  \
        //         --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8902 --variable update_app:False testsuites"
        //     }
        //     }
        // }
        // }
    }
    }
    stage('Thread remove key report1 1') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                dir("${FOLDER_AUTO_POSTOUCH}") {
                    sh "rebot --removekeywords all --output   ${FOLDER_REPORT_1}/output_final.xml   ${FOLDER_REPORT_1}/output.xml"
                }
            }
        }
    }
    stage('Thread remove key report1 2') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                dir("${FOLDER_AUTO_POSTOUCH}") {
                    sh "rebot --removekeywords all --output   ${FOLDER_REPORT_1}/output_final_1.xml   ${FOLDER_REPORT_1}/output1.xml"
                }
            }
        }
    }
    
    stage('Thread remove key report2 1') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                dir("${FOLDER_AUTO_POSTOUCH}") {
                    sh "rebot --removekeywords all --output   ${FOLDER_REPORT_2}/output_final.xml   ${FOLDER_REPORT_2}/output.xml"
                }
            }
        }
    }
    stage('Thread remove key report2 2') {
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                dir("${FOLDER_AUTO_POSTOUCH}") {
                    sh "rebot --removekeywords all --output   ${FOLDER_REPORT_2}/output_final_1.xml   ${FOLDER_REPORT_2}/output1.xml"
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
    stage('Push infor with botchat') {
        steps {
                script {
                sh """
            source ${HTTPLIB2_ENV} &&
            python3 ${PUSH_CHAT_BOT_PY} ${MERGED_OUTPUT_XML} ${params.Jira_Xray_Ticket} "${CLIENT_RUN_AUTO}" 
        """
            }
        }
    }
    // stage('Push Xray') {
    //     steps {
    //                 dir("/Users/thanh.pc/Desktop/Automation_Android_Jenkins") {
    //                         sh "java -DprojectKey=FNB -DtestExecKey=${params.Jira_Xray_Ticket}  -Dfile=${MERGED_OUTPUT_XML} -jar xray_importer.jar"
    //                 }
    //             }
    //         }
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

    //  stage('Shutdown server with port after') {
    //     steps {
    //         catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
    //             script {
    //             sh """
    //         source ${HTTPLIB2_ENV} &&
    //         sudo python3 ${SHUTDOWN_SERVER_WITH_PORTS}  ${LIST_PORT}
    //     """
    //         }
    //     }
    //     }
    // }
    // stage('Close related Terminal windows') {
    //         steps {
    //             script {
    //                 sh '''
    //                     FILE_PATH=$(dirname ${SHUTDOWN_SERVER_WITH_PORTS})/terminals_to_close.txt
    //                     if [ -f $FILE_PATH ]; then
    //                         while read pid; do
    //                             osascript -e "tell application \\"Terminal\\" to close (every window whose id is $pid)"
    //                         done < $FILE_PATH
    //                     fi
    //                 '''
    //             }
    //         }
    //     }
    //   stage('Delete app on local') {
    //     parallel{
    //       stage('Delete app on device 1'){
    //           steps {
    //                 catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
    //                   dir('/Users/thanh.pc/Desktop/Clone') {
    //                     sh "Sleep 2"
    //                     sh "adb -s emulator-5554 uninstall net.citigo.kiotviet.pos.fnb.dev"
                        
    //       }
    //     }
    //   }
    //       }
    //      stage('Delete app on device 2'){
    //         steps {
    //                 catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
    //                   dir('/Users/thanh.pc/Desktop/Clone') {
    //                     sh "Sleep 2"
    //                     sh "adb -s emulator-5556 uninstall net.citigo.kiotviet.pos.fnb.dev"                     
    //       }
    //     }
    //   }
    //       }
    //     }
       
    //   }
    //  stage('Close all Terminal windows') {
    //         steps {
    //             script {
    //                 sh """
    //                     sudo osascript -e 'tell application "Terminal" to close every window'
    //                 """
    //             }
    //         }
    //     }
    // stage('Close all terminal and simulator') {
    // steps {
    //     dir("${ROOT_MAC}") {
    //     sh "Sleep 2"
    //     sh "pkill -a Terminal"
    //     // sh "killall \"Simulator\" || true"
    //     }
    // }
    // }   

      }
    }
 