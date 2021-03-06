"""
Take the nodes to be clustered as input from a file,
cluster them using the meanshift clustering algorithm
and write the results in the requisite output file.
"""
import math
import re
import argparse
from itertools import cycle
from sklearn.cluster import MeanShift
import numpy as np
import matplotlib.pyplot as plt

ENV = "cmd"

def cluster(coord, bandwidth):
    """
    cluster function clusters the elements in the array coord using the other two as parameters
    """
    global ENV
    mean_shift = MeanShift(bandwidth=bandwidth)
    mean_shift.fit(coord)
    labels = mean_shift.labels_
    cluster_centers = mean_shift.cluster_centers_
    # print (cluster_centers) # Debug

    n_clusters_ = len(np.unique(labels))
    print("number of estimated clusters : %d, % d" % (n_clusters_, len(labels)))

    ## ###   #############################################################   ### ##
    plt.figure(1)
    plt.clf()
    plots = np.array(coord)

    colors = cycle('bgrcmykbgrcmykbgrcmykbgrcmyk')
    for k, col in zip(range(n_clusters_), colors):
        my_members = labels == k
        cluster_center = cluster_centers[k]
        plt.plot(plots[my_members, 0], plots[my_members, 1], col + '.')
        plt.plot(cluster_center[0], cluster_center[1], 'o', markerfacecolor=col,
                 markeredgecolor='k', markersize=14)
    plt.title('Estimated number of clusters: %d' % n_clusters_)
    plt.show()
    ## ###   #############################################################   ### ##

    # Write to a file
    if ENV is "eclipse".__str__:
        file = open("./files/interface/output.txt", "w")
    else:
        file = open("./interface/output.txt", "w")

    file.write("CARPARK_SECTION\n")
    file.write("%d\n" % n_clusters_)
    i = 0
    for center in cluster_centers:
        # print(center.item(0), center.item(1))
        file.write("%d %d %d\n" % (i, int(center.item(0)), int(center.item(1))))
        i = i+1

    return cluster_centers

def main():
    """
    The main function that reads from the input file and calls cluster over it.
    """
    global ENV
    parser = argparse.ArgumentParser(description='Relative or Absolute Path')
    parser.add_argument('density', type=int, help='density of data points')
    parser.add_argument('max_range', type=int, help='maximum range of data points')
    parser.add_argument('--eclipse', action='store_true', help='calling environment')
    parser.add_argument('--bandwidth', action='store_true', help='input file has custom bandwidth')
    args = parser.parse_args()
    print(args)
    if args.eclipse:
        file = open("./files/interface/input.txt", "r")
        ENV = "eclipse"
    else:
        file = open("./interface/input.txt", "r")

    get_bandwidth = True
    if args.bandwidth:
        get_bandwidth = False
        bandwidth_ = 0
    else:
        bandwidth_ = 0.2 * math.pow(1.414, -(args.density*args.density)) * args.max_range

    coord = []
    for line in file:
        list_line = re.findall(r'[0-9]+', line)
        if get_bandwidth:
            coord.append([int(list_line[0]), int(list_line[1])])
        else:
            bandwidth_ = float(list_line[0])
            get_bandwidth = True
    cluster(coord, bandwidth_)

if __name__ == '__main__':
    main()
