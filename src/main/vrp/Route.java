package vrp;

import java.util.Vector;
import java.util.Collections;

// Class to represent a Route.
// All routes are defined as a vector of integers, and hence, all customers are to be accessed from the Main.nodes array
class Route {
    Vector<Integer> route;
    int routeCost;
    int demand;
    public Route() {
        route = new Vector<Integer>();
        routeCost = 0;
        demand = 0;
    }
    public String toString() {
        // Give the string representation for the Route object
        String route = "";
        for (int cust : this.route) {
            route = route + cust + ", ";
        }
        return route;
    }
    public Route clone() {
        Route clonedRoute = new Route();
        clonedRoute.routeCost = this.routeCost;
        clonedRoute.demand = this.demand;
        clonedRoute.route = (Vector<Integer>) this.route.clone();
        return clonedRoute;
    }
    public void addCustomer(int id) {
        route.add(id);
        // Add the demand accordingly, whether the added node is a customer or a carpark 
        if (id > Main.numCarpark && id < Main.numNodes) demand += Main.customers[id-Main.numCarpark-1].demand;
        else if (id >= Main.numNodes) demand += Main.routedCarparks.elementAt(id-Main.numNodes).route.demand;
        if (route.size() > 1) {
            int prevNode = route.elementAt(route.size()-2);
            int newNode = route.elementAt(route.size()-1);
            if (prevNode >= Main.numNodes)  prevNode = Main.routedCarparks.elementAt(prevNode-Main.numNodes).cpindex;
            if (newNode >= Main.numNodes)  newNode = Main.routedCarparks.elementAt(newNode-Main.numNodes).cpindex;
            routeCost += Main.nodesDistance[prevNode][newNode];
        }
    }
    public void addCustomer(int id, int index) {
        this.route.add(index, id);
        if (id > Main.numCarpark && id < Main.numNodes) demand += Main.customers[id-Main.numCarpark-1].demand;
        else if (id >= Main.numNodes) demand += Main.routedCarparks.elementAt(id-Main.numNodes).route.demand;
        // Conditions on whether there is a node after insert index or not, and what is the route size
        // Also, accordingly change the elements at index
        if (index >= route.size() -1 && index != 0) {
            int prevNode = route.elementAt(index-1);
            int currNode = route.elementAt(index);
            if (prevNode >= Main.numNodes)  prevNode = Main.routedCarparks.elementAt(prevNode-Main.numNodes).cpindex;
            if (currNode >= Main.numNodes)  currNode = Main.routedCarparks.elementAt(currNode-Main.numNodes).cpindex;
            routeCost = routeCost + Main.nodesDistance[prevNode][currNode];
        } else if (index == 0 && index < route.size() - 1) {
            int currNode = route.elementAt(index);
            int nextNode = route.elementAt(index+1);
            if (currNode >= Main.numNodes)  currNode = Main.routedCarparks.elementAt(currNode-Main.numNodes).cpindex;
            if (nextNode >= Main.numNodes)  nextNode = Main.routedCarparks.elementAt(nextNode-Main.numNodes).cpindex;                
            routeCost = routeCost + Main.nodesDistance[currNode][nextNode];
        } else if (index > 0 && index < route.size() - 1) {
            int prevNode = route.elementAt(index-1);
            int currNode = route.elementAt(index);
            int nextNode = route.elementAt(index+1);
            if (prevNode >= Main.numNodes)  prevNode = Main.routedCarparks.elementAt(prevNode-Main.numNodes).cpindex;
            if (currNode >= Main.numNodes)  currNode = Main.routedCarparks.elementAt(currNode-Main.numNodes).cpindex;
            if (nextNode >= Main.numNodes)  nextNode = Main.routedCarparks.elementAt(nextNode-Main.numNodes).cpindex;                                
            routeCost = routeCost - Main.nodesDistance[prevNode][nextNode] + Main.nodesDistance[currNode][nextNode] + Main.nodesDistance[prevNode][currNode];         
        } 
    }
    public void addAllCustomers(Vector<Integer> v, int index) {
        // Add the vector v at index 'index' in the route vector
        int insertIndex = index;
        for (int cust : v) {
            this.addCustomer(cust, insertIndex);
            insertIndex++;
        }
    }
    public void removeCustomer(int custIndex) {
        // This function shall be required only for customers and not for depots
        int cust = this.route.elementAt(custIndex);
        this.routeCost = routeCost - Main.nodesDistance[cust][route.elementAt(custIndex-1)] - Main.nodesDistance[cust][route.elementAt(custIndex+1)] + Main.nodesDistance[route.elementAt(custIndex-1)][route.elementAt(custIndex+1)];
        this.demand -= (Main.customers[cust-Main.numCarpark-1].demand);
        this.route.remove(custIndex);
    }
    public void setCustomer(int id, int index) {
        int offset = Main.numCarpark+1;
        int removedCost = Main.nodesDistance[route.elementAt(index)][route.elementAt(index+1)] + Main.nodesDistance[route.elementAt(index-1)][route.elementAt(index)];
        int addedCost = Main.nodesDistance[id][route.elementAt(index+1)] + Main.nodesDistance[route.elementAt(index-1)][id];
        this.demand = this.demand - Main.customers[this.route.elementAt(index)-offset].demand + Main.customers[id-offset].demand;
        this.routeCost = this.routeCost - removedCost + addedCost; 
        this.route.set(index, id);
    }
    public int positionOf(int customer) {
        // This function gives the position of the customer in the route.
        // It returns -2 if the customer is at the begin or end of the route
        // And returns simple index otherwise
        int index = this.route.indexOf(customer);
        if (index == 1) {
            index = -2;
        } else if (index == route.size()-2) {
            index = -2;
        }
        return index;
    }
    public Route mergeRoute(Route r) {
        // Function to return the merged rotue with the current route
        Route merged = new Route();
        int startCust = this.route.elementAt(1);
        int endCust = this.route.elementAt(this.route.size()-2);
        Vector<Integer> thisRoute = (Vector<Integer>) this.route.clone();
        if (endCust == r.route.elementAt(1)) {
            thisRoute.remove(thisRoute.size()-1);
            merged.addAllCustomers(thisRoute, 0); // Add the current route
            r.route.remove(0);
            r.route.remove(0); // Trim the new route
            merged.addAllCustomers(r.route,thisRoute.size()); // Merge them
        } else if (startCust == r.route.elementAt(r.route.size()-2)) {
            r.route.remove(r.route.size()-1);   
            merged.addAllCustomers(r.route, 0);
            thisRoute.remove(0);   
            thisRoute.remove(0);   
            merged.addAllCustomers(thisRoute, r.route.size());
        } else if (startCust == r.route.elementAt(1)) {
            Collections.reverse(r.route);
            return (this.mergeRoute(r));
        } else if (endCust == r.route.elementAt(r.route.size()-2)) {
            Collections.reverse(r.route);
            return (this.mergeRoute(r));            
        }
        // System.out.println("Merged Route: " + merged); // Debug
        return merged;
    }
    public int getCost() {
        // Function to evaluate the total costs of the solution, including the infeasibility costs
        // Presently, no infeasibility costs are taken into account
        int cost = 0, prevIndex = -1;
        for (int index : this.route) {
            if (prevIndex == -1) {
                prevIndex = index;
                continue;
            }
            prevIndex = prevIndex>=Main.numNodes ? Main.routedCarparks.elementAt(prevIndex-Main.numNodes).cpindex : prevIndex;
            int tempIndex = index>=Main.numNodes ? Main.routedCarparks.elementAt(index-Main.numNodes).cpindex : index;
            cost += Main.nodesDistance[prevIndex][tempIndex];
            prevIndex = index;
        }
        // Add the infeasibility costs here.
        // System.out.println(cost); // Debug
        return cost;
    }
}