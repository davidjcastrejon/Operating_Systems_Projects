/*
David Castrejon
Assignment 3
CSC 139 Section 2
Tested on: Mac & Linux
 */

import java.io.*;
import java.util.*;
import java.math.*;


class Process implements Comparable<Process>{
    int id;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int priority;

    public Process(int id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = 0;
    }

    public Process(int id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }

    @Override
    public int compareTo(Process other) {
        if (this.priority == 0) { // Algorithms without priority
            if (this.burstTime == other.burstTime && this.arrivalTime != other.arrivalTime) {
                return Integer.compare(this.arrivalTime, other.arrivalTime);
            } else if (this.burstTime == other.burstTime && this.arrivalTime == other.arrivalTime) {
                return Integer.compare(this.id, other.id);
            } else {
                return Integer.compare(this.burstTime, other.burstTime);
            }
        } else if (this.priority == other.priority) { // Algorithms with priority
            return Integer.compare(this.id, other.id);
        } else {
            return Integer.compare(this.priority, other.priority);
        }
    }
}

/*-----------------------------------------------------------------------------------------------------------------------/
--------------------------------------------------------------------------------------------------------------------------

 RUN THIS PROGRAM IN THE COMMAND LINE BY PASSING A FILE WITH "java CPU_Scheduling_Algorithms path_to_input_file_name.txt"

--------------------------------------------------------------------------------------------------------------------------
/-----------------------------------------------------------------------------------------------------------------------*/
public class CPU_Scheduling_Algorithms {
    public static void main(String[] args) {
        try {

            // Checks for invalid command line input
            if (args.length != 1) {
                System.out.println("Usage: java ModifyTextFile <filename>");
                System.exit(1);
            }

            // Creates reader object for input file
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));

            // Creates writer object for output file
            BufferedWriter writer = new BufferedWriter(new FileWriter("Output.txt"));

            // First line of file
            String line;
            // Token values of first line of file
            int timeQuantum = 0;
            String algorithm = "";

            // Checks for valid first line in file
            if ((line = reader.readLine()) != null) {

                // Splits first line of files into tokens
                String[] tokens = line.split(" ");

                // Sets algorithm name to first token
                algorithm = tokens[0];

                // Sets timeQuantum if appropriate
                if (tokens.length == 2) {
                    timeQuantum = Integer.parseInt(tokens[1]);
                }

            } else {

                // Exits if file is empty
                System.out.println("Error: File is empty");
                System.exit(1);

            }

            writer.write("ALGORITHM: " + algorithm + "\n");
            switch (algorithm) {
                case "RR":
                    RoundRobin(reader, writer, timeQuantum);
                    break;
                case "SJF":
                    ShortestJobFirst(reader, writer);
                    break;
                case "PR_noPREMP":
                    PriorityNoPreemption(reader, writer);
                    break;
                case "PR_withPREMP": // Compares speed of a binary heap vs unsorted array priority queue implementation
                    // Record start time
                    long startTime = System.currentTimeMillis();
                    PriorityWithPreemption(reader, writer, timeQuantum);
                    // Record end time
                    long endTime = System.currentTimeMillis();
                    // Calculate and print the elapsed time
                    long binaryHeapElapsedTime = endTime - startTime;
                    writer.write("\nSPEED EVALUATION:\n");
                    writer.write("Binary Heap Elapsed Time: " + binaryHeapElapsedTime + " milliseconds\n");
                    break;
            }

