import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class NodeTransformer {
    public static void main(String[] args) {
        String url = "https://storage.googleapis.com/maoz-event/rawdata.txt";
        Set<String> nodes = new LinkedHashSet<>();
        List<String> nodesList = new ArrayList<>();
        List<String> addressInList = new ArrayList<>();
        List<String> addressOutList = new ArrayList<>();
        Map<String, String> nodeTypes = new HashMap<>();
        Map<String, List<String>> addressInMap = new HashMap<>();
        Map<String, List<String>> addressOutMap = new HashMap<>();
        
        try {
            
            URL rawDataUrl = new URL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(rawDataUrl.openStream()));
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(jsonText.toString());
            JSONArray nodesArray = jsonObject.getJSONArray("nodes");
            JSONArray edgesArray = jsonObject.getJSONArray("edges");

            // Process edges to build the node connections
            for (int i = 0; i < edgesArray.length(); i++) {
                JSONObject edge = edgesArray.getJSONObject(i);
                String source = edge.getString("source");
                String target = edge.getString("target");

                nodes.add(source);
                nodes.add(target);
                
                addressOutMap.putIfAbsent(source, new ArrayList<>());
                addressOutMap.get(source).add(target);

                addressInMap.putIfAbsent(target, new ArrayList<>());
                addressInMap.get(target).add(target);
            }

            for (int i = 0; i < nodesArray.length(); i++) {
                JSONObject node = nodesArray.getJSONObject(i);
                String nodeId = node.getString("id");
                String nodeType = node.getString("type").trim();
                nodeTypes.put(nodeId, nodeType);
            }

            int index = 0;
            for (String nodeId : nodes) {
                String nodeType = nodeTypes.getOrDefault(nodeId, "Unknown Type");
                List<String> addressIn = addressInMap.getOrDefault(nodeId, new ArrayList<>());
                List<String> addressOut = addressOutMap.getOrDefault(nodeId, new ArrayList<>());
                
                System.out.println("Index: " + index + " | Node: " + nodeType + " | addressIn: " + addressIn + " | addressOut: " + addressOut);
                index++;

                nodesList.add("'" + nodeTypes.get(nodeId) + "'");
                addressInList.add(addressIn.isEmpty() ? "''" : "'" + String.join("', '", addressIn) + "'");
                
                if (nodeId.equals("to-publish-verticle-node-10")) {
                    addressOutList.add("['" + String.join("', '", addressOut) + "']");
                } else {
                    addressOutList.add(addressOut.isEmpty() ? "''" : "'" + String.join("', '", addressOut) + "'");
                }
            }
            System.out.println();
            System.out.println("Nodes = " + nodesList);
            System.out.println();
            System.out.println("addressIn = " + addressInList);
            System.out.println();
            System.out.println("addressOut = " + addressOutList);
        } catch (IOException | JSONException e) {
            System.err.println("Error processing data: " + e.getMessage());
        }
    }
}
