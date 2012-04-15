#!/usr/bin/python
	
import base64
import urllib2
from xml.dom import minidom
	
print "Content-Type: text/plain\n"
	
import cgi
form = cgi.FieldStorage()
if 'username' not in form or 'json_callback' not in form:
	print 'username not specified'
else:
	
	nameToFind = form['username'].value
	jsonCallback = form['json_callback'].value
	
	# justgiving.com application id
	applicationId = 'ba6a9f9d'
	
	# Urls
	stagingUrl = 'https://api.staging.justgiving.com/'
	liveUrl = 'https://api.justgiving.com/'
	
	baseUrl = liveUrl
	
	requestUrl = baseUrl + applicationId + '/v1/fundraising/pages/tectonicus/donations'
	
	#credentials = 'basic ' + base64.standard_b64encode( username + ':' + password )
	credentials = 'basic T3Jhbmd5VGFuZzp0cmlncmFwaA=='
	
	request = urllib2.Request(requestUrl)
	request.add_header('accept', 'application/xml')
	request.add_header('authorize', credentials);
	
	opener = urllib2.build_opener()
	stream = opener.open(request) 
	data = stream.read()
	
	xmldoc = minidom.parseString(data)
	
	root = xmldoc.childNodes
	
	fundraisingNode = root.item(0)
	donationsNode = fundraisingNode.getElementsByTagName('donations')[0]
	
	foundAmount = 0
	
	for donation in donationsNode.childNodes:
		
		nameNode = donation.getElementsByTagName('donorDisplayName')[0]
		name = nameNode.childNodes.item(0).data
	
		if name == nameToFind:	
			amountNode = donation.getElementsByTagName('amount')[0]
			
			amount = amountNode.childNodes.item(0).data
			foundAmount = int(float(amount)*100+0.5) # rounded to whole number of pence
			break
	
	# jsonp123({"name" : "Remy", "id" : "10", "blog" : "http://remysharp" });
	output = ''
	output += jsonCallback
	output += '( {\"amount\" : \"'
	output += str(foundAmount)
	output += '\" });'
	
#	print 'foo'
	print output