            reader.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nNo file was inputed.");
        }
    }

    private static void PriorityWithPreemptionUnsortedArray(BufferedReader reader, BufferedWriter writer, int timeQuantum) {
    }

    private static void RoundRobin(BufferedReader reader, BufferedWriter writer, int timeQuantum) throws IOException {
        Queue<Process> queue = new LinkedList<>();
        Process currentProcess = null;
        int totalProcesses = Integer.parseInt(reader.readLine());
        int totalWaitingTime = 0;
        int numberOfProcesses = 0;
        int currentTime = 0;
        Process[] processes = new Process[totalProcesses];
        String line;
        writer.write("Total # of processes: " + totalProcesses + "\n");
        writer.write(String.format("%-10s %-10s %-10s%n", "ID", "ARRIVAL TIME", "BURST TIME"));
        for (int i = 0; i < totalProcesses; i++) {
            line = reader.readLine();
            String[] tokens = line.split(" ");
            int id = Integer.parseInt(tokens[0]);
            int arrivalTime = Integer.parseInt(tokens[1]);
            int burstTime = Integer.parseInt(tokens[2]);
            writer.write(String.format("%-10s %-12s %-10s%n", id, arrivalTime, burstTime));
            processes[i] = new Process(id, arrivalTime, burstTime);
        }

        writer.write("\nRUNNING!\n\n");
        writer.write(String.format("%-10s %-10s%n", "Current Process", "Remaining Time"));

        while(numberOfProcesses < totalProcesses || !queue.isEmpty() || currentProcess != null) {
            if (numberOfProcesses < totalProcesses) {
                for (int i = 0; i < totalProcesses; i++) {
                    if (processes[i].arrivalTime < currentTime) {
                        numberOfProcesses += 1;
                        queue.add(new Process(processes[i].id, processes[i].arrivalTime, processes[i].burstTime));
                    }
                }
            }
            if (currentProcess != null) {
                queue.add(currentProcess);
            }
            if (numberOfProcesses < totalProcesses) {
                for (int i = 0; i < totalProcesses; i++) {
                    if (processes[i].arrivalTime == currentTime) {
                        numberOfProcesses += 1;
                        queue.add(new Process(processes[i].id, processes[i].arrivalTime, processes[i].burstTime));
                    }
                }
            }

            currentProcess = queue.poll();
            writer.write(String.format("%-15d %-15d%n", currentProcess.id, currentProcess.remainingTime));

            if (currentProcess != null) {
                totalWaitingTime += currentTime - currentProcess.arrivalTime;
                currentTime += Math.min(currentProcess.remainingTime, timeQuantum);
                if ((currentProcess.remainingTime - timeQuantum) <= 0) {
                    currentProcess = null;
                } else {
                    currentProcess.remainingTime -= timeQuantum;
                    currentProcess.arrivalTime = currentTime;
                }
            }
        }

        double avgWaitingTime = (double) totalWaitingTime / (double) totalProcesses;
        writer.write("AVG Waiting Time: " + Double.toString(avgWaitingTime) + " cycles\n");
    }

    private static void ShortestJobFirst(BufferedReader reader, BufferedWriter writer) throws IOException {
        PriorityQueue<Process> minheap = new PriorityQueue<>();
        Process currentProcess = null;
        int totalProcesses = Integer.parseInt(reader.readLine());
        int totalWaitingTime = 0;
        int currentTime = 0;
        String line;
        writer.write("Total # of processes: " + totalProcesses + "\n");
        writer.write(String.format("%-10s %-10s %-10s%n", "ID", "ARRIVAL TIME", "BURST TIME"));
        for (int i = 0; i < totalProcesses; i++) {
            line = reader.readLine();
            String[] tokens = line.split(" ");
            int id = Integer.parseInt(tokens[0]);
            int arrivalTime = Integer.parseInt(tokens[1]);
            int burstTime = Integer.parseInt(tokens[2]);
            writer.write(String.format("%-10s %-12s %-10s%n", id, arrivalTime, burstTime));
            minheap.add(new Process(id, arrivalTime, burstTime));
        }

        writer.write("\nRUNNING!\n\n");
        writer.write(String.format("%-10s %-10s%n", "Time", "Process ID"));

        while (!minheap.isEmpty()) {
            currentProcess = minheap.poll();
            totalWaitingTime += currentTime - currentProcess.arrivalTime;

            //writer.write(currentTime + "  " + currentProcess.id + "\n");
            writer.write(String.format("%-10d %-10d%n", currentTime, currentProcess.id));

            while (currentProcess != null) {
                currentTime += 1;
                if ((currentProcess.remainingTime - 1) <= 0) {
                    currentProcess = null;
                } else {
                    currentProcess.remainingTime -= 1;
                }
            }
        }

        double avgWaitingTime = (double) totalWaitingTime / (double) totalProcesses;
        writer.write("AVG Waiting Time: " + Double.toString(avgWaitingTime) + " cycles\n");
    }

    /*---------------------------------------------------------------------------------------------------------------/
                                      Priority Queue implemented with Binary Heap
    /---------------------------------------------------------------------------------------------------------------*/
    private static void PriorityNoPreemption(BufferedReader reader, BufferedWriter writer) throws IOException {
        PriorityQueue<Process> minheap = new PriorityQueue<>();
        Process currentProcess = null;
        int totalProcesses = Integer.parseInt(reader.readLine());
        int totalWaitingTime = 0;
        int currentTime = 0;
        String line;
        writer.write("Total # of processes: " + totalProcesses + "\n");
        writer.write(String.format("%-10s %-10s %-10s %-10s%n", "ID", "ARRIVAL TIME", "BURST TIME", "PRIORITY"));
        for (int i = 0; i < totalProcesses; i++) {
            line = reader.readLine();
            String[] tokens = line.split(" ");
            int id = Integer.parseInt(tokens[0]);
            int arrivalTime = Integer.parseInt(tokens[1]);
            int burstTime = Integer.parseInt(tokens[2]);
            int priority = Integer.parseInt(tokens[3]);
            writer.write(String.format("%-10s %-12s %-10s %-9s%n", id, arrivalTime, burstTime, priority));
            minheap.add(new Process(id, arrivalTime, burstTime, priority));
        }

        writer.write("\nRUNNING!\n\n");
        writer.write(String.format("%-10s %-10s%n", "Time", "Process ID"));

        while (!minheap.isEmpty()) {
            currentProcess = minheap.poll();
            totalWaitingTime += currentTime - currentProcess.arrivalTime;

            writer.write(String.format("%-10d %-10d%n", currentTime, currentProcess.id));

            while (currentProcess != null) {
                currentTime += 1;
                if ((currentProcess.remainingTime - 1) <= 0) {
                    currentProcess = null;
                } else {
                    currentProcess.remainingTime -= 1;
                }
            }
        }

        double avgWaitingTime = (double) totalWaitingTime / (double) totalProcesses;
        writer.write("AVG Waiting Time: " + Double.toString(avgWaitingTime) + " cycles\n");
    }

    /*---------------------------------------------------------------------------------------------------------------/
                                      Priority Queue implemented with Binary Heap
    /---------------------------------------------------------------------------------------------------------------*/
    private static void PriorityWithPreemption(BufferedReader reader, BufferedWriter writer, int timeQuantum) throws IOException {
        PriorityQueue<Process> minheap = new PriorityQueue<>();
        Process currentProcess = null;
        int totalProcesses = Integer.parseInt(reader.readLine());
        int totalWaitingTime = 0;
        int numberOfProcesses = 0;
        int currentTime = 0;

        Process[] processes = new Process[totalProcesses];
        String line;
        writer.write("Total # of processes: " + totalProcesses + "\n");
        writer.write(String.format("%-10s %-10s %-10s %-10s%n", "ID", "ARRIVAL TIME", "BURST TIME", "PRIORITY"));

        for (int i = 0; i < totalProcesses; i++) {
            line = reader.readLine();
            String[] tokens = line.split(" ");
            int id = Integer.parseInt(tokens[0]);
            int arrivalTime = Integer.parseInt(tokens[1]);
            int burstTime = Integer.parseInt(tokens[2]);
            int priority = Integer.parseInt(tokens[3]);
            writer.write(String.format("%-10s %-12s %-10s %-9s%n", id, arrivalTime, burstTime, priority));
            processes[i] = new Process(id, arrivalTime, burstTime, priority);
        }

        writer.write("\nRUNNING!\n\n");
        writer.write(String.format("%-10s %-10s%n", "Time", "Process ID"));

        // Every cycle, I must check if there is a process which has arrived and should take over the CPU
        // a.k.a. => while queue is not empty
        // We must iterate over our array of processes to find the process which must take control of the CPU
        // THIS IS IT

        while (numberOfProcesses < totalProcesses || !minheap.isEmpty() || currentProcess != null) {
            for (int i = 0; i < processes.length; i++) {
                if (processes[i].arrivalTime > currentTime) {
                    // Do nothing
                } else if (processes[i].arrivalTime == currentTime) {
                    numberOfProcesses += 1;
                    minheap.add(new Process(processes[i].id, processes[i].arrivalTime, processes[i].burstTime, processes[i].priority));
                    //System.out.println("Added Processes " + processes[i].id + " with a priority of 0");
                }
            }

            if (currentProcess == null) { // Process completed or none have entered the heap
                currentProcess = minheap.poll();
                if (currentProcess != null) {
                    totalWaitingTime += currentTime - currentProcess.arrivalTime;
                }
                writer.write(String.format("%-10d %-10d%n", currentTime, currentProcess.id));
            } else if (minheap.peek() != null){ // There are other processes in the heap
                //System.out.println("Next in line is Process " + minheap.peek().id + " with a priority of " + minheap.peek().priority);
                if (currentProcess.priority > minheap.peek().priority) {
                    currentProcess.arrivalTime = currentTime;
                    totalWaitingTime += currentTime - minheap.peek().arrivalTime;
                    minheap.add(currentProcess);
                    currentProcess = minheap.poll();
                    writer.write(String.format("%-10d %-10d%n", currentTime, currentProcess.id));
                } else if (currentProcess.priority == minheap.peek().priority && currentProcess.id > minheap.peek().id) {
                    currentProcess.arrivalTime = currentTime;
                    totalWaitingTime += currentTime - minheap.peek().arrivalTime;
                    minheap.add(currentProcess);
                    currentProcess = minheap.poll();
                    writer.write(String.format("%-10d %-10d%n", currentTime, currentProcess.id));
                    }
                }

            // Simulate one clock cycle
            if (currentProcess != null) {
                currentProcess.remainingTime -= 1;
                if (currentProcess.remainingTime <= 0) {
                    //System.out.println("Process " + currentProcess.id + " completed!");
                    currentProcess = null;
                }
            }
            currentTime += 1;
        }

        double avgWaitingTime = (double) totalWaitingTime / (double) totalProcesses;
        writer.write("AVG Waiting Time: " + Double.toString(avgWaitingTime) + " cycles\n");

    }

}
