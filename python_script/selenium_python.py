# from selenium import webdriver
# from selenium.webdriver.chrome.service import Service
# from selenium.webdriver.chrome.options import Options
# from selenium.webdriver.common.by import By
# from selenium.webdriver.support.ui import WebDriverWait
# from selenium.webdriver.support import expected_conditions as EC
# import os
# import platform

# def setup_chrome_driver():
#     system = platform.system()

#     if system == "Windows":
#         print("okkkkkkkkkkkkkk")
#         chrome_driver_path = os.path.abspath("F:\\Downloads\\chromedriver-win64\\chromedriver.exe")
#         chrome_binary_path = os.path.abspath("F:\\Downloads\\chrome-win64\\chrome.exe")
#     elif system == "Darwin":  # macOS
#         chrome_driver_path = os.path.abspath("chromedriver/mac64/chromedriver")
#         chrome_binary_path = os.path.abspath("chrome/chrome-mac/Chromium.app/Contents/MacOS/Chromium")
#     else:
#         raise Exception("Hệ điều hành không được hỗ trợ")

#     # Thiết lập các tùy chọn cho Chrome
#     options = Options()
#     options.binary_location = chrome_binary_path
#     driver = webdriver.Chrome(chrome_options=options, executable_path=chrome_driver_path)
#     return driver

# def login(driver, retailer_xpath, username_xpath, password_xpath, login_button_xpath, retailer, username, password):
#     try:
#         WebDriverWait(driver, 10).until(
#             EC.visibility_of_element_located((By.XPATH, retailer_xpath))
#         ).send_keys(retailer)

#         WebDriverWait(driver, 10).until(
#             EC.visibility_of_element_located((By.XPATH, username_xpath))
#         ).send_keys(username)

#         WebDriverWait(driver, 10).until(
#             EC.visibility_of_element_located((By.XPATH, password_xpath))
#         ).send_keys(password)

#         WebDriverWait(driver, 10).until(
#             EC.element_to_be_clickable((By.XPATH, login_button_xpath))
#         ).click()

#         print("Đã thực hiện đăng nhập")
#     except Exception as e:
#         print(f"Lỗi trong quá trình đăng nhập: {e}")

# def wait_for_btn_thanhtoan(driver, btn_thanhtoan_xpath, timeout=10):
#     try:
#         WebDriverWait(driver, timeout).until(
#             EC.visibility_of_element_located((By.XPATH, btn_thanhtoan_xpath))
#         )
#         print("Nút Thanh toán đã visible")
#         return True
#     except Exception as e:
#         print(f"Không tìm thấy nút Thanh toán: {e}")
#         return False

# # Khởi tạo WebDriver
# driver = setup_chrome_driver()

# # Mở trang web bạn muốn kiểm tra
# driver.get("URL_CUA_BAN")  # Thay "URL_CUA_BAN" bằng URL thực tế của trang web

# # Thông tin đăng nhập
# retailer = "autopostouch2"
# username = "admin"
# password = "123"

# # XPath của các phần tử input và nút login
# retailer_xpath = "//input[@id='Retailer']"
# username_xpath = "//input[@id='UserName']"
# password_xpath = "//input[@id='Password']"
# login_button_xpath = "//span[@id='loginNewSale']"
# btn_thanhtoan_xpath = "//button[contains(@class,'btn-success')]"

# # Thực hiện đăng nhập
# login(driver, retailer_xpath, username_xpath, password_xpath, login_button_xpath, retailer, username, password)

# # Đợi cho nút Thanh toán xuất hiện sau khi đăng nhập
# if wait_for_btn_thanhtoan(driver, btn_thanhtoan_xpath):
#     print("Đăng nhập thành công và đã sẵn sàng xử lý tiếp theo")

# # Đóng trình duyệt sau khi hoàn tất
# driver.quit()

import time
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options

# Đường dẫn đến chromedriver
chromedriver_path = 'F:\\Downloads\\chromedriver-win64\\chromedriver.exe'
# Đường dẫn đến binary của trình duyệt Chrome
chrome_binary_path = 'F:\\Downloads\\chrome-win64\\chrome.exe'

# Cấu hình các tùy chọn cho Chrome
service = Service(executable_path=chromedriver_path)
driver = webdriver.Chrome(service=service)
# Mở trang web
driver.get('https://www.google.com')

# Đợi 20 giây để xem trang
time.sleep(20)

# Đóng trình duyệt
driver.quit()
