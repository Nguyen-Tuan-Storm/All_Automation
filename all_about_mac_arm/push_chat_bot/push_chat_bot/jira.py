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
    api_token = 'Basic dGhhbmgucGNAa2lvdHZpZXQuY29tOkFUQVRUM3hGZkdGMFVhNGpnMGdJbWRabkFvVm9NYTNrbHI2bm9jX0cwUXBScnBhYk5sSDFzUEV2S3Z5eVZocWRRMHRSdncwVkpqNkxyUTdnM1Jib01Za25VTkhqbXNmMGJuZ2xtbnZ5QmQwM3VtMHQ1clhZb3A4Y2hoZm5VMWZYaU1ZRmNma1JYUUN3MWNtVTBJRmFweTB2VktyUW1qeXJwS3dLOU9ub3Q3S09fcy1UMkdCbHIzdz00MEFEMjI0Qg=='  # Thay đổi thành API token của bạn
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
    # auth_header = (email, api_token)
    headers = {'Content-Type': 'application/json', 'Authorization': f'{api_token}'}

    # Gửi yêu cầu PUT để cập nhật ticket
    response = requests.put(f'{base_url}/rest/api/2/issue/{issue_key}', json=data, headers=headers)

    # Kiểm tra xem yêu cầu có thành công không
    if response.status_code == 204:
        print("Ticket đã được cập nhật thành công.")
    else:
        print("Đã xảy ra lỗi:", response.content)

def upload_pdf_report(path_pdf):
    # Thông tin xác thực và endpoint API
    # username = 'thanh.pc@kiotviet.com'
    api_token = 'Basic dGhhbmgucGNAa2lvdHZpZXQuY29tOkFUQVRUM3hGZkdGMFVhNGpnMGdJbWRabkFvVm9NYTNrbHI2bm9jX0cwUXBScnBhYk5sSDFzUEV2S3Z5eVZocWRRMHRSdncwVkpqNkxyUTdnM1Jib01Za25VTkhqbXNmMGJuZ2xtbnZ5QmQwM3VtMHQ1clhZb3A4Y2hoZm5VMWZYaU1ZRmNma1JYUUN3MWNtVTBJRmFweTB2VktyUW1qeXJwS3dLOU9ub3Q3S09fcy1UMkdCbHIzdz00MEFEMjI0Qg=='

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
        headers={'Accept': 'application/json', 'X-Atlassian-Token': 'no-check', 'Authorization': f'{api_token}'}
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