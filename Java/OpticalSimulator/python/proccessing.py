from sklearn.preprocessing import MinMaxScaler
import pandas as pd
import numpy as np

binaryDataset = []
gainScaler    = MinMaxScaler(feature_range = (-1, 1))
lossScaler    = MinMaxScaler(feature_range = (-1, 1))

def datasetBalancing(dataset):
    newDataset      = []
    linesPerChannel = []
    currentChannel  = 2

    # Finding channel minimum target 
    count = 0
    for line in dataset.values:
        channels = np.count_nonzero(line[0: 40], axis=0)

        if currentChannel == channels:
            count += 1
        else:
            linesPerChannel.append(count)

            currentChannel = channels
            count          = 0

    linesPerChannel.append(count)   
    
    #print(linesPerChannel)
    
    guide = np.array(linesPerChannel)
    guide = guide[guide != 0]
    
    target     = min(guide)
    newDataset = cutDown(dataset, guide, target)
    
    return newDataset

def cutDown(dataset, guide, target):
    newDataset   = []
    currentEntry = 0

    for entry in guide:
        #print(currentEntry, currentEntry + target)

        subset = dataset.values[currentEntry : currentEntry + target]
        newDataset.extend(subset)
        currentEntry += entry
    
    return newDataset
    
def preProccessing(ampNumber):
    global binaryDataset
    
    originalDataset = pd.read_csv('../output/datasets/MultipleRuns/d2/dataset-{}.txt'.format(ampNumber), sep = ',',header = None)
    originalDataset = originalDataset.sample(frac = 1, random_state = 5)
    binaryDataset   = []
    
    balancedDataset = datasetBalancing(originalDataset)

    for line in balancedDataset:
        myList = [1 if j != 0 else 0 for j in line[:40]]
        myList.extend(line[40:])
        binaryDataset.append(myList)
    
    binaryDataset = pd.DataFrame(binaryDataset)
    
def loadDataset(ampNumber): 
    dataset = binaryDataset.values[:, :40]
    
    for i in range(0, ampNumber * 2, 2):
        gain    = np.array(binaryDataset.values[:, 40 + i])
        gain    = gain.reshape(-1, 1)
        dataset = np.hstack((dataset, gain))
        
        loss    = np.array(binaryDataset.values[:, 40 + i + 1])
        loss    = loss.reshape(-1, 1)
        dataset = np.hstack((dataset, loss))
                
    X, y = np.array(dataset[:, :40]), np.array(dataset[:, 40:])
    return X, y

def getGainError(value1, value2):
    return abs(value1 - value2)

def getLossError(value1, value2):
    return abs(value1 - value2)

'''def invertGainNorm(value):
    auxArray = np.array([value, 0, 0, 0, 0, 0]).reshape(-1, 1)
    return round(gainScaler.inverse_transform(auxArray)[0][0])

def invertLossNorm(value):
    auxArray = np.array([value, 0, 0, 0, 0, 0]).reshape(-1, 1)
    return round(lossScaler.inverse_transform(auxArray)[0][0])
    
def getGainError(value1, value2):
    return abs(invertGainNorm(value1) - invertGainNorm(value2))

def getLossError(value1, value2):
    return abs(invertLossNorm(value1) - invertLossNorm(value2))'''