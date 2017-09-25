from datetime import datetime
import os
commandToRun = "./simpledynamo-grading.osx /Users/sudie/Desktop/Academics/Spring_2017/Distributed_Systems_586/DS_Projects/DS_PROJECT_4/SimpleDynamo/app/build/outputs/apk/app-debug.apk"

totalScoreCheck = "Total score: 23"
phase56check = "Phase 6: pass"
timeCommand = "date"
import subprocess
#subprocess.check_output(['ls','-l']) #all that is technically needed...
count = 0

for i in range(20):
	subprocess.call(timeCommand + " > AllPhases_Date" + str(i),shell=True)
	subprocess.call(commandToRun + " > AllPhases_termOutput" + str(i),shell=True)
	fo = open("AllPhases_termOutput" + str(i), "r")
	text = fo.read()

	if totalScoreCheck in text:
		print "success!"
	else:
		print "fail!"
		while True:
			os.system('say "your program has failed"')
		break

while True:
	os.system('say "your program has finished"')


# print "successful runs - " + str(count)
