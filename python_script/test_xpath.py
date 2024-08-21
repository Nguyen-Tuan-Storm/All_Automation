from lxml import etree

xml = '''
<bookstore>
    <book category="fiction">
        <title lang="en">Harry Potter</title>
        <author>J.K. Rowling</author>
        <year>2005</year>
        <price>29.99</price>
    </book>
    <book category="programming">
        <title lang="en">Learning Python</title>
        <author>Mark Lutz</author>
        <year>2013</year>
        <price>39.95</price>
    </book>
</bookstore>
'''

# Parse XML
tree = etree.XML(xml)

# Tạo XPath để tìm tất cả các tiêu đề sách
titles = tree.xpath('//book/title/text()')

# In kết quả
for title in titles:
    print(title)
