package com.smartpulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpulse.model.Body;
import com.smartpulse.model.IntraDayTrade;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeService {

    private final RestTemplate restTemplate;

    public TradeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public HashMap<String, List<Double>> getTradeHistoryList(String endDate, String startDate) throws IOException {

        String url = "https://seffaflik.epias.com.tr/transparency/service/market/intra-day-trade-history" +
                "?endDate=" + endDate + "&startDate=" + startDate;

        ObjectMapper objectMapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
           url, HttpMethod.GET, requestEntity, String.class
        );

        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode bodyNode = rootNode.get("body");
        Body responseBody = objectMapper.treeToValue(bodyNode, Body.class);

        List<IntraDayTrade> tradeList = responseBody.getIntraDayTradeHistoryList();

        // Only picking the conracts that start with PH
        List<IntraDayTrade> resList = deleteConractStartingWithPB(tradeList);

        // Grouping the conracts in a HashMap
        HashMap<String, List<IntraDayTrade>> map = groupConracts(resList);

        // Calculating values
        HashMap<String, List<Double>> resultMap = calculateValues(map);

        HashMap<String, List<Double>> parsedMap = new HashMap<>();

        // Parsing Conracts to Dates
        for (Map.Entry<String, List<Double>> set : resultMap.entrySet()) {
            String date = set.getKey();
            String trimmedDate = date.substring(2);

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyMMddHH");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            LocalDateTime dateTime = LocalDateTime.parse(trimmedDate, inputFormatter);
            String formattedDate = dateTime.format(outputFormatter);
            parsedMap.put(formattedDate, set.getValue());
        }

        return parsedMap;
    }

    public HashMap<String, List<IntraDayTrade>> groupConracts(List<IntraDayTrade> list){
        HashMap<String, List<IntraDayTrade>> map = new HashMap<>();

        for(int i = 0; i < list.size(); i++){
            String conract = list.get(i).getConract();
            if(map.containsKey(conract)){
                List<IntraDayTrade> tempList = map.get(conract);
                tempList.add(list.get(i));
                map.put(conract, tempList);
            }
            else {
                List<IntraDayTrade> freshList = new ArrayList<>();
                map.put(conract, freshList);
            }
        }

        return map;
    }

    public List<IntraDayTrade> deleteConractStartingWithPB(List<IntraDayTrade> list){
        List<IntraDayTrade> res = new ArrayList<>();

        for(int i = 0; i < list.size(); i++){
            String conract = list.get(i).getConract();
            if(!conract.startsWith("PB")){
                res.add(list.get(i));
            }
        }

        return res;
    }

    public HashMap<String, List<Double>> calculateValues(HashMap<String, List<IntraDayTrade>> map){
        HashMap<String, List<Double>> tta = totalTransactionAmount(map);
        HashMap<String, List<Double>> ttc = totalTransactionCount(map, tta);
        HashMap<String, List<Double>> wap = weightedAveragePrice(ttc);

        return wap;
    }

    public HashMap<String, List<Double>> totalTransactionAmount(HashMap<String, List<IntraDayTrade>> map){
        HashMap<String, List<Double>> results = new HashMap<>();
        Double ttaResult = Double.valueOf(0);

        for (Map.Entry<String, List<IntraDayTrade>> set : map.entrySet()) {
            List<IntraDayTrade> list = set.getValue();
            for(int i = 0; i < list.size(); i++){
                Double tempResult = Double.valueOf(0);
                tempResult = (list.get(i).getPrice() * list.get(i).getQuantity()) / 10;
                ttaResult += tempResult;
            }
            List<Double> values = new ArrayList<>();
            values.add(ttaResult);
            results.put(set.getKey(), values);
            ttaResult = Double.valueOf(0);
        }

        return results;
    }

    public HashMap<String, List<Double>> totalTransactionCount(HashMap<String, List<IntraDayTrade>> map, HashMap<String, List<Double>> valuesOfConract){
        HashMap<String, List<Double>> results = new HashMap<>();

        Double ttcResult = Double.valueOf(0);

        for (Map.Entry<String, List<IntraDayTrade>> set : map.entrySet()) {
            List<IntraDayTrade> list = set.getValue();
            for(int i = 0; i < list.size(); i++){
                Double tempResult = Double.valueOf(0);
                tempResult = Double.valueOf(list.get(i).getQuantity() / 10);
                ttcResult += tempResult;
            }
            List<Double> values = valuesOfConract.get(set.getKey());
            values.add(ttcResult);
            results.put(set.getKey(), values);
            ttcResult = Double.valueOf(0);
        }

        return results;
    }

    public HashMap<String, List<Double>> weightedAveragePrice(HashMap<String, List<Double>> map){

        Double wapResult = Double.valueOf(0);

        for (Map.Entry<String, List<Double>> set : map.entrySet()) {
            List<Double> list = set.getValue();
            Double tta = list.get(0);
            Double ttc = list.get(1);
            wapResult = tta / ttc;
            list.add(wapResult);
            map.put(set.getKey(), list);
        }

        return map;
    }
}
