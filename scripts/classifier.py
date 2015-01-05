#!/usr/bin/env python

import ghmm
import csv
import os
import numpy
import numpy.random
import random
import math
import matplotlib.pyplot as plt

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

def initNoHmm():
	# Domain
	F = ghmm.Float()

	# Transition matrix
	A = [ 
			[0.15,0.20,0.25,0.40],
			[0.15,0.20,0.25,0.40],
			[0.15,0.20,0.25,0.40],
			[0.15,0.20,0.25,0.40]
		]

	# Emission distributions
	B = [ 
		  	[0, 0.5**2],
		  	[0, 0.5**2],
		  	[0, 0.5**2],
		  	[0, 0.5**2]
		]

	# Initial transition probabilities
	pi = [0.25, 0.25, 0.25, 0.25]

	return ghmm.HMMFromMatrices(F,ghmm.GaussianDistribution(F), A, B, pi)

def initYesHmm():
	# Domain
	F = ghmm.Float()

	# Transition matrix
	A = [ 
			[0.15,0.20,0.25,0.40],
			[0.15,0.20,0.25,0.40],
			[0.15,0.20,0.25,0.40],
			[0.15,0.20,0.25,0.40]
		]

	# Emission distributions
	B = [ 
		  	[0, 0.5**2],
		  	[0, 0.5**2],
		  	[0, 0.5**2],
		  	[0, 0.5**2]
		]

	# Initial transition probabilities
	pi = [0.25, 0.25, 0.25, 0.25]

	return ghmm.HMMFromMatrices(F,ghmm.GaussianDistribution(F), A, B, pi)

def flatten(data):
	return [item for sublist in data for item in sublist]

def sequence(data):
	# Extract data we want to use
	gyrX = [row["gyrX"] for row in data]
	gyrY = [row["gyrY"] for row in data]

	seq = flatten(zip(gyrX, gyrY))

	F = ghmm.Float()
	return ghmm.EmissionSequence(F, seq)

# -------- Script

# Get files
no_files = filter(isCsv, os.listdir("no"))
yes_files = filter(isCsv, os.listdir("yes"))
no_files = map(lambda x: "no/" + x, no_files)
yes_files = map(lambda x: "yes/" + x, yes_files)

# Filter out training and test data 3:1 split
train_no_files = no_files[:int(len(no_files) * 0.75)]
train_yes_files = yes_files[:int(len(yes_files) * 0.75)]
test_no_files = no_files[int(len(no_files) * 0.75):]
test_yes_files = yes_files[int(len(yes_files) * 0.75):]

# Some files do not contain data as collection was termitated early
train_no_files = train_no_files
train_yes_files = train_yes_files
test_no_files = test_no_files
test_yes_files = test_yes_files

# Read csv data from files
train_no_data = map(sequence, filter(lambda x: len(x) > 0, map(readCsv, train_no_files)))
train_yes_data = map(sequence, filter(lambda x: len(x) > 0, map(readCsv, train_yes_files)))
test_no_data = map(sequence, filter(lambda x: len(x) > 0, map(readCsv, test_no_files)))
test_yes_data = map(sequence, filter(lambda x: len(x) > 0, map(readCsv, test_yes_files)))

# Init both hmms
noModel = initNoHmm();
yesModel = initYesHmm();

# Print models
print "\nInitial No-Model:\n", noModel, "\n"
print "\nInitial Yes-Model:\n", yesModel, "\n"

random.shuffle(train_no_data)
random.shuffle(train_yes_data)

# Train models
noModel.baumWelch(ghmm.SequenceSet(ghmm.Float(), train_no_data), 100, 0.0001)
yesModel.baumWelch(ghmm.SequenceSet(ghmm.Float(), train_yes_data), 100, 0.0001)

# Test on trained models
no_given_yes = []
yes_given_no = []
no_given_no = []
yes_given_yes = []
for data in test_no_data:
	no_given_no.append(noModel.loglikelihood(data))
	yes_given_no.append(yesModel.loglikelihood(data))

for data in test_yes_data:
	no_given_yes.append(noModel.loglikelihood(data))
	yes_given_yes.append(yesModel.loglikelihood(data))

# Print models
print "\nTrained No-Model:\n", noModel, "\n"
print "\nTrained Yes-Model:\n", yesModel, "\n"

yes_ratio = []
no_ratio = []
for i in range(0, len(yes_given_yes)):
	yes_ratio.append(yes_given_yes[i] / yes_given_no[i])
for i in range(0, len(yes_given_yes)):
	no_ratio.append(no_given_yes[i] / no_given_no[i])

plt.plot(range(0, 36), yes_ratio, 'r-', range(0, 36), no_ratio, 'b-')
plt.show();

# Print statistics
print "loglikelihood mean ratio Y: ", numpy.mean(yes_given_yes) / numpy.mean(yes_given_no)
print "loglikelihood mean ratio N: ", numpy.mean(no_given_yes) / numpy.mean(no_given_no)
