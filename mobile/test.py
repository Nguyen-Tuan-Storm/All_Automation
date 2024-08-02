from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
import time

APPIUM_SERVER_URL = 'http://localhost:4723/wd/hub'

options = UiAutomator2Options()
options.platformVersion = '11'
options.app = "F:\\Downloads\\app-dev-release.apk"
options.appPackage = 'net.citigo.kiotviet.fnb.manager'
options.appActivity = 'net.citigo.kiotviet.manager.flutter.FlutterManActivity'
options.automationName = 'UiAutomator2'
options.newCommandTimeout = 300

def login(driver, retailer_value, username_value, password_value):
    retailer = "//android.widget.EditText[@resource-id='text_field_retailer']"
    user_name = "//android.widget.EditText[@resource-id='text_field_username']"
    password = "//android.widget.EditText[@resource-id='text_field_password']"
    btn_login = "//android.view.View[@resource-id='btn_login']"

    driver.find_element(by=AppiumBy.XPATH, value=retailer).click()
    driver.find_element(by=AppiumBy.XPATH, value=retailer).send_keys(retailer_value)
    driver.find_element(by=AppiumBy.XPATH, value=user_name).click()
    driver.find_element(by=AppiumBy.XPATH, value=user_name).send_keys(username_value)
    driver.find_element(by=AppiumBy.XPATH, value=password).click()
    driver.find_element(by=AppiumBy.XPATH, value=password).send_keys(password_value)
    driver.find_element(by=AppiumBy.XPATH, value=btn_login).click()

def create_driver():
    driver = webdriver.Remote(APPIUM_SERVER_URL, options=options)
    return driver

def test_login_app(driver):
    time.sleep(5)
    driver.implicitly_wait(10)
    login(driver, "anna52", "admin", "123")
    time.sleep(10)

if __name__ == '__main__':
    driver = create_driver()
    try:
        test_login_app(driver)
    finally:
        driver.quit()
