import urllib.request, json
req = urllib.request.Request(
    'http://localhost:8080/api/user/login',
    data=b'{"username":"admin","password":"123456"}',
    headers={'Content-Type': 'application/json'}
)
resp = json.load(urllib.request.urlopen(req))
print(resp['data']['token'])
