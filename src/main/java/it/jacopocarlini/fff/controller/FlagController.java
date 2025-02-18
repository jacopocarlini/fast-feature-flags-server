package it.jacopocarlini.fff.controller;

import it.jacopocarlini.fff.models.FlagDetails;
import it.jacopocarlini.fff.models.FlagItem;
import it.jacopocarlini.fff.providers.FlagService;
import it.jacopocarlini.fff.providers.MongoDBFeatureFlagProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FlagController {

    @Autowired
    private MongoDBFeatureFlagProvider provider;

    @Autowired
    FlagService flagService;



    @GetMapping("/flags")
    List<FlagItem> getFlags() {
        return flagService.getFlags();
    }

    @GetMapping("/flag/{key}")
    FlagDetails getFlag(@PathVariable String key) {
        return flagService.getFlag(key);
    }


    @GetMapping("/flag/{key}/eval")
    String evaluateFlag(@PathVariable String key) {
        return provider.getStringEvaluation(key, "vuoto", null).getValue();
    }



}
