from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
import time

APPIUM_SERVER_URL = 'http://localhost:4727/wd/hub'

options = UiAutomator2Options()
options.platformVersion = '11'
options.app = "F:\\Downloads\\app.apk"
options.appPackage = "net.citigo.kiotviet.pos.fnb.dev"
options.appActivity = "net.citigo.kiotviet.pos.fnb.ui.activities.SplashScreenActivity"
# options.appActivity = "net.citigo.kiotviet.pos.fnb.ui.activities.LoginActivity"
options.automationName = 'UiAutomator2'
options.newCommandTimeout = 300
options.locale="VN"
options.language="vi"
options.system_port="8802"
options.platform_name="Android"
options.full_reset="true"

def login(driver, retailer_value, username_value, password_value):
    envi = "net.citigo.kiotviet.pos.fnb.dev:id/btn_choose_server"
    staging ="net.citigo.kiotviet.pos.fnb.dev:id/btn_choose_server"
    item ="//android.widget.CheckedTextView[contains(@text,'Staging')]"
    retailer = "//android.widget.LinearLayout[@resource-id='net.citigo.kiotviet.pos.fnb.dev:id/input_layout_shop_login']//android.widget.EditText"
    user_name = "//android.widget.LinearLayout[@resource-id='net.citigo.kiotviet.pos.fnb.dev:id/input_layout_username']//android.widget.EditText"
    password = "//android.widget.LinearLayout[@resource-id='net.citigo.kiotviet.pos.fnb.dev:id/input_layout_password']//android.widget.EditText"
    btn_login = "net.citigo.kiotviet.pos.fnb:id/btn_login_action"
    driver.find_element(by=AppiumBy.ID, value=envi).click()
    driver.find_element(by=AppiumBy.ID, value=staging).click()
    driver.find_element(by=AppiumBy.XPATH, value=item).click()
    driver.find_element(by=AppiumBy.XPATH, value=retailer).click()
    driver.find_element(by=AppiumBy.XPATH, value=retailer).send_keys(retailer_value)
    driver.find_element(by=AppiumBy.XPATH, value=user_name).click()
    driver.find_element(by=AppiumBy.XPATH, value=user_name).send_keys(username_value)
    driver.find_element(by=AppiumBy.XPATH, value=password).click()
    driver.find_element(by=AppiumBy.XPATH, value=password).send_keys(password_value)
    driver.find_element(by=AppiumBy.ID, value=btn_login).click()

def create_driver():
    driver = webdriver.Remote(APPIUM_SERVER_URL, options=options)
    return driver

def test_login_app(driver):
    # time.sleep(5)
    driver.implicitly_wait(10)
    login(driver, "anna52", "admin", "123")
    time.sleep(10)

if __name__ == '__main__':
    driver = create_driver()
    try:
        test_login_app(driver)
    finally:
        driver.quit()
