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
                robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True \
                --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1}  --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                -o ${folder_report}/del.xml  -r ${folder_report}/del.html  -l ${folder_report}/log_del.html   prepare-data
            """
            sh """
                robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True \
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
                robot --variable app_name:MAN --variable retailer:${retailer} --variable username:admin --variable enable_log:True \
                --variable isHeadless:${isHeadless} --variable device:android --variable deviceName:android --variable platformVersion:${platformVersion} \
                --variable adv_1:${adv_1} --variable adv_2:${adv_2} --variable udid_1:${udid_1}  --variable udid_2:${udid_2} --variable remote_node_1:${remote_node_1} --variable remote_node_2:${remote_node_2} \
                --variable port_1:${port_1} --variable port_2:${port_2} --variable update_app:${update_app} -i DEL_ALL_MHTN \
                -o ${folder_report}/del.xml  -r ${folder_report}/del.html  -l ${folder_report}/log_del.html   prepare-data
            """
            sh """
                robot --rerunfailed  ${folder_report}/output.xml   -o ${folder_report}/output1.xml  -r ${folder_report}/output1.html  -l ${folder_report}/log1.html \
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

        APPIUM_AUTOMATION_PLATFORM_ANDROID='Android'
        APPIUM_AUTOMATION_PLATFORM_IOS='iOS'
        APPIUM_AUTOMATION_NAME_ANDROID='UiAutomator2'
        APPIUM_AUTOMATION_NAME_IOS='XCUITest'
        IS_HEADLESS = "true"
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
        //  stage('Get User Name') {
        //     steps {
        //         script {
        //             // Set the USER_NAME variable dynamically
        //             USER_NAME = sh(script: 'whoami', returnStdout: true).trim()
        //             ROOT_MAC = "/Users/${USER_NAME}/Desktop"
        //         }
        //     }
        // }
        // stage('Get IP Address') {
        //     steps {
        //         script {
        //             ipAddress = sh(returnStdout: true, script: "ifconfig | grep 'inet ' | grep -v 127.0.0.1 | awk '{print \$2}' | head -n 1").trim()
        //             echo "IP Address: ${ipAddress}"
        //         }
        //     }
        // }
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
        stage('Setup Virtual Environments And Install Dependencies CREATE_HTTPLIB2_ENV') {
            steps {
                script {
                    // Create virtual environment httplib2env
                    sh "python3 -m venv ${env.CREATE_HTTPLIB2_ENV}"
                    // Install dependencies for httplib2env
                    sh """
                    source ${env.CREATE_HTTPLIB2_ENV}/bin/activate

                    pip install \\
                            attrs==23.2.0 \\
                            certifi==2024.2.2 \\
                            charset-normalizer==3.3.2 \\
                            exceptiongroup==1.2.0 \\
                            h11==0.14.0 \\
                            httplib2==0.22.0 \\
                            idna==3.6 \\
                            importlib-metadata==6.7.0 \\
                            jsons==1.6.3 \\
                            outcome==1.3.0.post0 \\
                            packaging==24.0 \\
                            psutil==5.9.8 \\
                            pyhtml2pdf==0.0.7 \\
                            pyparsing==3.1.1 \\
                            PySocks==1.7.1 \\
                            python-dotenv==0.21.1 \\
                            requests==2.31.0 \\
                            selenium==4.11.2 \\
                            sniffio==1.3.1 \\
                            sortedcontainers==2.4.0 \\
                            trio==0.22.2 \\
                            trio-websocket==0.11.1 \\
                            typing-extensions==4.7.1 \\
                            typish==1.9.3 \\
                            urllib3==2.0.7 \\
                            webdriver-manager==4.0.1 \\
                            wsproto==1.2.0 \\
                            xml-python==0.4.3 \\
                            zipp==3.15.0 \\
                            robotframework-SikuliLibrary==2.0.3
                    deactivate
                    """
                }
            }
        }
        stage('Setup Virtual Environments And Install Dependencies CREATE_JIRA_ENV') {
            steps {
                    script {
                        // Create virtual environments jiraenv
                        sh "python3 -m venv ${env.CREATE_JIRA_ENV}"                  
                        // Install dependencies for jiraenv
                        sh """
                        source ${env.CREATE_JIRA_ENV}/bin/activate
                        pip install \\
                                attrs==23.2.0 \\
                                certifi==2024.2.2 \\
                                charset-normalizer==3.3.2 \\
                                exceptiongroup==1.2.0 \\
                                h11==0.14.0 \\
                                idna==3.6 \\
                                importlib-metadata==6.7.0 \\
                                outcome==1.3.0.post0 \\
                                packaging==24.0 \\
                                pyhtml2pdf==0.0.7 \\
                                PySocks==1.7.1 \\
                                python-dotenv==0.21.1 \\
                                requests==2.31.0 \\
                                selenium==4.11.2 \\
                                sniffio==1.3.1 \\
                                sortedcontainers==2.4.0 \\
                                trio==0.22.2 \\
                                trio-websocket==0.11.1 \\
                                typing_extensions==4.7.1 \\
                                urllib3==1.26.7 \\
                                webdriver-manager==4.0.1 \\
                                wheel==0.37.1 \\
                                wsproto==1.2.0 \\
                                zipp==3.15.0
                        deactivate
                        """
                    }
                }
            }
        stage('Create Python Files Push Chat Bot') {
                steps {
                    script {
                        writeFile file: "${env.PUSH_CHAT_BOT_PY}", text: """
import sys
import xml.etree.ElementTree as ET
from json import dumps
from httplib2 import Http
def parse_robot_xml(xml_path):
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()

        # Lấy thông tin từ thẻ <stat> trong <total>
        total_stat = root.find('.//total/stat')
        total_pass = total_stat.attrib.get('pass', 'N/A')
        total_fail = total_stat.attrib.get('fail', 'N/A')
        total_sum = int(total_pass) + int(total_fail)
        return total_sum, total_pass, total_fail

    except ET.ParseError as e:
        print(f"Error parsing XML: {e}")
        return None


def main():
    xray_ticket = "FNB-20197"
    if len(sys.argv) > 1:
        file_xml = sys.argv[1]
        xray_ticket = sys.argv[2]
        client_run = sys.argv[3]
        if "ANDROID" in client_run or "IOS" in client_run:
            imageUrl = "https://cdn-icons-png.flaticon.com/128/4488/4488516.png"
        else:
            imageUrl = "https://cdn-icons-png.flaticon.com/128/6261/6261574.png"
        link_result = f"https://citigo.atlassian.net/browse/{xray_ticket}"
        link_run = f'Link run: <a href="{link_result}">{xray_ticket}</a>'
        total_sum, total_pass, total_fail = parse_robot_xml(file_xml)
    if xray_ticket == "FNB-20197":
         url = "https://chat.googleapis.com/v1/spaces/AAAASUZcN4o/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=rhVuEexqDqwwLOz_5c9BoHksjcHMwn0BIDbZQSnYW0U"
    else:
        url = "https://chat.googleapis.com/v1/spaces/AAAAZcrwJgo/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=zMSyE34NZEJ2fZUP7FDOPWRyP1CNSt7vLsAQPeWxaxo"
    app_message = {
        "cards": [
            {
                "header": {
                    "title": f"{client_run}",
                    "imageUrl": f"{imageUrl}"
                },
                "sections": [
                    {
                        "widgets": [
                            {
                                "textParagraph": {
                                    "text": f"<b>Total<b>: {total_sum} | <font color=\"#7CFC00\"><b>Pass<b></font>: {total_pass} | <font color=\"#FF0000\"><b>Fail<b></font>: {total_fail} <br> {link_run}"
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    }
    message_headers = {"Content-Type": "application/json; charset=UTF-8"}
    http_obj = Http()
    response = http_obj.request(
        uri=url,
        method="POST",
        headers=message_headers,
        body=dumps(app_message)
    )


if __name__ == "__main__":
    main()
                    """
                }
            }
        }


        stage('Create Python Files Push Jira Descriptions') {
                steps {
                    script {
                        writeFile file: "${env.PUSH_JIRA_DESCRIPTIONS}", text: """
import os
import xml.etree.ElementTree as ET
from pyhtml2pdf import converter
import argparse
import requests

"""Jira automation generate report ticket"""
parser = argparse.ArgumentParser(description="Jira automation generate report ticket")
parser.add_argument("--file_path", type=str, help="file report.html from robot fw")
parser.add_argument("--output_path", type=str, help="output pdf file converted from report.html file")
parser.add_argument("--xml_file_path", type=str, help="output.xml file report from robot fw")
parser.add_argument("--mapping_tag", type=str, help="tag mapping jira ticket")
parser.add_argument("--id_ticket", type=str, help="jira ticket id")

args = parser.parse_args()

if not args.file_path:
    print("Error: Please provide --file_path parameter.")

if not args.id_ticket:
    print("Error: Please provide --id_ticket parameter.")

if not args.output_path:
    print("Error: Please provide --output_path parameter.")

if not args.xml_file_path:
    print("Error: Please provide --xml_file_path parameter.")

if not args.mapping_tag:
    print("Error: Please provide --mapping_tag parameter.")

def convert_html_to_pdf():
    path = os.path.abspath(args.file_path)
    converter.convert(f'file:///{path}', args.output_path)
    return args.output_path

def extract_result_from_report():
    # Load file XML
    tree = ET.parse(args.xml_file_path)
    root = tree.getroot()
    list_result = []
    # Tìm tất cả các thẻ <test> có thẻ con là status, lấy thẻ cuối cùng và có status là fail
    for tag in root.findall(".//test"): 
        a = tag.findall(".//status")
        b = len(a)
        status_check = a[b-1]
        if status_check.get('status') == 'FAIL':
            list_result.append(tag.get('name').strip())
    return list_result

def extract_pass_fail_from_report():
    # Load file XML
    tree = ET.parse(args.xml_file_path)
    root = tree.getroot()
    for tag in root.findall(".//stat"): 
        if tag.text == 'All Tests':
            print(tag.get('pass'), tag.get('fail'))
            return  tag.get('pass'), tag.get('fail')

def extract_tag_mapping():
    list_tag_mapping = []
    with open(args.mapping_tag, 'r') as file:
        for line in file:
            list_tag_mapping.append(line.strip())
    return list_tag_mapping

def generate_jira_ticket(tag_mapping, tag_from_report, num_pass, num_fail):
    description = ''
    for tag_report in tag_from_report:
        for index, tag in enumerate(tag_mapping):
            if tag.startswith(tag_report):
                description = tag +'\n' + description 

    # Thông tin xác thực
    email = 'thanh.pc@kiotviet.com'  # Thay đổi thành địa chỉ email của bạn
    api_token = 'ATATT3xFfGF0tW0uCTvpDHNA1dK-X9kz6s6rdC_gpUrp2-FWJ-I5Y-CIL905szSRvadsIa0Eb77wtRlM6fEYzsfAxtB4VPToyhmEiau-c3NZ3qDbsGfgfdTiPqwbX1WvXVgqBV0azwmvAPbq-zCGBsktENideWUGwznjvfONIouvsrodDroNejQ=626A4E6E'  # Thay đổi thành API token của bạn
    base_url = 'https://citigo.atlassian.net'  # Thay đổi địa chỉ URL của Jira của bạn


    # Dữ liệu cập nhật cho ticket
    issue_key = args.id_ticket 
    data = {
        "fields": {
            "description": "Link tag fail:" + "\n" + description, # Thay đổi thành mô tả mới
            "customfield_12637": num_pass, #Pass
            "customfield_12638": num_fail  #Fail
        }
    }

    # Tạo tiêu đề xác thực và header cho request
    auth_header = (email, api_token)
    headers = {'Content-Type': 'application/json'}

    # Gửi yêu cầu PUT để cập nhật ticket
    response = requests.put(f'{base_url}/rest/api/2/issue/{issue_key}', json=data, headers=headers, auth=auth_header)

    # Kiểm tra xem yêu cầu có thành công không
    if response.status_code == 204:
        print("Ticket đã được cập nhật thành công.")
    else:
        print("Đã xảy ra lỗi:", response.content)

def upload_pdf_report(path_pdf):
    # Thông tin xác thực và endpoint API
    username = 'thanh.pc@kiotviet.com'
    api_token = 'ATATT3xFfGF0tW0uCTvpDHNA1dK-X9kz6s6rdC_gpUrp2-FWJ-I5Y-CIL905szSRvadsIa0Eb77wtRlM6fEYzsfAxtB4VPToyhmEiau-c3NZ3qDbsGfgfdTiPqwbX1WvXVgqBV0azwmvAPbq-zCGBsktENideWUGwznjvfONIouvsrodDroNejQ=626A4E6E'

    url = f'https://citigo.atlassian.net/rest/api/3/issue/{args.id_ticket}/attachments'

    # Dữ liệu cần đính kèm
    file_path = path_pdf
    file_name = 'Report'
    file_content_type = 'text/pdf'  # Loại dữ liệu của file

    # Đọc nội dung của file
    with open(file_path, 'rb') as f:
        file_data = f.read()

    # Tạo yêu cầu POST để đính kèm file
    files = {'file': (file_name, file_data, file_content_type)}
    response = requests.post(
        url,
        files=files,
        auth=(username, api_token),
        headers={'Accept': 'application/json', 'X-Atlassian-Token': 'no-check'}
    )

    # Kiểm tra phản hồi
    if response.status_code == 200:
        print('File attached successfully.')
    else:
        print('Failed to attach file:', response.text)

if __name__ == "__main__":
    tag = extract_tag_mapping()
    file = extract_result_from_report()
    num_pass, num_fail = extract_pass_fail_from_report()
    generate_jira_ticket(tag, file, num_pass, num_fail)
    path = convert_html_to_pdf()
    upload_pdf_report(path)
                    """
                }
            }
        }
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
        stage('Delete All .apk OLD Files') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${DOWNLOAD_DIR_OLD}") {
                            sh "rm -r ${DOWNLOAD_DIR_OLD}/*.apk"
                        }
                    }
                }
            }
        stage('Delete All .apk NEW Files') {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("${DOWNLOAD_DIR_NEW}") {
                            sh "rm -r ${DOWNLOAD_DIR_NEW}/*.apk"
                        }
                    }
                }
            }
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
        stage('Run automation') {
            parallel {
                stage('Thread 1') {
                    steps {
                        runRobotTests(FOLDER_AUTO_MAN, FOLDER_REPORT_1, RETAILER_1, IS_HEADLESS, APPIUM_SERVER_DEVICE_NAME_1, APPIUM_SERVER_DEVICE_NAME_2, APPIUM_SERVER_UDID_1, APPIUM_SERVER_UDID_2, APPIUM_AUTOMATION_PLATFORM_ANDROID, REMOTE_NODE_1, REMOTE_NODE_2, APPIUM_SERVER_PORT_1, APPIUM_SERVER_PORT_2, OS, app_path_pos, update_app)
                    }
                }
                stage('Thread 2') {
                    steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True -i DEL_ALL_MHTN --output ../report2/outputdel.xml   prepare-data"
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True -o ../report2/output.xml -l ../report2/output.html -r ../report2/report.html   -i SYS2  testsuites"
                            }
                        }
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_2} --variable port_1:8101 --variable update_app:True -i DEL_ALL_MHTN --output ../report2/outputdel.xml   prepare-data"
                                sh "robot --rerunfailed  ../report2/output.xml -o ../report2/output1.xml -l ../report2/output1.html -r ../report2/report1.html --variable app_name:POS \
                                --variable retailer:${params.retailer2} --variable username:admin --variable enable_log:True  \
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
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_3} --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102 --variable update_app:True --variable port_2:8103 -i DEL_ALL_MHTN \
                                --output ../report2may/outputdel.xml   prepare-data"
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_3} --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102 --variable update_app:True --variable port_2:8103 -o ../report2may/output.xml -l \
                                ../report2may/output.html -r ../report2may/report.html -i  SYS2M  testsuites"
                            }
                        }
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            dir("${FOLDER_AUTO_IOS}") {
                                sh "robot --variable app_name:POS --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True  \
                                --variable isHeadless:${params.statusSimulator} --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false \
                                --variable remote_node_1:${REMOTE_NODE_3} --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102 --variable update_app:True --variable port_2:8103 -i DEL_ALL_MHTN --output ../report2may/outputdel.xml   prepare-data"
                                sh "robot --rerunfailed  ../report2may/output.xml -o ../report2may/output1.xml -l ../report2may/output1.html -r ../report2may/report1.html --variable app_name:POS \
                                --variable retailer:${params.retailer3} --variable username:admin --variable enable_log:True  --variable isHeadless:${params.statusSimulator} \
                                --variable device:iPhone --variable deviceName:iPhone --variable platformVersion:15.5 --variable record_video:false --variable remote_node_1:${REMOTE_NODE_3} \
                                --variable remote_node_2:${REMOTE_NODE_4} --variable port_1:8102  --variable port_2:8103 --variable update_app:True  testsuites"
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
                        sh "rebot --outputdir ${FOLDER_REPORT_3} --merge --output ${MERGED_OUTPUT_XML} -l ${MERGED_OUTPUT_LOG} -r ${MERGED_OUTPUT_REPORT}  ${filesToMerge}"
                    } else {
                        echo "No files to merge."
                    }
                }
                  }
            }
        } 

        }
    post {
        always {
            script {
                // Stop first Appium server
                sh """
                if [ -f ${env.FILE_PID_SERVER_1} ]; then
                    kill $(cat ${env.FILE_PID_SERVER_1})
                fi
                """
                // Stop second Appium server
                sh """
                if [ -f ${env.FILE_PID_SERVER_2} ]; then
                    kill $(cat ${env.FILE_PID_SERVER_2})
                fi
                """
            }
        }
    }
    
}