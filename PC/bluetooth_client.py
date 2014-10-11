#!/usr/bin/python3.4
from bluetooth import *

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
print("Looking for service with UUID = %s" % uuid)

service_matches = find_service( uuid = uuid )

if len(service_matches) == 0:
    print("Couldn't find service with UUID = %s" % uuid)
    sys.exit(0)
    
print("Select your device:")
for i,match in enumerate(service_matches):
	print("%d: Server name:\"%s\" \t MAC address: %s" % (i,match["name"], match["host"]))
print("-------------------------------------------------------------")
	
selectedDevice = int(input())

while selectedDevice < 0 or selectedDevice > i:
	print("The number you typed is not allowed. Please enter a number between 0 and %d" % i)
	selectedDevice = int(input())

first_match = service_matches[selectedDevice]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print("Connecting to \"%s\" on %s on port %s" % (name, host, port))

sock = BluetoothSocket( RFCOMM )
try:
	sock.connect((host, port))
except BluetoothError as err:
	print("BluetoothError: %s" % err)
	sys.exit(0)

print("Successfully connected.")
data = input("Send a message:")
sock.send(data)

sock.close()

