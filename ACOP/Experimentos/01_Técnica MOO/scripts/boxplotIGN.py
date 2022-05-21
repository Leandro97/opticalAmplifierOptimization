import matplotlib.pyplot as plt
from matplotlib import rcParams
rcParams['font.size'] = 14

data_e1= []
#2a
data_e1.append([0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.26647424827652144, 0.15833304865586004, 0.32919244094024674, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.15833304865586004, 0.16608217889956767, 0.15833304865586004])
#3a
data_e1.append([0.08206138689115744, 0.08943374907823501, 0.08264067044638843, 0.08943374907823501, 0.08281248743391376, 0.09098421596544488, 0.08206138689115744, 0.08206138689115744, 0.08206138689115744, 0.08943374907823501, 0.10191297849253386, 0.08206138689115744, 0.08206138689115744, 0.08206138689115744, 0.09100265432114675, 0.08943374907823501, 0.08943374907823501, 0.08206138689115744, 0.08943374907823501, 0.09552556599732631, 1.0166872229013757, 0.08288144300180768, 0.08947976295470653, 0.09121563198423649, 0.08288144300180768, 0.08947976295470653, 0.08947976295470653, 0.08206138689115744, 0.09247832119658725, 0.10191297849253386])
data_e2 = []
#2a
data_e2.append([0.04009098718600441, 0.1141661279968845, 0.0533876400598974, 0.04009098718600441, 0.04009098718600441, 0.0533876400598974, 0.04009098718600441, 0.20500503536045156, 0.4705004347225387, 0.20500503536045156, 0.0533876400598974, 0.04009098718600441, 0.04009098718600441, 0.04009098718600441, 0.2659083679481174, 0.3471372708201705, 0.04009098718600441, 0.4946504532324137, 0.04009098718600441, 0.04009098718600441, 0.41081572481846934, 0.04009098718600441, 0.20500503536045156, 0.20500503536045156, 0.20500503536045156, 0.20500503536045156, 0.0533876400598974, 0.04009098718600441, 0.04009098718600441, 0.2659083679481174])
#3a
data_e2.append([0.05228550186314919, 0.06304645127696168, 0.19286207919718867, 0.028748164214731142, 0.22273676849617752, 0.07286185310264888, 0.007742197382851661, 0.01901221729809778, 0.07078161240979726, 0.05228550186314919, 0.0705572062974095, 0.10817943644493387, 0.0889328715797254, 0.02228718724088839, 0.05236739907542032, 0.12817995631580445, 0.15735873038762394, 0.14471685637670895, 0.10473329542189969, 0.20413967756395507, 0.3017254919770405, 0.45861122742551835, 0.01731038672283318, 0.01667421957172139, 0.1547615230994761, 0.052901767612903564, 0.16907489251066327, 0.16873206793572365, 0.05172602721150721, 0.08871360464747198])

ax_e1 = plt.subplot(121)
ax_e1.set_title('EDFA 1')
ax_e1.boxplot(data_e1, showfliers=False)

plt.ylabel('Inverted Generational Distance')
plt.tight_layout(pad=0.5)
plt.xticks([1,2],['2','3'])

ax_e2 = plt.subplot(122)
ax_e2.set_title('EDFA 2')
ax_e2.boxplot(data_e2, showfliers=False)

plt.xlabel('Number of Amplifiers')
plt.tight_layout(pad=0.5)
plt.xticks([1,2],['2','3'])

plt.show()