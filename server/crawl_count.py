import requests
import urllib
from bs4 import BeautifulSoup

url_Cacl1="http://data.seoul.go.kr/openinf/sheetview.jsp?infId=OA-1255"
url_Cacl2="http://data.seoul.go.kr/openinf/sheetview.jsp?infId=OA-1253"

payload = {'key1':'value1', 'key2':'value2'}
req=requests.post(url_Cacl1, data=payload)
print (req.text)


"""
req=requests.get(url_Cacl1)
soup = BeautifulSoup(req.content, "html.parser")

#data = urllib.urlopen(url_Cacl1)
#soup = BeautifulSoup(data,'html.parser')
#print soup

#count= soup.findAll('div',{'class':'AXgridStatus'})
`count=soup.find('div',{'class':'AXGrid'})
print count

count=count.child

print count

count=count.child

print count
"""
