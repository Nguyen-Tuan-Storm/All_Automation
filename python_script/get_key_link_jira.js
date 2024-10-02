function getTextByXPath(xpath, document) {
    const result = document.evaluate(xpath, document, null, XPathResult.STRING_TYPE, null);
    return result.stringValue.trim();
}

function mapKeyValue(xpathColumn1Template, xpathColumn2Template, startRow, endRow, document) {
    const keyValueMap = {};

    for (let i = startRow; i <= endRow; i++) {
        const xpathKey = xpathColumn1Template.replace("index", i);  // Thay index bằng giá trị i (số hàng)
        const xpathValue = xpathColumn2Template.replace("index", i); // Thay index bằng giá trị i (số hàng)

        const key = getTextByXPath(xpathKey, document);
        const value = getTextByXPath(xpathValue, document);

        if (key && value) {
            keyValueMap[key] = value;
        }
    }

    return keyValueMap;
}

const xpathColumn1Template = '//tr[index]/td[3]/div//a'; // Cột key
const xpathColumn2Template = '//tr[index]/td[2]/div//a'; // Cột value

const startRow = 1;
const endRow = 50;


const result = mapKeyValue(xpathColumn1Template, xpathColumn2Template, startRow, endRow, document);

function printKeyValueOnSameLine(keyValueMap) {
    for (const [key, value] of Object.entries(keyValueMap)) {
        console.log(`${key}: ${value}`);
    }
}
printKeyValueOnSameLine(result);

