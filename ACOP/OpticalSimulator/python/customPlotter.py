from flask import Flask, escape, request
app = Flask(__name__)

import math
import datetime
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

import locale
locale.setlocale(locale.LC_NUMERIC, "pt_BR.utf8")

gainArray      = []
lossArray      = []
heuristicArray = ["MOO", "BR", "RF", "DT", "LCV", "SM"]
markers        = ['o', 'D', '^', 'x', 'v', 's']

@app.route('/')
def index():
    return ""

def legend_without_duplicate_labels(fig):
    handles, labels = plt.gca().get_legend_handles_labels()
    unique = [(h, l) for i, (h, l) in enumerate(zip(handles, labels)) if l not in labels[:i]]
    fig.legend(*zip(*unique), loc = 4, bbox_to_anchor = (0.97, 0.13), ncol = 2)

def plotParetoBaselineV1():
    ampNumberEntries     = [2, 3, 4, 5, 6, 7, 8] #[2, 3, 4]
    channelNumberEntries = [10, 20, 40]
    
    plt.rcdefaults()
    plt.rcParams['axes.formatter.use_locale'] = True
    
    for ampNumber in ampNumberEntries:
        col = -1
        fig, axis = plt.subplots(1, 3, figsize = (10, 4))
    
        for channelNumber in channelNumberEntries:
            col += 1
            osnr = open("../output/optimizedPareto/osnr_{}amp_{}channel".format(ampNumber, channelNumber), "r")  
            osnr = osnr.readlines()[0].replace("[[", "").replace("]]", "").split("], [")
            
            ripple = open("../output/optimizedPareto/ripple_{}amp_{}channel".format(ampNumber, channelNumber), "r")
            ripple = ripple.readlines()[0].replace("[[", "").replace("]]", "").split("], [")

            count = 0
            for i in range(len(heuristicArray)):
                osnrArray   = osnr[i].split(',')
                rippleArray = ripple[i].split(',')
                
                if len(osnrArray) == 0:
                    osnrArray = ["0"]
                if len(rippleArray) == 0:
                    rippleArray = ["0"]
                
                s = [20*4**1 for n in range(len(rippleArray))]
                
                axis[col].set_title("{} amps - {} channels".format(ampNumber, channelNumber), fontsize = 20)
                axis[col].scatter(list(map(float, rippleArray)), list(map(float, osnrArray)), s = s, marker = markers[count], label = heuristicArray[count])
                
                axis[col].xaxis.set_tick_params(labelsize = 16)
                axis[col].yaxis.set_tick_params(labelsize = 16)
                
                if col == 0:
                    axis[col].set_ylabel("Minimum OSNR (dB)", fontsize = 18)
                    
                if col == 1:
                    axis[col].set_xlabel("OSNR Ripple (dB)", fontsize = 18)
                
                axis[col].grid(color = 'b', linestyle = '-', linewidth = 0.5)
                
                count += 1
        
        fig.tight_layout(pad = 1.0)
        legend_without_duplicate_labels(fig)
        
        plt.savefig('../output/optimizedPareto/pareto{}.pdf'.format(ampNumber), format = 'pdf', dpi = 1000, bbox_inches = "tight")
             
