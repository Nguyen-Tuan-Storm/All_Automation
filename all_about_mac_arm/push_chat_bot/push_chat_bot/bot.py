import sys
import requests
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

def get_title_jira_ticket(jira_key): 

    # Thông tin xác thực
    api_token = 'Basic dGhhbmgucGNAa2lvdHZpZXQuY29tOkFUQVRUM3hGZkdGMFVhNGpnMGdJbWRabkFvVm9NYTNrbHI2bm9jX0cwUXBScnBhYk5sSDFzUEV2S3Z5eVZocWRRMHRSdncwVkpqNkxyUTdnM1Jib01Za25VTkhqbXNmMGJuZ2xtbnZ5QmQwM3VtMHQ1clhZb3A4Y2hoZm5VMWZYaU1ZRmNma1JYUUN3MWNtVTBJRmFweTB2VktyUW1qeXJwS3dLOU9ub3Q3S09fcy1UMkdCbHIzdz00MEFEMjI0Qg=='  # Thay đổi thành API token của bạn
    base_url = 'https://citigo.atlassian.net'  # Thay đổi địa chỉ URL của Jira của bạn


    # Dữ liệu cập nhật cho ticket
    issue_key = jira_key
    # Tạo tiêu đề xác thực và header cho request
    headers = {'Content-Type': 'application/json', 'Authorization': f'{api_token}'}

    # Gửi yêu cầu PUT để cập nhật ticket
    response = requests.get(f'{base_url}/rest/api/2/issue/{issue_key}', headers=headers)

    # Kiểm tra xem yêu cầu có thành công không
    if response.status_code == 200:
        issue = response.json()
        summary = issue['fields']['summary']
        return summary
    else:
        print(f'Failed to get ticket: {response.status_code}')
        print(response.text)

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
    elif xray_ticket == "FNB-26069":
        url = "https://chat.googleapis.com/v1/spaces/AAAA7a7Wr5U/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=1eSH1w7BNHs2KLW-nLIXSdWy7_NXefzmFIg2uLEaWZA"
    elif xray_ticket == "FNB-26421":
        url = "https://chat.googleapis.com/v1/spaces/AAAA7a7Wr5U/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=1eSH1w7BNHs2KLW-nLIXSdWy7_NXefzmFIg2uLEaWZA"
    else:
        url = "https://chat.googleapis.com/v1/spaces/AAAAz9a45Jk/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=WozGChBdx325Y0yMkHLoah-teW7AfNo3Ceg-Otb1Yow"
    title = get_title_jira_ticket(xray_ticket)
    app_message = {
        "cards": [
            {
                "header": {
                    "title": f"{title}",
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
