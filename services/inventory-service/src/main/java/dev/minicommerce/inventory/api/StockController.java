package dev.minicommerce.inventory.api;

import dev.minicommerce.inventory.domain.StockItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockItemRepository stock;

    public StockController(StockItemRepository stock) {
        this.stock = stock;
    }

    @GetMapping
    List<StockResponse> list() {
        return stock.findAll().stream().map(StockResponse::from).toList();
    }

    @GetMapping("/{sku}")
    StockResponse get(@PathVariable String sku) {
        return stock.findById(sku)
                .map(StockResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "sku not found: " + sku));
    }
}
