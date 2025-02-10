package edu.jsu.mcis.cs310;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;

public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}"; // default return value; replace later!
        
        try {
            /*
                Planning the steps
                1. I need read the CSV Input
                2. next process the header row, then the data rows
                3. create a JSON object, then add the info to the object
                4. Serializize the JSON object into a JSON string
                
                - I think using arrays would be the best for transfering the data
                    one for ProdNums, ColHeadings and Data
            */
            // --- --- --- --- ---
            
            // 1 - creates a CSVReader to parse the CSV input string
            CSVReader reader = new CSVReader(new StringReader(csvString));
            List<String[]> rows = reader.readAll();
            
            // 2 - The first row contains the column headers
            String[] header = rows.get(0);
            JsonArray colHeadings = new JsonArray();
            for (String col : header) {
                colHeadings.add(col);
            }
            
            // 2 - create two arrays to hold the "ProdNums" and the "Data" rows
            JsonArray prodNums = new JsonArray();
            JsonArray data = new JsonArray();
            
            // 2 - process each data row, excluding the header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                
                // 2 - The first column is the ProdNum.
                prodNums.add(row[0]);
                
                // 2 - make a JSON array for the remaining columns.
                JsonArray rowData = new JsonArray();
                for (int j = 1; j < row.length; j++) {
                    String colHeader = header[j];
                    String cell = row[j];
                    
                    // 2- converts "Season" and "Episode" to integers.
                    if (colHeader.equals("Season") || colHeader.equals("Episode")) {
                        rowData.add(Integer.parseInt(cell));
                    }
                    else {
                        rowData.add(cell);
                    }
                }
                data.add(rowData);
            }
            
            // 3 - Makes the JSON object.
            JsonObject jsonObject = new JsonObject();
            // 3 - Puts data into the object
            jsonObject.put("ProdNums", prodNums);
            jsonObject.put("ColHeadings", colHeadings);
            jsonObject.put("Data", data);
            
            // 4 - Serialize the JSON object to a string.
            result = Jsoner.serialize(jsonObject);
            // --- --- --- --- ---

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {
        
        String result = ""; // default return value; replace later!
        
        try {
            /*
                Planning the steps
                Basically overall the same steps as csvToJson but inverted
                4. deserializize the JSON string into a JSON object
                3. set up CSV writing
                2. process the JSON object rows into the CSV
                1. Read the CSV output
            */
            // --- --- --- --- ---
            // 4 - JSON string into a JsonObject.
            JsonObject json = (JsonObject) Jsoner.deserialize(jsonString, new JsonObject());
            
            // 3 - StringWriter and CSVWriter to make the CSV output.
            StringWriter sw = new StringWriter();
            CSVWriter writer = new CSVWriter(sw);
            
            // 2 - get the "ProdNums", "ColHeadings", and "Data" arrays.
            JsonArray prodNums = (JsonArray) json.get("ProdNums");
            JsonArray colHeadings = (JsonArray) json.get("ColHeadings");
            JsonArray data = (JsonArray) json.get("Data");
            
            // 2- copy the header row from the "ColHeadings" array.
            int numCols = colHeadings.size();
            String[] header = new String[numCols];
            for (int i = 0; i < numCols; i++) {
                header[i] = colHeadings.get(i).toString();
            }
            writer.writeNext(header);
            
            // 2 - Each  row in the JSON Data array corresponds to a CSV row.
            for (int i = 0; i < data.size(); i++) {
                JsonArray jsonRow = (JsonArray) data.get(i);
                String[] csvRow = new String[numCols];
                
                // 2 - First column: ProdNum
                csvRow[0] = prodNums.get(i).toString();
                
                for (int j = 1; j < numCols; j++) {
                    Object value = jsonRow.get(j - 1);
                    
                    if (header[j].equals("Episode") && value instanceof Number) {
                        int episode = ((Number)value).intValue();
                        csvRow[j] = String.format("%02d", episode);
                    }
                    else {
                        csvRow[j] = value.toString();
                    }
                }
                
                writer.writeNext(csvRow);
            }
            
            writer.close();
            // 1 - The CSV output
            result = sw.toString();
            // --- --- --- --- ---
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
}
