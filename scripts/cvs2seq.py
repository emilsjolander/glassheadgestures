#!/usr/bin/env python

import csv
import os

# -------- Functions

def readCsv(file):
	reader = csv.reader(open(file, 'r'))
	keys = reader.next()
	data = []

	for row in reader:
		rowDict = {}
		for i in range(0, len(row)):
			rowDict[keys[i]] = float(row[i])
		data.append(rowDict)

	return data

def isCsv(file):
	return file.endswith(".csv")

def sequence(data):
	gyrX = [row["gyrX"] for row in data]
	gyrY = [row["gyrY"] for row in data]

	seq = zip(gyrX, gyrY)

	return seq

# -------- Script

# Get files
no_files = filter(isCsv, os.listdir("no"))
yes_files = filter(isCsv, os.listdir("yes"))
no_files = map(lambda x: "no/" + x, no_files)
yes_files = map(lambda x: "yes/" + x, yes_files)

# Read csv data from files
no_data = map(sequence, filter(lambda x: len(x) > 0, map(readCsv, no_files)))
yes_data = map(sequence, filter(lambda x: len(x) > 0, map(readCsv, yes_files)))

f = open('no.seq','w')
for data in no_data:
	for p in data:
		f.write("[")
		for i in range(0,2):
			f.write("%.10f" % p[i])
			if i == 0:
				f.write(" ")
		f.write("]; ")
	f.write("\n")
f.close()

f = open('yes.seq','w')
for data in yes_data:
	for p in data:
		f.write("[")
		for i in range(0,2):
			f.write("%.10f" % p[i])
			if i == 0:
				f.write(" ")
		f.write("]; ")
	f.write("\n")
f.close()
