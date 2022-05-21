from flask import Flask, escape, request
app = Flask(__name__)

import math
import datetime
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.multioutput import MultiOutputRegressor
from sklearn.ensemble import BaggingRegressor
from sklearn.tree import ExtraTreeRegressor
from sklearn.neural_network import MLPRegressor
from sklearn import linear_model
from sklearn.preprocessing import MinMaxScaler
from scipy import interpolate 
import pandas as pd
import matplotlib.pyplot as plt

gainArray    = []
lossArray    = []
layerArray   = ["2 layers", "3 layers", "4 layers", "5 layers", "6 layers"]
neuronsArray = [16, 32, 64, 128, 256, 512, 1024]
markers      = ['o', 'D', 'P', 'd', 's']

originalDataSet = pd.read_csv('dataset.txt', sep = ',',header = None)
scaler          = MinMaxScaler()

def loadDataset():    
    scaler.fit(originalDataSet.sample(frac=1).values.reshape(-1, 1))
    dataSet = scaler.transform(originalDataSet.values) 
    
    features, result = np.array(dataSet[:, :4]), np.array(dataSet[:, 4:])
    return features, result

def invertNorm(value):
    auxArray = np.array([value, 0, 0, 0, 0, 0]).reshape(-1, 1)
    return scaler.inverse_transform(auxArray)[0][0]
    
def getError(value1, value2):
    return abs(invertNorm(value1) - invertNorm(value2))

def crossValidate(regressor, features, result, folds = 5):
    foldSize   = math.ceil(features.shape[0] / folds)
    gainErrors = []
    lossErrors = []
    
    for i in range(folds): 
        sliceBegin = i * foldSize
        sliceEnd   = (i + 1) * foldSize
        
        X_train = np.delete(features, np.s_[sliceBegin: sliceEnd], 0)
        y_train = np.delete(result, np.s_[sliceBegin: sliceEnd], 0)
        
        regressor.fit(X_train, y_train) #get train error!!!
            
        X_test = features[sliceBegin: sliceEnd]
        y_test = result[sliceBegin: sliceEnd]
        
        gainError = 0
        lossError = 0
        
        prediction = regressor.predict(X_test)
        
        for predicted, expected in zip(prediction, y_test):
            gainError += getError(predicted[0], expected[0])
            lossError += getError(predicted[1], expected[1])
            
        gainErrors.append(gainError / foldSize)
        lossErrors.append(lossError / foldSize)

    return np.array(gainErrors), np.array(lossErrors)

def trainModel(model):   
    features, result       = loadDataset()
    gainErrors, lossErrors = crossValidate(model, features, result)

    gainArray.append(gainErrors)
    lossArray.append(lossErrors)
    
gainArray = []
lossArray = []

regressor = None

@app.route('/')
def index():
    return ""

def setMLP(layers, neurons):  
    global regressor  
    if layers == 2:
        regressor = MLPRegressor(hidden_layer_sizes = (neurons, neurons), activation = 'relu', solver = 'lbfgs', learning_rate = 'constant', alpha = 0.001, random_state = 0)
    
    if layers == 3:
        regressor = MLPRegressor(hidden_layer_sizes = (neurons, neurons, neurons), activation = 'relu', solver = 'lbfgs', learning_rate = 'constant', alpha = 0.001, random_state = 0)
    
    if layers == 4:
        regressor = MLPRegressor(hidden_layer_sizes = (neurons, neurons, neurons, neurons), activation = 'relu', solver = 'lbfgs', learning_rate = 'constant', alpha = 0.001, random_state = 0)
    
    if layers == 5:
        regressor = MLPRegressor(hidden_layer_sizes = (neurons, neurons, neurons, neurons, neurons), activation = 'relu', solver = 'lbfgs', learning_rate = 'constant', alpha = 0.001, random_state = 0)
    
    if layers == 6:
        regressor = MLPRegressor(hidden_layer_sizes = (neurons, neurons, neurons, neurons, neurons, neurons), activation = 'relu', solver = 'lbfgs', learning_rate = 'constant', alpha = 0.001, random_state = 0)

    trainModel(regressor)
    
@app.route('/predict')
def predict():
    layers   = int(request.args.get('layers', None))
    neurons  = int(request.args.get('neurons', None))
    ampNmbr  = float(request.args.get('ampNmbr', None))
    inpPwr   = float(request.args.get('inpPwr', None))
    chLength = float(request.args.get('chLength', None))
    ampIndex = float(request.args.get('ampIndex', None))
    
    if not regressor:
        print("Setting model")
        setMLP(layers, neurons)
          
    prediction = regressor.predict(scaler.transform([[ampNmbr, inpPwr, chLength, ampIndex]]))
    return str(invertNorm(prediction[0][0])) + "," + str(invertNorm(prediction[0][1]))

@app.route('/resetModel')
def resetModel():
    global regressor    
    regressor = None
    return "Model resetted"

@app.route('/plotNeuronsXpout')
def plotNeuronsXpout():
    data = open("output/neuronsXpout", "r")  
    data = data.readlines()[0].replace("[[", "").replace("]]", "").split("], [")

    fig, axis = plt.subplots(figsize = (10, 5))
    
    count = 0
    for i in range(len(layerArray)):
        dataArray = data[i].split(',')
    
        x_new     = np.linspace(16, 1024, 300)
        a_BSpline = interpolate.make_interp_spline(neuronsArray, list(map(float, dataArray)))
        y_new     = a_BSpline(x_new)
            
        axis.plot(x_new, y_new, label = layerArray[count])
        axis.scatter(neuronsArray, list(map(float, dataArray)), marker = markers[count], linewidths = 1)
        count += 1
    
    box = axis.get_position()
    axis.set_position([box.x0, box.y0, box.width * 0.8, box.height])
    axis.legend(loc='center left', bbox_to_anchor=(1, 0.5))
    
    plt.xlabel("Neurons in each hidden layer")
    plt.ylabel("RMSE (dB)")
    
    plt.savefig('output/plotNeuronsXpout.png')
    
    return "Figure saved"