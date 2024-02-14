/*
Tested on: macOS, Linux
 */

import java.io.*;
import java.util.*;

/*-----------------------------------------------------------------------------------------------------------------------/
--------------------------------------------------------------------------------------------------------------------------

       RUN THIS PROGRAM IN THE COMMAND LINE BY PASSING A FILE WITH "java Memory_Management path_to_test_case.txt"

--------------------------------------------------------------------------------------------------------------------------
/-----------------------------------------------------------------------------------------------------------------------*/

public class Memory_Management {
    public static void main(String[] args) throws IOException {

        // Checking for valid file input
        if (args.length != 1) {
            System.out.println("Please pass only one .txt file as a command line argument.");
            System.out.println("Example: \"java Memory_Management input.txt\"");
            System.exit(0);
        }

        // Create reader object
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        // Create writer object
        BufferedWriter writer = new BufferedWriter(new FileWriter("Output.txt"));

        // First line of file
        String line;
        // Token values of first line of file
        int pages = 0;
        int frames = 0;
        int requests = 0;

        if ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            // System.out.println(tokens.length);
            // Check for valid input
            if (tokens.length == 3) {
                pages = Integer.parseInt(tokens[0]);
                frames = Integer.parseInt(tokens[1]);
                requests = Integer.parseInt(tokens[2]);
            } else {
                System.out.println("First line of input was not formatted correctly.");
                System.exit(0);
            }
        }
        writer.write("FIFO\n");
        fifo(reader, writer, frames);

        reader = new BufferedReader(new FileReader(args[0]));
        // Skip the first line (already processed in FIFO)
        reader.readLine();
        writer.write("Optimal\n");
        optimal(reader, writer, frames, pages, requests);

        reader = new BufferedReader(new FileReader(args[0]));
        // Skip the first line (already processed in FIFO)
        reader.readLine();
        writer.write("LRU\n");
        lru(reader, writer, frames, pages);

