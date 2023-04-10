package com.smartpulse.controller;

import com.smartpulse.service.TradeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/v1/trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /*
    @GetMapping
    public ResponseEntity<Body> getTradeHistory(@RequestParam String endDate, @RequestParam String startDate) throws IOException {
        return ResponseEntity.ok(tradeService.getTradeHistoryList(endDate, startDate));
    }
    */

    @GetMapping
    public String tradeDataView(@RequestParam String endDate, @RequestParam String startDate, Model model) throws IOException {
        Map<String, List<Double>> resultMap = tradeService.getTradeHistoryList(endDate, startDate);

        model.addAttribute("tradeData", resultMap);
        return "tradeDataView";
    }
}
