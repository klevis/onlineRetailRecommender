package ramo.klevis.data;


import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.catalyst.expressions.In;

import java.util.Comparator;
import java.util.List;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class PrepareData {

    private static final String ONLINE_RETAIL_XLSX = "data/Online Retail.csv";
    private HashMap<Integer, User> userHashMap = new HashMap<>();
    private HashMap<Integer, Item> itemHashMap = new HashMap<>();

    public static int prepareItemId(String val) {
        return Integer.parseInt(val.toString().toLowerCase()
                .replaceAll("amazonfee", "999998")
                .replaceAll("bank charges", "999997")
                .replaceAll("stockcode", "999996")
                .replaceAll("dcgs", "99")
                .replaceAll("_", "")
                .replaceAll("gift", "88")
                .replaceAll("boy", "444")
                .replaceAll("girl", "333")
                .replaceAll("a", "1")
                .replaceAll("b", "2")
                .replaceAll("c", "3")
                .replaceAll("l", "4")
                .replaceAll("j", "5")
                .replaceAll("p", "6")
                .replaceAll("f", "8")
                .replaceAll("t", "9")
                .replaceAll("e", "10")
                .replaceAll("o", "11")
                .replaceAll("s", "12")
                .replaceAll("m", "13")
                .replaceAll("g", "14")
                .replaceAll("w", "15")
                .replaceAll("k", "16")
                .replaceAll("d", "17")
                .replaceAll("h", "18")
                .replaceAll("u", "19")
                .replaceAll("r", "20")
                .replaceAll("v", "21")
                .replaceAll("i", "22")
                .replaceAll("y", "23")
                .replaceAll("z", "24")
                .replaceAll("n", "7"));
    }
    ;

    public HashMap<Integer, Item> getItemHashMap() {
        return itemHashMap;
    }

    public List<Row> readRowsFromFile() throws IOException, URISyntaxException {
        return Files.readAllLines(getPath(ONLINE_RETAIL_XLSX), StandardCharsets.ISO_8859_1)
                .stream().parallel().skip(1).map(line -> {
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    return new Row(data[6], data[7], data[1], data[2], Integer.parseInt(data[3]), Double.parseDouble(data[5]));
                }).filter(e -> e != null).collect(Collectors.toList());
    }

    public List<User> transformRowsToUserAndItems(List<Row> rows) {
        return rows.stream().map(row -> {
            User user;
            if (StringUtils.isEmpty(row.getUserId())) {
                return null;
            }
            if (userHashMap.containsKey(Integer.parseInt(row.getUserId()))) {

                user = userHashMap.get(Integer.parseInt(row.getUserId()));
                addItem(row, user);
                return null;
            } else {
                user = new User(Integer.parseInt(row.getUserId()), row.getUserCountry());
                addItem(row, user);
                userHashMap.put(user.getId(), user);
                return user;
            }

        }).filter(e -> e != null).sorted(Comparator.comparing(User::getId)).collect(Collectors.toList());
    }

    public List<User> readData() throws Exception {
        return transformRowsToUserAndItems(readRowsFromFile());
    }

    private void addItem(Row row, User user) {
        Item item = new Item(prepareItemId(row.getItemID()), row.getItemDescription(), row.getItemPrice(), row.getItemsBoughNumber());
        itemHashMap.put(item.getId(), item);
        user.addItem(item);
    }

    private Path getPath(String path) throws IOException, URISyntaxException {
        return getPath(this.getClass().getResource("/" + path).toURI());
    }

    private Path getPath(URI uri) throws IOException {
        Path start;
        try {
            start = Paths.get(uri);
        } catch (FileSystemNotFoundException e) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            FileSystems.newFileSystem(uri, env);
            start = Paths.get(uri);
        }
        return start;
    }

    public HashMap<Integer, User> getUserHashMap() {
        return userHashMap;
    }
}
