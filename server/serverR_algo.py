#!/usr/bin/env python
#-*-coding:utf-8-*-


import requests
from bs4 import BeautifulSoup
import math


def requests_api(url):

        r = requests.get(url)


        soup = BeautifulSoup(r.content,"html.parser")

        print (soup)

        #find axis x, y from soup
        axis_xy=[]

        soup = str( soup)
        soup1 = soup.split(":")[1]
        print(soup1)
        axis_xy.append(soup1.split(",")[0])
        print(axis_xy[0])
        soup = soup.split(":")[2]
        axis_xy.append(soup.split("}")[0])

        print (axis_xy)

        return axis_xy

#gain user's location and return minimun distance < 500
def near_user(WTM_Y, WTM_X):

        data_Y=[1,2,3]
        data_X=[5,3,6]

        re_dataX=[]
        re_dataY=[]
        max_dist=200
        dist=[]
        min_dist=[]
##read from data base


	WTM_Y=float(WTM_Y)
	WTM_X=float(WTM_X)
        num_data = 3

        for i in range(0,num_data):
                distFrom=abs((data_Y[i]-WTM_Y)*(data_Y[i]-WTM_Y)-(data_X[i]-WTM_X)*(data_X[i]-WTM_X))
                print distFrom
                distFrom =math.sqrt(distFrom)
                dist.append(distFrom)
                re_dataX.append(data_X[i])
                re_dataY.append(data_Y[i])

        print(dist)
        sorted_dataY=[]
        sorted_dataX=[]
	
        sorted_dist=sorted(dist)
	print sorted(dist)
	print "sorted"
        print sorted_dist
	for i in sorted_dist:
                sorted_dataY.append(str(re_dataY[dist.index(i)]))
                sorted_dataX.append(str(re_dataX[dist.index(i)]))
                min_dist.append(i)
		if i>=max_dist:
                        break
        return_con=[]
        return_con.append(sorted_dataY)
        return_con.append(sorted_dataX)

        return return_con


"""

def application(environ, start_response):
	status = '200 OK'
	response_header = [('Content-Type', 'text/html')]
	start_response(status, response_header)

	query_string = environ['QUERY_STRING']

	
#	return query_string
#convert 
tp_str=query_string
tp_str=tp_str.split('&')

if 'latitude' in tp_str[0]:
	latitude=tp_str[0].split('=')[1]
	longitude=tp_str[1].split('=')[1]
else:
	latitude=tp_str[1].split('=')[1]
	longitude=tp_str[0].split('=')[1]
"""

WTM_Y=str(453361.9)
WTM_X=str(201385.8)
WGS_Y=str(37.559395)
WGS_X=str(126.964443)

#WTM is format of data's axis
url_fromWTM="https://apis.daum.net/local/geo/transcoord?apikey=b3e823707ab3ad438ce5183bed752acf&fromCoord=WTM84&y="+WTM_Y+"&x="+WTM_X+"&toCoord=WGS84&output=json"

#WGS is format of user's axis
url_fromWGS="https://apis.daum.net/local/geo/transcoord?apikey=b3e823707ab3ad438ce5183bed752acf&fromCoord=WGS84&y="+WGS_Y+"&x="+WGS_X+"&toCoord=WTM84&output=json"

	#convert axis (WGS->WTM)
WTM_Y,WTM_X=requests_api(url_fromWGS)

	#minimum dist
WTM_Ys, WTM_Xs=near_user(WTM_Y,WTM_X)
	
	#convert axis (WTM->WGS)
WGS_Ys=[]
WGS_Xs=[]
json_res={}
json_RES=[]
for i in range(0,len(WTM_Ys)):

	url_fromWTM="https://apis.daum.net/local/geo/transcoord?apikey=b3e823707ab3ad438ce5183bed752acf&fromCoord=WTM84&y="+WTM_Ys[i]+"&x="+WTM_Xs[i]+"&toCoord=WGS84&output=json"

	Y,X=requests_api(url_fromWTM)
	#	WGS_Ys.append(Y)
	#	WGS_Xs.append(X)
	json_res['longtitude']=Y
	json_res['latitude']=X
	json_res['location']="서울시 중구"
	json_RES.append(json_res)
		


#return json.dumps(json_RES)