        writer.close();

    }

    private static void optimal(BufferedReader reader, BufferedWriter writer, int numFrames, int numPages, int numRequests) throws IOException {
        int[] requests = new int[numRequests];
        int[] frames = new int[numFrames];
        HashSet<Integer> lookup = new HashSet<>();
        int counter = 0;
        int faults = 0;
        String line;

        // Initialize requests array
        while((line = reader.readLine()) != null) {
            String[] token = line.split(" ");
            requests[counter] = Integer.parseInt(token[0]);
            counter++;
        }

        counter = 0;
        for (int request: requests) {
            // Page is already loaded into frames
            if (lookup.contains(request)){
                int requestIndex = -1;
                for (int i = 0; i < frames.length; i++) {
                    if (frames[i] == request) {
                        requestIndex = i;
                        break;
                    }
                }
                writer.write("Page " + request + " already loaded into Frame " + requestIndex);

            } else { // Page is not loaded into frames
                // If frames are NOT all filled
                if (lookup.size() < frames.length) {
                    frames[lookup.size()] = request;
                    writer.write("Page " + request + " loaded into Frame " + lookup.size());
                    lookup.add(request);
                    faults++;
                } else { // If frames ARE all filled we must find the furthest used page
                    faults++;
                    // Initialize future hashmap
                    HashMap<Integer, Integer> future = new HashMap<>();
                    for (int page: lookup) {
                        future.put(page, 0);
                    }

                    int keyWithMaxValue = -1;
                    int maxValue = 0;
                    for (int i = counter + 1; i < requests.length; i++) {
                        // Request found in future
                        if (future.containsKey(requests[i]) && future.get(requests[i]) == 0) {
                            // Update Hashmap
                            future.replace(requests[i], i);
                            // Update maximum values
                            if (future.get(requests[i]) > maxValue) {
                                keyWithMaxValue = requests[i];
                                maxValue = i;
                            }
                            //System.out.println(keyWithMaxValue + " " + maxValue);
                        }
                    }

                    // Iterate through key-value pairs
                    for (Map.Entry<Integer, Integer> page : future.entrySet()) {
                        int key = page.getKey();
                        int value = page.getValue();

                        if (value == 0) {
                            // Finds page with the lowest frame if both are not used in the future
                            if (maxValue == 0 && findIndex(frames, key) < findIndex(frames, keyWithMaxValue)) {
                                keyWithMaxValue = key;
                                maxValue = 0;
                            // Checks for any 0's
                            } else if (value < maxValue) {
                                keyWithMaxValue = key;
                                maxValue = 0;
                            }
                        }
                    }

                    // Replace the page if found
                    if (keyWithMaxValue != -1) {
                        int swapIndex = -1;
                        for (int i = 0; i < frames.length; i++) {
                            if (frames[i] == keyWithMaxValue) {
                                swapIndex = i;
                                break;
                            }
                        }

                        writer.write("Page " + frames[swapIndex] + " unloaded from Frame " + swapIndex +
                                ", Page " + request + " loaded into Frame " + swapIndex);
                        lookup.remove(frames[swapIndex]);
                        frames[swapIndex] = request;
                        lookup.add(request);

                    } else { // Replace the page in the first frame
                        writer.write("Page " + frames[0] + " unloaded from Frame " + 0 +
                                ", Page " + request + " loaded into Frame " + 0);
                        lookup.remove(frames[0]);
                        frames[0] = request;
                        lookup.add(request);
                    }
                }
            }
            counter++;
            writer.write("\n");
        }
        writer.write(faults + " page faults\n\n");
    }

    // Helper function used in optimal algorithm
    private static int findIndex(int[] array, int value) {
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                index = i;
                break;
            }
        }
        return index;
    }

    private static void lru(BufferedReader reader, BufferedWriter writer, int numFrames, int numPages) throws IOException {
        LinkedHashMap<Integer, Integer> pages = new LinkedHashMap<>(numFrames, 0.75f, true);
        int faults = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            String[] token = line.split(" ");
            int page = Integer.parseInt(token[0]);
            //System.out.println(token[0]);

            // Page is not present in pages
            if (!pages.containsKey(page)) { // If there's still room in the frames
                if (pages.size() < numFrames) {
                    pages.put(page, pages.size());
                    writer.write("Page " + page + " loaded into Frame " + (pages.size() - 1));
                } else { // Frames are full, need to replace the least recently used page
                    int upload = pages.entrySet().iterator().next().getKey();
                    int swapIndex = pages.get(upload);
                    pages.remove(upload);
                    pages.put(page, swapIndex);
                    writer.write("Page " + upload + " unloaded from Frame " + swapIndex +
                            ", Page " + page + " loaded into Frame " + swapIndex);
                }
                faults++;
            } else { // Page is already present in the frames
                writer.write("Page " + page + " already loaded into Frame " + pages.get(page));
            }
            writer.write("\n");
        }
        writer.write(faults + " page faults\n\n");
    }
    private static void fifo(BufferedReader reader, BufferedWriter writer, int numFrames) throws IOException {
        int faults = 0;
        Queue<Integer> lookup = new LinkedList<>();
        String line;

        int[] frames = new int[numFrames];

        while ((line = reader.readLine()) != null) {
            String[] token = line.split(" ");
            int page = Integer.parseInt(token[0]);
            int index = -1;
            for (int i = 0; i < frames.length; i++) {
                if (frames[i] == page) {
                    index = i;
                    break;
                }
            }
            if (index < 0 && lookup.size() < frames.length) {
                frames[lookup.size()] = page;
                writer.write("Page " + page + " loaded into Frame " + lookup.size());
                lookup.add(page);
                faults += 1;
            } else if (index < 0) {
                int unload = lookup.poll();
                int swapIndex = -1;
                for (int i = 0; i < frames.length; i++) {
                    if (frames[i] == unload) {
                        swapIndex = i;
                        break;
                    }
                }
                writer.write("Page " + unload + " unloaded from Frame "
                        + swapIndex + ", Page " + page + " loaded into Frame " + swapIndex);
                frames[swapIndex] = page;
                lookup.add(page);
                faults += 1;
            } else {
                writer.write("Page " + page + " already loaded into Frame " + index);
            }
            writer.write("\n");
        }
        writer.write(faults + " page faults\n\n");
    }
}


