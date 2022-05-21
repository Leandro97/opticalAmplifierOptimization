from flask import Flask, escape, request
app = Flask(__name__)

import matplotlib.pyplot as plt
import math
import numpy as np
import time

import proccessing
import customPlotter as cp 

from sklearn.multioutput import MultiOutputRegressor
from sklearn.linear_model import BayesianRidge
from sklearn.ensemble import RandomForestRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.linear_model import LassoCV
from sklearn.dummy import DummyRegressor

model     = None
ampNumber = 2

timeIndex = 0
timeValues = [0, 0, 0, 0, 0]

def crossValidate(regressor, X, y, folds = 5):
    foldSize   = math.ceil(X.shape[0] / folds)
    gainErrors = []
    lossErrors = []
    
    for i in range(folds): 
        sliceBegin = i * foldSize
        sliceEnd   = (i + 1) * foldSize
        
        X_train = np.delete(X, np.s_[sliceBegin: sliceEnd], 0)
        y_train = np.delete(y, np.s_[sliceBegin: sliceEnd], 0)
        
        regressor.fit(X_train, y_train)
            
        X_test = X[sliceBegin: sliceEnd]
        y_test = y[sliceBegin: sliceEnd]
        
        gainError = 0
        lossError = 0
        
        prediction = regressor.predict(X_test)
        
        for predicted, expected in zip(prediction, y_test):
            gainError += proccessing.getGainError(predicted[0], expected[0]) 
            lossError += proccessing.getLossError(predicted[1], expected[1])
                 
        gainErrors.append((gainError / ampNumber) / foldSize) # average gain error by amp
        lossErrors.append((lossError / ampNumber) / foldSize) # average loss error by amp
        
    return np.array(gainErrors), np.array(lossErrors)

def trainModel(model):   
    proccessing.preProccessing(ampNumber)
    
    X, y                   = proccessing.loadDataset(ampNumber)
    gainErrors, lossErrors = crossValidate(model, X, y)
    
    print(gainErrors, "=> %0.2f (+/- %0.2f)" % (np.mean(gainErrors), gainErrors.std() * 2))
    print(lossErrors, "=> %0.2f (+/- %0.2f)\n" % (np.mean(lossErrors), lossErrors.std() * 2))
    
    return gainErrors, lossErrors

@app.route('/')
def index():
    return "Connection granted"

@app.route('/createModel')
def createModel():
    global model, ampNumber
    
    regressor = request.args.get('regressor', None)
    ampNumber = int(request.args.get('ampNumber', None))
    
    if regressor == "BayesianRidge":
        model = MultiOutputRegressor(BayesianRidge())
    elif regressor == "RandomForestRegressor":
        model = MultiOutputRegressor(RandomForestRegressor())
    elif regressor == "DecisionTreeRegressor":
        model = MultiOutputRegressor(DecisionTreeRegressor())
    elif regressor == "LassoCV":
        model = MultiOutputRegressor(LassoCV())
    else:
        model = MultiOutputRegressor(DummyRegressor())
    
    trainModel(model)
    
    return "Model trained"

@app.route('/predict')
def predict():
    regressor   = request.args.get('regressor', None)
    channelList = request.args.getlist('channelList', None)
    
    start = time.time()
    
    print("#####\n")
    formatedChannelList = [[int(x) for x in channelList]]

    prediction = model.predict(formatedChannelList)
    result = ""
    
    for i in range(0, ampNumber * 2, 2):
        gain = prediction[0][i]
        loss = prediction[0][i + 1]
        
        result += str(gain) + "," + str(loss) 
        
        if i < ampNumber * 2 - 2:
            result += ","
    
    if regressor == "BayesianRidge":
        timeIndex = 0
    elif regressor == "RandomForestRegressor":
        timeIndex = 1
    elif regressor == "DecisionTreeRegressor":
        timeIndex = 2
    elif regressor == "LassoCV":
        timeIndex = 3
    else:
        timeIndex = 4
    
    end = time.time()
    
    timeValues[timeIndex] += (end - start) * (1/21)
    print("{}[{}]: {}".format(regressor, timeIndex, timeValues[timeIndex]))
    print("#####\n\n")
    
    return result

@app.route('/plotParetoBaselineV1')
def plotParetoBaselineV1():
	cp.plotParetoBaselineV1()
	return "Figure saved"

