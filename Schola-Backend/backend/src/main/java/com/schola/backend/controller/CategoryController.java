package com.schola.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    // GET /categories
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "c1", "label", "STEM",           "icon", "🔬", "count", 142),
                Map.of("id", "c2", "label", "Medicine",        "icon", "🏥", "count", 89),
                Map.of("id", "c3", "label", "Law",             "icon", "⚖️", "count", 63),
                Map.of("id", "c4", "label", "Arts",            "icon", "🎨", "count", 77),
                Map.of("id", "c5", "label", "Business",        "icon", "💼", "count", 108),
                Map.of("id", "c6", "label", "Social Sciences", "icon", "🌍", "count", 54)
        ));
    }
}