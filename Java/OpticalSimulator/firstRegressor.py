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
from sklearn.neighbors import KNeighborsRegressor
import pandas as pd
import matplotlib.pyplot as plt

gainArray      = []
lossArray      = []
#heuristicArray = ["MOO", "Max Gain", "AdGC", "AdGC NLI", "AsHB", "AsHB NLI", "Loss Comp", "MLP"]
heuristicArray = ["MOO", "Huber", "KNN", "ETR"]
markers        = ['o', 'D', 'x', 'P']

#originalDataSet = pd.read_csv('dataset.txt', sep = ',',header = None)
originalDataSet = pd.read_csv('reduced_dataset.txt', sep = ',',header = None)
scaler = MinMaxScaler()

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
        
        regressor.fit(X_train, y_train)
            
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

def trainModel(strategy):   
    features, result = loadDataset()
    gainErrors, lossErrors = crossValidate(strategy, features, result)

    gainArray.append(gainErrors)
    lossArray.append(lossErrors)
    
gainArray = []
lossArray = []

huber = MultiOutputRegressor(linear_model.HuberRegressor())
trainModel(huber)

knn = MultiOutputRegressor(KNeighborsRegressor())
trainModel(knn)

extra_tree = ExtraTreeRegressor(criterion = "mse", random_state=0)
bret = MultiOutputRegressor(BaggingRegressor(base_estimator = extra_tree, n_estimators = 100, random_state = 0))
trainModel(bret)

@app.route('/')
def index():
    trainModel(huber)
    trainModel(knn)
    trainModel(bret)
    return "Model trained"

@app.route('/predict')
def predict():
    # ?ampNmbr=2&inpPwr=-30.0&chLength=80.0&ampIndex=0
    regressor = request.args.get('regressor', None)
    ampNmbr   = float(request.args.get('ampNmbr', None))
    inpPwr    = float(request.args.get('inpPwr', None))
    chLength  = float(request.args.get('chLength', None))
    ampIndex  = float(request.args.get('ampIndex', None))
    
    model = None
    if regressor == "huber":
        model = huber
        print("HUBER")
    elif regressor == "knn":
        model = knn
        print("KNN")
    else:
        print("ETR")
        model = bret
        
    prediction = model.predict(scaler.transform([[ampNmbr, inpPwr, chLength, ampIndex]]))
    
    gain = invertNorm(prediction[0][0]) if invertNorm(prediction[0][0]) > 0 else 0 
    loss = invertNorm(prediction[0][1]) if invertNorm(prediction[0][1]) > 0 else 0
                      
    print(str(gain) + "," + str(loss))
    return str(gain) + "," + str(loss)
  
@app.route('/osnrBoxplot')
def plotOSNR(): 
    osnrResult   = []

    osnr = open("output/osnr", "r")  
    osnr = osnr.readlines()[0].replace("[[", "").replace("]]", "").split("], [")

    for array in osnr:
        array = array.split(',')
        osnrResult.append(list(map(float, array)))
    
    fig, axis = plt.subplots(figsize = (10, 5))
    data      = osnrResult

    axis.boxplot(data)
    axis.set_title("OSNR")
    axis.set_xticklabels(np.repeat(heuristicArray, 1))

    plt.savefig('output/osnrBoxplot.png')
    
    return "Figure saved"

@app.route('/rippleBoxplot')
def plotRipple(): 
    rippleResult = []

    ripple = open("output/ripple", "r")
    ripple = ripple.readlines()[0].replace("[[", "").replace("]]", "").split("], [")

    for array in ripple:
        array = array.split(',')
        rippleResult.append(list(map(float, array)))
    
    fig, axis = plt.subplots(figsize = (10, 5))
    data      = rippleResult

    axis.boxplot(data)
    axis.set_title("Ripple")
    axis.set_xticklabels(np.repeat(heuristicArray, 1))

    plt.savefig('output/rippleBoxplot.png')
    return "Figure saved"

def legend_without_duplicate_labels(fig):
    handles, labels = plt.gca().get_legend_handles_labels()
    unique = [(h, l) for i, (h, l) in enumerate(zip(handles, labels)) if l not in labels[:i]]
    fig.legend(*zip(*unique), loc = 3, ncol = 2)
    
@app.route('/plotParetoGrid')
def plotParetoGrid():
    ampNumberEntries = [2, 3, 4]
    ampTypeEntries   = [1, 2]

    col = 0
    fig, axis = plt.subplots(2, 3, figsize = (10, 5))
    
    for ampNumber in ampNumberEntries:
        row = 0
        for ampType in ampTypeEntries:
            osnr = open("output/osnr_{}amp_edfa{}".format(ampNumber, ampType), "r")  
            osnr = osnr.readlines()[0].replace("[[", "").replace("]]", "").split("], [")
            
            ripple = open("output/ripple_{}amp_edfa{}".format(ampNumber, ampType), "r")
            ripple = ripple.readlines()[0].replace("[[", "").replace("]]", "").split("], [")
            
            count = 0
            for i in range(len(heuristicArray)):
                osnrArray   = osnr[i].split(',')
                rippleArray = ripple[i].split(',')
                
                axis[row, col].set_title("EDFA {} - {} amplifiers".format(ampType, ampNumber))
                axis[row, col].scatter(list(map(float, rippleArray)), list(map(float, osnrArray)), marker = markers[count], label = heuristicArray[count])
                axis[row, col].set_xlabel("OSNR Ripple (dB)")
                axis[row, col].set_ylabel("Min. OSNR (dB)")
                
                if row == 0:
                    axis[row, col].set_ylim(18, 32)
                    axis[row, col].set_yticks(np.arange(18, 32, 2).tolist())
                else:
                    axis[row, col].set_ylim(12, 30)
                
                axis[row, col].grid(color = 'b', linestyle = '-', linewidth = 0.5)
                count += 1
                
            row += 1
        col += 1
        
    fig.tight_layout(pad = 1.0)
    legend_without_duplicate_labels(fig)
    
    plt.savefig('output/pareto.png')
    
    return "Figure saved"

@app.route('/plotPareto')
def plotPareto():
    osnrResult   = []

    osnr = open("output/osnr", "r")  
    osnr = osnr.readlines()[0].replace("[[", "").replace("]]", "").split("], [")
    
    ripple = open("output/ripple", "r")  
    ripple = ripple.readlines()[0].replace("[[", "").replace("]]", "").split("], [")

    fig, axis = plt.subplots(figsize = (10, 5))
    data      = osnrResult
    
    count = 0
    for i in range(len(heuristicArray)):
        osnrArray   = osnr[i].split(',')
        rippleArray = ripple[i].split(',')
        axis.scatter(list(map(float, rippleArray)), list(map(float, osnrArray)), marker = markers[count], label = heuristicArray[count])
        count += 1
        
    axis.set_title("EDFA 2 - 8 amplifiers")
    
    box = axis.get_position()
    axis.set_position([box.x0, box.y0, box.width * 0.8, box.height])
    axis.legend(loc='center left', bbox_to_anchor=(1, 0.5))
    
    plt.xlabel("Ripple (dB)")
    plt.ylabel("OSNR (dB)")
    
    plt.savefig('output/pareto.png')
    
    return "Figure saved"
